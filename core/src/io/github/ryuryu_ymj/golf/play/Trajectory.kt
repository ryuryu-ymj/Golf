package io.github.ryuryu_ymj.golf.play

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.scenes.scene2d.Actor
import ktx.box2d.body
import ktx.box2d.circle
import ktx.box2d.createWorld
import ktx.math.vec2

class Trajectory(asset: AssetManager, gravity: Vector2) : Actor() {
    private val texture = asset.get<Texture>("image/ball.png")
    private val world = createWorld(gravity)
    private val ball = world.body {
        circle(radius = BALL_SIZE / 2) {
            density = BALL_DENSITY
        }
        type = BodyDef.BodyType.DynamicBody
        linearDamping = NORMAL_DAMPING
    }
    private val dots = Array(20) { vec2() }
    private val dotSize = BALL_SIZE / 2

    init {
        isVisible = false
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
        impulseX: Float, impulseY: Float,
    ) {
        isVisible = true
        ball.setTransform(startX, startY, 0f)
        ball.setLinearVelocity(0f, 0f)
        ball.applyLinearImpulse(
            impulseX, impulseY,
            ball.position.x, ball.position.y,
            true
        )
        dots.forEach {
            for (i in 1..10) world.step(1f / 60, 6, 2)
            it.set(ball.position)
        }
    }
}