package io.github.ryuryu_ymj.golf.play

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Actor

class BackGround(width: Float, height: Float) : Actor() {
    private val texture: Texture
    private val cell = width / 10

    init {
        this.width = width + cell * 2
        this.height = height + cell * 2
        val pixmap = Pixmap(
            600, (500 * height / width + 100).toInt(),
            Pixmap.Format.RGBA8888
        )
        pixmap.setColor(Color.WHITE)
        pixmap.fill()
        pixmap.setColor(Color.LIGHT_GRAY)
        for (lx in 0 until pixmap.width step 50) {
            pixmap.drawLine(lx, 0, lx, pixmap.height)
        }
        for (ly in 0 until pixmap.height step 50) {
            pixmap.drawLine(0, ly, pixmap.width, ly)
        }
        texture = Texture(pixmap)
        pixmap.dispose()
    }

    override fun act(delta: Float) {
        super.act(delta)
        val cx = x - stage.camera.position.x + stage.width / 2
        if (cx > 0) {
            x -= cell
        } else if (cx <= -cell) {
            x += cell
        }
        val cy = y - stage.camera.position.y + stage.height / 2
        if (cy > 0) {
            y -= cell
        } else if (cy <= -cell) {
            y += cell
        }
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        batch.draw(texture, x, y, width, height)
    }

    fun dispose() {
        texture.dispose()
    }
}