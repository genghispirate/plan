package app.haven.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// ===========================================================================
//  RAW PALETTE
//  A warm, artisan set: parchment & ink for surfaces, walnut & brass for the
//  physical frame, muted overcast tones for the guilt-free Rest State.
// ===========================================================================

// Neutrals — light
val Parchment = Color(0xFFF4EFE6)
val ParchmentRaised = Color(0xFFFBF8F2)
val Ink = Color(0xFF1E2530)
val InkMuted = Color(0xFF5A6472)

// Neutrals — dark
val Obsidian = Color(0xFF0E1116)
val ObsidianRaised = Color(0xFF141A22)
val Bone = Color(0xFFEDE6D8)
val BoneMuted = Color(0xFF9AA4B2)

// Materials — shared across themes
val WalnutDeep = Color(0xFF3A2817)
val Walnut = Color(0xFF4A3524)
val WalnutLight = Color(0xFF6B4E34)
val Brass = Color(0xFFCBA36A)
val BrassBright = Color(0xFFE4C784)

// Diorama environment accents
val SunlitGold = Color(0xFFE9C46A)
val MeadowGreen = Color(0xFF5E8B6A)
val StreamBlue = Color(0xFF3E6E8E)
val OvercastGrey = Color(0xFF6E7681)
val FireflyYellow = Color(0xFFF2E28C)

// ===========================================================================
//  MATERIAL 3 SCHEMES
// ===========================================================================

val HavenLightColors = lightColorScheme(
    primary = Walnut,
    onPrimary = Parchment,
    secondary = Brass,
    onSecondary = Ink,
    tertiary = MeadowGreen,
    background = Parchment,
    onBackground = Ink,
    surface = ParchmentRaised,
    onSurface = Ink,
    surfaceVariant = Color(0xFFE7DFD1),
    onSurfaceVariant = InkMuted,
    outline = Color(0xFFB9AE9B),
)

val HavenDarkColors = darkColorScheme(
    primary = Brass,
    onPrimary = Obsidian,
    secondary = BrassBright,
    onSecondary = Obsidian,
    tertiary = MeadowGreen,
    background = Obsidian,
    onBackground = Bone,
    surface = ObsidianRaised,
    onSurface = Bone,
    surfaceVariant = Color(0xFF2A3340),
    onSurfaceVariant = BoneMuted,
    outline = Color(0xFF3A4552),
)

// ===========================================================================
//  EXTENDED SEMANTIC COLORS
//  Everything the diorama frame, frosted panels and environmental states need
//  that Material3's slots don't express. Exposed via CompositionLocal (Theme).
// ===========================================================================

data class HavenExtendedColors(
    val frameOuter: Color,
    val frameInner: Color,
    val frameKeyline: Color,
    val frostedGlass: Color,
    val frostedBorder: Color,
    val coinMetal: Color,
    val coinMetalHighlight: Color,
    val growthGauge: Color,
    val restOvercast: Color,
    val skyTop: Color,
    val skyBottom: Color,
    val firefly: Color,
    val water: Color,
)

val LightExtendedColors = HavenExtendedColors(
    frameOuter = Walnut,
    frameInner = WalnutDeep,
    frameKeyline = Brass,
    frostedGlass = Color(0xCCFBF8F2),
    frostedBorder = Color(0x33FFFFFF),
    coinMetal = Brass,
    coinMetalHighlight = BrassBright,
    growthGauge = MeadowGreen,
    restOvercast = OvercastGrey,
    skyTop = Color(0xFFBFDDF2),
    skyBottom = Color(0xFFF6E7C7),
    firefly = FireflyYellow,
    water = StreamBlue,
)

val DarkExtendedColors = HavenExtendedColors(
    frameOuter = WalnutDeep,
    frameInner = Color(0xFF241811),
    frameKeyline = Brass,
    frostedGlass = Color(0xCC141A22),
    frostedBorder = Color(0x1FFFFFFF),
    coinMetal = Brass,
    coinMetalHighlight = BrassBright,
    growthGauge = MeadowGreen,
    restOvercast = OvercastGrey,
    skyTop = Color(0xFF15243A),
    skyBottom = Color(0xFF2A2A33),
    firefly = FireflyYellow,
    water = Color(0xFF2C4A57),
)
