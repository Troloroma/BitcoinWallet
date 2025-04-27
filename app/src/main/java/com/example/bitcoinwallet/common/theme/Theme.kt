package com.example.bitcoinwallet.common.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val LightColors = lightColorScheme(
    primary = Color(0xFF1E88E5),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBBDEFB),
    secondary = Color(0xFF90CAF9),
    background = Color(0xFFF5F5F5),
    surface = Color.White,
    onSurface = Color(0xFF212121),
    error = Color(0xFFD32F2F)
)

@Composable
fun BitcoinWalletTheme(content: @Composable () -> Unit) {
    val typography = Typography(
        displayLarge = TextStyle(fontSize = 36.sp, fontWeight = FontWeight.Bold),
        headlineSmall = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Medium),
        labelLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
        bodyMedium = TextStyle(fontSize = 14.sp)
    )
    val shapes = Shapes(
        small = RoundedCornerShape(12.dp),
        medium = RoundedCornerShape(16.dp),
        large = RoundedCornerShape(24.dp)
    )

    MaterialTheme(
        colorScheme = LightColors,
        typography = typography,
        shapes = shapes,
        content = content
    )
}