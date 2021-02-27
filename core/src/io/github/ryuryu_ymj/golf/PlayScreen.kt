package io.github.ryuryu_ymj.golf

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.FitViewport
import ktx.actors.plusAssign
import ktx.app.KtxScreen
import ktx.box2d.createWorld
import ktx.math.vec2
import kotlin.math.hypot

class PlayScreen(asset: AssetManager) : KtxScreen, MyTouchable {
    private val batch = SpriteBatch()
    private val camera = OrthographicCamera(4f, 2.25f)
    private val viewport = FitViewport(
        camera.viewportWidth,
        camera.viewportHeight, camera
    )
    private val stage = Stage(viewport, batch)
    private val world = createWorld(vec2(0f, -10f))
    private val debugRenderer = Box2DDebugRenderer()
    private val input = MyInputProcessor(viewport, this)
    private var isDraggingArrow = false

    private val bg = BackGround(stage.width, stage.height)
    private val ball = Ball(asset, world, 0f, 1f)
    private val arrow = DirectingArrow()
    //private val fairways = GdxArray<Ground>()

    private val course = CourseManager(stage, world)

    init {
        camera.position.x = 0f

        stage += bg
        //course.readCourse(1)
        stage += ball
        stage += arrow
    }

    override fun show() {
        Gdx.input.inputProcessor = input
    }

    override fun hide() {
        Gdx.input.inputProcessor = null
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
    }

    override fun render(delta: Float) {
        //camera.update()
        stage.draw()
        debugRenderer.render(world, camera.combined)

        world.step(1f / 60, 6, 2)
        stage.act()
        if (ball.body.isAwake) {
            camera.position.set(ball.x, ball.y, 0f)
            if (input.isDragging) input.cancelDragging()
        }
    }

    override fun touchDown(x: Float, y: Float): Boolean {
        if (hypot(x - ball.centerX, y - ball.centerY) < ball.width * 5) {
            isDraggingArrow = true
            return true
        }
        return false
    }

    override fun touchDragged(x: Float, y: Float): Boolean {
        if (isDraggingArrow) {
            if (hypot(x - ball.centerX, y - ball.centerY) < ball.width * 5) {
                arrow.isVisible = false
            } else {
                arrow.setBeginAndEnd(
                    ball.centerX, ball.centerY,
                    ball.centerX * 2 - x,
                    ball.centerY * 2 - y
                )
            }
            return true
        }
        return false
    }

    override fun touchUp(x: Float, y: Float): Boolean {
        if (isDraggingArrow) {
            isDraggingArrow = false
            if (arrow.isVisible) {
                val power = ball.body.mass * 5f
                ball.body.applyLinearImpulse(
                    (ball.centerX - x) * power, (ball.centerY - y) * power,
                    ball.body.worldCenter.x, ball.body.worldCenter.y,
                    true
                )
            }
            arrow.isVisible = false
            return true
        }
        return false
    }

    override fun dispose() {
        course.dispose()
        bg.dispose()
        arrow.dispose()
        debugRenderer.dispose()
        world.dispose()
        stage.dispose()
        batch.dispose()
    }
}

val Actor.centerX; inline get() = x + width / 2
val Actor.centerY; inline get() = y + height / 2