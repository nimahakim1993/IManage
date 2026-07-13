package com.nima.app.imanage.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
@ReadOnlyComposable
fun scaledSp(value: Float): TextUnit = (value * LocalFontScale.current).sp

@Composable
@ReadOnlyComposable
fun scaledSp(value: Int): TextUnit = (value * LocalFontScale.current).sp
