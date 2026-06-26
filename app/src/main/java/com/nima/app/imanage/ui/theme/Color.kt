package com.nima.app.imanage.ui.theme

import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

val IncomeLight = Color(0xFF2E7D32)
val IncomeDark = Color(0xFF66BB6A)

val DebtLight = Color(0xFFC62828)
val DebtDark = Color(0xFFEF5350)

data class NoteBoxPalette(
    val primary: Color,
    val secondary: Color,
    val accent: Color
)

val NoteBoxPurple = NoteBoxPalette(
    primary = Color(0xFF7C3AED),
    secondary = Color(0xFFA78BFA),
    accent = Color(0xFFEDE9FE)
)

val NoteBoxBlue = NoteBoxPalette(
    primary = Color(0xFF1D4ED8),
    secondary = Color(0xFF60A5FA),
    accent = Color(0xFFDBEAFE)
)

val NoteBoxTeal = NoteBoxPalette(
    primary = Color(0xFF0F766E),
    secondary = Color(0xFF2DD4BF),
    accent = Color(0xFFCCFBF1)
)

val NoteBoxPink = NoteBoxPalette(
    primary = Color(0xFFBE185D),
    secondary = Color(0xFFF472B6),
    accent = Color(0xFFFCE7F3)
)

val NoteBoxOrange = NoteBoxPalette(
    primary = Color(0xFFC2410C),
    secondary = Color(0xFFFB923C),
    accent = Color(0xFFFFEDD5)
)

val NoteBoxGreen = NoteBoxPalette(
    primary = Color(0xFF15803D),
    secondary = Color(0xFF4ADE80),
    accent = Color(0xFFDCFCE7)
)

val NoteBoxPalettes = listOf(
    NoteBoxPurple,
    NoteBoxBlue,
    NoteBoxTeal,
    NoteBoxPink,
    NoteBoxOrange,
    NoteBoxGreen
)