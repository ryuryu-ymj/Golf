package io.github.ryuryu_ymj.golf.edit

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Actor

class Brush(private val asset: AssetManager) : Actor() {
    var type: CellType? = null; private set

    init {
        setPosition(0f, 0f)
        setSize(50f, 50f)
    }

    override fun act(delta: Float) {
        super.act(delta)
        when {
            Gdx.input.isKeyPressed(Input.Keys.M) ->
                type = null
            Gdx.input.isKeyPressed(Input.Keys.N) ->
                type = CellType.NULL
            Gdx.input.isKeyPressed(Input.Keys.S) &&
                    !Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) ->
                type = CellType.START
            Gdx.input.isKeyPressed(Input.Keys.F) ->
                type = CellType.FAIRWAY
        }
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        type?.let {
            val texture = asset.get<Texture>(it.path)
            batch.draw(texture, x, y, width, height)
        }
    }
}