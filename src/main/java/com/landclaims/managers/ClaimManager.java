package com.landclaims.managers;

import com.landclaims.config.BlockGroups;
import com.landclaims.config.PluginConfig;
import com.landclaims.data.Claim;
import com.landclaims.data.ClaimStorage;
import com.landclaims.data.PlayerClaims;
import com.landclaims.data.PlaytimeData;
import com.landclaims.data.PlaytimeStorage;
import com.landclaims.data.TrustLevel;
import com.landclaims.util.ChunkUtil;

import java.util.UUID;

/**
 * Core claim logic and protection checks.
 */
public class ClaimManager {
    private final ClaimStorage claimStorage;
    private final PlaytimeStorage playtimeStorage;
    private final PluginConfig config;
    private final BlockGroups blockGroups;

    public ClaimManager(ClaimStorage claimStorage, PlaytimeStorage playtimeStorage, PluginConfig config, BlockGroups blockGroups) {
        this.claimStorage = claimStorage;
        this.playtimeStorage = playtimeStorage;
        this.config = config;
        this.blockGroups = blockGroups;
    }

    /**
     * Gets the block groups configuration.
     */
    public BlockGroups getBlockGroups() {
        return blockGroups;
    }

    /**
     * Gets the claim storage for direct access (used by map overlay system).
     */
    public ClaimStorage getClaimStorage() {
        return claimStorage;
    }

    /**
     * Attempts to claim a chunk for a player.
     * @return ClaimResult indicating success or failure reason
     */
    public ClaimResult claimChunk(UUID playerId, String world, double x, double z) {
        int chunkX = ChunkUtil.toChunkX(x);
        int chunkZ = ChunkUtil.toChunkZ(z);

        // Check if already claimed
        UUID existingOwner = claimStorage.getClaimOwner(world, chunkX, chunkZ);
        if (existingOwner != null) {
            if (existingOwner.equals(playerId)) {
                return ClaimResult.ALREADY_OWN;
            }
            return ClaimResult.CLAIMED_BY_OTHER;
        }

        // Check claim limit
        PlayerClaims claims = claimStorage.getPlayerClaims(playerId);
        PlaytimeData playtime = playtimeStorage.getPlaytime(playerId);
        int maxClaims = config.calculateMaxClaims(playtime.getTotalHoursWithCurrentSession());
        int currentClaims = claims.getClaimCount();

        if (currentClaims >= maxClaims) {
            return ClaimResult.LIMIT_REACHED;
        }

        // Create the claim
        Claim claim = new Claim(world, chunkX, chunkZ);
        claimStorage.addClaim(playerId, claim);

        return ClaimResult.SUCCESS;
    }

    /**
     * Attempts to unclaim a chunk.
     * @return true if successful, false if not owned by player
     */
    public boolean unclaimChunk(UUID playerId, String world, double x, double z) {
        int chunkX = ChunkUtil.toChunkX(x);
        int chunkZ = ChunkUtil.toChunkZ(z);

        UUID owner = claimStorage.getClaimOwner(world, chunkX, chunkZ);
        if (owner == null || !owner.equals(playerId)) {
            return false;
        }

        claimStorage.removeClaim(playerId, world, chunkX, chunkZ);
        return true;
    }

    /**
     * Unclaims all chunks owned by a player.
     * @return the number of chunks unclaimed
     */
    public int unclaimAll(UUID playerId) {
        PlayerClaims claims = claimStorage.getPlayerClaims(playerId);
        int count = claims.getClaimCount();
        claimStorage.removeAllClaims(playerId);
        return count;
    }

    /**
     * Legacy method - checks if player can interact (has any trust level).
     * Returns true if: unclaimed, owner, or has any trust.
     */
    public boolean canInteract(UUID playerId, String world, double x, double z) {
        return hasPermissionAt(playerId, world, x, z, TrustLevel.USE);
    }

    /**
     * Checks if a player has at least the specified trust level at a location.
     * Returns true if: unclaimed, owner, or has sufficient trust level.
     */
    public boolean hasPermissionAt(UUID playerId, String world, double x, double z, TrustLevel required) {
        int chunkX = ChunkUtil.toChunkX(x);
        int chunkZ = ChunkUtil.toChunkZ(z);

        UUID owner = claimStorage.getClaimOwner(world, chunkX, chunkZ);
        if (owner == null) {
            return true; // Unclaimed
        }
        if (owner.equals(playerId)) {
            return true; // Owner
        }

        // Check if trusted with sufficient level
        PlayerClaims ownerClaims = claimStorage.getPlayerClaims(owner);
        return ownerClaims.hasPermission(playerId, required);
    }

