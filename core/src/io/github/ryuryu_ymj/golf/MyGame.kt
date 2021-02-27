package io.github.ryuryu_ymj.golf

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Texture
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.assets.load

class MyGame : KtxGame<KtxScreen>() {
    private val asset = AssetManager()

    override fun create() {
        asset.load<Texture>("image/ball.png")

        asset.finishLoading()

        addScreen(PlayScreen(asset))
        setScreen<PlayScreen>()
    }

    override fun dispose() {
        super.dispose()
        asset.dispose()
    }
}