package io.github.ryuryu_ymj.golf.edit

import com.badlogic.gdx.assets.AssetManager

class Fairway(asset: AssetManager, ix: Int, iy: Int)
    : CourseComponent(ix, iy, 1, 1, asset.get("image/fairway.png")) {
    override fun createCourseComponentData() =
        CourseComponentData(ix, iy, iw, ih, CourseComponentType.FAIRWAY)
}