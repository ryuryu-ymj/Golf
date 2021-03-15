package io.github.ryuryu_ymj.golf.play

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import ktx.box2d.body
import ktx.box2d.circle
import ktx.box2d.createWorld
import ktx.graphics.use

class Trajectory(gravity: Vector2) {
    private val shape = ShapeRenderer()
    private val world = createWorld(gravity)
    private val ball = world.body {
        circle(radius = BALL_SIZE / 2) {
            density = BALL_DENSITY
        }
        type = BodyDef.BodyType.DynamicBody
        linearDamping = NORMAL_DAMPING
    }
    private val x1arr = FloatArray(100)
    private val y1arr = FloatArray(100)
    private val x2arr = FloatArray(100)
    private val y2arr = FloatArray(100)
    var isVisible = false

    fun draw(camera: Camera) {
        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        shape.use(ShapeRenderer.ShapeType.Filled, camera) {
            it.setColor(1f, 1f, 1f, 0.4f)
            for (i in 0 until x1arr.lastIndex) {
                it.triangle(
                    x1arr[i], y1arr[i],
                    x1arr[i + 1], y1arr[i + 1],
                    x2arr[i], y2arr[i]
                )
            }
            for (i in 0 until x2arr.lastIndex) {
                it.triangle(
                    x2arr[i], y2arr[i],
                    x2arr[i + 1], y2arr[i + 1],
                    x1arr[i + 1], y1arr[i + 1]
                )
            }
        }
        Gdx.gl.glDisable(GL20.GL_BLEND)
    }

    fun setBallCondition(
        startX: Float, startY: Float,
        velocityX: Float, velocityY: Float,
        minRatio: Float, maxRatio: Float
    ) {
        isVisible = true

        val vxSign = if (velocityX > 0) 1 else -1

        ball.setTransform(startX, startY, 0f)
        ball.setLinearVelocity(0f, 0f)
        ball.applyLinearImpulse(
            velocityX * minRatio * ball.mass,
            velocityY * minRatio * ball.mass,
            ball.position.x, ball.position.y,
            true
        )
        for (i in 0..x1arr.lastIndex) {
            val v = ball.linearVelocity
            val lenV = v.len()
            x1arr[i] = ball.position.x + BALL_SIZE / 2 * v.y / lenV * vxSign
            y1arr[i] = ball.position.y - BALL_SIZE / 2 * v.x / lenV * vxSign
            for (j in 1..4) world.step(1f / 60, 6, 2)
        }

        ball.setTransform(startX, startY, 0f)
        ball.setLinearVelocity(0f, 0f)
        ball.applyLinearImpulse(
            velocityX * maxRatio * ball.mass,
            velocityY * maxRatio * ball.mass,
            ball.position.x, ball.position.y,
            true
        )
        for (i in 0..x1arr.lastIndex) {
            val v = ball.linearVelocity
            val lenV = v.len()
            x2arr[i] = ball.position.x - BALL_SIZE / 2 * v.y / lenV * vxSign
            y2arr[i] = ball.position.y + BALL_SIZE / 2 * v.x / lenV * vxSign
            for (j in 1..4) world.step(1f / 60, 6, 2)
        }
    }

    fun dispose() {
        shape.dispose()
    }
}