package io.github.ryuryu_ymj.golf.edit

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Actor
import kotlinx.serialization.Serializable

const val COMPONENT_UNIT_SIZE = 0.2f

abstract class CourseComponent(
    val ix: Int, val iy: Int,
    val iw: Int, val ih: Int,
    private val texture: Texture
) : Actor() {
    init {
        setPosition(ix * COMPONENT_UNIT_SIZE, iy * COMPONENT_UNIT_SIZE)
        setSize(iw * COMPONENT_UNIT_SIZE, ih * COMPONENT_UNIT_SIZE)
    }

    final override fun setPosition(x: Float, y: Float) {
        super.setPosition(x, y)
    }

    final override fun setSize(width: Float, height: Float) {
        super.setSize(width, height)
    }

    final override fun draw(batch: Batch, parentAlpha: Float) {
        batch.draw(texture, x, y, width, height)
    }

    abstract fun createCourseComponentData() : CourseComponentData;
}

@Serializable
class CourseComponentData(
    val ix: Int, val iy: Int,
    val iw: Int, val ih: Int,
    val type: CourseComponentType
) {
    fun createCourseComponent(asset: AssetManager): CourseComponent {
        return when (type) {
            CourseComponentType.FAIRWAY ->
                Fairway(asset, ix, iy)
        }
    }
}

enum class CourseComponentType {
    FAIRWAY
}