package com.landclaims.map;

import com.hypixel.hytale.protocol.packets.worldmap.MapImage;

/**
 * Composites claim overlays onto map images.
 * Draws semi-transparent fills with solid borders to show claim boundaries.
 */
public class MapImageCompositor {

    // Default chunk map image size (pixels per chunk)
    // This may need adjustment based on actual Hytale map resolution
    public static final int DEFAULT_CHUNK_SIZE = 16;

    // Overlay transparency (0-255, where 255 = fully opaque)
    // Using bright red and high opacity for testing visibility
    public static final int FILL_ALPHA = 180;     // More opaque fill for visibility
    public static final int BORDER_ALPHA = 255;   // Fully opaque border
    public static final int BORDER_WIDTH = 3;     // Thicker border for visibility

    // Debug: Force red color for testing
    public static final boolean DEBUG_RED_OVERLAY = true;

    /**
     * Creates a new claim overlay image for a chunk.
     * This creates a semi-transparent colored overlay with a border.
     *
     * @param ownerName The claim owner's username (for color generation)
     * @param width     Image width in pixels
     * @param height    Image height in pixels
     * @return A new MapImage with the claim overlay
     */
    public static MapImage createClaimOverlay(String ownerName, int width, int height) {
        int[] pixels = new int[width * height];

        int fillColor;
        int borderColor;

        if (DEBUG_RED_OVERLAY) {
            // Bright red for testing - ARGB format
            fillColor = (FILL_ALPHA << 24) | (255 << 16) | (0 << 8) | 0;      // Red fill
            borderColor = (BORDER_ALPHA << 24) | (139 << 16) | (0 << 8) | 0;  // Dark red border
        } else {
            fillColor = ClaimColorGenerator.getPlayerColorARGB(ownerName, FILL_ALPHA);
            borderColor = ClaimColorGenerator.getBorderColorARGB(ownerName, BORDER_ALPHA);
        }

        // Fill the entire area with semi-transparent color
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = fillColor;
        }

        // Draw border on all edges
        drawBorder(pixels, width, height, borderColor, BORDER_WIDTH);

