package com.landclaims.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.landclaims.LandClaims;
import com.landclaims.util.ChunkUtil;
import com.landclaims.util.Messages;

import javax.annotation.Nonnull;

/**
 * /unclaim - Unclaim the chunk you're standing in.
 */
public class UnclaimCommand extends AbstractPlayerCommand {
    private final LandClaims plugin;

    public UnclaimCommand(LandClaims plugin) {
        super("unclaim", "Unclaim the chunk you're standing in");
        this.plugin = plugin;
        requirePermission("landclaims.unclaim");
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

        int chunkX = ChunkUtil.toChunkX(position.getX());
        int chunkZ = ChunkUtil.toChunkZ(position.getZ());

        boolean success = plugin.getClaimManager().unclaimChunk(
                playerData.getUuid(),
                worldName,
                position.getX(),
                position.getZ()
        );

        if (success) {
            playerData.sendMessage(Messages.chunkUnclaimed(chunkX, chunkZ));
            // Refresh the world map to show the unclaimed chunk
            plugin.refreshWorldMapChunk(worldName, chunkX, chunkZ);
        } else {
            playerData.sendMessage(Messages.notYourClaim());
        }
    }
}
