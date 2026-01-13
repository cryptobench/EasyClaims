package com.landclaims.selection;

import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolSelectionUpdate;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages claim mode for players.
 * When in claim mode, players can click to set selection corners,
 * and a bounding box is rendered by sending BuilderToolSelectionUpdate packets.
 */
public class ClaimSelectionManager {

    // Y bounds for chunk visualization (full world height)
    private static final int MIN_Y = -64;
    private static final int MAX_Y = 320;

    // Track which players are in claim mode
    private final Map<UUID, ClaimModeState> claimModeStates = new ConcurrentHashMap<>();

    /**
     * Checks if a player is in claim mode.
     */
    public boolean isInClaimMode(UUID playerId) {
        return claimModeStates.containsKey(playerId);
    }

    /**
     * Enters claim mode for a player.
     */
    public void enterClaimMode(UUID playerId, String worldName) {
        claimModeStates.put(playerId, new ClaimModeState(worldName));
    }

    /**
     * Exits claim mode for a player and clears their selection visualization.
     */
    public void exitClaimMode(UUID playerId, Player player, PlayerRef playerRef) {
        claimModeStates.remove(playerId);
        clearVisualization(player, playerRef);
    }

    /**
     * Gets the claim mode state for a player (null if not in claim mode).
     */
    public ClaimModeState getClaimModeState(UUID playerId) {
        return claimModeStates.get(playerId);
    }

    /**
     * Sets position 1 and updates the visualization.
     */
    public void setPos1(UUID playerId, Player player, PlayerRef playerRef, Vector3i pos, String worldName) {
        ClaimModeState state = claimModeStates.get(playerId);
        if (state == null) return;

        state.setPos1(pos, worldName);
        updateVisualization(player, playerRef, state);
    }

    /**
     * Sets position 2 and updates the visualization.
     */
    public void setPos2(UUID playerId, Player player, PlayerRef playerRef, Vector3i pos, String worldName) {
        ClaimModeState state = claimModeStates.get(playerId);
        if (state == null) return;

        state.setPos2(pos, worldName);
        updateVisualization(player, playerRef, state);
    }

    /**
     * Updates the visual selection rendering by sending a packet to the client.
     */
    private void updateVisualization(Player player, PlayerRef playerRef, ClaimModeState state) {
        if (playerRef == null) return;

        try {
            if (state.hasCompleteSelection()) {
                // Show full selection box (extend to full Y height for chunk claiming)
                sendSelectionPacket(playerRef,
                    state.getMinX(), MIN_Y, state.getMinZ(),
                    state.getMaxX(), MAX_Y, state.getMaxZ());
            } else if (state.getPos1() != null) {
                // Show just pos1 as a single-block column
                Vector3i pos = state.getPos1();
                sendSelectionPacket(playerRef,
                    pos.getX(), MIN_Y, pos.getZ(),
                    pos.getX(), MAX_Y, pos.getZ());
            } else if (state.getPos2() != null) {
                // Show just pos2 as a single-block column
                Vector3i pos = state.getPos2();
                sendSelectionPacket(playerRef,
                    pos.getX(), MIN_Y, pos.getZ(),
                    pos.getX(), MAX_Y, pos.getZ());
            }
        } catch (Exception e) {
            // Log error but don't crash
            e.printStackTrace();
        }
    }

    /**
     * Sends a BuilderToolSelectionUpdate packet to show the selection box.
     */
    private void sendSelectionPacket(PlayerRef playerRef, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        try {
            System.out.println("[ClaimSelection] Sending selection packet: " +
                minX + "," + minY + "," + minZ + " to " + maxX + "," + maxY + "," + maxZ);
            BuilderToolSelectionUpdate packet = new BuilderToolSelectionUpdate(
                minX, minY, minZ,
                maxX, maxY, maxZ
            );
            playerRef.getPacketHandler().write(packet);
            System.out.println("[ClaimSelection] Packet sent successfully!");
        } catch (Exception e) {
            System.out.println("[ClaimSelection] ERROR sending packet: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Clears the selection visualization for a player.
     */
    public void clearVisualization(Player player, PlayerRef playerRef) {
        if (playerRef == null) return;

        try {
            // Send a "zero" selection to clear
            // Using Integer.MAX_VALUE/MIN_VALUE to indicate no selection
            BuilderToolSelectionUpdate packet = new BuilderToolSelectionUpdate(0, 0, 0, 0, 0, 0);
            playerRef.getPacketHandler().write(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets all chunk coordinates within a player's current selection.
     * Returns list of int[2] arrays: {chunkX, chunkZ}
     */
    public List<int[]> getChunksInSelection(UUID playerId) {
        List<int[]> chunks = new ArrayList<>();
        ClaimModeState state = claimModeStates.get(playerId);
        if (state == null || !state.hasCompleteSelection()) {
            return chunks;
        }

        int minChunkX = (int) Math.floor(state.getMinX() / 16.0);
        int maxChunkX = (int) Math.floor(state.getMaxX() / 16.0);
        int minChunkZ = (int) Math.floor(state.getMinZ() / 16.0);
        int maxChunkZ = (int) Math.floor(state.getMaxZ() / 16.0);

        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                chunks.add(new int[]{cx, cz});
            }
        }

        return chunks;
    }

    /**
     * Shows a chunk boundary visualization (for single chunk claiming).
     */
    public void showChunkBounds(Player player, PlayerRef playerRef, int chunkX, int chunkZ) {
        if (playerRef == null) return;

        int minX = chunkX * 16;
        int maxX = chunkX * 16 + 15;
        int minZ = chunkZ * 16;
        int maxZ = chunkZ * 16 + 15;

        sendSelectionPacket(playerRef, minX, MIN_Y, minZ, maxX, MAX_Y, maxZ);
    }

    /**
     * Shows multiple chunks as a bounding box selection.
     */
    public void showMultipleChunks(Player player, PlayerRef playerRef, int[][] chunks) {
        if (playerRef == null || chunks == null || chunks.length == 0) {
            clearVisualization(player, playerRef);
            return;
        }

        // Calculate bounding box of all chunks
        int minChunkX = Integer.MAX_VALUE;
        int maxChunkX = Integer.MIN_VALUE;
        int minChunkZ = Integer.MAX_VALUE;
        int maxChunkZ = Integer.MIN_VALUE;

        for (int[] chunk : chunks) {
            if (chunk.length >= 2) {
                minChunkX = Math.min(minChunkX, chunk[0]);
                maxChunkX = Math.max(maxChunkX, chunk[0]);
                minChunkZ = Math.min(minChunkZ, chunk[1]);
                maxChunkZ = Math.max(maxChunkZ, chunk[1]);
            }
        }

        if (minChunkX == Integer.MAX_VALUE) {
            clearVisualization(player, playerRef);
            return;
        }

        int minX = minChunkX * 16;
        int maxX = maxChunkX * 16 + 15;
        int minZ = minChunkZ * 16;
        int maxZ = maxChunkZ * 16 + 15;

        sendSelectionPacket(playerRef, minX, MIN_Y, minZ, maxX, MAX_Y, maxZ);
    }

    /**
     * Cleans up claim mode state when a player disconnects.
     */
    public void onPlayerDisconnect(UUID playerId) {
        claimModeStates.remove(playerId);
    }
}
