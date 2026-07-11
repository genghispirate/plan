package app.haven.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Two type roles from Section 4:
 *   Display / headers -> Playfair Display (a high-contrast serif)
 *   Body / UI         -> Inter (a neutral humanist sans)
 *
 * To stay fully offline and guarantee a clean build, the shipping families
 * resolve to the platform [FontFamily.Serif] and [FontFamily.SansSerif], which
 * render as the intended serif/sans pairing on-device. Dropping the actual
 * Playfair Display and Inter `.ttf` files into `res/font/` and swapping the two
 * `FontFamily` values below is the only change needed to brand it exactly —
 * every TextStyle already points at these two aliases.
 */
private val Playfair: FontFamily = FontFamily.Serif
private val Inter: FontFamily = FontFamily.SansSerif

val HavenTypography = Typography(
    // --- Serif display: reserved for headers ("Playfair Display") ---
    displayLarge = TextStyle(
        fontFamily = Playfair, fontWeight = FontWeight.SemiBold,
        fontSize = 40.sp, lineHeight = 46.sp, letterSpacing = (-0.5).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = Playfair, fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp, lineHeight = 38.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = Playfair, fontWeight = FontWeight.Medium,
        fontSize = 28.sp, lineHeight = 34.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = Playfair, fontWeight = FontWeight.Medium,
        fontSize = 24.sp, lineHeight = 30.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = Playfair, fontWeight = FontWeight.Medium,
        fontSize = 20.sp, lineHeight = 26.sp,
    ),

    // --- Sans body: everything interactive ("Inter") ---
    titleMedium = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp, lineHeight = 22.sp, letterSpacing = 0.1.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.2.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp, lineHeight = 18.sp, letterSpacing = 0.4.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = Inter, fontWeight = FontWeight.Medium,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp,
    ),
)
