package com.easyclaims.commands.subcommands.admin;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.easyclaims.EasyClaims;
import com.easyclaims.data.AdminClaims;
import com.easyclaims.data.Claim;
import com.easyclaims.util.Messages;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * Admin command to create server-owned admin claims.
 * Admin claims have PvP disabled by default and bypass buffer zones.
 */
public class AdminClaimSubcommand extends AbstractPlayerCommand {
    private final EasyClaims plugin;
    private final OptionalArg<String> displayNameArg;

    public AdminClaimSubcommand(EasyClaims plugin) {
        super("claim", "Create an admin claim at your location");
        this.plugin = plugin;
        this.displayNameArg = withOptionalArg("name", "Display name for the claim (e.g., Spawn)", ArgTypes.STRING);
        requirePermission("easyclaims.admin");
    }

    @Override
    protected void execute(@Nonnull CommandContext ctx,
                          @Nonnull Store<EntityStore> store,
                          @Nonnull Ref<EntityStore> playerRef,
                          @Nonnull PlayerRef playerData,
                          @Nonnull World world) {
        TransformComponent transform = store.getComponent(playerRef, TransformComponent.getComponentType());
        Vector3d position = transform.getPosition();
        String worldName = world.getName();

        int chunkX = ChunkUtil.chunkCoordinate((int) position.getX());
        int chunkZ = ChunkUtil.chunkCoordinate((int) position.getZ());

        // Check if already claimed
        UUID existingOwner = plugin.getClaimStorage().getClaimOwner(worldName, chunkX, chunkZ);
        if (existingOwner != null) {
            String ownerName = plugin.getClaimStorage().getPlayerName(existingOwner);
            if (AdminClaims.isAdminClaim(existingOwner)) {
                playerData.sendMessage(Messages.alreadyOwnChunk());
            } else {
                playerData.sendMessage(Messages.chunkClaimedByOther(ownerName));
            }
            return;
        }

        // Get optional display name
        String displayName = displayNameArg.get(ctx);

        // Register admin name for map display
        plugin.getClaimStorage().setPlayerName(AdminClaims.ADMIN_UUID,
                displayName != null ? displayName : AdminClaims.DEFAULT_DISPLAY_NAME);

        // Create admin claim (PvP disabled by default)
        Claim claim = Claim.createAdminClaim(worldName, chunkX, chunkZ, displayName);
        plugin.getClaimStorage().addClaim(AdminClaims.ADMIN_UUID, claim);

        // Refresh map
        plugin.refreshWorldMapChunk(worldName, chunkX, chunkZ);

        playerData.sendMessage(Messages.adminClaimCreated(chunkX, chunkZ, displayName));
        playerData.sendMessage(Messages.pvpStatusDisabled());
    }
}
