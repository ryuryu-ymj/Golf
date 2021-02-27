package io.github.ryuryu_ymj.golf.edit

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import ktx.actors.KtxInputListener

const val CELL_SIZE = 0.2f

class Cell(private val asset: AssetManager, ix: Int, iy: Int) : Actor() {
    var type = CellType.NULL

    init {
        setPosition(ix * CELL_SIZE, iy * CELL_SIZE)
        setSize(CELL_SIZE, CELL_SIZE)
        addListener(object : KtxInputListener() {
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                cursorType?.let {
                    type = it
                    return true
                }
                return false
            }
        })
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        val texture = asset.get<Texture>(type.path)
        batch.draw(texture, x, y, width, height)
    }
}

enum class CellType(val path: String) {
    NULL("image/cell.png"), FAIRWAY("image/fairway.png")
}