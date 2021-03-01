package io.github.ryuryu_ymj.golf.edit

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Actor
import io.github.ryuryu_ymj.golf.play.BALL_SIZE

const val CELL_SIZE = 0.2f

class Cell(
    private val asset: AssetManager,
    val ix: Int, val iy: Int,
    var localIX: Int = 0, var localIY: Int = 0
) : Actor() {
    var type = CellType.NULL

    init {
        setPosition(ix * CELL_SIZE, iy * CELL_SIZE)
        setSize(CELL_SIZE, CELL_SIZE)
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        if (localIX == 0 && localIY == 0) {
            if (type != CellType.START) {
                val texture = asset.get<Texture>(type.path)
                batch.draw(
                    texture, x, y,
                    CELL_SIZE * type.width, CELL_SIZE * type.height
                )
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
}

enum class CellType(val path: String, val width: Int = 1, val height: Int = 1) {
    NULL("image/cell.png"),
    START("image/ball.png"),
    FAIRWAY("image/fairway.png"),
    FAIRWAY_SLOPE_UP_21("image/fairway-slope-up-21.png", 2, 1)
}