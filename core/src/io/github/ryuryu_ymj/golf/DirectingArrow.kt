package io.github.ryuryu_ymj.golf

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.Actor
import kotlin.math.atan2
import kotlin.math.hypot

class DirectingArrow : Actor() {
    private val texture: Texture

    init {
        isVisible = false
        height = 0.3f
        setOrigin(0f, height / 2)
        val pixmap = Pixmap(160, 640, Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.GREEN)
        pixmap.fillTriangle(
            0, pixmap.height / 2,
            pixmap.width * 3 / 4, pixmap.height / 4,
            pixmap.width * 3 / 4, pixmap.height * 3 / 4
        )
        pixmap.fillTriangle(
            pixmap.width * 3 / 4, 0,
            pixmap.width * 3 / 4, pixmap.height,
            pixmap.width, pixmap.height / 2
        )
        texture = Texture(pixmap)
        pixmap.dispose()
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        batch.draw(
            texture, x, y, originX, originY,
            width, height, scaleX, scaleY, rotation,
            0, 0, texture.width, texture.height, false, false
        )
    }

    fun setBeginAndEnd(x1: Float, y1: Float, x2: Float, y2: Float) {
        isVisible = true
        setPosition(x1 - originX, y1 - originY)
        width = hypot(x2 - x1, y2 - y1)
        rotation = atan2(y2 - y1, x2 - x1) * MathUtils.radiansToDegrees
    }

    fun dispose() {
        texture.dispose()
    }
}