package io.github.ryuryu_ymj.golf.play

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.GdxRuntimeException
import ktx.actors.plusAssign
import ktx.box2d.body
import ktx.box2d.box
import ktx.box2d.polygon
import ktx.math.vec2

class CourseManager {
    private var texture: Texture? = null

    fun readCourse(index: Int, stage: Stage, world: World) {
        createBody(index, world)
        //createTexture(index)
    }

    private fun createBody(index: Int, world: World) {
        val file: FileHandle
        try {
            file = Gdx.files.internal("course/${"%02d".format(index)}body")
        } catch (e: GdxRuntimeException) {
            Gdx.app.error("my-error", "コースファイルの読み込みに失敗しました", e)
            return
        }
        for (line in file.readString().lines()) {
            val cells = line.split(',')
            if (cells.isEmpty()) continue
            when (cells[0]) {
                "fairway" -> {
                    when (cells[1]) {
                        "box" -> {
                            val x = cells[2].toFloat()
                            val y = cells[3].toFloat()
                            val w = cells[4].toFloat()
                            val h = cells[5].toFloat()
                            world.body {
                                box(width = w, height = h, position = vec2(w / 2, h / 2)) {
                                    restitution = 0f
                                    friction = 0.5f
                                }
                                position.set(x, y)
                                type = BodyDef.BodyType.StaticBody
                                userData = GroundType.FAIRWAY
                            }
                        }
                        "polygon" -> {
                            val vertices = FloatArray(cells.size - 3) { cells[it + 2].toFloat() }
                            world.body {
                                polygon(vertices) {
                                    restitution = 0f
                                    friction = 0.5f
                                }
                                type = BodyDef.BodyType.StaticBody
                                userData = GroundType.FAIRWAY
                            }
                        }
                    }
                }
            }
        }
    }

    private fun createTexture(index: Int, stage: Stage) {
        val file = Gdx.files.internal("course/${"%02d".format(index)}pixmap")
        val lines = file.readString().lines()

        val firstCells = lines[0].split(',')
        val coursePixmapWidth = firstCells[0].toInt()
        val coursePixmapHeight = firstCells[1].toInt()
        val courseTextureX = firstCells[2].toFloat()
        val courseTextureY = firstCells[3].toFloat()
        val courseTextureWidth = firstCells[4].toFloat()
        val courseTextureHeight = firstCells[5].toFloat()

        val coursePixmap = Pixmap(coursePixmapWidth, coursePixmapHeight, Pixmap.Format.RGBA8888)
        val pixmap = Pixmap(Gdx.files.internal("image/ground.png"))

        for (line in lines.subList(1, lines.size - 1)) {
            val cells = line.split(',')
            val x = cells[0].toInt()
            val y = cells[1].toInt()
            val srcX = cells[2].toInt()
            val srcY = cells[3].toInt()
            val srcW = cells[4].toInt()
            val srcH = cells[5].toInt()
            coursePixmap.drawPixmap(pixmap, x, y, srcX, srcY, srcW, srcH)
        }

        texture?.dispose()
        texture = Texture(coursePixmap)
        stage += MyImage(texture!!).apply {
            setPosition(courseTextureX, courseTextureY)
            setSize(courseTextureWidth, courseTextureHeight)
        }
        coursePixmap.dispose()
        pixmap.dispose()
    }

    fun dispose() {
        texture?.dispose()
    }
}