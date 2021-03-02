package io.github.ryuryu_ymj.golf.edit

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.utils.viewport.FitViewport
import io.github.ryuryu_ymj.golf.MyGame
import io.github.ryuryu_ymj.golf.play.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ktx.app.KtxScreen
import ktx.graphics.use
import ktx.math.vec2
import ktx.scene2d.actors
import ktx.scene2d.table
import ktx.scene2d.textField
import kotlin.math.max
import kotlin.math.min

class EditScreen(private val game: MyGame) : KtxScreen, MyTouchable {
    private val batch = SpriteBatch()
    private val camera = OrthographicCamera(4f, 2.25f)
    private val viewport = FitViewport(
        camera.viewportWidth,
        camera.viewportHeight, camera
    )
    private val stage = Stage(viewport, batch)
    private val uiViewport = FitViewport(1600f, 900f)
    private val uiStage = Stage(uiViewport, batch)
    private val input = InputMultiplexer().also {
        it.addProcessor(uiStage)
        it.addProcessor(stage)
        it.addProcessor(MyInputProcessor(viewport, this))
    }
    private val shape = ShapeRenderer()

    private val bg = BackGround(stage.width, stage.height)
    private val courseComponents = mutableListOf<CourseComponent>()

    private var startX = 0
    private var startY = 0
    private var isSelecting = false
    private val selectBegin = vec2()
    private val selectEnd = vec2()

    private val brush = Brush()
    private val courseIndexField: TextField

    init {
        stage.addActor(bg)

        //uiStage.addActor(brush)
        uiStage.actors {
            table {
                setFillParent(true)
                //debug = true
                top()
                add(brush).expandX().left()
                courseIndexField = textField(text = courseIndex.toString()) {
                    it.expandX().right()
                }
            }
        }
    }

    override fun show() {
        stage.addActor(bg)
        camera.position.setZero()

        try {
            val file = Gdx.files.internal("course/${"%02d".format(courseIndex)}raw")
            val dataList =
                Json.decodeFromString<Array<CourseComponentData>>(file.readString())
            dataList.forEach {
                val component = it.createCourseComponent(game.asset)
                courseComponents.add(component)
                stage.addActor(component)
            }
        } catch (e: Exception) {
        }

        Gdx.input.inputProcessor = input
    }

    override fun hide() {
        courseComponents.clear()
        stage.clear()
        Gdx.input.inputProcessor = null
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
        uiViewport.update(width, height)
    }

