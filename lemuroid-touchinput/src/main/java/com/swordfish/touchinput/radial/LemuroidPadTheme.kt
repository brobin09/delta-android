package com.swordfish.touchinput.radial

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.DefaultShadowColor
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class PadSkin(val key: String) {
    GLASS("glass"),
    DELTA_DARK("delta_dark"),
    DELTA_LIGHT("delta_light"),
    ;

    companion object {
        fun parse(key: String?): PadSkin = values().firstOrNull { it.key == key } ?: GLASS
    }
}

class LemuroidPadTheme(skin: PadSkin = PadSkin.GLASS) {
    private fun gray(
        luminosity: Float,
        opacity: Float,
    ): Color {
        return Color(luminosity, luminosity, luminosity, opacity)
    }

    val foregroundPadding: Dp = 8.dp
    val padding: Dp = 4.dp

    // Delta's signature purple, used as the pressed highlight in the Delta skins.
    private val accentPressed = Color(0.65f, 0.42f, 1.0f, 0.85f)

    private val icons: Color
    private val iconsPressed: Color

    private val level3Fill: Color
    private val level3FillPressed: Color
    val level3Shadow = DefaultShadowColor.copy(0.05f)
    val level3ShadowWidth = 4.dp

    private val level2Fill: Color
    private val level2FillPressed: Color
    val level2Shadow = DefaultShadowColor.copy(0.05f)
    val level2ShadowWidth = 4.dp

    val level1Fill: Color
    val level1Shadow = DefaultShadowColor.copy(0.10f)
    val level1ShadowWidth = 4.dp

    val level0CornerRadius = 0.dp
    val level0Fill: Color
    val level0Shadow = DefaultShadowColor.copy(0.10f)
    val level0ShadowWidth = 2.dp

    init {
        when (skin) {
            PadSkin.GLASS -> {
                icons = gray(0.0f, 0.50f)
                iconsPressed = gray(1.0f, 0.50f)
                level3Fill = gray(1.0f, 0.50f)
                level3FillPressed = gray(0.0f, 0.50f)
                level2Fill = gray(1.0f, 0.125f)
                level2FillPressed = gray(0.0f, 0.125f)
                level1Fill = gray(1.0f, 0.10f)
                level0Fill = gray(1.0f, 0.05f)
            }
            PadSkin.DELTA_DARK -> {
                icons = gray(1.0f, 0.90f)
                iconsPressed = gray(1.0f, 1.0f)
                level3Fill = Color(0.17f, 0.17f, 0.18f, 0.80f)
                level3FillPressed = accentPressed
                level2Fill = gray(0.0f, 0.35f)
                level2FillPressed = gray(0.0f, 0.55f)
                level1Fill = gray(0.0f, 0.25f)
                level0Fill = gray(0.0f, 0.15f)
            }
            PadSkin.DELTA_LIGHT -> {
                icons = gray(0.10f, 0.85f)
                iconsPressed = gray(1.0f, 1.0f)
                level3Fill = gray(1.0f, 0.85f)
                level3FillPressed = accentPressed
                level2Fill = gray(1.0f, 0.35f)
                level2FillPressed = gray(1.0f, 0.55f)
                level1Fill = gray(1.0f, 0.25f)
                level0Fill = gray(1.0f, 0.12f)
            }
        }
    }

    fun compositeFill(pressed: Boolean): Color {
        return if (pressed) {
            level2FillPressed
        } else {
            level2Fill
        }
    }

    fun foregroundFill(pressed: Boolean): Color {
        return if (pressed) {
            level3FillPressed
        } else {
            level3Fill
        }
    }

    fun icons(pressed: Boolean): Color {
        return if (pressed) {
            iconsPressed
        } else {
            icons
        }
    }
}

val LocalLemuroidPadTheme =
    compositionLocalOf<LemuroidPadTheme> {
        error("LemuroidPadTheme is missing")
    }
