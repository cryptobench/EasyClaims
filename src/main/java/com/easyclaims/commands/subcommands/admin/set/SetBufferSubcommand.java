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

public class SetBufferSubcommand extends AbstractPlayerCommand {
    private final EasyClaims plugin;
    private final RequiredArg<Integer> valueArg;

    private static final Color GREEN = new Color(85, 255, 85);

    public SetBufferSubcommand(EasyClaims plugin) {
        super("buffer", "Set claim buffer zone size (0 to disable)");
        this.plugin = plugin;
        this.valueArg = withRequiredArg("value", "Buffer size in chunks", ArgTypes.INTEGER);
        addAliases("buffersize", "claimbuffer");
        requirePermission("easyclaims.admin");
    }

    @Override
    protected void execute(@Nonnull CommandContext ctx,
                          @Nonnull Store<EntityStore> store,
                          @Nonnull Ref<EntityStore> playerRef,
                          @Nonnull PlayerRef playerData,
                          @Nonnull World world) {
        int value = valueArg.get(ctx);
        plugin.getPluginConfig().setClaimBufferSize(value);
        if (value == 0) {
            playerData.sendMessage(Message.raw("Claim buffer zone disabled!").color(GREEN));
        } else {
            playerData.sendMessage(Message.raw("Claim buffer zone set to " + value + " chunks!").color(GREEN));
        }
    }
}
