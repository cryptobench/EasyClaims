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
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.InteractivelyPickupItemEvent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
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
 * ECS System that intercepts item pickup events to protect claimed areas.
 * This prevents players from picking up items (like flowers) in protected chunks.
 */
public class ItemPickupProtectionSystem extends EntityEventSystem<EntityStore, InteractivelyPickupItemEvent> {

    private final ClaimManager claimManager;
    private final HytaleLogger logger;

    // Rate limit messages - don't spam players
    private static final Map<UUID, Long> lastMessageTime = new ConcurrentHashMap<>();
    private static final long MESSAGE_COOLDOWN_MS = 2000; // 2 seconds

    public ItemPickupProtectionSystem(ClaimManager claimManager, HytaleLogger logger) {
        super(InteractivelyPickupItemEvent.class);
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
                       @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull InteractivelyPickupItemEvent event) {
        // Get the entity that triggered this event
        Ref<EntityStore> entityRef = chunk.getReferenceTo(entityIndex);
        if (entityRef == null) return;

        // Get player components
        Player player = store.getComponent(entityRef, Player.getComponentType());
        PlayerRef playerRef = store.getComponent(entityRef, PlayerRef.getComponentType());
        if (player == null || playerRef == null) return;

        // Get player's position from TransformComponent
        TransformComponent transform = store.getComponent(entityRef, TransformComponent.getComponentType());
        if (transform == null) return;

        Vector3d position = transform.getPosition();
        if (position == null) return;

        UUID playerId = playerRef.getUuid();
        String worldName = player.getWorld().getName();

        // Picking up items requires USE trust level (same as basic interaction)
        if (!claimManager.hasPermissionAt(playerId, worldName, position.getX(), position.getZ(), TrustLevel.USE)) {
            event.setCancelled(true);
            if (canSendMessage(playerId)) {
                player.sendMessage(Messages.cannotPickupItemsHere());
            }
            logger.atFine().log("Blocked item pickup: player=%s position=[%.1f, %.1f]",
                playerId, position.getX(), position.getZ());
        }
    }
}
