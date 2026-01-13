package com.landclaims.selection;

import com.hypixel.hytale.math.vector.Vector3i;

/**
 * Tracks a player's claim mode state.
 * When in claim mode, clicks set selection corners instead of normal block interaction.
 */
public class ClaimModeState {
    private Vector3i pos1;
    private Vector3i pos2;
    private String worldName;
    private final long startTime;

    public ClaimModeState(String worldName) {
        this.worldName = worldName;
        this.startTime = System.currentTimeMillis();
    }

    public void setPos1(Vector3i pos, String world) {
        this.pos1 = pos;
        // If world changed, reset pos2
        if (this.worldName != null && !this.worldName.equals(world)) {
            this.pos2 = null;
        }
        this.worldName = world;
    }

    public void setPos2(Vector3i pos, String world) {
        this.pos2 = pos;
        // If world changed, reset pos1
        if (this.worldName != null && !this.worldName.equals(world)) {
            this.pos1 = null;
        }
        this.worldName = world;
    }

    public Vector3i getPos1() {
        return pos1;
    }

    public Vector3i getPos2() {
        return pos2;
    }

    public String getWorldName() {
        return worldName;
    }

    public long getStartTime() {
        return startTime;
    }

    /**
     * Returns true if both positions are set.
     */
    public boolean hasCompleteSelection() {
        return pos1 != null && pos2 != null;
    }

    /**
     * Returns true if at least one position is set.
     */
    public boolean hasAnyPosition() {
        return pos1 != null || pos2 != null;
    }

    /**
     * Gets the minimum corner of the selection.
     */
    public Vector3i getMin() {
        if (!hasCompleteSelection()) return null;
        return new Vector3i(
            Math.min(pos1.getX(), pos2.getX()),
            Math.min(pos1.getY(), pos2.getY()),
            Math.min(pos1.getZ(), pos2.getZ())
        );
    }

    /**
     * Gets the maximum corner of the selection.
     */
    public Vector3i getMax() {
        if (!hasCompleteSelection()) return null;
        return new Vector3i(
            Math.max(pos1.getX(), pos2.getX()),
            Math.max(pos1.getY(), pos2.getY()),
            Math.max(pos1.getZ(), pos2.getZ())
        );
    }

    /**
     * Gets the minimum X coordinate.
     */
    public int getMinX() {
        if (!hasCompleteSelection()) return 0;
        return Math.min(pos1.getX(), pos2.getX());
    }

    /**
     * Gets the maximum X coordinate.
     */
    public int getMaxX() {
        if (!hasCompleteSelection()) return 0;
        return Math.max(pos1.getX(), pos2.getX());
    }

    /**
     * Gets the minimum Z coordinate.
     */
    public int getMinZ() {
        if (!hasCompleteSelection()) return 0;
        return Math.min(pos1.getZ(), pos2.getZ());
    }

    /**
     * Gets the maximum Z coordinate.
     */
    public int getMaxZ() {
        if (!hasCompleteSelection()) return 0;
        return Math.max(pos1.getZ(), pos2.getZ());
    }

    /**
     * Gets the number of chunks in this selection.
     */
    public int getChunkCount() {
        if (!hasCompleteSelection()) return 0;

        int minChunkX = (int) Math.floor(getMinX() / 16.0);
        int maxChunkX = (int) Math.floor(getMaxX() / 16.0);
        int minChunkZ = (int) Math.floor(getMinZ() / 16.0);
        int maxChunkZ = (int) Math.floor(getMaxZ() / 16.0);

        return (maxChunkX - minChunkX + 1) * (maxChunkZ - minChunkZ + 1);
    }
}