    /**
     * Gets the trust level a player has at a location.
     * @return BUILD if owner, the trust level if trusted, or NONE
     */
    public TrustLevel getTrustLevelAt(UUID playerId, String world, double x, double z) {
        int chunkX = ChunkUtil.toChunkX(x);
        int chunkZ = ChunkUtil.toChunkZ(z);

        UUID owner = claimStorage.getClaimOwner(world, chunkX, chunkZ);
        if (owner == null) {
            return TrustLevel.BUILD; // Unclaimed = full access
        }
        if (owner.equals(playerId)) {
            return TrustLevel.BUILD; // Owner = full access
        }

        PlayerClaims ownerClaims = claimStorage.getPlayerClaims(owner);
        return ownerClaims.getTrustLevel(playerId);
    }

    /**
     * Gets the owner of a claim at a location.
     */
    public UUID getOwnerAt(String world, double x, double z) {
        int chunkX = ChunkUtil.toChunkX(x);
        int chunkZ = ChunkUtil.toChunkZ(z);
        return claimStorage.getClaimOwner(world, chunkX, chunkZ);
    }

    /**
     * Gets the chunk coordinates for a world position.
     */
    public int[] getChunkCoords(double x, double z) {
        return new int[] { ChunkUtil.toChunkX(x), ChunkUtil.toChunkZ(z) };
    }

    /**
     * Gets the player's current claims data.
     */
    public PlayerClaims getPlayerClaims(UUID playerId) {
        return claimStorage.getPlayerClaims(playerId);
    }

    /**
     * Adds a trusted player with a specific trust level.
     */
    public void addTrust(UUID ownerId, UUID trustedId, String trustedName, TrustLevel level) {
        PlayerClaims claims = claimStorage.getPlayerClaims(ownerId);
        claims.addTrustedPlayer(trustedId, trustedName, level);
        claimStorage.savePlayerClaims(ownerId);
    }

    /**
     * Legacy method - adds trusted player with BUILD level.
     */
    public void addTrust(UUID ownerId, UUID trustedId, String trustedName) {
        addTrust(ownerId, trustedId, trustedName, TrustLevel.BUILD);
    }

    /**
     * Removes a trusted player.
     * @return the removed player's name, or null if not found
     */
    public String removeTrust(UUID ownerId, UUID trustedId) {
        PlayerClaims claims = claimStorage.getPlayerClaims(ownerId);
        String removedName = claims.removeTrustedPlayer(trustedId);
        if (removedName != null) {
            claimStorage.savePlayerClaims(ownerId);
        }
        return removedName;
    }

    /**
     * Checks if a player is trusted by another (has any trust level).
     */
    public boolean isTrusted(UUID ownerId, UUID playerId) {
        PlayerClaims claims = claimStorage.getPlayerClaims(ownerId);
        return claims.isTrusted(playerId);
    }

    /**
     * Gets the trust level of a player in another's claims.
     */
    public TrustLevel getTrustLevel(UUID ownerId, UUID playerId) {
        PlayerClaims claims = claimStorage.getPlayerClaims(ownerId);
        return claims.getTrustLevel(playerId);
    }

    /**
     * Gets how many claims a player can have.
     */
    public int getMaxClaims(UUID playerId) {
        PlaytimeData playtime = playtimeStorage.getPlaytime(playerId);
        return config.calculateMaxClaims(playtime.getTotalHoursWithCurrentSession());
    }

    /**
     * Gets hours until the player can claim another chunk.
     */
    public double getHoursUntilNextClaim(UUID playerId) {
        PlaytimeData playtime = playtimeStorage.getPlaytime(playerId);
        PlayerClaims claims = claimStorage.getPlayerClaims(playerId);
        return config.hoursUntilNextClaim(playtime.getTotalHoursWithCurrentSession(), claims.getClaimCount());
    }

    public enum ClaimResult {
        SUCCESS,
        ALREADY_OWN,
        CLAIMED_BY_OTHER,
        LIMIT_REACHED
    }
}
