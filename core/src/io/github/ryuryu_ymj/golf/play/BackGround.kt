package io.github.ryuryu_ymj.golf.play

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.Actor
import io.github.ryuryu_ymj.golf.edit.COMPONENT_UNIT_SIZE

class BackGround(width: Float, height: Float) : Actor() {
    private val texture: Texture

    init {
        val cellSizePx = 20
        val col = (width / COMPONENT_UNIT_SIZE).toInt() + 2
        val row = (height / COMPONENT_UNIT_SIZE).toInt() + 2
        setSize(col * COMPONENT_UNIT_SIZE, row * COMPONENT_UNIT_SIZE)
        setOrigin(col / 2 * COMPONENT_UNIT_SIZE, row / 2 * COMPONENT_UNIT_SIZE)
        setPosition(-originX, -originY)
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
        val ix = MathUtils.floor(stage.camera.position.x / COMPONENT_UNIT_SIZE)
        val iy = MathUtils.floor(stage.camera.position.y / COMPONENT_UNIT_SIZE)
        setPosition(
            ix * COMPONENT_UNIT_SIZE - originX,
            iy * COMPONENT_UNIT_SIZE - originY
        )
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        batch.draw(texture, x, y, width, height)
    }

    fun dispose() {
        texture.dispose()
    }
}