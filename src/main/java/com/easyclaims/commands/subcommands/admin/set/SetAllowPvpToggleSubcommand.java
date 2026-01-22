package com.easyclaims.commands.subcommands.admin.set;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.easyclaims.EasyClaims;
import com.easyclaims.util.Messages;

import javax.annotation.Nonnull;

/**
 * Admin command to set whether PvP is enabled in player claims.
 * true = PvP server (fighting allowed in player claims)
 * false = PvE server (no fighting in player claims)
 */
public class SetAllowPvpToggleSubcommand extends AbstractPlayerCommand {
    private final EasyClaims plugin;
    private final RequiredArg<Boolean> valueArg;

    public SetAllowPvpToggleSubcommand(EasyClaims plugin) {
        super("pvpinclaims", "Set whether PvP is enabled in player claims");
        this.plugin = plugin;
        this.valueArg = withRequiredArg("enabled", "true (PvP server) / false (PvE server)", ArgTypes.BOOLEAN);
        addAliases("claimpvp", "pvp");
        requirePermission("easyclaims.admin");
    }

    @Override
    protected void execute(@Nonnull CommandContext ctx,
                          @Nonnull Store<EntityStore> store,
                          @Nonnull Ref<EntityStore> playerRef,
                          @Nonnull PlayerRef playerData,
                          @Nonnull World world) {
        boolean value = valueArg.get(ctx);
        plugin.getPluginConfig().setPvpInPlayerClaims(value);
        playerData.sendMessage(Messages.pvpModeChanged(value));
    }
}
