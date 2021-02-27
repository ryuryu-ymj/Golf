package io.github.ryuryu_ymj.golf.edit

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Actor

internal var cursorType: CellType? = null; private set

class Cursor(private val asset: AssetManager) : Actor() {
    init {
        setPosition(0f, 0f)
        setSize(50f, 50f)
    }

    override fun act(delta: Float) {
        super.act(delta)
        when {
            Gdx.input.isKeyPressed(Input.Keys.F) ->
                cursorType = CellType.FAIRWAY
            Gdx.input.isKeyPressed(Input.Keys.N) ->
                cursorType = CellType.NULL
            Gdx.input.isKeyPressed(Input.Keys.M) ->
                cursorType = null
        }
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        cursorType?.let {
            val texture = asset.get<Texture>(it.path)
            batch.draw(texture, x, y, width, height)
        }
    }
}