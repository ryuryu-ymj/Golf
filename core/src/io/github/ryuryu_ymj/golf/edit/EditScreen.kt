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
import java.io.PrintWriter
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
    private lateinit var tee: CourseComponent
    private lateinit var hole: CourseComponent

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

        try {
            val file = Gdx.files.internal("course/${"%02d".format(courseIndex)}raw")
            val dataList =
                Json.decodeFromString<Array<CourseComponentData>>(file.readString())
            dataList.forEach {
                val component = it.createCourseComponent(game.asset)
                courseComponents.add(component)
                stage.addActor(component)
                if (component.type == CourseComponentType.TEE) {
                    tee = component
                } else if (component.type == CourseComponentType.HOLE) {
                    hole = component
                }
            }
        } catch (e: Exception) {
        }
        if (!::tee.isInitialized) {
            courseComponents.findAt(0, 0)?.let {
                it.remove()
                courseComponents.remove(it)
            }
            courseComponents
            tee = CourseComponent(game.asset, CourseComponentType.TEE, 0, 0)
            courseComponents.add(tee)
            stage.addActor(tee)
        }
        if (!::hole.isInitialized) {
            courseComponents.findAt(10, 0)?.let {
                it.remove()
                courseComponents.remove(it)
            }
            courseComponents
            hole = CourseComponent(game.asset, CourseComponentType.HOLE, 10, 0)
            courseComponents.add(hole)
            stage.addActor(hole)
        }

        camera.position.set(tee.centerX, tee.centerY, 0f)
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

            courseComponents.forEach { it.setContact(courseComponents) }
            saveBodyFile()
        } else if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) &&
            Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) &&
            Gdx.input.isKeyJustPressed(Input.Keys.A)
        ) {
            // clear course components
            courseComponents.forEach {
                if (it !== tee && it !== hole) {
                    it.remove()
                }
            }
            courseComponents.clear()
            courseComponents.add(tee)
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
                BrushType.TEE -> {
                    addCourseComponent(
                        CourseComponentType.TEE, beginIX, beginIY
                    )?.let {
                        courseComponents.remove(tee)
                        tee.remove()
                        tee = it
                    }
                }
                BrushType.HOLE -> {
                    addCourseComponent(
                        CourseComponentType.HOLE, beginIX, beginIY
                    )?.let {
                        courseComponents.remove(hole)
                        hole.remove()
                        hole = it
                    }
                }
                BrushType.DELETE -> {
                    for (ix in rangeX) {
                        for (iy in rangeY) {
                            removeCourseComponent(ix, iy)
                        }
                    }
                }
                BrushType.FAIRWAY -> {
                    for (ix in rangeX) {
                        for (iy in rangeY) {
                            addCourseComponent(
                                CourseComponentType.FAIRWAY,
                                ix, iy
                            )
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
    ): CourseComponent? {
        val old = courseComponents.findAt(ix, iy)
        if (old != null) return null
        val new = CourseComponent(game.asset, type, ix, iy)
        stage.addActor(new)
        courseComponents.add(new)
        return new
    }

    private fun removeCourseComponent(ix: Int, iy: Int): Boolean {
        val old = courseComponents.findAt(ix, iy) ?: return false
        if (old === tee) return false
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

    private fun saveBodyFile() {
        val file = Gdx.files.local("course/${"%02d".format(courseIndex)}body")
        val writer = PrintWriter(file.writer(false))

        val courseComponents = courseComponents.toMutableList()
        courseComponents.forEach {
            it.setContact(courseComponents)
        }

        // hole
        run {
            val x = (hole.ix - tee.ix - 0.5f) * COMPONENT_UNIT_SIZE
            val y = (hole.iy - tee.iy - 1) * COMPONENT_UNIT_SIZE
            writer.println(
                "fairway,box,$x,$y," +
                        "${COMPONENT_UNIT_SIZE * 0.2f},${COMPONENT_UNIT_SIZE}"
            )
            writer.println(
                "fairway,box,${x + COMPONENT_UNIT_SIZE * 0.2f},$y," +
                        "${COMPONENT_UNIT_SIZE * 0.6f},${COMPONENT_UNIT_SIZE * 0.2f}"
            )
            writer.println(
                "fairway,box,${x + COMPONENT_UNIT_SIZE * 0.8f},$y," +
                        "${COMPONENT_UNIT_SIZE * 0.2f},${COMPONENT_UNIT_SIZE}"
            )
        }
        courseComponents.remove(hole)

        // horizontal box
        while (true) {
            val left = courseComponents.filter {
                (!it.topContacted || !it.bottomContacted) &&
                        it.type.vertex == 0b1111
            }.minByOrNull {
                it.ix
            } ?: break
            courseComponents.remove(left)
            var iw = 1
            while (true) {
                val next = courseComponents.find {
                    it.ix == left.ix + iw && it.iy == left.iy &&
                            (!it.topContacted || !it.bottomContacted) &&
                            it.type.vertex == 0b1111
                } ?: break
                courseComponents.remove(next)
                iw++
            }
            val x = (left.ix - tee.ix - 0.5f) * COMPONENT_UNIT_SIZE
            val y = (left.iy - tee.iy - 1) * COMPONENT_UNIT_SIZE
            val w = iw * COMPONENT_UNIT_SIZE
            val h = 1 * COMPONENT_UNIT_SIZE
            writer.println("fairway,box,$x,$y,$w,$h,")
        }

        // vertical box
        while (true) {
            val bottom = courseComponents.filter {
                (!it.rightContacted || !it.leftContacted) &&
                        it.type.vertex == 0b1111
            }.minByOrNull {
                it.iy
            } ?: break
            courseComponents.remove(bottom)
            var ih = 1
            while (true) {
                val next = courseComponents.find {
                    it.ix == bottom.ix && it.iy == bottom.iy + ih &&
                            (!it.rightContacted || !it.leftContacted) &&
                            it.type.vertex == 0b1111
                } ?: break
                courseComponents.remove(next)
                ih++
            }
            val x = (bottom.ix - tee.ix - 0.5f) * COMPONENT_UNIT_SIZE
            val y = (bottom.iy - tee.iy - 1) * COMPONENT_UNIT_SIZE
            val w = 1 * COMPONENT_UNIT_SIZE
            val h = ih * COMPONENT_UNIT_SIZE
            writer.println("fairway,box,$x,$y,$w,$h,")
        }

        // up slope polygon
        while (true) {
            val left = courseComponents.filter {
                !it.topContacted && it.type.vertex == 0b0111
            }.minByOrNull {
                it.ix
            } ?: break
            courseComponents.remove(left)
            var i = 1
            while (true) {
                val next = courseComponents.find {
                    it.ix == left.ix + i * left.iw && it.iy == left.iy + i &&
                            it.iw == left.iw &&
                            !it.topContacted && it.type.vertex == 0b0111
                } ?: break
                courseComponents.remove(next)
                i++
            }
            val x = (left.ix - tee.ix - 0.5f) * COMPONENT_UNIT_SIZE
            val y = (left.iy - tee.iy - 1) * COMPONENT_UNIT_SIZE
            val w = i * left.iw * COMPONENT_UNIT_SIZE
            val h = i * COMPONENT_UNIT_SIZE
            writer.println(
                "fairway,polygon,$x,$y,$x,${y - COMPONENT_UNIT_SIZE}," +
                        "${x + w},${y - COMPONENT_UNIT_SIZE + h},${x + w},${y + h},"
            )
        }

        // down slope polygon
        while (true) {
            val left = courseComponents.filter {
                !it.topContacted && it.type.vertex == 0b1011
            }.minByOrNull {
                it.ix
            } ?: break
            courseComponents.remove(left)
            var i = 1
            while (true) {
                val next = courseComponents.find {
                    it.ix == left.ix + i * left.iw && it.iy == left.iy - i &&
                            it.iw == left.iw &&
                            !it.topContacted && it.type.vertex == 0b1011
                } ?: break
                courseComponents.remove(next)
                i++
            }
            val x = (left.ix - tee.ix - 0.5f) * COMPONENT_UNIT_SIZE
            val y = (left.iy - tee.iy - 1) * COMPONENT_UNIT_SIZE
            val w = i * left.iw * COMPONENT_UNIT_SIZE
            val h = i * COMPONENT_UNIT_SIZE
            writer.println(
                "fairway,polygon,$x,$y,${x + w},${y - h}," +
                        "${x + w},${y + COMPONENT_UNIT_SIZE - h},$x,${y + COMPONENT_UNIT_SIZE},"
            )
        }

        writer.close()
        println("save body file to course/${"%02d".format(courseIndex)}body")
    }
}