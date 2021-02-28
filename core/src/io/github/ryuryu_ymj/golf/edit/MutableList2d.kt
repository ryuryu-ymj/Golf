package io.github.ryuryu_ymj.golf.edit

import kotlinx.serialization.Serializable

@Serializable
class MutableList2d<T>() {
    private val topRight = mutableListOf<MutableList<T>>()
    private val topLeft = mutableListOf<MutableList<T>>()
    private val bottomRight = mutableListOf<MutableList<T>>()
    private val bottomLeft = mutableListOf<MutableList<T>>()

    var firstX = 0; private set
    var lastX = -1; private set
    var firstY = 0; private set
    var lastY = -1; private set

    constructor(firstX: Int, lastX: Int, firstY: Int, lastY: Int, init: (Int, Int) -> T) : this() {
        add(lastX + 1, -firstX, lastY + 1, -firstY, init)
    }

    constructor(rangeX: IntRange, rangeY: IntRange, init: (Int, Int) -> T) :
            this(rangeX.first, rangeX.last, rangeY.first, rangeY.last, init)

    val width get() = lastX - firstX + 1
    val height get() = lastY - firstY + 1
    val rangeX get() = firstX..lastX
    val rangeY get() = firstY..lastY
    val isEmpty get() = width == 0 || height == 0

    operator fun get(x: Int, y: Int): T {
        return if (x >= 0) {
            if (y >= 0) topRight[x][y]
            else bottomRight[x][-y - 1]
        } else {
            if (y >= 0) topLeft[-x - 1][y]
            else bottomLeft[-x - 1][-y - 1]
        }
    }

    operator fun set(x: Int, y: Int, value: T) {
        if (x >= 0) {
            if (y >= 0) topRight[x][y] = value
            else bottomRight[x][-y - 1] = value
        } else {
            if (y >= 0) topLeft[-x - 1][y] = value
            else bottomLeft[-x - 1][-y - 1] = value
        }
    }

    fun add(
        right: Int = 0, left: Int = 0, top: Int = 0, bottom: Int = 0,
        init: (Int, Int) -> T
    ) {
        if (top > 0) {
            topRight.forEachIndexed { x, col ->
                for (y in lastY + 1..lastY + top) {
                    col.add(init(x, y))
                }
            }
            topLeft.forEachIndexed { nx, col ->
                for (y in lastY + 1..lastY + top) {
                    col.add(init(-nx - 1, y))
                }
            }
            lastY += top
        }
        if (bottom > 0) {
            bottomRight.forEachIndexed { x, col ->
                for (y in firstY - 1 downTo firstY - bottom) {
                    col.add(init(x, y))
                }
            }
            bottomLeft.forEachIndexed { nx, col ->
                for (y in firstY - 1 downTo firstY - bottom) {
                    col.add(init(-nx - 1, y))
                }
            }
            firstY -= bottom
        }
        if (right > 0) {
            for (x in lastX + 1..lastX + right) {
                val col = mutableListOf<T>()
                for (y in 0..lastY) {
                    col.add(init(x, y))
                }
                topRight.add(col)
            }
            for (x in lastX + 1..lastX + right) {
                val col = mutableListOf<T>()
                for (y in -1 downTo firstY) {
                    col.add(init(x, y))
                }
                bottomRight.add(col)
            }
            lastX += right
        }
        if (left > 0) {
            for (x in firstX - 1 downTo firstX - left) {
                val col = mutableListOf<T>()
                for (y in 0..lastY) {
                    col.add(init(x, y))
                }
                topLeft.add(col)
            }
            for (x in firstX - 1 downTo firstX - left) {
                val col = mutableListOf<T>()
                for (y in -1 downTo firstY) {
                    col.add(init(x, y))
                }
                bottomLeft.add(col)
            }
            firstX -= left
        }
        bottomLeft.toTypedArray()
    }

    fun <R> map(transform: (T) -> R): MutableList2d<R> {
        return MutableList2d(firstX, lastX, firstY, lastY) { x, y ->
            transform(get(x, y))
        }
    }

    fun forEach(action: (T) -> Unit) {
        topRight.forEach { it.forEach(action) }
        topLeft.forEach { it.forEach(action) }
        bottomRight.forEach { it.forEach(action) }
        bottomLeft.forEach { it.forEach(action) }
    }
}

inline fun <reified T> MutableList2d<T>.toArray2d() =
    Array(width) { x ->
        Array(height) { y ->
            get(firstX + x, firstY + y)
        }
    }