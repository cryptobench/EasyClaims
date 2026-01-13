package com.landclaims.map;

import java.awt.Color;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Generates unique, distinguishable colors for players based on their username.
 * Uses golden ratio distribution across the hue spectrum to ensure adjacent claims
 * have visually distinct colors.
 */
public class ClaimColorGenerator {

    // Golden ratio conjugate for optimal hue distribution
    private static final double GOLDEN_RATIO_CONJUGATE = 0.618033988749895;

    // Cache colors to ensure consistency
    private static final Map<String, Color> colorCache = new ConcurrentHashMap<>();

    // Predefined distinct colors for the first few players (most visible)
    private static final Color[] PRESET_COLORS = {
        new Color(66, 135, 245),   // Blue
        new Color(245, 66, 66),    // Red
        new Color(66, 245, 96),    // Green
        new Color(245, 206, 66),   // Yellow
        new Color(245, 66, 218),   // Magenta
        new Color(66, 245, 230),   // Cyan
        new Color(245, 140, 66),   // Orange
        new Color(165, 66, 245),   // Purple
    };

    /**
     * Gets a unique color for a player based on their username.
     * The same username will always return the same color.
     */
    public static Color getPlayerColor(String username) {
        if (username == null || username.isEmpty()) {
            return Color.GRAY;
        }

        return colorCache.computeIfAbsent(username.toLowerCase(), ClaimColorGenerator::generateColor);
    }

    /**
     * Gets a unique color for a player based on their UUID.
     */
    public static Color getPlayerColor(UUID playerId) {
        if (playerId == null) {
            return Color.GRAY;
        }
        return colorCache.computeIfAbsent(playerId.toString(), ClaimColorGenerator::generateColor);
    }

    /**
     * Gets an ARGB integer color value with the specified alpha.
     */
    public static int getPlayerColorARGB(String username, int alpha) {
        Color color = getPlayerColor(username);
        return (alpha << 24) | (color.getRed() << 16) | (color.getGreen() << 8) | color.getBlue();
    }

    /**
     * Gets an ARGB integer color value with the specified alpha.
     */
    public static int getPlayerColorARGB(UUID playerId, int alpha) {
        Color color = getPlayerColor(playerId);
        return (alpha << 24) | (color.getRed() << 16) | (color.getGreen() << 8) | color.getBlue();
    }

    /**
     * Gets a darker border color for the player (for claim boundaries).
     */
    public static Color getBorderColor(String username) {
        Color base = getPlayerColor(username);
        return base.darker().darker();
    }

    /**
     * Gets a darker border color ARGB value.
     */
    public static int getBorderColorARGB(String username, int alpha) {
        Color color = getBorderColor(username);
        return (alpha << 24) | (color.getRed() << 16) | (color.getGreen() << 8) | color.getBlue();
    }

    private static Color generateColor(String key) {
        // Use hash code to get a deterministic but well-distributed value
        int hash = betterHash(key);

        // Use the hash to select from preset colors first, then generate
        int presetIndex = Math.abs(hash) % (PRESET_COLORS.length * 3);
        if (presetIndex < PRESET_COLORS.length) {
            return PRESET_COLORS[presetIndex];
        }

        // Generate color using golden ratio for good distribution
        // This ensures colors are spread evenly across the spectrum
        double hue = ((hash & 0xFFFFFFL) * GOLDEN_RATIO_CONJUGATE) % 1.0;

        // Use moderate saturation and brightness for visibility
        float saturation = 0.65f + (((hash >> 8) & 0xFF) / 255f) * 0.25f;  // 0.65-0.90
        float brightness = 0.75f + (((hash >> 16) & 0xFF) / 255f) * 0.20f; // 0.75-0.95

        return Color.getHSBColor((float) hue, saturation, brightness);
    }

    /**
     * Better hash function for more uniform distribution.
     */
    private static int betterHash(String s) {
        int h = 0;
        for (int i = 0; i < s.length(); i++) {
            h = 31 * h + s.charAt(i);
            h ^= (h >>> 16);
        }
        // Mix the bits more thoroughly
        h ^= (h >>> 16);
        h *= 0x85ebca6b;
        h ^= (h >>> 13);
        h *= 0xc2b2ae35;
        h ^= (h >>> 16);
        return h;
    }

    /**
     * Clears the color cache (useful if usernames change).
     */
    public static void clearCache() {
        colorCache.clear();
    }
}
