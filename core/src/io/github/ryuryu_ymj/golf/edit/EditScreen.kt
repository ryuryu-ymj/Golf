package io.github.ryuryu_ymj.golf.edit

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.utils.viewport.FitViewport
import io.github.ryuryu_ymj.golf.MyGame
import io.github.ryuryu_ymj.golf.play.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ktx.app.KtxScreen
import ktx.collections.GdxArray
import ktx.collections.gdxArrayOf
import ktx.collections.isNotEmpty
import ktx.collections.lastIndex
import ktx.graphics.use
import ktx.math.minus
import ktx.math.vec2
import ktx.scene2d.actors
import ktx.scene2d.table
import ktx.scene2d.textField
import java.io.PrintWriter
import kotlin.math.max
import kotlin.math.min

class EditScreen(private val game: MyGame) : KtxScreen, MyTouchable {
    private val batch = SpriteBatch()
    private val camera = OrthographicCamera(4f, 2.25f)
    private val viewport = FitViewport(
        camera.viewportWidth,
        camera.viewportHeight, camera
    )
    private val stage = Stage(viewport, batch)
    private val uiViewport = FitViewport(1600f, 900f)
    private val uiStage = Stage(uiViewport, batch)
    private val input = InputMultiplexer().also {
        it.addProcessor(uiStage)
        it.addProcessor(stage)
        it.addProcessor(MyInputProcessor(viewport, this))
    }
    private val shape = ShapeRenderer()

    private val bg = BackGround(stage.width, stage.height)
    private val courseComponents = mutableListOf<CourseComponent>()
    private lateinit var tee: CourseComponent

    private var isSelecting = false
    private val selectBegin = vec2()
    private val selectEnd = vec2()

    private val brush = Brush()
    private val courseIndexField: TextField

    init {
        stage.addActor(bg)

        //uiStage.addActor(brush)
        uiStage.actors {
            table {
                setFillParent(true)
                //debug = true
                top()
                add(brush).expandX().left()
                courseIndexField = textField(text = courseIndex.toString()) {
                    it.expandX().right()
                }
            }
        }
    }

    override fun show() {
        stage.addActor(bg)
        camera.position.setZero()

        try {
            val file = Gdx.files.internal("course/${"%02d".format(courseIndex)}raw")
            val dataList =
                Json.decodeFromString<Array<CourseComponentData>>(file.readString())
            dataList.forEach {
                val component = it.createCourseComponent(game.asset)
                courseComponents.add(component)
                stage.addActor(component)
                if (component.type == CourseComponentType.TEE) {
                    tee = component
                }
            }
        } catch (e: Exception) {
        }
        if (!::tee.isInitialized) {
            courseComponents.findAt(0, 0)?.let {
                it.remove()
                courseComponents.remove(it)
            }
            courseComponents
            tee = CourseComponent(game.asset, CourseComponentType.TEE, 0, 0)
            courseComponents.add(tee)
            stage.addActor(tee)
        }

        Gdx.input.inputProcessor = input
    }

    override fun hide() {
        courseComponents.clear()
        stage.clear()
        Gdx.input.inputProcessor = null
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
        uiViewport.update(width, height)
    }

