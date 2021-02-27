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

    init {
        camera.position.setZero()
        uiStage.addActor(Brush(asset))

        val file = Gdx.files.internal("course/01raw")
        cellList =
            if (file.exists()) {
                val read = Json.decodeFromString<GdxArray2d<CellType>>(file.readString())
                GdxArray2d(read.rangeX(), read.rangeY()) { x, y ->
                    Cell(asset, x, y).also {
                        stage.addActor(it)
                        it.type = read[x, y]
                    }
                }
            } else {
                GdxArray2d(-3..3, -3..3) { x, y ->
                    Cell(asset, x, y).also {
                        stage.addActor(it)
                    }
                }
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
        if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            val cellTypeList = cellList.map { it.type }
            val file = Gdx.files.local("course/01raw")
            file.writeString(Json.encodeToString(cellTypeList), false)
            println("save edit file to course/01raw")
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
}