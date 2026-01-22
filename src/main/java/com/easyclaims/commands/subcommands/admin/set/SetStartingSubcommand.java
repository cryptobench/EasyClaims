package com.easyclaims.commands.subcommands.admin.set;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.easyclaims.EasyClaims;

import javax.annotation.Nonnull;
import java.awt.Color;

public class SetStartingSubcommand extends AbstractPlayerCommand {
    private final EasyClaims plugin;
    private final RequiredArg<Integer> valueArg;

    private static final Color GREEN = new Color(85, 255, 85);

    public SetStartingSubcommand(EasyClaims plugin) {
        super("starting", "Set starting claims for new players");
        this.plugin = plugin;
        this.valueArg = withRequiredArg("value", "Number of starting claims", ArgTypes.INTEGER);
        addAliases("startingclaims");
        requirePermission("easyclaims.admin");
    }

    @Override
    protected void execute(@Nonnull CommandContext ctx,
                          @Nonnull Store<EntityStore> store,
                          @Nonnull Ref<EntityStore> playerRef,
                          @Nonnull PlayerRef playerData,
                          @Nonnull World world) {
        int value = valueArg.get(ctx);
        plugin.getPluginConfig().setStartingClaims(value);
        playerData.sendMessage(Message.raw("New players will now start with " + value + " claims!").color(GREEN));
    }
}
