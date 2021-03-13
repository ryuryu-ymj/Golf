package io.github.ryuryu_ymj.golf.play

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Actor
import ktx.math.vec2

class Trajectory(asset: AssetManager) : Actor() {
    private val dots = Array(20) { vec2() }
    private val dotSize = BALL_SIZE / 2
    private val texture = asset.get<Texture>("image/ball.png")

    init {
        setCondition(0f, 0f, 1f, 1f, -3f)
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        dots.forEach {
            batch.draw(
                texture,
                it.x - dotSize / 2, it.y - dotSize / 2,
                dotSize, dotSize
            )
        }
    }

    fun setCondition(
        startX: Float, startY: Float,
        velocityX: Float, velocityY: Float,
        gravityY: Float
    ) {
        val dt = 0.2f
        dots.forEachIndexed { i, dot ->
            val t = dt * i
            dot.set(
                startX + velocityX * t,
                startY + velocityY * t + gravityY * t * t / 2
            )
        }
    }
}