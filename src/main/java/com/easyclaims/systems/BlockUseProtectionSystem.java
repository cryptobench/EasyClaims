package com.easyclaims.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.RootDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.protocol.InteractionType;
import com.easyclaims.config.BlockGroups;
import com.easyclaims.data.TrustLevel;
import com.easyclaims.managers.ClaimManager;
import com.easyclaims.util.Messages;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ECS System that intercepts block use events (chests, doors, etc.) to protect claimed areas.
 * Uses granular trust levels to determine what actions are allowed.
 */
public class BlockUseProtectionSystem extends EntityEventSystem<EntityStore, UseBlockEvent.Pre> {

    private final ClaimManager claimManager;
    private final HytaleLogger logger;

    // Rate limit messages - don't spam players
    private static final Map<UUID, Long> lastMessageTime = new ConcurrentHashMap<>();
    private static final long MESSAGE_COOLDOWN_MS = 2000; // 2 seconds

    public BlockUseProtectionSystem(ClaimManager claimManager, HytaleLogger logger) {
        super(UseBlockEvent.Pre.class);
        this.claimManager = claimManager;
        this.logger = logger;
    }

    private boolean canSendMessage(UUID playerId) {
        long now = System.currentTimeMillis();
        Long lastTime = lastMessageTime.get(playerId);
        if (lastTime == null || now - lastTime > MESSAGE_COOLDOWN_MS) {
            lastMessageTime.put(playerId, now);
            return true;
        }
        return false;
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return PlayerRef.getComponentType();
    }

    @Nonnull
    @Override
    public Set<Dependency<EntityStore>> getDependencies() {
        return Collections.singleton(RootDependency.first());
    }

    @Override
    public void handle(int entityIndex, @Nonnull ArchetypeChunk<EntityStore> chunk, @Nonnull Store<EntityStore> store,
                       @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull UseBlockEvent.Pre event) {
        Vector3i targetBlock = event.getTargetBlock();
        if (targetBlock == null) return;

        // Get the entity that triggered this event
        Ref<EntityStore> entityRef = chunk.getReferenceTo(entityIndex);
        if (entityRef == null) return;

        // Get player components
        Player player = store.getComponent(entityRef, Player.getComponentType());
        PlayerRef playerRef = store.getComponent(entityRef, PlayerRef.getComponentType());
        if (player == null || playerRef == null) return;

        UUID playerId = playerRef.getUuid();
        String worldName = player.getWorld().getName();
        InteractionType interactionType = event.getInteractionType();

        // Admin bypass - allow all actions
        if (player.hasPermission("easyclaims.admin")) {
            return;
        }

        // Determine required trust level based on interaction type and block type
        BlockType blockType = event.getBlockType();
        TrustLevel requiredLevel = getRequiredTrustLevel(blockType, interactionType);

        // Check if player has permission
        if (!claimManager.hasPermissionAt(playerId, worldName, targetBlock.getX(), targetBlock.getZ(), requiredLevel)) {
            event.setCancelled(true);
            if (canSendMessage(playerId)) {
                // Send appropriate message based on interaction type
                if (interactionType == InteractionType.Pickup) {
                    player.sendMessage(Messages.cannotPickupItemsHere());
                } else {
                    player.sendMessage(Messages.cannotUseBlock(requiredLevel));
                }
            }
            logger.atFine().log("Blocked UseBlockEvent: player=%s block=%s interaction=%s required=%s",
                playerId, targetBlock, interactionType, requiredLevel);
        }
    }

    /**
     * Determines the required trust level based on block type and interaction type.
     * Pickup interactions (harvesting flowers, etc.) require BUILD trust since they destroy the block.
     */
    private TrustLevel getRequiredTrustLevel(BlockType blockType, InteractionType interactionType) {
        // Pickup interactions (harvesting flowers, bottles, etc.) require BUILD trust
        // since they effectively destroy/take the block
        if (interactionType == InteractionType.Pickup) {
            return TrustLevel.BUILD;
        }

        if (blockType == null) {
            return TrustLevel.USE; // Default to USE for unknown blocks
        }

        BlockGroups blockGroups = claimManager.getBlockGroups();

        // Check in order of specificity
        if (blockGroups.isWorkstationBlock(blockType)) {
            return TrustLevel.WORKSTATION;
        }
        if (blockGroups.isContainerBlock(blockType)) {
            return TrustLevel.CONTAINER;
        }
        if (blockGroups.isUseBlock(blockType)) {
            return TrustLevel.USE;
        }

        // Default: any interaction with unknown blocks requires USE level
        return TrustLevel.USE;
    }
}
