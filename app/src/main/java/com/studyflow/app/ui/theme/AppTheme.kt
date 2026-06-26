package com.studyflow.app.ui.theme

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOfpackage com.studyflow.app.ui.theme

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

enum class AppTheme(val displayName: String, val tagline: String) {
    FLOW("Flow", "Calm focus, cyan glow"),
    PULSE("Pulse", "Energetic green"),
    VAULT("Vault", "Deep violet focus"),
    SLATE("Slate", "Monochrome minimal"),
    CRIMSON("Crimson", "Urgency mode"),
}

data class AppPalette(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val primary: Color,
    val onPrimary: Color,
    val textPrimary: Color,
    val textSecondary: Color,
)

private val PALETTES: Map<AppTheme, AppPalette> = mapOf(
    // Cyan. New default — calm, focused, slightly cool-toned black.
    AppTheme.FLOW to AppPalette(
        background     = Color(0xFF000000),
        surface        = Color(0xFF13161A),
        surfaceVariant = Color(0xFF1E2226),
        primary        = Color(0xFF2DD4FF),
        onPrimary      = Color(0xFF00222B),
        textPrimary    = Color(0xFFFFFFFF),
        textSecondary  = Color(0xFF8B98A0),
    ),
    // Green. The original StudyFlow look, preserved exactly as it was.
    AppTheme.PULSE to AppPalette(
        background     = Color(0xFF121212),
        surface        = Color(0xFF1E1E1E),
        surfaceVariant = Color(0xFF2A2A2A),
        primary        = Color(0xFF69FF47),
        onPrimary      = Color(0xFF0D2200),
        textPrimary    = Color(0xFFFFFFFF),
        textSecondary  = Color(0xFF9E9E9E),
    ),
    // Deep violet. Tinted near-black background for a richer, "boutique" feel.
    AppTheme.VAULT to AppPalette(
        background     = Color(0xFF0D0B14),
        surface        = Color(0xFF18141F),
        surfaceVariant = Color(0xFF241F2E),
        primary        = Color(0xFFA177FF),
        onPrimary      = Color(0xFF1A0F33),
        textPrimary    = Color(0xFFFFFFFF),
        textSecondary  = Color(0xFF9A8FB0),
    ),
    // Monochrome. Accent is near-white instead of a saturated color.
    AppTheme.SLATE to AppPalette(
        background     = Color(0xFF000000),
        surface        = Color(0xFF1A1A1A),
        surfaceVariant = Color(0xFF272727),
        primary        = Color(0xFFE9E9E9),
        onPrimary      = Color(0xFF161616),
        textPrimary    = Color(0xFFFFFFFF),
        textSecondary  = Color(0xFF9A9A9A),
    ),
    // Red. Tinted near-black background, urgency framing.
    AppTheme.CRIMSON to AppPalette(
        background     = Color(0xFF140707),
        surface        = Color(0xFF201010),
        surfaceVariant = Color(0xFF2C1414),
        primary        = Color(0xFFFF5252),
        onPrimary      = Color(0xFF330505),
        textPrimary    = Color(0xFFFFFFFF),
        textSecondary  = Color(0xFFAD8C8C),
    ),
)

private const val PREFS_NAME = "studyflow_theme"
private const val KEY_THEME  = "selected_theme"

/**
 * Owns the active [AppTheme] and pushes its colors into the dynamic tokens
 * declared in Color.kt. Switching themes is just reassigning those `var`s —
 * every screen that already reads `Primary`/`Background`/etc. picks up the
 * change automatically on next recomposition, with no per-screen code needed.
 */
object ThemeManager {

    var currentTheme: AppTheme by mutableStateOf(AppTheme.FLOW)
        private set

    /** Call once, before setContent, so the saved theme applies with no flash. */
    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_THEME, AppTheme.FLOW.name)
        val theme = runCatching { AppTheme.valueOf(saved ?: AppTheme.FLOW.name) }.getOrDefault(AppTheme.FLOW)
        applyInternal(theme)
    }

    fun selectTheme(context: Context, theme: AppTheme) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_THEME, theme.name).apply()
        applyInternal(theme)
    }

    fun palette(theme: AppTheme): AppPalette = PALETTES.getValue(theme)

    private fun applyInternal(theme: AppTheme) {
        val p = PALETTES.getValue(theme)
        currentTheme   = theme
        Background     = p.background
        Surface         = p.surface
        SurfaceVariant  = p.surfaceVariant
        Primary         = p.primary
        OnPrimary       = p.onPrimary
        TextPrimary     = p.textPrimary
        TextSecondary   = p.textSecondary
    }
}
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

