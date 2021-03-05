package io.github.ryuryu_ymj.golf

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import io.github.ryuryu_ymj.golf.edit.EditScreen
import io.github.ryuryu_ymj.golf.play.PlayScreen
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.assets.load
import ktx.scene2d.Scene2DSkin

class MyGame : KtxGame<KtxScreen>() {
    val asset = AssetManager()

    override fun create() {
        asset.load<Texture>("image/ball.png")
        asset.load<Texture>("image/hole.png")
        asset.load<Texture>("image/fairway.png")
        asset.load<Texture>("image/fairway-slope-up-21.png")
        asset.load<Texture>("image/fairway-slope-down-21.png")

        asset.load<Skin>("skin/test-skin.json")

        asset.finishLoading()

        Scene2DSkin.defaultSkin = asset.get("skin/test-skin.json")

        addScreen(PlayScreen(this))
        addScreen(EditScreen(this))
        setScreen<EditScreen>()
    }

    override fun dispose() {
        super.dispose()
        asset.dispose()
    }
}