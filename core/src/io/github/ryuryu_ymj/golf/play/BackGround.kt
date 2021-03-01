package io.github.ryuryu_ymj.golf.play

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Actor
import io.github.ryuryu_ymj.golf.edit.CELL_SIZE

class BackGround(width: Float, height: Float) : Actor() {
    private val texture: Texture

    init {
        val cellSizePx = 20
        val col = (width / CELL_SIZE).toInt() + 2
        val row = (height / CELL_SIZE).toInt() + 2
        this.width = col * CELL_SIZE
        this.height = row * CELL_SIZE
        val pixmap = Pixmap(
            col * cellSizePx, row * cellSizePx,
            Pixmap.Format.RGBA8888
        )
        pixmap.setColor(Color.WHITE)
        pixmap.fill()
        pixmap.setColor(Color.LIGHT_GRAY)
        for (lx in 0 until pixmap.width step cellSizePx) {
            pixmap.drawLine(lx, 0, lx, pixmap.height)
        }
        for (ly in 0 until pixmap.height step cellSizePx) {
            pixmap.drawLine(0, ly, pixmap.width, ly)
        }
        texture = Texture(pixmap)
        pixmap.dispose()
    }

    override fun act(delta: Float) {
        super.act(delta)
        val cx = x - stage.camera.position.x + stage.width / 2
        if (cx > 0) {
            x -= CELL_SIZE
        } else if (cx <= -CELL_SIZE) {
            x += CELL_SIZE
        }
        val cy = y - stage.camera.position.y + stage.height / 2
        if (cy > 0) {
            y -= CELL_SIZE
        } else if (cy <= -CELL_SIZE) {
            y += CELL_SIZE
        }
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        batch.draw(texture, x, y, width, height)
    }

    fun dispose() {
        texture.dispose()
    }
}