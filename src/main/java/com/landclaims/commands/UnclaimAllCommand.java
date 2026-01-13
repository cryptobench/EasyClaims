package com.landclaims.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.landclaims.LandClaims;
import com.landclaims.util.Messages;

import javax.annotation.Nonnull;

/**
 * /unclaimall - Unclaim all your chunks.
 */
public class UnclaimAllCommand extends AbstractPlayerCommand {
    private final LandClaims plugin;

    public UnclaimAllCommand(LandClaims plugin) {
        super("unclaimall", "Unclaim all your chunks");
        this.plugin = plugin;
        requirePermission("landclaims.unclaim");
    }

    @Override
    protected void execute(@Nonnull CommandContext ctx,
                          @Nonnull Store<EntityStore> store,
                          @Nonnull Ref<EntityStore> playerRef,
                          @Nonnull PlayerRef playerData,
                          @Nonnull World world) {

        int count = plugin.getClaimManager().unclaimAll(playerData.getUuid());

        if (count > 0) {
            playerData.sendMessage(Messages.allClaimsRemoved(count));
            // Refresh world maps for all known worlds since claims could be in any world
            for (String worldName : LandClaims.WORLDS.keySet()) {
                plugin.refreshWorldMap(worldName);
            }
        } else {
            playerData.sendMessage(Messages.noClaimsToRemove());
        }
    }
}
