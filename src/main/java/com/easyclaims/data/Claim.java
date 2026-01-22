package com.easyclaims.data;

/**
 * Represents a single claimed chunk.
 */
public class Claim {
    private final String world;
    private final int chunkX;
    private final int chunkZ;
    private final long claimedAt;
    private boolean pvpEnabled;
    private final boolean adminClaim;
    private final String displayName;

    public Claim(String world, int chunkX, int chunkZ) {
        this(world, chunkX, chunkZ, System.currentTimeMillis(), true, false, null);
    }

    public Claim(String world, int chunkX, int chunkZ, long claimedAt) {
        this(world, chunkX, chunkZ, claimedAt, true, false, null);
    }

    /**
     * Full constructor with all fields.
     *
     * @param world The world name
     * @param chunkX Chunk X coordinate
     * @param chunkZ Chunk Z coordinate
     * @param claimedAt Timestamp when claimed
     * @param pvpEnabled Whether PvP is enabled in this claim
     * @param adminClaim Whether this is an admin claim
     * @param displayName Custom display name (for admin claims, e.g., "Spawn")
     */
    public Claim(String world, int chunkX, int chunkZ, long claimedAt,
                 boolean pvpEnabled, boolean adminClaim, String displayName) {
        this.world = world;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.claimedAt = claimedAt;
        this.pvpEnabled = pvpEnabled;
        this.adminClaim = adminClaim;
        this.displayName = displayName;
    }

    /**
     * Creates an admin claim with PvP disabled by default.
     */
    public static Claim createAdminClaim(String world, int chunkX, int chunkZ, String displayName) {
        return new Claim(world, chunkX, chunkZ, System.currentTimeMillis(), false, true, displayName);
    }

    public String getWorld() {
        return world;
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }

    public long getClaimedAt() {
        return claimedAt;
    }

    public boolean isPvpEnabled() {
        return pvpEnabled;
    }

    public void setPvpEnabled(boolean pvpEnabled) {
        this.pvpEnabled = pvpEnabled;
    }

    public boolean isAdminClaim() {
        return adminClaim;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns a unique key for this claim (world:chunkX,chunkZ)
     */
    public String getKey() {
        return world + ":" + chunkX + "," + chunkZ;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Claim claim = (Claim) obj;
        return chunkX == claim.chunkX && chunkZ == claim.chunkZ && world.equals(claim.world);
    }

    @Override
    public int hashCode() {
        int result = world.hashCode();
        result = 31 * result + chunkX;
        result = 31 * result + chunkZ;
        return result;
    }
}
