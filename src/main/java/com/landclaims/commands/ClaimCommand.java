package com.landclaims.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.landclaims.LandClaims;
import com.landclaims.managers.ClaimManager;
import com.landclaims.util.Messages;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * /claim - Claims the chunk you are currently standing in.
 */
public class ClaimCommand extends AbstractPlayerCommand {
    private final LandClaims plugin;

    public ClaimCommand(LandClaims plugin) {
        super("claim", "Claims the chunk you are standing in");
        this.plugin = plugin;
        requirePermission("landclaims.claim");
    }

    @Override
    protected void execute(@Nonnull CommandContext ctx,
                          @Nonnull Store<EntityStore> store,
                          @Nonnull Ref<EntityStore> playerRef,
                          @Nonnull PlayerRef playerData,
                          @Nonnull World world) {

        Player player = store.getComponent(playerRef, Player.getComponentType());
        if (player == null) return;

        UUID playerId = playerData.getUuid();
        String worldName = world.getName();

        // Get player position
        double x = playerData.getTransform().getPosition().getX();
        double z = playerData.getTransform().getPosition().getZ();

        // Get chunk coordinates for display
        int chunkX = ChunkUtil.chunkCoordinate((int) x);
        int chunkZ = ChunkUtil.chunkCoordinate((int) z);

        ClaimManager claimManager = plugin.getClaimManager();

        // Check claim limits first
        int currentClaims = claimManager.getPlayerClaims(playerId).getClaimCount();
        int maxClaims = claimManager.getMaxClaims(playerId);

        // Attempt to claim
        ClaimManager.ClaimResult result = claimManager.claimChunk(playerId, worldName, x, z);

        switch (result) {
            case SUCCESS:
                playerData.sendMessage(Messages.chunkClaimed(chunkX, chunkZ));
                playerData.sendMessage(Messages.claimCount(currentClaims + 1, maxClaims));
                // Refresh world map to show the new claim (includes neighboring chunks for borders)
                plugin.refreshWorldMapChunk(worldName, chunkX, chunkZ);
                break;

            case ALREADY_OWN:
                playerData.sendMessage(Messages.alreadyOwnChunk());
                break;

            case CLAIMED_BY_OTHER:
                UUID owner = claimManager.getOwnerAt(worldName, x, z);
                String ownerName = owner != null ? plugin.getClaimStorage().getPlayerName(owner) : "Unknown";
                playerData.sendMessage(Messages.chunkOwnedByOther(ownerName));
                break;

            case LIMIT_REACHED:
                playerData.sendMessage(Messages.claimLimitReached(currentClaims, maxClaims));
                double hoursUntilNext = claimManager.getHoursUntilNextClaim(playerId);
                if (hoursUntilNext > 0) {
                    playerData.sendMessage(Messages.hoursUntilNextClaim(hoursUntilNext));
                }
                break;
        }
    }
}
