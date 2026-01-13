package com.landclaims.data;

/**
 * Trust levels for granular permissions in claimed areas.
 * Higher levels include all permissions from lower levels.
 */
public enum TrustLevel {
    /**
     * No access - cannot interact with anything in the claim.
     */
    NONE(0, "none", "No access"),

    /**
     * Can use basic interactive blocks (doors, buttons, levers, pressure plates).
     */
    USE(1, "use", "Doors & buttons"),

    /**
     * Can open containers (chests, barrels, etc.) + all USE permissions.
     */
    CONTAINER(2, "container", "Chests & containers"),

    /**
     * Can use workstations (crafting tables, furnaces, anvils) + all CONTAINER permissions.
     */
    WORKSTATION(3, "workstation", "Crafting & workstations"),

    /**
     * Can damage/attack blocks but not fully break them + all WORKSTATION permissions.
     */
    DAMAGE(4, "damage", "Damage blocks"),

    /**
     * Full access - can break and place blocks + all other permissions.
     */
    BUILD(5, "build", "Full build access");

    private final int level;
    private final String key;
    private final String description;

    TrustLevel(int level, String key, String description) {
        this.level = level;
        this.key = key;
        this.description = description;
    }

    public int getLevel() {
        return level;
    }

    public String getKey() {
        return key;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Check if this trust level grants at least the given permission level.
     */
    public boolean hasPermission(TrustLevel required) {
        return this.level >= required.level;
    }

    /**
     * Parse a trust level from string (case-insensitive).
     * @return the trust level, or null if not found
     */
    public static TrustLevel fromString(String str) {
        if (str == null) return null;
        String lower = str.toLowerCase().trim();
        for (TrustLevel level : values()) {
            if (level.key.equals(lower) || level.name().equalsIgnoreCase(lower)) {
                return level;
            }
        }
        return null;
    }

    /**
     * Get a formatted list of all trust levels for display.
     */
    public static String getAvailableLevels() {
        StringBuilder sb = new StringBuilder();
        for (TrustLevel level : values()) {
            if (level != NONE) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(level.key);
            }
        }
        return sb.toString();
    }
}
