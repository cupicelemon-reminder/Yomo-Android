package com.binye.yomo.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.binye.yomo.ui.theme.YomoColors

@Composable
fun GradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(YomoColors.Background)
            .background(
                Brush.radialGradient(
                    colors = listOf(YomoColors.GradientBlue, Color.Transparent),
                    center = Offset(0.85f * 1000f, 0.15f * 1000f),
                    radius = 600f
                )
            )
            .background(
                Brush.radialGradient(
                    colors = listOf(YomoColors.GradientGold, Color.Transparent),
                    center = Offset(0.15f * 1000f, 0.85f * 1000f),
                    radius = 600f
                )
            )
    ) {
        content()
    }
}
