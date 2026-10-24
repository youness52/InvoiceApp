package com.example.ui.i18n

import androidx.compose.ui.unit.LayoutDirection

enum class AppLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String,
    val layoutDirection: LayoutDirection
) {
    FR("fr", "Français", "Français", LayoutDirection.Ltr),
    AR("ar", "Arabic", "العربية", LayoutDirection.Rtl),
    EN("en", "English", "English", LayoutDirection.Ltr);

    companion object {
        fun fromCode(code: String): AppLanguage {
            return values().firstOrNull { it.code.equals(code, ignoreCase = true) } ?: FR
        }
    }
}
