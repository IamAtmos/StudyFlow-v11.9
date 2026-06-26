package com.studyflow.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studyflow.app.ui.theme.AppPalette
import com.studyflow.app.ui.theme.AppTheme
import com.studyflow.app.ui.theme.OnPrimary
import com.studyflow.app.ui.theme.Primary
import com.studyflow.app.ui.theme.Surface
import com.studyflow.app.ui.theme.SurfaceVariant
import com.studyflow.app.ui.theme.TextPrimary
import com.studyflow.app.ui.theme.TextSecondary
import com.studyflow.app.ui.theme.ThemeManager

@Composable
fun ThemePickerSheet(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val active  = ThemeManager.currentTheme

    Box(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.55f)).clickable { onDismiss() })

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Surface)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Theme", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Icon(Icons.Default.Close, null, tint = TextSecondary, modifier = Modifier.clickable { onDismiss() })
            }

            AppTheme.values().forEach { theme ->
                ThemeCard(
                    theme    = theme,
                    palette  = ThemeManager.palette(theme),
                    selected = theme == active,
                    onSelect = { ThemeManager.selectTheme(context, theme); onDismiss() },
                )
            }

            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun ThemeCard(
    theme: AppTheme,
    palette: AppPalette,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceVariant)
            .border(1.dp, if (selected) Primary.copy(alpha = 0.6f) else Color.Transparent, RoundedCornerShape(14.dp))
            .clickable { onSelect() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            // Mini swatch preview: background + primary + surface dots
            Row(horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
                SwatchDot(palette.background, borderColor = palette.primary.copy(alpha = 0.5f))
                SwatchDot(palette.primary)
                SwatchDot(palette.surfaceVariant)
            }
            Column {
                Text(theme.displayName, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                Text(theme.tagline, color = TextSecondary, fontSize = 12.sp)
            }
        }

        if (selected) {
            Box(
                modifier = Modifier.size(22.dp).clip(CircleShape).background(Primary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Check, null, tint = OnPrimary, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun SwatchDot(color: Color, borderColor: Color = Color.White.copy(alpha = 0.12f)) {
    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(CircleShape)
            .background(color)
            .border(1.dp, borderColor, CircleShape),
    )
}
