package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = MindPrimaryDark,
    onPrimary = MindOnPrimaryDark,
    primaryContainer = MindPrimaryContainerDark,
    onPrimaryContainer = MindOnPrimaryContainerDark,
    secondary = MindSecondaryDark,
    onSecondary = MindOnSecondaryDark,
    secondaryContainer = MindSecondaryContainerDark,
    onSecondaryContainer = MindOnSecondaryContainerDark,
    tertiary = MindTertiaryDark,
    onTertiary = MindOnTertiaryDark,
    tertiaryContainer = MindTertiaryContainerDark,
    onTertiaryContainer = MindOnTertiaryContainerDark,
    background = MindBackgroundDark,
    onBackground = MindOnBackgroundDark,
    surface = MindSurfaceDark,
    onSurface = MindOnSurfaceDark,
    surfaceVariant = MindSurfaceVariantDark,
    onSurfaceVariant = MindOnSurfaceVariantDark,
    error = MindUrgentRed,
    errorContainer = MindUrgentContainer,
    onErrorContainer = MindOnUrgentContainer
  )

private val LightColorScheme =
  lightColorScheme(
    primary = MindPrimaryLight,
    onPrimary = MindOnPrimaryLight,
    primaryContainer = MindPrimaryContainerLight,
    onPrimaryContainer = MindOnPrimaryContainerLight,
    secondary = MindSecondaryLight,
    onSecondary = MindOnSecondaryLight,
    secondaryContainer = MindSecondaryContainerLight,
    onSecondaryContainer = MindOnSecondaryContainerLight,
    tertiary = MindTertiaryLight,
    onTertiary = MindOnTertiaryLight,
    tertiaryContainer = MindTertiaryContainerLight,
    onTertiaryContainer = MindOnTertiaryContainerLight,
    background = MindBackgroundLight,
    onBackground = MindOnBackgroundLight,
    surface = MindSurfaceLight,
    onSurface = MindOnSurfaceLight,
    surfaceVariant = MindSurfaceVariantLight,
    onSurfaceVariant = MindOnSurfaceVariantLight,
    error = MindUrgentRed,
    errorContainer = MindUrgentContainer,
    onErrorContainer = MindOnUrgentContainer
  )

@Composable
fun MindMatrixTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Keep intentional brand colors for consistent trauma-informed aesthetic
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) = MindMatrixTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)

