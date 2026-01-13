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
 * /hideclaims - This command is deprecated since claims are now shown on the map.
 */
public class HideClaimsCommand extends AbstractPlayerCommand {
    private final LandClaims plugin;

    public HideClaimsCommand(LandClaims plugin) {
        super("hideclaims", "Hide claim visualization (deprecated - see map)");
        this.plugin = plugin;
        requirePermission("landclaims.list");
    }

    @Override
    protected void execute(@Nonnull CommandContext ctx,
                          @Nonnull Store<EntityStore> store,
                          @Nonnull Ref<EntityStore> playerRef,
                          @Nonnull PlayerRef playerData,
                          @Nonnull World world) {
        // Claims are now shown on the world map automatically
        playerData.sendMessage(Messages.claimsHidden());
    }
}
