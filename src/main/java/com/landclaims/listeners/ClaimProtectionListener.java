package com.landclaims.listeners;

import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.event.events.ecs.DamageBlockEvent;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerInteractEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.protocol.InteractionType;
import com.landclaims.LandClaims;
import com.landclaims.managers.ClaimManager;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Listens for block and interaction events to protect claimed areas.
 *
 * Protection strategy:
 * 1. PlayerInteractEvent - Track which player is interacting with which block
 * 2. DamageBlockEvent - Prevent block damage in protected areas (blocks mining)
 * 3. BreakBlockEvent - Final safety net to prevent block breaking
 * 4. PlaceBlockEvent - Prevent block placing in protected areas
 */
public class ClaimProtectionListener {
    private final LandClaims plugin;
    private final ClaimManager claimManager;

    // Track player interactions to correlate with ECS events
    // Key: "x,y,z" block position, Value: PlayerInteraction data
    private final Map<String, PlayerInteraction> pendingInteractions = new ConcurrentHashMap<>();

    // Also track by player UUID for cases where block position differs (e.g., placing)
    // Key: player UUID, Value: PlayerInteraction with last interacted position
    private final Map<UUID, PlayerInteraction> playerLastInteraction = new ConcurrentHashMap<>();

    // How long to keep interaction data (ms) - block breaks happen quickly after interaction
    private static final long INTERACTION_TIMEOUT_MS = 5000;

    public ClaimProtectionListener(LandClaims plugin) {
        this.plugin = plugin;
        this.claimManager = plugin.getClaimManager();
    }

    /**
     * Register all protection event handlers.
     */
    public void register(EventRegistry eventRegistry) {
        // Player interaction - track who is interacting with what
        eventRegistry.registerGlobal(PlayerInteractEvent.class, this::onPlayerInteract);

        // ECS block events - these fire when blocks are actually modified
        eventRegistry.registerGlobal(DamageBlockEvent.class, this::onDamageBlock);
        eventRegistry.registerGlobal(BreakBlockEvent.class, this::onBreakBlock);
        eventRegistry.registerGlobal(PlaceBlockEvent.class, this::onPlaceBlock);

        // Player join/leave for playtime tracking
        eventRegistry.register(PlayerConnectEvent.class, this::onPlayerConnect);
        eventRegistry.registerGlobal(PlayerDisconnectEvent.class, this::onPlayerDisconnect);
    }

    /**
     * Track player interactions and cancel if in protected area.
     * This fires BEFORE block damage/break events.
     */
    private void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;

        Vector3i targetBlock = event.getTargetBlock();
        if (targetBlock == null) return;

        UUID playerId = player.getUuid();
        String worldName = "default"; // TODO: Get actual world name from context
        InteractionType actionType = event.getActionType();

        // Track this interaction for ECS event correlation
        String blockKey = getBlockKey(targetBlock);
        PlayerInteraction interaction = new PlayerInteraction(playerId, worldName, targetBlock, System.currentTimeMillis());
        pendingInteractions.put(blockKey, interaction);

        // Also track by player UUID for block placement (placed position differs from looked-at position)
        playerLastInteraction.put(playerId, interaction);

        // Clean up old interactions periodically
        cleanupOldInteractions();

