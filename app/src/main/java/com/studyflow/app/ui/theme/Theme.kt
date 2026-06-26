package com.studyflow.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

@Composable
fun StudyFlowTheme(content: @Composable () -> Unit) {
    // Built INSIDE the composable body (not as a top-level val) so it reads
    // the current Background/Primary/etc. on every recomposition — this is
    // what lets a theme switch repaint the whole app instantly.
    val colorScheme = darkColorScheme(
        background     = Background,
        surface        = Surface,
        surfaceVariant = SurfaceVariant,
        primary        = Primary,
        onPrimary      = OnPrimary,
        onBackground   = TextPrimary,
        onSurface      = TextPrimary,
        secondary      = Primary,
        onSecondary    = OnPrimary,
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content,
    )
}
