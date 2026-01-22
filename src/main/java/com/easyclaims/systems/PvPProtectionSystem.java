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
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
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
 * ECS System that intercepts damage events to protect players in PvP-disabled areas.
 * When PvP is disabled in a claim, players cannot damage each other.
 */
public class PvPProtectionSystem extends EntityEventSystem<EntityStore, Damage> {

    private final ClaimManager claimManager;
    private final HytaleLogger logger;

    // Rate limit messages - don't spam players
    private static final Map<UUID, Long> lastMessageTime = new ConcurrentHashMap<>();
    private static final long MESSAGE_COOLDOWN_MS = 2000; // 2 seconds

    public PvPProtectionSystem(ClaimManager claimManager, HytaleLogger logger) {
        super(Damage.class);
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
        // Only handle damage events for player entities (the victim)
        return PlayerRef.getComponentType();
    }

    @Nonnull
    @Override
    public Set<Dependency<EntityStore>> getDependencies() {
        return Collections.singleton(RootDependency.first());
    }

    @Override
    public void handle(int entityIndex, @Nonnull ArchetypeChunk<EntityStore> chunk, @Nonnull Store<EntityStore> store,
                       @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull Damage event) {
        // Get the victim entity (the one being damaged)
        Ref<EntityStore> victimRef = chunk.getReferenceTo(entityIndex);
        if (victimRef == null) return;

        // Get victim's player components
        Player victimPlayer = store.getComponent(victimRef, Player.getComponentType());
        PlayerRef victimPlayerRef = store.getComponent(victimRef, PlayerRef.getComponentType());
        if (victimPlayer == null || victimPlayerRef == null) return; // Not a player

        // Check if damage source is another player (PvP)
        Damage.Source source = event.getSource();
        if (!(source instanceof Damage.EntitySource)) return;

        Damage.EntitySource entitySource = (Damage.EntitySource) source;
        Ref<EntityStore> attackerRef = entitySource.getRef();
        if (attackerRef == null) return;

        // Check if attacker is a player
        PlayerRef attackerPlayerRef = store.getComponent(attackerRef, PlayerRef.getComponentType());
        if (attackerPlayerRef == null) return; // Not player-on-player damage

        // Don't block self-damage
        if (victimPlayerRef.getUuid().equals(attackerPlayerRef.getUuid())) return;

        // Get victim's position
        TransformComponent victimTransform = store.getComponent(victimRef, TransformComponent.getComponentType());
        if (victimTransform == null) return;

        // Check if PvP is disabled at victim's location
        String worldName = victimPlayer.getWorld().getName();
        double x = victimTransform.getPosition().getX();
        double z = victimTransform.getPosition().getZ();

        if (!claimManager.isPvPEnabledAt(worldName, x, z)) {
            // PvP is disabled - cancel the damage
            event.setCancelled(true);

            // Notify attacker (with rate limiting)
            UUID attackerId = attackerPlayerRef.getUuid();
            if (canSendMessage(attackerId)) {
                Player attackerPlayer = store.getComponent(attackerRef, Player.getComponentType());
                if (attackerPlayer != null) {
                    attackerPlayer.sendMessage(Messages.pvpDisabledHere());
                }
            }
        }
    }
}
