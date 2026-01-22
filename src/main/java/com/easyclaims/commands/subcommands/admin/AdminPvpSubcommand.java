package com.easyclaims.commands.subcommands.admin;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.easyclaims.EasyClaims;
import com.easyclaims.data.Claim;
import com.easyclaims.util.Messages;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * Admin command to toggle PvP in any claim.
 * Admins can override PvP settings regardless of server mode.
 * Usage: /easyclaims admin pvp [on/off]
 */
public class AdminPvpSubcommand extends AbstractPlayerCommand {
    private final EasyClaims plugin;

    public AdminPvpSubcommand(EasyClaims plugin) {
        super("pvp", "Toggle PvP in the current claim");
        this.plugin = plugin;
        setAllowsExtraArguments(true); // Allow optional positional on/off argument
        requirePermission("easyclaims.admin");
    }

    @Override
    protected void execute(@Nonnull CommandContext ctx,
                          @Nonnull Store<EntityStore> store,
                          @Nonnull Ref<EntityStore> playerRef,
                          @Nonnull PlayerRef playerData,
                          @Nonnull World world) {
        TransformComponent transform = store.getComponent(playerRef, TransformComponent.getComponentType());
        Vector3d position = transform.getPosition();
        String worldName = world.getName();

        // Get the claim at current location
        Claim claim = plugin.getClaimManager().getClaimAt(worldName, position.getX(), position.getZ());
        if (claim == null) {
            playerData.sendMessage(Messages.notInClaim());
            return;
        }

        // Get optional state from raw input (everything after "pvp")
        String state = null;
        String input = ctx.getInputString();
        int pvpIndex = input.toLowerCase().lastIndexOf("pvp");
        if (pvpIndex >= 0) {
            String afterPvp = input.substring(pvpIndex + 3).trim();
            if (!afterPvp.isEmpty()) {
                state = afterPvp.split("\\s+")[0]; // Take first word only
            }
        }

        // Determine new state
        boolean newPvpEnabled;

        if (state == null || state.isEmpty()) {
            // Toggle
            newPvpEnabled = !claim.isPvpEnabled();
        } else if (state.equalsIgnoreCase("on") || state.equalsIgnoreCase("true") || state.equals("1")) {
            newPvpEnabled = true;
        } else if (state.equalsIgnoreCase("off") || state.equalsIgnoreCase("false") || state.equals("0")) {
            newPvpEnabled = false;
        } else {
            playerData.sendMessage(Messages.helpEntry("easyclaims admin pvp", "on/off - Toggle PvP"));
            return;
        }

        // Check if already in desired state
        if (newPvpEnabled == claim.isPvpEnabled()) {
            if (newPvpEnabled) {
                playerData.sendMessage(Messages.pvpAlreadyEnabled());
            } else {
                playerData.sendMessage(Messages.pvpAlreadyDisabled());
            }
            return;
        }

        // Update claim
        claim.setPvpEnabled(newPvpEnabled);

        // Save the change
        UUID owner = plugin.getClaimManager().getOwnerAt(worldName, position.getX(), position.getZ());
        if (owner != null) {
            plugin.getClaimStorage().updatePlayerClaims(owner);
        }

        // Refresh map to show new PvP status
        int chunkX = claim.getChunkX();
        int chunkZ = claim.getChunkZ();
        plugin.refreshWorldMapChunk(worldName, chunkX, chunkZ);

        // Notify
        if (newPvpEnabled) {
            playerData.sendMessage(Messages.pvpEnabled());
        } else {
            playerData.sendMessage(Messages.pvpDisabled());
        }
    }
}
