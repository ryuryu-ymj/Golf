package io.github.ryuryu_ymj.golf.edit

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.scenes.scene2d.ui.Label
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.defaultStyle

class Brush : Label("MOVE", Scene2DSkin.defaultSkin, defaultStyle) {
    var type: BrushType = BrushType.MOVE; private set

    override fun act(delta: Float) {
        super.act(delta)
        when {
            Gdx.input.isKeyJustPressed(Input.Keys.M) -> {
                type = BrushType.MOVE
                setText(type.name)
            }
            Gdx.input.isKeyJustPressed(Input.Keys.D) -> {
                type = BrushType.DELETE
                setText(type.name)
            }
            Gdx.input.isKeyJustPressed(Input.Keys.T) -> {
                type = BrushType.TEE
                setText(type.name)
            }
            Gdx.input.isKeyJustPressed(Input.Keys.F) -> {
                type = when (type) {
                    BrushType.FAIRWAY -> BrushType.FAIRWAY_SLOPE_UP
                    BrushType.FAIRWAY_SLOPE_UP -> BrushType.FAIRWAY_SLOPE_DOWN
                    BrushType.FAIRWAY_SLOPE_DOWN -> BrushType.FAIRWAY
                    else -> BrushType.FAIRWAY
                }
                setText(type.name)
            }
        }
    }
}

enum class BrushType {
    MOVE, DELETE, TEE, FAIRWAY, FAIRWAY_SLOPE_UP, FAIRWAY_SLOPE_DOWN
}