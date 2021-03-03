package io.github.ryuryu_ymj.golf.edit

import com.badlogic.gdx.math.Vector2

data class Edge(val begin: Vector2, val end: Vector2)

fun intVec2(x: Int, y: Int) = Vector2(x.toFloat(), y.toFloat())