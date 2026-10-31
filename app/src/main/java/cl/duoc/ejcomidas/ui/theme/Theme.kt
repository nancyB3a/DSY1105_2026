package cl.duoc.ejcomidas.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Colores tomados de theme/variables.scss del proyecto Ionic
val IonPrimary = Color(0xFF3880FF)
val IonDark = Color(0xFF222428)
val IonDarkShade = Color(0xFF1E2023)
val IonDarkTint = Color(0xFF383A3E)
val IonMedium = Color(0xFF92949C)

private val EsquemaOscuro = darkColorScheme(
    primary = IonPrimary,
    onPrimary = Color.White,
    secondary = Color(0xFF3DC2FF),
    background = IonDark,
    onBackground = Color.White,
    surface = IonDark,
    onSurface = Color.White,
    surfaceVariant = IonDarkTint,
    onSurfaceVariant = Color(0xFFD7D8DA),
    surfaceContainer = IonDarkShade,
    secondaryContainer = IonPrimary.copy(alpha = 0.25f),
    outline = IonMedium
)

/** La app original siempre usaba el color "dark", por eso el tema es fijo oscuro. */
@Composable
fun EjComidasTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EsquemaOscuro,
        content = content
    )
}
