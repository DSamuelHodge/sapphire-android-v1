package com.example.domain.model

import androidx.compose.ui.graphics.Color

enum class IslandThemePreset(
    val title: String,
    val description: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val glowColor: Color,
    val surfaceColor: Color,
    val surfaceBorder: Color
) {
    SAPPHIRE_NEON(
        title = "Sapphire Neon",
        description = "Deep sleek obsidian with glowing electric blue & indigo accents",
        primaryColor = Color(0xFF3B82F6),
        secondaryColor = Color(0xFF6366F1),
        glowColor = Color(0x333B82F6),
        surfaceColor = Color(0xFF141414),
        surfaceBorder = Color(0x1AFFFFFF)
    ),
    MIDNIGHT_ONYX(
        title = "Midnight Onyx",
        description = "Pure stealth obsidian black with silver chrome highlights",
        primaryColor = Color(0xFFF8FAFC),
        secondaryColor = Color(0xFFA1A1AA),
        glowColor = Color(0x26FFFFFF),
        surfaceColor = Color(0xFF141414),
        surfaceBorder = Color(0x1AFFFFFF)
    ),
    EMERALD_AURORA(
        title = "Emerald Aurora",
        description = "Vibrant sleek jade with glowing cyber mint",
        primaryColor = Color(0xFF10B981),
        secondaryColor = Color(0xFF059669),
        glowColor = Color(0x3310B981),
        surfaceColor = Color(0xFF141414),
        surfaceBorder = Color(0x1AFFFFFF)
    ),
    RUBY_EMBER(
        title = "Ruby Ember",
        description = "Intense crimson flame with warm amber neon energy",
        primaryColor = Color(0xFFEF4444),
        secondaryColor = Color(0xFFF97316),
        glowColor = Color(0x33EF4444),
        surfaceColor = Color(0xFF141414),
        surfaceBorder = Color(0x1AFFFFFF)
    ),
    CYBER_VIOLET(
        title = "Cyber Violet",
        description = "Synthwave violet with ultraviolet laser glow",
        primaryColor = Color(0xFFA855F7),
        secondaryColor = Color(0xFFEC4899),
        glowColor = Color(0x33A855F7),
        surfaceColor = Color(0xFF141414),
        surfaceBorder = Color(0x1AFFFFFF)
    );

    companion object {
        fun fromString(name: String?): IslandThemePreset {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: SAPPHIRE_NEON
        }
    }
}