        return new MapImage(width, height, pixels);
    }

    /**
     * Creates a claim overlay with the default chunk size.
     */
    public static MapImage createClaimOverlay(String ownerName) {
        return createClaimOverlay(ownerName, DEFAULT_CHUNK_SIZE, DEFAULT_CHUNK_SIZE);
    }

    /**
     * Composites a claim overlay on top of an existing map image.
     * The overlay is blended with the base image using alpha blending.
     *
     * @param base      The base map image to overlay on
     * @param ownerName The claim owner's username
     * @return A new MapImage with the overlay composited
     */
    public static MapImage compositeOverlay(MapImage base, String ownerName) {
        if (base == null) {
            return createClaimOverlay(ownerName);
        }

        int width = base.width;
        int height = base.height;
        int[] result = new int[width * height];

        int fillColor = ClaimColorGenerator.getPlayerColorARGB(ownerName, FILL_ALPHA);
        int borderColor = ClaimColorGenerator.getBorderColorARGB(ownerName, BORDER_ALPHA);

        // Copy base image and blend overlay
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int idx = y * width + x;
                int basePixel = base.data[idx];

                // Check if this pixel is on the border
                boolean isBorder = x < BORDER_WIDTH || x >= width - BORDER_WIDTH ||
                                   y < BORDER_WIDTH || y >= height - BORDER_WIDTH;

                int overlayColor = isBorder ? borderColor : fillColor;
                result[idx] = blendPixels(basePixel, overlayColor);
            }
        }

        return new MapImage(width, height, result);
    }

    /**
     * Creates a border-only overlay (no fill) for claimed chunks.
     * Useful for less obtrusive claim visualization.
     */
    public static MapImage createBorderOnlyOverlay(String ownerName, int width, int height) {
        int[] pixels = new int[width * height];

        int borderColor = ClaimColorGenerator.getBorderColorARGB(ownerName, BORDER_ALPHA);

        // Initialize with transparent
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = 0; // Fully transparent
        }

        // Draw border only
        drawBorder(pixels, width, height, borderColor, BORDER_WIDTH);

        return new MapImage(width, height, pixels);
    }

    /**
     * Draws a rectangular border on the pixel array.
     */
    private static void drawBorder(int[] pixels, int width, int height, int color, int borderWidth) {
        // Top border
        for (int y = 0; y < borderWidth; y++) {
            for (int x = 0; x < width; x++) {
                pixels[y * width + x] = color;
            }
        }

        // Bottom border
        for (int y = height - borderWidth; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixels[y * width + x] = color;
            }
        }

        // Left border
        for (int y = borderWidth; y < height - borderWidth; y++) {
            for (int x = 0; x < borderWidth; x++) {
                pixels[y * width + x] = color;
            }
        }

        // Right border
        for (int y = borderWidth; y < height - borderWidth; y++) {
            for (int x = width - borderWidth; x < width; x++) {
                pixels[y * width + x] = color;
            }
        }
    }

    /**
     * Blends two ARGB pixels using standard alpha blending.
     * The overlay is blended on top of the base.
     */
    private static int blendPixels(int base, int overlay) {
        int baseA = (base >> 24) & 0xFF;
        int baseR = (base >> 16) & 0xFF;
        int baseG = (base >> 8) & 0xFF;
        int baseB = base & 0xFF;

        int overlayA = (overlay >> 24) & 0xFF;
        int overlayR = (overlay >> 16) & 0xFF;
        int overlayG = (overlay >> 8) & 0xFF;
        int overlayB = overlay & 0xFF;

        // Alpha blending formula
        float alpha = overlayA / 255f;
        float invAlpha = 1f - alpha;

        int resultR = (int) (overlayR * alpha + baseR * invAlpha);
        int resultG = (int) (overlayG * alpha + baseG * invAlpha);
        int resultB = (int) (overlayB * alpha + baseB * invAlpha);
        int resultA = Math.max(baseA, overlayA);

        return (resultA << 24) | (resultR << 16) | (resultG << 8) | resultB;
    }

    /**
     * Creates a multi-chunk overlay image for visualizing larger claim areas.
     * Each chunk in the grid gets the appropriate overlay based on ownership.
     *
     * @param chunkOwners 2D array of owner names (null = unclaimed)
     * @param chunkSize   Pixels per chunk
     * @return Combined MapImage for the entire grid
     */
    public static MapImage createMultiChunkOverlay(String[][] chunkOwners, int chunkSize) {
        int chunksX = chunkOwners.length;
        int chunksZ = chunkOwners[0].length;
        int width = chunksX * chunkSize;
        int height = chunksZ * chunkSize;
        int[] pixels = new int[width * height];

        for (int cx = 0; cx < chunksX; cx++) {
            for (int cz = 0; cz < chunksZ; cz++) {
                String owner = chunkOwners[cx][cz];
                if (owner == null) continue;

                int fillColor = ClaimColorGenerator.getPlayerColorARGB(owner, FILL_ALPHA);
                int borderColor = ClaimColorGenerator.getBorderColorARGB(owner, BORDER_ALPHA);

                int startX = cx * chunkSize;
                int startZ = cz * chunkSize;

                // Fill this chunk's area
                for (int y = 0; y < chunkSize; y++) {
                    for (int x = 0; x < chunkSize; x++) {
                        int px = startX + x;
                        int py = startZ + y;
                        int idx = py * width + px;

                        // Check for border
                        boolean isBorder = x < BORDER_WIDTH || x >= chunkSize - BORDER_WIDTH ||
                                           y < BORDER_WIDTH || y >= chunkSize - BORDER_WIDTH;

                        // Also check if adjacent chunk has different owner (draw border between claims)
                        if (!isBorder && x < BORDER_WIDTH && cx > 0) {
                            String leftOwner = chunkOwners[cx - 1][cz];
                            if (leftOwner == null || !leftOwner.equals(owner)) {
                                isBorder = true;
                            }
                        }
                        if (!isBorder && x >= chunkSize - BORDER_WIDTH && cx < chunksX - 1) {
                            String rightOwner = chunkOwners[cx + 1][cz];
                            if (rightOwner == null || !rightOwner.equals(owner)) {
                                isBorder = true;
                            }
                        }
                        if (!isBorder && y < BORDER_WIDTH && cz > 0) {
                            String topOwner = chunkOwners[cx][cz - 1];
                            if (topOwner == null || !topOwner.equals(owner)) {
                                isBorder = true;
                            }
                        }
                        if (!isBorder && y >= chunkSize - BORDER_WIDTH && cz < chunksZ - 1) {
                            String bottomOwner = chunkOwners[cx][cz + 1];
                            if (bottomOwner == null || !bottomOwner.equals(owner)) {
                                isBorder = true;
                            }
                        }

                        pixels[idx] = isBorder ? borderColor : fillColor;
                    }
                }
            }
        }

        return new MapImage(width, height, pixels);
    }
}
