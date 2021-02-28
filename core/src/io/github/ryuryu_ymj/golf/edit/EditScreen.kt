package io.github.ryuryu_ymj.golf.edit

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.FitViewport
import io.github.ryuryu_ymj.golf.MyInputProcessor
import io.github.ryuryu_ymj.golf.MyTouchable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ktx.app.KtxScreen
import java.io.PrintWriter
import java.lang.Exception

class EditScreen(asset: AssetManager) : KtxScreen, MyTouchable {
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
        it.addProcessor(stage)
        it.addProcessor(MyInputProcessor(viewport, this))
    }

    private val cellList: GdxArray2d<Cell>

    private var startX = 0
    private var startY = 0

    internal fun setStart(x: Int, y: Int) {
        cellList[startX, startY].type = CellType.NULL
        startX = x
        startY = y
    }

    init {
        camera.position.setZero()
        uiStage.addActor(Brush(asset))

        fun defaultCellList() = GdxArray2d(-10..10, -10..10) { x, y ->
            Cell(asset, this, x, y).also {
                stage.addActor(it)
                if (x == startX && y == startY) {
                    it.type = CellType.START
                }
            }
        }

        val file = Gdx.files.internal("course/01raw")
        cellList =
            if (file.exists()) {
                try {
                    val read = Json.decodeFromString<GdxArray2d<CellType>>(file.readString())
                    GdxArray2d(read.rangeX(), read.rangeY()) { x, y ->
                        Cell(asset, this, x, y).also {
                            stage.addActor(it)
                            it.type = read[x, y]
                            if (it.type == CellType.START) {
                                startX = x
                                startY = y
                            }
                        }
                    }
                } catch (e: Exception) {
                    defaultCellList()
                }
            } else {
                defaultCellList()
            }
    }

    override fun show() {
        Gdx.input.inputProcessor = input
    }

    override fun hide() {
        Gdx.input.inputProcessor = null
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
        uiViewport.update(width, height)
    }

    override fun render(delta: Float) {
        stage.draw()
        uiStage.draw()

        stage.act()
        uiStage.act()
        if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) &&
            Gdx.input.isKeyJustPressed(Input.Keys.S)
        ) {
            val cellTypeList = cellList.map { it.type }
            val file = Gdx.files.local("course/01raw")
            file.writeString(Json.encodeToString(cellTypeList), false)
            println("save edit file to course/01raw")

            saveBody(cellTypeList)
        }
    }

    override fun touchDown(x: Float, y: Float): Boolean {
        return false
    }

    override fun touchDragged(x: Float, y: Float): Boolean {
        return false
    }

    override fun touchUp(x: Float, y: Float): Boolean {
        return false
    }

    override fun dispose() {
        stage.dispose()
        batch.dispose()
    }

    private fun saveBody(cellTypeList: GdxArray2d<CellType>) {
        val file = Gdx.files.local("course/01body")
        val writer = PrintWriter(file.writer(false))

        while (true) {
            var boxX: Int? = null
            var boxY: Int? = null
            var boxW = 0
            var boxH = 0
            xLoop@
            for (x in cellTypeList.rangeX()) {
                for (y in cellTypeList.rangeY()) {
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
            val x = (boxX - startX) * CELL_SIZE
            val y = (boxY - startY) * CELL_SIZE
            val width = boxW * CELL_SIZE
            val height = boxH * CELL_SIZE
            writer.println("fairway,box,${x}f,${y}f,${width}f,${height}f")
        }
        writer.close()
        println("save body file to course/01body")
    }
}