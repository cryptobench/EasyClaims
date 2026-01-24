package com.easyclaims.commands.subcommands.admin.grant;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.easyclaims.EasyClaims;

/**
 * Admin grant command collection for managing player claim allowances.
 * All grant commands are additive (running twice adds more).
 *
 * Usage:
 *   /easyclaims admin grant claims <player> <amount>     - Add bonus claim slots
 *   /easyclaims admin grant maxclaims <player> <amount>  - Add to max claims cap
 *   /easyclaims admin grant maxclaims <player> unlimited - Set unlimited claims
 */
public class AdminGrantSubcommand extends AbstractCommandCollection {

    public AdminGrantSubcommand(EasyClaims plugin) {
        super("grant", "Grant bonus claims to players");
        requirePermission("easyclaims.admin");

        // Register grant subcommands
        addSubCommand(new GrantClaimsSubcommand(plugin));
        addSubCommand(new GrantMaxClaimsSubcommand(plugin));
    }
}
