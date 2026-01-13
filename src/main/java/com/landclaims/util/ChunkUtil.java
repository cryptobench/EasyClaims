package com.landclaims.util;

/**
 * Utility methods for chunk coordinate calculations.
 */
public class ChunkUtil {

    // Hytale uses 32-block chunks (not 16 like Minecraft)
    public static final int CHUNK_SIZE = 32;

    /**
     * Converts a world X coordinate to a chunk X coordinate.
     */
    public static int toChunkX(double worldX) {
        return (int) Math.floor(worldX / CHUNK_SIZE);
    }

    /**
     * Converts a world Z coordinate to a chunk Z coordinate.
     */
    public static int toChunkZ(double worldZ) {
        return (int) Math.floor(worldZ / CHUNK_SIZE);
    }

    /**
     * Gets the minimum world X coordinate for a chunk.
     */
    public static int getChunkMinX(int chunkX) {
        return chunkX * CHUNK_SIZE;
    }

    /**
     * Gets the maximum world X coordinate for a chunk.
     */
    public static int getChunkMaxX(int chunkX) {
        return (chunkX + 1) * CHUNK_SIZE - 1;
    }

    /**
     * Gets the minimum world Z coordinate for a chunk.
     */
    public static int getChunkMinZ(int chunkZ) {
        return chunkZ * CHUNK_SIZE;
    }

    /**
     * Gets the maximum world Z coordinate for a chunk.
     */
    public static int getChunkMaxZ(int chunkZ) {
        return (chunkZ + 1) * CHUNK_SIZE - 1;
    }

    /**
     * Creates a chunk key string for use in maps/indexes.
     */
    public static String chunkKey(int chunkX, int chunkZ) {
        return chunkX + "," + chunkZ;
    }

    /**
     * Parses a chunk key string back to coordinates.
     * Returns int[2] with {chunkX, chunkZ} or null if invalid.
     */
    public static int[] parseChunkKey(String key) {
        if (key == null || !key.contains(",")) {
            return null;
        }
        String[] parts = key.split(",");
        if (parts.length != 2) {
            return null;
        }
        try {
            return new int[] { Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) };
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
