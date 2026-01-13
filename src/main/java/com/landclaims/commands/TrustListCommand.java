package com.landclaims.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.landclaims.LandClaims;
import com.landclaims.data.PlayerClaims;
import com.landclaims.data.TrustedPlayer;
import com.landclaims.util.Messages;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.UUID;

/**
 * /trustlist - List all players you've trusted with their trust levels.
 */
public class TrustListCommand extends AbstractPlayerCommand {
    private final LandClaims plugin;

    public TrustListCommand(LandClaims plugin) {
        super("trustlist", "List players you've trusted");
        this.plugin = plugin;
        requirePermission("landclaims.trustlist");
    }

    @Override
    protected void execute(@Nonnull CommandContext ctx,
                          @Nonnull Store<EntityStore> store,
                          @Nonnull Ref<EntityStore> playerRef,
                          @Nonnull PlayerRef playerData,
                          @Nonnull World world) {

        PlayerClaims claims = plugin.getClaimManager().getPlayerClaims(playerData.getUuid());
        Map<UUID, TrustedPlayer> trustedPlayers = claims.getTrustedPlayersMap();

        if (trustedPlayers.isEmpty()) {
            playerData.sendMessage(Messages.noTrustedPlayers());
            return;
        }

        playerData.sendMessage(Messages.trustedPlayersHeader(trustedPlayers.size()));

        for (TrustedPlayer tp : trustedPlayers.values()) {
            playerData.sendMessage(Messages.trustedPlayerEntry(tp.getName(), tp.getLevel()));
        }
    }
}
