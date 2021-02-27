package io.github.ryuryu_ymj.golf.desktop

import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import io.github.ryuryu_ymj.golf.MyGame

object DesktopLauncher {
    @JvmStatic
    fun main(arg: Array<String>) {
        val config = LwjglApplicationConfiguration().apply {
            width = 960
            height = 540
        }
        LwjglApplication(MyGame(), config)
    }
}