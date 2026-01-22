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
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
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
 * ECS System that intercepts block place events to protect claimed areas.
 */
public class BlockPlaceProtectionSystem extends EntityEventSystem<EntityStore, PlaceBlockEvent> {

    private final ClaimManager claimManager;
    private final HytaleLogger logger;

    // Rate limit messages - don't spam players
    private static final Map<UUID, Long> lastMessageTime = new ConcurrentHashMap<>();
    private static final long MESSAGE_COOLDOWN_MS = 2000; // 2 seconds

    public BlockPlaceProtectionSystem(ClaimManager claimManager, HytaleLogger logger) {
        super(PlaceBlockEvent.class);
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
                       @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull PlaceBlockEvent event) {
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

        // Admin bypass - allow all actions
        if (player.hasPermission("easyclaims.admin")) {
            return;
        }

        // Placing blocks requires BUILD trust level
        if (!claimManager.hasPermissionAt(playerId, worldName, targetBlock.getX(), targetBlock.getZ(), TrustLevel.BUILD)) {
            event.setCancelled(true);
            if (canSendMessage(playerId)) {
                player.sendMessage(Messages.cannotBuildHere());
            }
        }
    }
}
