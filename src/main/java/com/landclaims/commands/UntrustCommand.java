package com.landclaims.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.landclaims.LandClaims;
import com.landclaims.data.PlayerClaims;
import com.landclaims.util.Messages;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * /untrust <player> - Remove trust from a player.
 * Accepts player name (for online players) or UUID (for offline players).
 */
public class UntrustCommand extends AbstractPlayerCommand {
    private final LandClaims plugin;
    private final RequiredArg<String> playerArg;

    public UntrustCommand(LandClaims plugin) {
        super("untrust", "Remove trust from a player");
        this.plugin = plugin;
        this.playerArg = withRequiredArg("player", "Player name or UUID to untrust", ArgTypes.STRING);
        requirePermission("landclaims.untrust");
    }

    @Override
    protected void execute(@Nonnull CommandContext ctx,
                          @Nonnull Store<EntityStore> store,
                          @Nonnull Ref<EntityStore> playerRef,
                          @Nonnull PlayerRef playerData,
                          @Nonnull World world) {

        String playerInput = playerArg.get(ctx);

        if (playerInput == null || playerInput.isEmpty()) {
            playerData.sendMessage(Messages.playerNotFound("unknown"));
            return;
        }

        UUID targetId = null;
        String targetName = playerInput;

        // First, try to find an online player by name
        PlayerRef targetPlayer = Universe.get().getPlayerByUsername(playerInput, NameMatching.EXACT_IGNORE_CASE);

        if (targetPlayer != null) {
            // Found online player
            targetId = targetPlayer.getUuid();
            targetName = targetPlayer.getUsername();
        } else {
            // Not online - check if it matches a stored trusted player name
            PlayerClaims claims = plugin.getClaimManager().getPlayerClaims(playerData.getUuid());
            UUID foundId = claims.getTrustedPlayerByName(playerInput);

            if (foundId != null) {
                targetId = foundId;
                String storedName = claims.getTrustedPlayerName(foundId);
                if (storedName != null) {
                    targetName = storedName;
                }
            } else {
                // Try to parse as UUID for offline players
                try {
                    targetId = UUID.fromString(playerInput);
                } catch (IllegalArgumentException e) {
                    // Not a valid UUID and not a known trusted player
                    playerData.sendMessage(Messages.playerNotTrusted(playerInput));
                    return;
                }
            }
        }

        String removedName = plugin.getClaimManager().removeTrust(playerData.getUuid(), targetId);

        if (removedName != null) {
            playerData.sendMessage(Messages.playerUntrusted(removedName));
        } else {
            playerData.sendMessage(Messages.playerNotTrusted(targetName));
        }
    }
}