        // Check if this location is protected
        if (!claimManager.canInteract(playerId, worldName, targetBlock.getX(), targetBlock.getZ())) {
            // Player cannot interact here - cancel the event
            event.setCancelled(true);

            // Send feedback for primary/secondary actions (breaking/placing attempts)
            if (actionType == InteractionType.Primary || actionType == InteractionType.Secondary) {
                // Message is sent via the event cancellation - player will notice action doesn't work
            }
        }
    }

    /**
     * Prevent block damage in protected areas.
     * This is the most effective protection as it stops mining progress entirely.
     */
    private void onDamageBlock(DamageBlockEvent event) {
        Vector3i targetBlock = event.getTargetBlock();
        if (targetBlock == null) return;

        String blockKey = getBlockKey(targetBlock);
        PlayerInteraction interaction = pendingInteractions.get(blockKey);

        if (interaction != null && !interaction.isExpired()) {
            // We know which player is damaging this block
            if (!claimManager.canInteract(interaction.playerId, interaction.worldName,
                    targetBlock.getX(), targetBlock.getZ())) {
                // Cancel damage - this prevents any mining progress
                event.setCancelled(true);
            }
        } else {
            // No tracked player - check if block is in ANY claimed chunk
            // If claimed, cancel damage as a safety measure
            String worldName = "default";
            UUID owner = claimManager.getOwnerAt(worldName, targetBlock.getX(), targetBlock.getZ());
            if (owner != null) {
                // Block is claimed - cancel unless we can verify the player has access
                // This is a conservative approach - may need adjustment based on testing
                event.setCancelled(true);
            }
        }
    }

    /**
     * Final safety net - prevent block breaking in protected areas.
     */
    private void onBreakBlock(BreakBlockEvent event) {
        Vector3i targetBlock = event.getTargetBlock();
        if (targetBlock == null) return;

        String blockKey = getBlockKey(targetBlock);
        PlayerInteraction interaction = pendingInteractions.get(blockKey);

        if (interaction != null && !interaction.isExpired()) {
            if (!claimManager.canInteract(interaction.playerId, interaction.worldName,
                    targetBlock.getX(), targetBlock.getZ())) {
                event.setCancelled(true);
            }
            // Clean up this interaction since the break attempt is done
            pendingInteractions.remove(blockKey);
        } else {
            // No tracked player - protect claimed chunks
            String worldName = "default";
            UUID owner = claimManager.getOwnerAt(worldName, targetBlock.getX(), targetBlock.getZ());
            if (owner != null) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * Prevent block placing in protected areas.
     * Note: For placing, the target block is where the NEW block goes, which is adjacent
     * to the block the player looked at. So we need to check by player's recent interaction.
     */
    private void onPlaceBlock(PlaceBlockEvent event) {
        Vector3i targetBlock = event.getTargetBlock();
        if (targetBlock == null) return;

        // First try exact block match
        String blockKey = getBlockKey(targetBlock);
        PlayerInteraction interaction = pendingInteractions.get(blockKey);

        // If no exact match, find the player who recently interacted nearby
        // Block placement happens at adjacent block to what player looked at
        if (interaction == null || interaction.isExpired()) {
            interaction = findNearbyInteraction(targetBlock);
        }

        if (interaction != null && !interaction.isExpired()) {
            if (!claimManager.canInteract(interaction.playerId, interaction.worldName,
                    targetBlock.getX(), targetBlock.getZ())) {
                event.setCancelled(true);
            }
        } else {
            // No tracked player - protect claimed chunks
            String worldName = "default";
            UUID owner = claimManager.getOwnerAt(worldName, targetBlock.getX(), targetBlock.getZ());
            if (owner != null) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * Find a player interaction that was on an adjacent block (for block placement).
     */
    private PlayerInteraction findNearbyInteraction(Vector3i targetBlock) {
        // Check all recent player interactions to find one adjacent to this block
        for (PlayerInteraction interaction : playerLastInteraction.values()) {
            if (interaction.isExpired()) continue;
            if (interaction.blockPos == null) continue;

            // Check if this interaction was on an adjacent block (within 1 block)
            int dx = Math.abs(targetBlock.getX() - interaction.blockPos.getX());
            int dy = Math.abs(targetBlock.getY() - interaction.blockPos.getY());
            int dz = Math.abs(targetBlock.getZ() - interaction.blockPos.getZ());

            if (dx <= 1 && dy <= 1 && dz <= 1) {
                return interaction;
            }
        }
        return null;
    }

    /**
     * Create a unique key for a block position.
     */
    private String getBlockKey(Vector3i pos) {
        return pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }

    /**
     * Remove expired interaction data to prevent memory leaks.
     */
    private void cleanupOldInteractions() {
        long now = System.currentTimeMillis();
        pendingInteractions.entrySet().removeIf(entry ->
            now - entry.getValue().timestamp > INTERACTION_TIMEOUT_MS);
        playerLastInteraction.entrySet().removeIf(entry ->
            now - entry.getValue().timestamp > INTERACTION_TIMEOUT_MS);
    }

    private void onPlayerConnect(PlayerConnectEvent event) {
        PlayerRef playerRef = event.getPlayerRef();
        if (playerRef != null) {
            plugin.onPlayerJoin(playerRef.getUuid());
        }
    }

    private void onPlayerDisconnect(PlayerDisconnectEvent event) {
        PlayerRef playerRef = event.getPlayerRef();
        if (playerRef != null) {
            plugin.onPlayerLeave(playerRef.getUuid());
        }
    }

    /**
     * Tracks a player's interaction with a block for correlating with ECS events.
     */
    private static class PlayerInteraction {
        final UUID playerId;
        final String worldName;
        final Vector3i blockPos;
        final long timestamp;

        PlayerInteraction(UUID playerId, String worldName, Vector3i blockPos, long timestamp) {
            this.playerId = playerId;
            this.worldName = worldName;
            this.blockPos = blockPos;
            this.timestamp = timestamp;
        }

        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > INTERACTION_TIMEOUT_MS;
        }
    }
}
