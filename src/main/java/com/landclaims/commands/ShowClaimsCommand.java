package com.landclaims.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.landclaims.LandClaims;
import com.landclaims.data.Claim;
import com.landclaims.data.PlayerClaims;
import com.landclaims.util.Messages;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.util.List;
import java.util.stream.Collectors;

/**
 * /showclaims - Shows your claims in the current world.
 * Claims are now displayed on the world map automatically.
 */
public class ShowClaimsCommand extends AbstractPlayerCommand {
    private final LandClaims plugin;
    private static final Color AQUA = new Color(85, 255, 255);

    public ShowClaimsCommand(LandClaims plugin) {
        super("showclaims", "Shows your claims - check the map to see them!");
        this.plugin = plugin;
        requirePermission("landclaims.list");
    }

    @Override
    protected void execute(@Nonnull CommandContext ctx,
                          @Nonnull Store<EntityStore> store,
                          @Nonnull Ref<EntityStore> playerRef,
                          @Nonnull PlayerRef playerData,
                          @Nonnull World world) {

        String worldName = world.getName();
        PlayerClaims playerClaims = plugin.getClaimManager().getPlayerClaims(playerData.getUuid());
        List<Claim> claims = playerClaims.getClaims();

        // Filter claims to current world
        List<Claim> worldClaims = claims.stream()
                .filter(c -> c.getWorld().equals(worldName))
                .collect(Collectors.toList());

        if (worldClaims.isEmpty()) {
            playerData.sendMessage(Messages.noClaimsToShow());
            return;
        }

        // Display claim info
        playerData.sendMessage(Messages.showingClaims(worldClaims.size()));
        playerData.sendMessage(Message.raw("Your claims are highlighted on the world map with your unique color.").color(AQUA));

        // List the claims
        for (Claim claim : worldClaims) {
            playerData.sendMessage(Messages.claimEntry(worldName, claim.getChunkX(), claim.getChunkZ()));
        }
    }
}
