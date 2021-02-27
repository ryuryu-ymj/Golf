package io.github.ryuryu_ymj.golf.edit

import kotlinx.serialization.Serializable

@Serializable
class GdxArray2d<T>() {
    private val topRight = mutableListOf<MutableList<T>>()
    private val topLeft = mutableListOf<MutableList<T>>()
    private val bottomRight = mutableListOf<MutableList<T>>()
    private val bottomLeft = mutableListOf<MutableList<T>>()

    private var w1 = 0
    private var w2 = 0
    private var h1 = 0
    private var h2 = 0

    constructor(startX: Int, endX: Int, startY: Int, endY: Int, init: (Int, Int) -> T) : this() {
        add(endX + 1, -startX, endY + 1, -startY, init)
    }

    constructor(rangeX: IntRange, rangeY: IntRange, init: (Int, Int) -> T) :
            this(rangeX.first, rangeX.last, rangeY.first, rangeY.last, init)

    fun width() = w1 + w2
    fun height() = h1 + h2
    fun startX() = -w2
    fun endX() = w1 - 1
    fun startY() = -h2
    fun endY() = h1 - 1
    fun rangeX() = startX()..endX()
    fun rangeY() = startY()..endY()

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
                for (y in h1 until h1 + top) {
                    col.add(init(x, y))
                }
            }
            topLeft.forEachIndexed { nx, col ->
                for (y in h1 until h1 + top) {
                    col.add(init(-nx - 1, y))
                }
            }
            h1 += top
        }
        if (bottom > 0) {
            bottomRight.forEachIndexed { x, col ->
                for (ny in h2 until h2 + bottom) {
                    col.add(init(x, -ny - 1))
                }
            }
            bottomLeft.forEachIndexed { nx, col ->
                for (ny in h2 until h2 + bottom) {
                    col.add(init(-nx - 1, -ny - 1))
                }
            }
            h2 += bottom
        }
        if (right > 0) {
            for (x in w1 until w1 + right) {
                val col = mutableListOf<T>()
                for (y in 0 until h1) {
                    col.add(init(x, y))
                }
                topRight.add(col)
            }
            for (x in w1 until w1 + right) {
                val col = mutableListOf<T>()
                for (ny in 0 until h2) {
                    col.add(init(x, -ny - 1))
                }
                bottomRight.add(col)
            }
            w1 += right
        }
        if (left > 0) {
            for (nx in w2 until w2 + left) {
                val col = mutableListOf<T>()
                for (y in 0 until h1) {
                    col.add(init(-nx - 1, y))
                }
                topLeft.add(col)
            }
            for (nx in w2 until w2 + left) {
                val col = mutableListOf<T>()
                for (ny in 0 until h2) {
                    col.add(init(-nx - 1, -ny - 1))
                }
                bottomLeft.add(col)
            }
            w2 += left
        }
    }

    fun <R> map(transform: (T) -> R): GdxArray2d<R> {
        return GdxArray2d(startX(), endX(), startY(), endY()) { x, y ->
            transform(get(x, y))
        }
    }
}