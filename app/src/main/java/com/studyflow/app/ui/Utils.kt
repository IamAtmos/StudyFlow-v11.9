package com.studyflow.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studyflow.app.ui.theme.Primary
import com.studyflow.app.ui.theme.TextSecondary

/** MM:SS for timer display */
fun formatTime(millis: Long): String {
    val s = millis / 1_000
    return "%02d:%02d".format(s / 60, s % 60)
}

/**
 * Smart compact duration:
 *   < 60s      → "45s"
 *   < 1h       → "5m 30s"
 *   >= 1h      → "2h 15m 30s"
 */
fun formatDuration(millis: Long): String {
    val totalSec = millis / 1_000
    val hours    = totalSec / 3600
    val minutes  = (totalSec % 3600) / 60
    val seconds  = totalSec % 60
    return when {
        hours > 0   -> "${hours}h ${minutes}m ${seconds}s"
        minutes > 0 -> "${minutes}m ${seconds}s"
        else        -> "${seconds}s"
    }
}

/** Short version for charts: "2h 15m" or "45m" — no seconds */
fun formatDurationShort(millis: Long): String {
    val totalSec = millis / 1_000
    val hours    = totalSec / 3600
    val minutes  = (totalSec % 3600) / 60
    return when {
        hours > 0   -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m"
        else        -> "${totalSec}s"
    }
}

// ─── Shared design primitives ──────────────────────────────────────────────
// Small, reusable pieces that give the app its "Flow" look consistently
// across screens: uppercase eyebrow labels, a glowing status dot, and a
// soft text-glow for hero numbers. Screens opt into these without any
// structural changes to their own layout.

/** Small uppercase, letter-spaced, muted section label ("TOTAL TODAY"). */
@Composable
fun EyebrowLabel(text: String, color: Color = TextSecondary) {
    Text(
        text.uppercase(),
        color = color,
        style = MaterialTheme.typography.labelMedium,
    )
}

/** A soft glow shadow tuned for big hero numbers, using the current accent color. */
fun glowShadow(color: Color = Primary, alpha: Float = 0.45f, blurRadius: Float = 28f): Shadow =
    Shadow(color = color.copy(alpha = alpha), blurRadius = blurRadius)

/** TextStyle preset for hero numbers: light weight, tight spacing, soft accent glow. */
fun heroNumberStyle(fontSize: TextUnit, color: Color = Primary): TextStyle =
    TextStyle(
        color         = color,
        fontSize      = fontSize,
        fontWeight    = FontWeight.Light,
        letterSpacing = (-0.5).sp,
        shadow        = glowShadow(color),
    )

/** Small glowing dot used as a status-indicator prefix (running/live/active). */
@Composable
fun GlowDot(color: Color = Primary, dotSize: Dp = 6.dp, haloSize: Dp = 16.dp) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(haloSize)) {
        Box(
            modifier = Modifier
                .size(haloSize)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(color.copy(alpha = 0.35f), Color.Transparent))),
        )
        Box(
            modifier = Modifier
                .size(dotSize)
                .clip(CircleShape)
                .background(color),
        )
    }
}

/** The one prominent action on a screen — outlined + soft tinted glow,
 *  matching the app's accent-forward CTA language, instead of a flat fill.
 *  Shared by Study and Timer screens so the "main action" always looks
 *  the same everywhere. */
@Composable
fun PrimaryGlowButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.verticalGradient(listOf(Primary.copy(alpha = 0.16f), Primary.copy(alpha = 0.04f))))
            .border(1.dp, Primary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = Primary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
    }
}
