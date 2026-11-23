package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.RetroRepository
import com.example.engine.ControlLayoutConfig
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `app name matches Retro PC Emulator`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Retro PC Emulator", appName)
    }

    @Test
    fun `control layout config json roundtrip preserves positions and scale`() {
        val original = ControlLayoutConfig(
            joystickX = 0.25f,
            joystickY = 0.62f,
            joystickScale = 1.25f,
            joystickAlpha = 0.8f,
        )
        val restored = ControlLayoutConfig.fromJson(original.toJson())
        assertEquals(0.25f, restored.joystickX, 0.001f)
        assertEquals(0.62f, restored.joystickY, 0.001f)
        assertEquals(1.25f, restored.joystickScale, 0.001f)
        assertEquals(4, restored.buttons.size)
    }

    @Test
    fun `starter library seeds City Racing and classic titles`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = RetroRepository(context)
        repo.ensureStarterLibrarySeeded()
        val games = repo.gamesFlow.first()
        assertTrue(games.any { it.title == "City Racing" })
        assertTrue(games.size >= 6)
    }
}
