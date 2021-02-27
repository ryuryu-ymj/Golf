package io.github.ryuryu_ymj.golf.edit

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.FitViewport
import io.github.ryuryu_ymj.golf.MyInputProcessor
import io.github.ryuryu_ymj.golf.MyTouchable
import ktx.app.KtxScreen

class EditScreen : KtxScreen, MyTouchable {
    private val batch = SpriteBatch()
    private val camera = OrthographicCamera(4f, 2.25f)
    private val viewport = FitViewport(
        camera.viewportWidth,
        camera.viewportHeight, camera
    )
    private val stage = Stage(viewport, batch)
    private val input = MyInputProcessor(viewport, this)

    init {
        Gdx.input.inputProcessor = input
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
    }

    override fun render(delta: Float) {
        stage.draw()

        stage.act()
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