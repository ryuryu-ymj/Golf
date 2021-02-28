package io.github.ryuryu_ymj.golf.edit

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import io.github.ryuryu_ymj.golf.BALL_SIZE
import ktx.actors.KtxInputListener

const val CELL_SIZE = 0.2f

class Cell(
    private val asset: AssetManager, editScreen: EditScreen,
    ix: Int, iy: Int
) : Actor() {
    var type = CellType.NULL

    init {
        setPosition(ix * CELL_SIZE, iy * CELL_SIZE)
        setSize(CELL_SIZE, CELL_SIZE)
        addListener(object : KtxInputListener() {
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                brushType?.let {
                    if (type != it && type != CellType.START) {
                        type = it
                        if (it == CellType.START) {
                            editScreen.setStart(ix, iy)
                        }
                        return true
                    }
                }
                return false
            }
        })
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        if (type != CellType.START) {
            val texture = asset.get<Texture>(type.path)
            batch.draw(texture, x, y, width, height)
        } else {
            val ball = asset.get<Texture>(CellType.START.path)
            val cell = asset.get<Texture>(CellType.NULL.path)
            batch.draw(cell, x, y, width, height)
            batch.draw(
                ball, x + width / 2 - BALL_SIZE / 2, y,
                BALL_SIZE, BALL_SIZE
            )
        }
    }
}

enum class CellType(val path: String) {
    NULL("image/cell.png"), START("image/ball.png"), FAIRWAY("image/fairway.png")
}