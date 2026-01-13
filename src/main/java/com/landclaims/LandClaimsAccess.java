package com.landclaims;

import com.landclaims.data.ClaimStorage;

import java.util.UUID;

/**
 * Static accessor for claim data used by the map system.
 * This is needed because the map image builder runs asynchronously
 * and needs access to claim information.
 */
public class LandClaimsAccess {
    private static ClaimStorage claimStorage;

    /**
     * Initializes the accessor with the claim storage instance.
     * Called during plugin startup.
     */
    public static void init(ClaimStorage storage) {
        claimStorage = storage;
        System.out.println("[LandClaimsAccess] Initialized with claimStorage: " + (storage != null ? "OK" : "NULL"));
    }

    /**
     * Gets the owner of a chunk, or null if unclaimed.
     * Used by ClaimImageBuilder to determine claim colors.
     */
    public static UUID getClaimOwner(String worldName, int chunkX, int chunkZ) {
        if (claimStorage == null) {
            System.out.println("[LandClaimsAccess] ERROR: claimStorage is null!");
            return null;
        }
        UUID owner = claimStorage.getClaimOwner(worldName, chunkX, chunkZ);
        // Debug: log when we find a claim (first few only to avoid spam)
        if (owner != null) {
            System.out.println("[LandClaimsAccess] Found claim at " + worldName + " " + chunkX + "," + chunkZ + " owner=" + owner);
        }
        return owner;
    }

    /**
     * Gets the name of a player by their UUID.
     */
    public static String getPlayerName(UUID playerId) {
        if (claimStorage == null || playerId == null) {
            return "Unknown";
        }
        return claimStorage.getPlayerName(playerId);
    }
}
