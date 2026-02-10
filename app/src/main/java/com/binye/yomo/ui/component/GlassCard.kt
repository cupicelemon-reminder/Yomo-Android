package com.binye.yomo.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.binye.yomo.ui.theme.CornerRadius
import com.binye.yomo.ui.theme.Elevation
import com.binye.yomo.ui.theme.Spacing
import com.binye.yomo.ui.theme.YomoColors

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = CornerRadius.xl,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .shadow(Elevation.card, shape)
            .clip(shape)
            .background(YomoColors.CardGlass)
            .border(0.5.dp, YomoColors.CardGlassBorder, shape)
            .padding(Spacing.md),
        content = content
    )
}
