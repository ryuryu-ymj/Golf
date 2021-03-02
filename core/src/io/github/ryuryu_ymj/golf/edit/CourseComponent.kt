package io.github.ryuryu_ymj.golf.edit

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Actor
import kotlinx.serialization.Serializable

const val COMPONENT_UNIT_SIZE = 0.2f

class CourseComponent(
    asset: AssetManager,
    val type: CourseComponentType,
    val ix: Int, val iy: Int,
) : Actor() {
    val ih: Int = type.ih
    val iw: Int = type.iw
    private val texture: Texture = asset.get(type.texturePath)

    init {
        setPosition(ix * COMPONENT_UNIT_SIZE, iy * COMPONENT_UNIT_SIZE)
        setSize(iw * COMPONENT_UNIT_SIZE, ih * COMPONENT_UNIT_SIZE)
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        batch.draw(texture, x, y, width, height)
    }

    fun createCourseComponentData() =
        CourseComponentData(ix, iy, type)
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
    val iw: Int, val ih: Int,
    val texturePath: String
) {
    FAIRWAY(1, 1, "image/fairway.png"),
    FAIRWAY_SLOPE_UP_21(2, 1, "image/fairway-slope-up-21.png"),
    FAIRWAY_SLOPE_DOWN_21(2, 1, "image/fairway-slope-up-21.png"),
}