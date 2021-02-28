package io.github.ryuryu_ymj.golf.edit

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Actor

internal var brushType: CellType? = null; private set

class Brush(private val asset: AssetManager) : Actor() {
    init {
        setPosition(0f, 0f)
        setSize(50f, 50f)
    }

    override fun act(delta: Float) {
        super.act(delta)
        when {
            Gdx.input.isKeyPressed(Input.Keys.M) ->
                brushType = null
            Gdx.input.isKeyPressed(Input.Keys.N) ->
                brushType = CellType.NULL
            Gdx.input.isKeyPressed(Input.Keys.S) &&
                    !Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) ->
                brushType = CellType.START
            Gdx.input.isKeyPressed(Input.Keys.F) ->
                brushType = CellType.FAIRWAY
        }
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        brushType?.let {
            val texture = asset.get<Texture>(it.path)
            batch.draw(texture, x, y, width, height)
        }
    }
}