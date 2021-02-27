package io.github.ryuryu_ymj.golf

import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.utils.viewport.Viewport
import ktx.math.vec3

class MyInputProcessor(private val viewport: Viewport, private val touchable: MyTouchable) : InputAdapter() {
    private val worldPos = vec3()
    private var screenX = 0
    private var screenY = 0
    var isDragging = false; private set

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        // ignore if its not left mouse button or first touch pointer
        if (button != Input.Buttons.LEFT || pointer > 0) return false
        viewport.unproject(worldPos.set(screenX.toFloat(), screenY.toFloat(), 0f))
        isDragging = true
        if (touchable.touchDown(worldPos.x, worldPos.y)) return true

        this.screenX = screenX
        this.screenY = screenY
        return true
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        if (!isDragging) return false
        viewport.unproject(worldPos.set(screenX.toFloat(), screenY.toFloat(), 0f))
        if (touchable.touchDragged(worldPos.x, worldPos.y)) return true

        viewport.camera.translate(
            -(screenX - this.screenX) * viewport.worldWidth / viewport.screenWidth,
            (screenY - this.screenY) * viewport.worldHeight / viewport.screenHeight,
            0f
        )
        this.screenX = screenX
        this.screenY = screenY
        return true
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        if (!isDragging) return false
        viewport.unproject(worldPos.set(screenX.toFloat(), screenY.toFloat(), 0f))
        isDragging = false
        touchable.touchUp(worldPos.x, worldPos.y)
        return true
    }

    fun cancelDragging() {
        isDragging = false
    }
}