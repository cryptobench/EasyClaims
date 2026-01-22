package com.easyclaims.commands.subcommands.admin;

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
import com.easyclaims.config.PluginConfig;

import javax.annotation.Nonnull;
import java.awt.Color;

public class AdminSetSubcommand extends AbstractPlayerCommand {
    private final EasyClaims plugin;
    private final RequiredArg<String> keyArg;
    private final RequiredArg<Integer> valueArg;

    private static final Color GOLD = new Color(255, 170, 0);
    private static final Color GREEN = new Color(85, 255, 85);
    private static final Color RED = new Color(255, 85, 85);
    private static final Color YELLOW = new Color(255, 255, 85);
    private static final Color GRAY = new Color(170, 170, 170);

    public AdminSetSubcommand(EasyClaims plugin) {
        super("set", "Change a server claim setting (starting/perhour/max/buffer)");
        this.plugin = plugin;
        this.keyArg = withRequiredArg("setting", "Setting name (starting/perhour/max/buffer)", ArgTypes.STRING);
        this.valueArg = withRequiredArg("value", "New value", ArgTypes.INTEGER);
        requirePermission("easyclaims.admin");
    }

    @Override
    protected void execute(@Nonnull CommandContext ctx,
                          @Nonnull Store<EntityStore> store,
                          @Nonnull Ref<EntityStore> playerRef,
                          @Nonnull PlayerRef playerData,
                          @Nonnull World world) {
        String key = keyArg.get(ctx);
        int value = valueArg.get(ctx);

        PluginConfig config = plugin.getPluginConfig();
        switch (key.toLowerCase()) {
            case "starting":
            case "startingclaims":
                config.setStartingClaims(value);
                playerData.sendMessage(Message.raw("New players will now start with " + value + " claims!").color(GREEN));
                break;
            case "perhour":
            case "claimsperhour":
                config.setClaimsPerHour(value);
                playerData.sendMessage(Message.raw("Players will now earn " + value + " claims per hour!").color(GREEN));
                break;
            case "max":
            case "maxclaims":
                config.setMaxClaims(value);
                playerData.sendMessage(Message.raw("Maximum claims is now " + value + "!").color(GREEN));
                break;
            case "buffer":
            case "buffersize":
            case "claimbuffer":
                config.setClaimBufferSize(value);
                if (value == 0) {
                    playerData.sendMessage(Message.raw("Claim buffer zone disabled!").color(GREEN));
                } else {
                    playerData.sendMessage(Message.raw("Claim buffer zone set to " + value + " chunks!").color(GREEN));
                }
                break;
            default:
                playerData.sendMessage(Message.raw("Unknown setting! Try: starting, perhour, max, or buffer").color(RED));
        }
    }
}
