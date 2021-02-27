package io.github.ryuryu_ymj.golf.edit

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.scenes.scene2d.Actor

const val CELL_SIZE = 0.2f

class Cell(private val asset: AssetManager, ix: Int, iy: Int) : Actor() {
    var type = CellType.NULL

    init {
        setPosition(ix * CELL_SIZE, iy * CELL_SIZE)
        setSize(CELL_SIZE, CELL_SIZE)
    }
}

enum class CellType {
    NULL, FAIRWAY
}