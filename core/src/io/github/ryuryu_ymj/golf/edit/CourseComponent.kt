package io.github.ryuryu_ymj.golf.edit

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import kotlinx.serialization.Serializable
import ktx.collections.gdxArrayOf

const val COMPONENT_UNIT_SIZE = 0.2f

class CourseComponent(
    asset: AssetManager,
    val type: CourseComponentType,
    val ix: Int, val iy: Int,
) : Actor() {
    val ih: Int = type.ih
    val iw: Int = type.iw
    var rightContacted = false; private set
    var leftContacted = false; private set
    var topContacted = false; private set
    var bottomContacted = false; private set
    private val texture: Texture = asset.get(type.texturePath)

    init {
        setPosition(ix * COMPONENT_UNIT_SIZE, iy * COMPONENT_UNIT_SIZE)
        setSize(iw * COMPONENT_UNIT_SIZE, ih * COMPONENT_UNIT_SIZE)
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        batch.draw(texture, x, y, width, height)
    }

    fun setContact(components: List<CourseComponent>) {
        rightContacted = components.findAt(ix + iw, iy) != null
        leftContacted = components.findAt(ix - 1, iy) != null
        topContacted = components.findAt(ix, iy + ih) != null
        bottomContacted = components.findAt(ix, iy - 1) != null
    }

    fun createCourseComponentData() =
        CourseComponentData(ix, iy, type)

    fun getBodyText(teeIX: Int, teeIY: Int): String {
        if (type.vertex == 0b1111) {
            val x = (ix - teeIX - 0.5f) * COMPONENT_UNIT_SIZE
            val y = (iy - teeIY - 1) * COMPONENT_UNIT_SIZE
            val w = iw * COMPONENT_UNIT_SIZE
            val h = ih * COMPONENT_UNIT_SIZE
            return "fairway,box,$x,$y,$w,$h,"
        } else {
            val vertexes = arrayOf(
                intVec2(ix, iy), intVec2(ix + iw, iy),
                intVec2(ix + iw, iy + ih), intVec2(ix, iy + ih)
            )
            val poly = gdxArrayOf<Vector2>()
            for (i in 0..3) {
                if (type.vertex and (1 shl i) != 0) {
                    poly.add(vertexes[i])
                }
            }
            var str = "fairway,polygon,"
            poly.forEach {
                val x = (it.x - teeIX - 0.5f) * COMPONENT_UNIT_SIZE
                val y = (it.y - teeIY - 1) * COMPONENT_UNIT_SIZE
                str += "$x,$y,"
            }
            return str
        }
    }
}

fun List<CourseComponent>.findAt(ix: Int, iy: Int) = find {
    ix >= it.ix && ix < it.ix + it.iw &&
            iy >= it.iy && iy < it.iy + it.ih
}

@Serializable
data class CourseComponentData(
    val ix: Int, val iy: Int,
    val type: CourseComponentType
) {
    fun createCourseComponent(asset: AssetManager) =
        CourseComponent(asset, type, ix, iy)
}

enum class CourseComponentType(
    val texturePath: String,
    val vertex: Int = 0b1111,
    val iw: Int = 1, val ih: Int = 1,
) {
    TEE("image/ball.png"),
    FAIRWAY("image/fairway.png"),
    FAIRWAY_SLOPE_UP_11("image/fairway-slope-up-21.png", 0b0111),
    FAIRWAY_SLOPE_DOWN_11("image/fairway-slope-down-21.png", 0b1011),
    FAIRWAY_SLOPE_UP_21("image/fairway-slope-up-21.png", 0b0111, 2, 1),
    FAIRWAY_SLOPE_DOWN_21("image/fairway-slope-down-21.png", 0b1011, 2, 1),
}