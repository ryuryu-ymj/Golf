package io.github.ryuryu_ymj.golf

interface MyTouchable {
    fun touchDown(x: Float, y: Float): Boolean
    fun touchDragged(x: Float, y: Float): Boolean
    fun touchUp(x: Float, y: Float): Boolean
}