    override fun render(delta: Float) {
        stage.draw()
        uiStage.draw()
        if (isSelecting) {
            Gdx.gl.glEnable(GL20.GL_BLEND)
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
            shape.use(ShapeRenderer.ShapeType.Filled, camera.combined) {
                it.setColor(1f, 0f, 0f, 0.2f)
                it.rect(
                    selectBegin.x, selectBegin.y,
                    selectEnd.x - selectBegin.x, selectEnd.y - selectBegin.y
                )
            }
            Gdx.gl.glDisable(GL20.GL_BLEND)
        }

        if (brush.type != BrushType.MOVE) {
            val margin = viewport.screenWidth / 8
            val speed = 0.01f
            if (Gdx.input.x < margin) {
                camera.position.x -= speed
            } else if (Gdx.input.x > viewport.screenWidth - margin) {
                camera.position.x += speed
            }
            if (Gdx.input.y < margin) {
                camera.position.y += speed
            } else if (Gdx.input.y > viewport.screenHeight - margin) {
                camera.position.y -= speed
            }
        }
        stage.act()
        uiStage.act()
        if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) &&
            Gdx.input.isKeyJustPressed(Input.Keys.S)
        ) {
            // save
            val dataList = courseComponents.map { it.createCourseComponentData() }
            val file = Gdx.files.local("course/${"%02d".format(courseIndex)}raw")
            file.writeString(Json.encodeToString(dataList), false)
            println("save edit file to course/${"%02d".format(courseIndex)}raw")

            courseComponents.forEach { it.setContact(courseComponents) }
            saveBodyFile()
        } else if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) &&
            Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) &&
            Gdx.input.isKeyJustPressed(Input.Keys.A)
        ) {
            // clear course components
            courseComponents.forEach {
                if (it !== tee) {
                    it.remove()
                }
            }
            courseComponents.clear()
            courseComponents.add(tee)
        } else if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) &&
            Gdx.input.isKeyJustPressed(Input.Keys.P)
        ) {
            // move to playScreen
            game.setScreen<PlayScreen>()
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) &&
            courseIndexField.hasKeyboardFocus()
        ) {
            uiStage.unfocusAll()
            // open a new course
            try {
                courseIndex = courseIndexField.text.toInt()
                game.setScreen<EditScreen>()
            } catch (e: NumberFormatException) {
                println("invalid input : " + courseIndexField.text)
            }
        }
    }

    override fun touchDown(x: Float, y: Float): Boolean {
        if (brush.type == BrushType.MOVE) return false
        selectBegin.set(x, y)
        selectEnd.set(x, y)
        isSelecting = true
        return true
    }

    override fun touchDragged(x: Float, y: Float): Boolean {
        if (isSelecting) {
            selectEnd.set(x, y)
            return true
        }
        return false
    }

    override fun touchUp(x: Float, y: Float): Boolean {
        if (isSelecting) {
            isSelecting = false
            val beginIX = MathUtils.floor(selectBegin.x / COMPONENT_UNIT_SIZE)
            val beginIY = MathUtils.floor(selectBegin.y / COMPONENT_UNIT_SIZE)
            val endIX = MathUtils.floor(selectEnd.x / COMPONENT_UNIT_SIZE)
            val endIY = MathUtils.floor(selectEnd.y / COMPONENT_UNIT_SIZE)
            val rangeX = min(beginIX, endIX)..max(beginIX, endIX)
            val rangeY = min(beginIY, endIY)..max(beginIY, endIY)

            when (brush.type) {
                BrushType.TEE -> {
                    courseComponents.remove(tee)
                    tee.remove()
                    tee = CourseComponent(
                        game.asset, CourseComponentType.TEE,
                        beginIX, beginIY
                    )
                    courseComponents.add(tee)
                    stage.addActor(tee)
                }
                BrushType.DELETE, BrushType.FAIRWAY -> {
                    for (ix in rangeX) {
                        for (iy in rangeY) {
                            when (brush.type) {
                                BrushType.DELETE -> {
                                    removeCourseComponent(ix, iy)
                                }
                                BrushType.FAIRWAY -> {
                                    addCourseComponent(
                                        CourseComponentType.FAIRWAY,
                                        ix, iy
                                    )
                                }
                            }
                        }
                    }
                }
                BrushType.FAIRWAY_SLOPE_UP -> {
                    when ((rangeX.last - rangeX.first + 1) /
                            (rangeY.last - rangeY.first + 1)) {
                        1 -> {
                            for (ix in 0..(rangeX.last - rangeX.first)) {
                                addCourseComponent(
                                    CourseComponentType.FAIRWAY_SLOPE_UP_11,
                                    rangeX.first + ix, rangeY.first + ix
                                )
                                for (iy in 0 until ix) {
                                    addCourseComponent(
                                        CourseComponentType.FAIRWAY,
                                        rangeX.first + ix, rangeY.first + iy
                                    )
                                }
                            }
                        }
                        2 -> {
                            for (ix in 0..(rangeX.last - rangeX.first)) {
                                if (ix % 2 == 0) {
                                    addCourseComponent(
                                        CourseComponentType.FAIRWAY_SLOPE_UP_21,
                                        rangeX.first + ix, rangeY.first + ix / 2
                                    )
                                }
                                for (iy in 0 until ix / 2) {
                                    addCourseComponent(
                                        CourseComponentType.FAIRWAY,
                                        rangeX.first + ix, rangeY.first + iy
                                    )
                                }
                            }
                        }
                    }
                }
                BrushType.FAIRWAY_SLOPE_DOWN -> {
                    when ((rangeX.last - rangeX.first + 1) /
                            (rangeY.last - rangeY.first + 1)) {
                        1 -> {
                            for (ix in 0..(rangeX.last - rangeX.first)) {
                                addCourseComponent(
                                    CourseComponentType.FAIRWAY_SLOPE_DOWN_11,
                                    rangeX.last - ix, rangeY.first + ix
                                )
                                for (iy in 0 until ix) {
                                    addCourseComponent(
                                        CourseComponentType.FAIRWAY,
                                        rangeX.last - ix, rangeY.first + iy
                                    )
                                }
                            }
                        }
                        2 -> {
                            for (ix in 0..(rangeX.last - rangeX.first)) {
                                if (ix % 2 == 0) {
                                    addCourseComponent(
                                        CourseComponentType.FAIRWAY_SLOPE_DOWN_21,
                                        rangeX.last - ix - 1, rangeY.first + ix / 2
                                    )
                                }
                                for (iy in 0 until ix / 2) {
                                    addCourseComponent(
                                        CourseComponentType.FAIRWAY,
                                        rangeX.last - ix, rangeY.first + iy
                                    )
                                }
                            }
                        }
                    }
                }
            }
            return true
        }
        return false
    }

    private fun addCourseComponent(
        type: CourseComponentType, ix: Int, iy: Int
    ): Boolean {
        val old = courseComponents.findAt(ix, iy)
        if (old != null) return false
        val new = CourseComponent(game.asset, type, ix, iy)
        stage.addActor(new)
        courseComponents.add(new)
        return true
    }

    private fun removeCourseComponent(ix: Int, iy: Int): Boolean {
        val old = courseComponents.findAt(ix, iy) ?: return false
        if (old === tee) return false
        old.remove()
        courseComponents.remove(old)
        return true
    }

    override fun dispose() {
        bg.dispose()
        shape.dispose()
        stage.dispose()
        uiStage.dispose()
        batch.dispose()
    }

    private fun saveBodyFile() {
        val file = Gdx.files.local("course/${"%02d".format(courseIndex)}body")
        val writer = PrintWriter(file.writer(false))

        val edges = gdxArrayOf<Edge>()
        courseComponents.forEach { edges.addAll(it.createOutline()) }
        val graphList = gdxArrayOf<GdxArray<Vector2>>()

        while (edges.isNotEmpty()) {
            val graph = gdxArrayOf<Vector2>()
            graph.add(edges[0].begin)
            while (true) {
                val edge = edges.find { it.begin == graph.last() } ?: break
                graph.add(edge.end)
                edges.removeValue(edge, true)
                if (graph.first() == graph.last()) {
                    break
                }
            }
            graphList.add(graph)
        }

        val cuts = listOf(vec2(1f, 0f), vec2(0f, 1f), vec2(-1f, 0f), vec2(0f, -1f))
        var counter = 0
        loop@
        while (true) {
            val graph = graphList[counter]
            for (i in 0 until graph.lastIndex) {
                val prev = graph[i] -
                        if (i == 0) graph[graph.lastIndex - 1]
                        else graph[i - 1]
                val next = graph[i + 1] - graph[i]
                if (prev.crs(next) < 0) { // concave
                    for (cut in cuts) {
                        if (prev.crs(cut) > 0 || next.crs(cut) > 0) {
                            for (j in 0 until graph.lastIndex) {
                                if (i != j &&
                                    (graph[j] - graph[i]).crs(cut) == 0f &&
                                    (graph[j] - graph[i]).dot(cut) > 0
                                ) {
                                    val s = min(i, j)
                                    val e = max(i, j)
                                    val graph2 = gdxArrayOf<Vector2>()
                                    graph2.addAll(graph, s, e - s + 1)
                                    graph2.add(graph[s])
                                    graphList.add(graph2)
                                    graph.removeRange(s + 1, e - 1)
                                    continue@loop
                                }
                            }
                        }
                    }
                }
            }
            counter++
            if (counter == graphList.size) {
                break@loop
            }
        }
        for (graph in graphList) {
            val poly = gdxArrayOf<Vector2>()
            var rightCnt = 0
            for (i in 0 until graph.lastIndex) {
                val prev = graph[i] -
                        if (i == 0) graph[graph.lastIndex - 1]
                        else graph[i - 1]
                val next = graph[i + 1] - graph[i]
                if (prev.crs(next) != 0f) {
                    poly.add(graph[i])
                    if (prev.dot(next) == 0f) {
                        rightCnt++
                    }
                }
            }
            if (rightCnt == 4) {
                val ix = poly.map { it.x }.minOrNull()!!
                val iy = poly.map { it.y }.minOrNull()!!
                val iw = poly.map { it.x }.maxOrNull()!! - ix
                val ih = poly.map { it.y }.maxOrNull()!! - iy
                val x = (ix - tee.ix - 0.5f) * COMPONENT_UNIT_SIZE
                val y = (iy - tee.iy) * COMPONENT_UNIT_SIZE
                val w = iw * COMPONENT_UNIT_SIZE
                val h = ih * COMPONENT_UNIT_SIZE
                writer.println("fairway,box,$x,$y,$w,$h,")
            } else if (poly.isNotEmpty()) {
                writer.print("fairway,polygon,")
                poly.forEach {
                    writer.print((it.x - tee.ix - 0.5f) * COMPONENT_UNIT_SIZE)
                    writer.print(',')
                    writer.print((it.y - tee.iy) * COMPONENT_UNIT_SIZE)
                    writer.print(',')
                }
                writer.println()
            }
        }

        writer.close()
        println("save body file to course/${"%02d".format(courseIndex)}body")
    }
}