enum class AppTheme(val displayName: String, val tagline: String) {
    FLOW("Flow", "Calm focus, cyan glow"),
    PULSE("Pulse", "Energetic green"),
    VAULT("Vault", "Deep violet focus"),
    SLATE("Slate", "Monochrome minimal"),
    CRIMSON("Crimson", "Urgency mode"),
}

data class AppPalette(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val primary: Color,
    val onPrimary: Color,
    val textPrimary: Color,
    val textSecondary: Color,
)

private val PALETTES: Map<AppTheme, AppPalette> = mapOf(
    // Cyan. New default — calm, focused, slightly cool-toned black.
    AppTheme.FLOW to AppPalette(
        background     = Color(0xFF000000),
        surface        = Color(0xFF13161A),
        surfaceVariant = Color(0xFF1E2226),
        primary        = Color(0xFF2DD4FF),
        onPrimary      = Color(0xFF00222B),
        textPrimary    = Color(0xFFFFFFFF),
        textSecondary  = Color(0xFF8B98A0),
    ),
    // Green. The original StudyFlow look, preserved exactly as it was.
    AppTheme.PULSE to AppPalette(
        background     = Color(0xFF121212),
        surface        = Color(0xFF1E1E1E),
        surfaceVariant = Color(0xFF2A2A2A),
        primary        = Color(0xFF69FF47),
        onPrimary      = Color(0xFF0D2200),
        textPrimary    = Color(0xFFFFFFFF),
        textSecondary  = Color(0xFF9E9E9E),
    ),
    // Deep violet. Tinted near-black background for a richer, "boutique" feel.
    AppTheme.VAULT to AppPalette(
        background     = Color(0xFF0D0B14),
        surface        = Color(0xFF18141F),
        surfaceVariant = Color(0xFF241F2E),
        primary        = Color(0xFFA177FF),
        onPrimary      = Color(0xFF1A0F33),
        textPrimary    = Color(0xFFFFFFFF),
        textSecondary  = Color(0xFF9A8FB0),
    ),
    // Monochrome. Accent is near-white instead of a saturated color.
    AppTheme.SLATE to AppPalette(
        background     = Color(0xFF000000),
        surface        = Color(0xFF1A1A1A),
        surfaceVariant = Color(0xFF272727),
        primary        = Color(0xFFE9E9E9),
        onPrimary      = Color(0xFF161616),
        textPrimary    = Color(0xFFFFFFFF),
        textSecondary  = Color(0xFF9A9A9A),
    ),
    // Red. Tinted near-black background, urgency framing.
    AppTheme.CRIMSON to AppPalette(
        background     = Color(0xFF140707),
        surface        = Color(0xFF201010),
        surfaceVariant = Color(0xFF2C1414),
        primary        = Color(0xFFFF5252),
        onPrimary      = Color(0xFF330505),
        textPrimary    = Color(0xFFFFFFFF),
        textSecondary  = Color(0xFFAD8C8C),
    ),
)

private const val PREFS_NAME = "studyflow_theme"
private const val KEY_THEME  = "selected_theme"

/**
 * Owns the active [AppTheme] and pushes its colors into the dynamic tokens
 * declared in Color.kt. Switching themes is just reassigning those `var`s —
 * every screen that already reads `Primary`/`Background`/etc. picks up the
 * change automatically on next recomposition, with no per-screen code needed.
 */
object ThemeManager {

    var currentTheme: AppTheme by mutableStateOf(AppTheme.FLOW)
        private set

    /** Call once, before setContent, so the saved theme applies with no flash. */
    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_THEME, AppTheme.FLOW.name)
        val theme = runCatching { AppTheme.valueOf(saved ?: AppTheme.FLOW.name) }.getOrDefault(AppTheme.FLOW)
        applyInternal(theme)
    }

    fun selectTheme(context: Context, theme: AppTheme) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_THEME, theme.name).apply()
        applyInternal(theme)
    }

    fun palette(theme: AppTheme): AppPalette = PALETTES.getValue(theme)

    private fun applyInternal(theme: AppTheme) {
        val p = PALETTES.getValue(theme)
        currentTheme   = theme
        Background     = p.background
        Surface         = p.surface
        SurfaceVariant  = p.surfaceVariant
        Primary         = p.primary
        OnPrimary       = p.onPrimary
        TextPrimary     = p.textPrimary
        TextSecondary   = p.textSecondary
    }
}
EOF
echo "AppTheme.kt created ✅"
