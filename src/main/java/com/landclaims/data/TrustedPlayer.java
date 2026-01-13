package com.landclaims.data;

import java.util.UUID;

/**
 * Holds information about a trusted player including their name and trust level.
 */
public class TrustedPlayer {
    private final UUID uuid;
    private String name;
    private TrustLevel level;

    public TrustedPlayer(UUID uuid, String name, TrustLevel level) {
        this.uuid = uuid;
        this.name = name;
        this.level = level;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TrustLevel getLevel() {
        return level;
    }

    public void setLevel(TrustLevel level) {
        this.level = level;
    }

    /**
     * Check if this player has at least the given trust level.
     */
    public boolean hasPermission(TrustLevel required) {
        return level.hasPermission(required);
    }
}
