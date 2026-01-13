package com.landclaims.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.landclaims.LandClaims;
import com.landclaims.data.PlayerClaims;
import com.landclaims.data.TrustLevel;
import com.landclaims.util.Messages;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * /trust <player> [level] - Trust a player in all your claims.
 * Accepts player name (for online players) or UUID (for offline players).
 * Trust levels: use, container, workstation, damage, build (default)
 */
public class TrustCommand extends AbstractPlayerCommand {
    private final LandClaims plugin;
    private final RequiredArg<String> playerArg;
    private final OptionalArg<String> levelArg;

    public TrustCommand(LandClaims plugin) {
        super("trust", "Trust a player in all your claims");
        this.plugin = plugin;
        this.playerArg = withRequiredArg("player", "Player name or UUID to trust", ArgTypes.STRING);
        this.levelArg = withOptionalArg("level", "Trust level: use, container, workstation, damage, build", ArgTypes.STRING);
        requirePermission("landclaims.trust");
    }

    @Override
    protected void execute(@Nonnull CommandContext ctx,
                          @Nonnull Store<EntityStore> store,
                          @Nonnull Ref<EntityStore> playerRef,
                          @Nonnull PlayerRef playerData,
                          @Nonnull World world) {

        String playerInput = playerArg.get(ctx);
        String levelInput = levelArg.get(ctx);

        if (playerInput == null || playerInput.isEmpty()) {
            playerData.sendMessage(Messages.playerNotFound("unknown"));
            return;
        }

        // Parse trust level (default to BUILD)
        TrustLevel level = TrustLevel.BUILD;
        if (levelInput != null && !levelInput.isEmpty()) {
            level = TrustLevel.fromString(levelInput);
            if (level == null || level == TrustLevel.NONE) {
                playerData.sendMessage(Messages.invalidTrustLevel(levelInput));
                playerData.sendMessage(Messages.trustLevelHelp());
                return;
            }
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
            // Not online - try to parse as UUID for offline players
            try {
                targetId = UUID.fromString(playerInput);
            } catch (IllegalArgumentException e) {
                // Not a valid UUID and player not online
                playerData.sendMessage(Messages.playerNotOnline(playerInput));
                return;
            }
        }

        if (targetId.equals(playerData.getUuid())) {
            playerData.sendMessage(Messages.cannotTrustSelf());
            return;
        }

        PlayerClaims claims = plugin.getClaimManager().getPlayerClaims(playerData.getUuid());
        TrustLevel existingLevel = claims.getTrustLevel(targetId);

        if (existingLevel == level) {
            playerData.sendMessage(Messages.playerAlreadyTrusted(targetName, level));
            return;
        }

        boolean isUpdate = existingLevel != TrustLevel.NONE;
        plugin.getClaimManager().addTrust(playerData.getUuid(), targetId, targetName, level);

        if (isUpdate) {
            playerData.sendMessage(Messages.playerTrustUpdated(targetName, level));
        } else {
            playerData.sendMessage(Messages.playerTrusted(targetName, level));
        }
    }
}
