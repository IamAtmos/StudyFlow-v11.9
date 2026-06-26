package com.studyflow.app.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

/**
 * Dynamic, theme-switchable design tokens.
 *
 * These are plain top-level properties backed by `mutableStateOf`, not a
 * CompositionLocal — deliberately, so every existing screen that already
 * reads `Primary`, `Background`, etc. by name keeps working completely
 * unchanged. Compose's snapshot system tracks ANY State<T> read during
 * composition, including these, so when [com.studyflow.app.ui.theme.ThemeManager]
 * reassigns them, every screen that reads them recomposes automatically —
 * the same mechanism as `collectAsState()`, just without the extra plumbing.
 *
 * Initial values match the FLOW theme (the new default) so there's no
 * flash-of-wrong-colors before ThemeManager.init() runs.
 */
var Background     by mutableStateOf(Color(0xFF000000))
var Surface         by mutableStateOf(Color(0xFF13161A))
var SurfaceVariant  by mutableStateOf(Color(0xFF1E2226))
var Primary         by mutableStateOf(Color(0xFF2DD4FF))
var OnPrimary       by mutableStateOf(Color(0xFF00222B))
var TextPrimary     by mutableStateOf(Color(0xFFFFFFFF))
var TextSecondary   by mutableStateOf(Color(0xFF8B98A0))

/**
 * Ordered palette for subject color-coding — intentionally INDEPENDENT of
 * the selected app theme. Subjects already keep a `colorIndex` into this
 * list; if this list changed per-theme, every subject's tag color would
 * silently shift whenever the user switches themes, which would be
 * confusing. It stays fixed regardless of theme.
 */
val SubjectColors = listOf(
    Color(0xFF69FF47), // neon green
    Color(0xFF4FC3F7), // sky blue
    Color(0xFFCE93D8), // soft purple
    Color(0xFFFFB74D), // warm orange
    Color(0xFFF48FB1), // soft pink
    Color(0xFF80CBC4), // teal
    Color(0xFFFFD54F), // warm yellow
    Color(0xFFEF9A9A), // soft red
)
