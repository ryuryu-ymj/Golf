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
import ktx.app.KtxScreen
import ktx.graphics.use
import ktx.math.vec2
import ktx.scene2d.actors
import ktx.scene2d.label
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
        camera.position.setZero()

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

        stage.act()
        uiStage.act()
        if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) &&
            Gdx.input.isKeyJustPressed(Input.Keys.S)
        ) {
            // save
            /*val cellTypeList = cellList.map { it.type }
            val file = Gdx.files.local("course/${"%02d".format(courseIndex)}raw")
            file.writeString(Json.encodeToString(cellTypeList), false)
            println("save edit file to course/${"%02d".format(courseIndex)}raw")

            saveBody(cellTypeList)*/
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
            val beginIX = MathUtils.floor(selectBegin.x / CELL_SIZE)
            val beginIY = MathUtils.floor(selectBegin.y / CELL_SIZE)
            val endIX = MathUtils.floor(selectEnd.x / CELL_SIZE)
            val endIY = MathUtils.floor(selectEnd.y / CELL_SIZE)
            val rangeX = min(beginIX, endIX)..max(beginIX, endIX)
            val rangeY = min(beginIY, endIY)..max(beginIY, endIY)
            for (ix in rangeX) {
                for (iy in rangeY) {
                    val old = courseComponents.find {
                        ix >= it.ix && ix < it.ix + it.iw &&
                                iy >= it.iy && iy < it.iy + it.ih
                    }
                    when (brush.type) {
                        BrushType.DELETE -> {
                            if (old != null) {
                                old.remove()
                                courseComponents.remove(old)
                            }
                        }
                        BrushType.FAIRWAY -> {
                            if (old == null || old !is Ground) {
                                val new = Ground(game.asset, ix, iy)
                                stage.addActor(new)
                                courseComponents.add(new)
                            }
                        }
                    }
                }
            }
            return true
        }
        return false
    }

    override fun dispose() {
        bg.dispose()
        shape.dispose()
        stage.dispose()
        uiStage.dispose()
        batch.dispose()
    }

    private fun saveBody(cellTypeList: MutableList2d<CellType>) {
        val file = Gdx.files.local("course/${"%02d".format(courseIndex)}body")
        val writer = PrintWriter(file.writer(false))

        while (true) {
            var boxX: Int? = null
            var boxY: Int? = null
            var boxW = 0
            var boxH = 0
            xLoop@
            for (x in cellTypeList.rangeX) {
                for (y in cellTypeList.rangeY) {
                    if (boxX == null || boxY == null) {
                        if (cellTypeList[x, y] == CellType.FAIRWAY) {
                            boxX = x
                            boxY = y
                        }
                    } else if (boxH == 0) {
                        if (cellTypeList[x, y] != CellType.FAIRWAY) {
                            boxH = y - boxY
                        } else if (y == cellTypeList.lastY) {
                            boxH = y + 1 - boxY
                        }
                    } else if (y < boxY) {
                        continue
                    } else if (y >= boxY + boxH) {
                        break
                    } else if (cellTypeList[x, y] != CellType.FAIRWAY) {
                        boxW = x - boxX
                        break@xLoop
                    } else if (x == cellTypeList.lastY) {
                        boxW = x + 1 - boxX
                    }
                }
            }
            if (boxX == null || boxY == null) break
            for (x in boxX until boxX + boxW) {
                for (y in boxY until boxY + boxH) {
                    cellTypeList[x, y] = CellType.NULL
                }
            }
            val x = (boxX - startX) * CELL_SIZE - CELL_SIZE / 2
            val y = (boxY - startY) * CELL_SIZE
            val width = boxW * CELL_SIZE
            val height = boxH * CELL_SIZE
            writer.println("fairway,box,${x}f,${y}f,${width}f,${height}f")
        }
        writer.close()
        println("save body file to course/${"%02d".format(courseIndex)}body")
    }
}