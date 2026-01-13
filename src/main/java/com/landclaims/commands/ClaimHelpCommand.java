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
 * /claimhelp - Show help for claim commands.
 */
public class ClaimHelpCommand extends AbstractPlayerCommand {
    private final LandClaims plugin;

    public ClaimHelpCommand(LandClaims plugin) {
        super("claimhelp", "Show help for land claiming commands");
        this.plugin = plugin;
        requirePermission("landclaims.help");
    }

    @Override
    protected void execute(@Nonnull CommandContext ctx,
                          @Nonnull Store<EntityStore> store,
                          @Nonnull Ref<EntityStore> playerRef,
                          @Nonnull PlayerRef playerData,
                          @Nonnull World world) {

        playerData.sendMessage(Messages.helpHeader());
        playerData.sendMessage(Messages.helpEntry("claim", "Claim selection or current chunk"));
        playerData.sendMessage(Messages.helpEntry("unclaim", "Unclaim the chunk you're standing in"));
        playerData.sendMessage(Messages.helpEntry("claims", "List all your claims"));
        playerData.sendMessage(Messages.helpEntry("claimpreview", "Preview the chunk you're standing in"));
        playerData.sendMessage(Messages.helpEntry("showclaims", "Visualize your claimed chunks"));
        playerData.sendMessage(Messages.helpEntry("hideclaims", "Hide claim visualization"));
        playerData.sendMessage(Messages.helpEntry("trust <player> [level]", "Trust a player in all your claims"));
        playerData.sendMessage(Messages.helpEntry("untrust <player>", "Remove trust from a player"));
        playerData.sendMessage(Messages.helpEntry("trustlist", "List all trusted players"));
        playerData.sendMessage(Messages.helpEntry("playtime", "Show your playtime and available claims"));
        playerData.sendMessage(Messages.helpEntry("claimhelp", "Show this help message"));

        // Add selection tool usage tip
        playerData.sendMessage(Messages.selectionToolTip());
    }
}
