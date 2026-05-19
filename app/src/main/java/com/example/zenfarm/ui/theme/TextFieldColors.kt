package com.example.zenfarm.ui.theme

import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Standard TextField colors untuk aplikasi ZenFarm
 * Mengatasi masalah text tidak terlihat dengan set explicit text colors
 */
@Composable
fun standardTextFieldColors(
    focusedBorderColor: Color = FarmGreen,
    unfocusedBorderColor: Color = DividerGray
): TextFieldColors {
    return OutlinedTextFieldDefaults.colors(
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary,
        disabledTextColor = TextSecondary,
        focusedBorderColor = focusedBorderColor,
        unfocusedBorderColor = unfocusedBorderColor,
        focusedLabelColor = focusedBorderColor,
        unfocusedLabelColor = TextSecondary,
        cursorColor = focusedBorderColor
    )
}
