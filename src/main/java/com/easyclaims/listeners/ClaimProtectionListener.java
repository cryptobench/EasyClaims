package com.easyclaims.listeners;

import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerInteractEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.protocol.InteractionType;
import com.easyclaims.EasyClaims;
import com.easyclaims.data.TrustLevel;
import com.easyclaims.managers.ClaimManager;
import com.easyclaims.util.Messages;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Listens for player interaction events for protection checks.
 */
public class ClaimProtectionListener {
    private final EasyClaims plugin;
    private final ClaimManager claimManager;
    private final HytaleLogger logger;

    // Track player interactions for ECS event correlation
    private static final Map<String, PlayerInteraction> pendingInteractions = new ConcurrentHashMap<>();
    private static final Map<UUID, PlayerInteraction> playerLastInteraction = new ConcurrentHashMap<>();

    private static final long INTERACTION_TIMEOUT_MS = 5000;

    public ClaimProtectionListener(EasyClaims plugin) {
        this.plugin = plugin;
        this.claimManager = plugin.getClaimManager();
        this.logger = plugin.getLogger();
    }

    /**
     * Register player event handlers.
     */
    public void register(EventRegistry eventRegistry) {
        eventRegistry.registerGlobal(PlayerInteractEvent.class, this::onPlayerInteract);
        eventRegistry.register(PlayerConnectEvent.class, this::onPlayerConnect);
        eventRegistry.registerGlobal(PlayerDisconnectEvent.class, this::onPlayerDisconnect);
    }

    // Rate limit messages - don't spam players
    private static final Map<UUID, Long> lastMessageTime = new ConcurrentHashMap<>();
    private static final long MESSAGE_COOLDOWN_MS = 2000; // 2 seconds

    private boolean canSendMessage(UUID playerId) {
        long now = System.currentTimeMillis();
        Long lastTime = lastMessageTime.get(playerId);
        if (lastTime == null || now - lastTime > MESSAGE_COOLDOWN_MS) {
            lastMessageTime.put(playerId, now);
            return true;
        }
        return false;
    }

    /**
     * Handle player interactions - check claim protection.
     */
    private void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;

        UUID playerId = player.getUuid();
        InteractionType actionType = event.getActionType();
        String worldName = player.getWorld().getName();

        Vector3i targetBlock = event.getTargetBlock();
        Entity targetEntity = event.getTargetEntity();

        // Determine the position to check - either from block or entity
        double checkX, checkZ;
        if (targetBlock != null) {
            checkX = targetBlock.getX();
            checkZ = targetBlock.getZ();
        } else if (targetEntity != null) {
            // For entity interactions (like picking up dropped items), use entity position
            var transformComponent = targetEntity.getTransformComponent();
            if (transformComponent == null) return;
            Vector3d entityPos = transformComponent.getPosition();
            if (entityPos == null) return;
            checkX = entityPos.getX();
            checkZ = entityPos.getZ();
        } else {
            return; // No target to check
        }

        // Track interaction for ECS event correlation (only for block interactions)
        if (targetBlock != null) {
            String blockKey = getBlockKey(targetBlock);
            PlayerInteraction interaction = new PlayerInteraction(playerId, worldName, targetBlock, System.currentTimeMillis());
            pendingInteractions.put(blockKey, interaction);
            playerLastInteraction.put(playerId, interaction);
        }

        cleanupOldInteractions();

        // Determine required trust level based on action type
        // Pickup interactions on blocks (harvesting flowers, etc.) require BUILD trust
        // since they effectively destroy the block
        TrustLevel requiredLevel;
        if (actionType == InteractionType.Pickup) {
            requiredLevel = TrustLevel.BUILD;
        } else if (actionType == InteractionType.Primary) {
            requiredLevel = TrustLevel.BUILD; // Attacking/breaking
        } else {
            requiredLevel = TrustLevel.USE; // Default for other interactions
        }

        // Check if this location is protected
        boolean hasPermission = claimManager.hasPermissionAt(playerId, worldName, checkX, checkZ, requiredLevel);

        if (!hasPermission) {
            logger.atFine().log("Blocked interaction: player=%s pos=[%.1f, %.1f] action=%s required=%s",
                playerId, checkX, checkZ, actionType, requiredLevel);
            event.setCancelled(true);

            // Send appropriate message based on action type
            if (canSendMessage(playerId)) {
                if (actionType == InteractionType.Pickup) {
                    player.sendMessage(Messages.cannotPickupItemsHere());
                } else if (actionType == InteractionType.Primary) {
                    player.sendMessage(Messages.cannotBuildHere());
                } else {
                    player.sendMessage(Messages.cannotInteractHere());
                }
            }
        }
    }

    // Static accessors for BlockProtectionSystems
    public static PlayerInteraction getInteraction(String blockKey) {
        PlayerInteraction interaction = pendingInteractions.get(blockKey);
        if (interaction != null && !interaction.isExpired()) {
            return interaction;
        }
        return null;
    }

    public static PlayerInteraction findNearbyInteraction(Vector3i targetBlock) {
        String blockKey = getBlockKey(targetBlock);
        PlayerInteraction exact = pendingInteractions.get(blockKey);
        if (exact != null && !exact.isExpired()) {
            return exact;
        }

        for (PlayerInteraction interaction : playerLastInteraction.values()) {
            if (interaction.isExpired()) continue;
            if (interaction.blockPos == null) continue;

            int dx = Math.abs(targetBlock.getX() - interaction.blockPos.getX());
            int dy = Math.abs(targetBlock.getY() - interaction.blockPos.getY());
            int dz = Math.abs(targetBlock.getZ() - interaction.blockPos.getZ());

            if (dx <= 1 && dy <= 1 && dz <= 1) {
                return interaction;
            }
        }
        return null;
    }

    public static void removeInteraction(String blockKey) {
        pendingInteractions.remove(blockKey);
    }

    public static String getBlockKey(Vector3i pos) {
        return pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }

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
            UUID playerId = playerRef.getUuid();
            plugin.onPlayerLeave(playerId);
        }
    }

    /**
     * Tracks a player's interaction with a block.
     */
    public static class PlayerInteraction {
        public final UUID playerId;
        public final String worldName;
        public final Vector3i blockPos;
        public final long timestamp;

        public PlayerInteraction(UUID playerId, String worldName, Vector3i blockPos, long timestamp) {
            this.playerId = playerId;
            this.worldName = worldName;
            this.blockPos = blockPos;
            this.timestamp = timestamp;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp > INTERACTION_TIMEOUT_MS;
        }
    }
}