    override fun render(delta: Float) {
        stage.draw()
        uiStage.draw()
        if (isSelecting) {
            Gdx.gl.glEnable(GL20.GL_BLEND)
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
            shape.use(ShapeRenderer.ShapeType.Filled, camera.combined) {
                it.setColor(1f, 0f, 0f, 0.2f)
                it.rect(
                    selectBegin.x, selectBegin.y,
                    selectEnd.x - selectBegin.x, selectEnd.y - selectBegin.y
                )
            }
            Gdx.gl.glDisable(GL20.GL_BLEND)
        }

        if (brush.type != BrushType.MOVE) {
            val margin = viewport.screenWidth / 8
            val speed = 0.01f
            if (Gdx.input.x < margin) {
                camera.position.x -= speed
            } else if (Gdx.input.x > viewport.screenWidth - margin) {
                camera.position.x += speed
            }
            if (Gdx.input.y < margin) {
                camera.position.y += speed
            } else if (Gdx.input.y > viewport.screenHeight - margin) {
                camera.position.y -= speed
            }
        }
        stage.act()
        uiStage.act()
        if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) &&
            Gdx.input.isKeyJustPressed(Input.Keys.S)
        ) {
            // save
            val dataList = courseComponents.map { it.createCourseComponentData() }
            val file = Gdx.files.local("course/${"%02d".format(courseIndex)}raw")
            file.writeString(Json.encodeToString(dataList), false)
            println("save edit file to course/${"%02d".format(courseIndex)}raw")
        } else if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) &&
            Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) &&
            Gdx.input.isKeyJustPressed(Input.Keys.A)
        ) {
            // clear with CellType.NULL
            courseComponents.forEach { it.remove() }
            courseComponents.clear()
        } else if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) &&
            Gdx.input.isKeyJustPressed(Input.Keys.P)
        ) {
            // move to playScreen
            game.setScreen<PlayScreen>()
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) &&
            courseIndexField.hasKeyboardFocus()
        ) {
            uiStage.unfocusAll()
            // open a new course
            try {
                courseIndex = courseIndexField.text.toInt()
                game.setScreen<EditScreen>()
            } catch (e: NumberFormatException) {
                println("invalid input : " + courseIndexField.text)
            }
        }
    }

    override fun touchDown(x: Float, y: Float): Boolean {
        if (brush.type == BrushType.MOVE) return false
        selectBegin.set(x, y)
        selectEnd.set(x, y)
        isSelecting = true
        return true
    }

    override fun touchDragged(x: Float, y: Float): Boolean {
        if (isSelecting) {
            selectEnd.set(x, y)
            return true
        }
        return false
    }

    override fun touchUp(x: Float, y: Float): Boolean {
        if (isSelecting) {
            isSelecting = false
            val beginIX = MathUtils.floor(selectBegin.x / COMPONENT_UNIT_SIZE)
            val beginIY = MathUtils.floor(selectBegin.y / COMPONENT_UNIT_SIZE)
            val endIX = MathUtils.floor(selectEnd.x / COMPONENT_UNIT_SIZE)
            val endIY = MathUtils.floor(selectEnd.y / COMPONENT_UNIT_SIZE)
            val rangeX = min(beginIX, endIX)..max(beginIX, endIX)
            val rangeY = min(beginIY, endIY)..max(beginIY, endIY)

            when (brush.type) {
                BrushType.DELETE, BrushType.FAIRWAY -> {
                    for (ix in rangeX) {
                        for (iy in rangeY) {
                            when (brush.type) {
                                BrushType.DELETE -> {
                                    removeCourseComponent(ix, iy)
                                }
                                BrushType.FAIRWAY -> {
                                    addCourseComponent(
                                        CourseComponentType.FAIRWAY,
                                        ix, iy
                                    )
                                }
                            }
                        }
                    }
                }
                BrushType.FAIRWAY_SLOPE_UP -> {
                    when ((rangeX.last - rangeX.first + 1) /
                            (rangeY.last - rangeY.first + 1)) {
                        1 -> {
                            for (ix in 0..(rangeX.last - rangeX.first)) {
                                addCourseComponent(
                                    CourseComponentType.FAIRWAY_SLOPE_UP_11,
                                    rangeX.first + ix, rangeY.first + ix
                                )
                                for (iy in 0 until ix) {
                                    addCourseComponent(
                                        CourseComponentType.FAIRWAY,
                                        rangeX.first + ix, rangeY.first + iy
                                    )
                                }
                            }
                        }
                        2 -> {
                            for (ix in 0..(rangeX.last - rangeX.first)) {
                                if (ix % 2 == 0) {
                                    addCourseComponent(
                                        CourseComponentType.FAIRWAY_SLOPE_UP_21,
                                        rangeX.first + ix, rangeY.first + ix / 2
                                    )
                                }
                                for (iy in 0 until ix / 2) {
                                    addCourseComponent(
                                        CourseComponentType.FAIRWAY,
                                        rangeX.first + ix, rangeY.first + iy
                                    )
                                }
                            }
                        }
                    }
                }
                BrushType.FAIRWAY_SLOPE_DOWN -> {
                    when ((rangeX.last - rangeX.first + 1) /
                            (rangeY.last - rangeY.first + 1)) {
                        1 -> {
                            for (ix in 0..(rangeX.last - rangeX.first)) {
                                addCourseComponent(
                                    CourseComponentType.FAIRWAY_SLOPE_DOWN_11,
                                    rangeX.last - ix, rangeY.first + ix
                                )
                                for (iy in 0 until ix) {
                                    addCourseComponent(
                                        CourseComponentType.FAIRWAY,
                                        rangeX.last - ix, rangeY.first + iy
                                    )
                                }
                            }
                        }
                        2 -> {
                            for (ix in 0..(rangeX.last - rangeX.first)) {
                                if (ix % 2 == 0) {
                                    addCourseComponent(
                                        CourseComponentType.FAIRWAY_SLOPE_DOWN_21,
                                        rangeX.last - ix - 1, rangeY.first + ix / 2
                                    )
                                }
                                for (iy in 0 until ix / 2) {
                                    addCourseComponent(
                                        CourseComponentType.FAIRWAY,
                                        rangeX.last - ix, rangeY.first + iy
                                    )
                                }
                            }
                        }
                    }
                }
            }
            return true
        }
        return false
    }

    private fun addCourseComponent(
        type: CourseComponentType, ix: Int, iy: Int
    ): Boolean {
        val old = courseComponents.find {
            ix >= it.ix && ix < it.ix + it.iw &&
                    iy >= it.iy && iy < it.iy + it.ih
        }
        if (old != null) return false
        val new = CourseComponent(game.asset, type, ix, iy)
        stage.addActor(new)
        courseComponents.add(new)
        return true
    }

    private fun removeCourseComponent(ix: Int, iy: Int): Boolean {
        val old = courseComponents.find {
            ix >= it.ix && ix < it.ix + it.iw &&
                    iy >= it.iy && iy < it.iy + it.ih
        } ?: return false
        old.remove()
        courseComponents.remove(old)
        return true
    }

    override fun dispose() {
        bg.dispose()
        shape.dispose()
        stage.dispose()
        uiStage.dispose()
        batch.dispose()
    }
}