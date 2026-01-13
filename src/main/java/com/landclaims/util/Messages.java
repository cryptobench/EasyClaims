package com.landclaims.util;

import com.hypixel.hytale.server.core.Message;
import com.landclaims.data.TrustLevel;

import java.awt.Color;

/**
 * Centralized message formatting for LandClaims.
 */
public class Messages {
    // Colors
    private static final Color GREEN = new Color(85, 255, 85);
    private static final Color RED = new Color(255, 85, 85);
    private static final Color YELLOW = new Color(255, 255, 85);
    private static final Color GOLD = new Color(255, 170, 0);
    private static final Color GRAY = new Color(170, 170, 170);
    private static final Color AQUA = new Color(85, 255, 255);
    private static final Color WHITE = new Color(255, 255, 255);

    // Claim messages
    public static Message chunkClaimed(int chunkX, int chunkZ) {
        return Message.raw("Claimed chunk at [" + chunkX + ", " + chunkZ + "]!").color(GREEN);
    }

    public static Message chunkUnclaimed(int chunkX, int chunkZ) {
        return Message.raw("Unclaimed chunk at [" + chunkX + ", " + chunkZ + "]!").color(GREEN);
    }

    public static Message chunkAlreadyClaimed() {
        return Message.raw("This chunk is already claimed!").color(RED);
    }

    public static Message chunkClaimedByOther(String ownerName) {
        return Message.raw("This chunk is claimed by " + ownerName + "!").color(RED);
    }

    public static Message notYourClaim() {
        return Message.raw("This chunk is not claimed by you!").color(RED);
    }

    public static Message notInClaim() {
        return Message.raw("You are not standing in a claimed chunk!").color(RED);
    }

    public static Message claimLimitReached(int current, int max) {
        return Message.raw("You've reached your claim limit (" + current + "/" + max + ")!").color(RED);
    }

    public static Message notEnoughPlaytime(double hoursNeeded) {
        return Message.raw("You need " + String.format("%.1f", hoursNeeded) + " more hours of playtime to claim another chunk!").color(RED);
    }

    public static Message alreadyOwnChunk() {
        return Message.raw("You already own this chunk!").color(YELLOW);
    }

    public static Message chunkOwnedByOther(String ownerName) {
        return Message.raw("This chunk is claimed by " + ownerName + "!").color(RED);
    }

    public static Message claimCount(int current, int max) {
        return Message.raw("Claims: " + current + "/" + max).color(GRAY);
    }

    public static Message hoursUntilNextClaim(double hours) {
        return Message.raw("Play " + String.format("%.1f", hours) + " more hours to unlock another claim slot.").color(GRAY);
    }

    // Claims list
    public static Message claimsHeader(int count, int max) {
        return Message.raw("Your claims (" + count + "/" + max + "):").color(GOLD);
    }

    public static Message claimEntry(String world, int chunkX, int chunkZ) {
        return Message.raw("- " + world + " [" + chunkX + ", " + chunkZ + "]").color(GRAY);
    }

    public static Message noClaims() {
        return Message.raw("You don't have any claims. Use /claim to claim the chunk you're standing in!").color(YELLOW);
    }

    // Trust messages
    public static Message playerTrusted(String playerName, TrustLevel level) {
        return Message.raw("Trusted " + playerName + " with " + level.getDescription() + " in all your claims!").color(GREEN);
    }

    public static Message playerTrusted(String playerName) {
        return Message.raw("Trusted " + playerName + " in all your claims!").color(GREEN);
    }

    public static Message playerTrustUpdated(String playerName, TrustLevel level) {
        return Message.raw("Updated " + playerName + "'s trust to " + level.getDescription() + "!").color(GREEN);
    }

    public static Message playerUntrusted(String playerName) {
        return Message.raw("Removed trust from " + playerName + "!").color(GREEN);
    }

    public static Message playerNotTrusted(String playerName) {
        return Message.raw(playerName + " is not trusted in your claims!").color(RED);
    }

    public static Message playerAlreadyTrusted(String playerName, TrustLevel level) {
        return Message.raw(playerName + " already has " + level.getDescription() + " trust!").color(YELLOW);
    }

    public static Message playerAlreadyTrusted(String playerName) {
        return Message.raw(playerName + " is already trusted in your claims!").color(YELLOW);
    }

    public static Message cannotTrustSelf() {
        return Message.raw("You cannot trust yourself!").color(RED);
    }

    public static Message invalidTrustLevel(String input) {
        return Message.raw("Invalid trust level: " + input + ". Use: " + TrustLevel.getAvailableLevels()).color(RED);
    }

    public static Message trustLevelHelp() {
        return Message.raw("Trust levels: use (doors), container (chests), workstation (crafting), damage, build (full)").color(GRAY);
    }

    public static Message trustedPlayersHeader(int count) {
        return Message.raw("Trusted players (" + count + "):").color(GOLD);
    }

    public static Message trustedPlayerEntry(String playerName, TrustLevel level) {
        return Message.raw("- " + playerName + " [" + level.getDescription() + "]").color(GRAY);
    }

    public static Message trustedPlayerEntry(String playerName) {
        return Message.raw("- " + playerName).color(GRAY);
    }

    public static Message noTrustedPlayers() {
        return Message.raw("You haven't trusted anyone. Use /trust <player> [level] to trust someone!").color(YELLOW);
    }

    // Playtime messages
    public static Message playtimeInfo(double hours, int claimsUsed, int claimsAvailable) {
        return Message.raw("Playtime: " + String.format("%.1f", hours) + " hours | Claims: " + claimsUsed + "/" + claimsAvailable).color(AQUA);
    }

    public static Message nextClaimIn(double hoursUntilNext) {
        return Message.raw("Next claim available in " + String.format("%.1f", hoursUntilNext) + " hours").color(GRAY);
    }

    // Player lookup
    public static Message playerNotFound(String playerName) {
        return Message.raw("Player '" + playerName + "' not found!").color(RED);
    }

    public static Message playerNotOnline(String playerName) {
        return Message.raw("Player '" + playerName + "' is not online. Use their UUID for offline players.").color(RED);
    }

    // Help messages
    public static Message helpHeader() {
        return Message.raw("=== LandClaims Help ===").color(GOLD);
    }

    public static Message helpEntry(String command, String description) {
        return Message.raw("/" + command + " - " + description).color(GRAY);
    }

    // Protection messages
    public static Message cannotBuildHere() {
        return Message.raw("You cannot build in this claimed area!").color(RED);
    }

    public static Message cannotInteractHere() {
        return Message.raw("You cannot interact in this claimed area!").color(RED);
    }

    public static Message cannotDamageHere() {
        return Message.raw("You cannot damage blocks in this claimed area!").color(RED);
    }

    public static Message cannotUseBlock(TrustLevel required) {
        String action = switch (required) {
            case USE -> "use this";
            case CONTAINER -> "open containers";
            case WORKSTATION -> "use workstations";
            case DAMAGE -> "damage blocks";
            case BUILD -> "build";
            default -> "interact";
        };
        return Message.raw("You don't have permission to " + action + " in this claimed area!").color(RED);
    }

    // Unclaim all messages
    public static Message allClaimsRemoved(int count) {
        return Message.raw("Removed all " + count + " claims!").color(GREEN);
    }

    public static Message noClaimsToRemove() {
        return Message.raw("You don't have any claims to remove!").color(YELLOW);
    }

    // Claims list with coordinates
    public static Message claimEntryWithCoords(String world, int chunkX, int chunkZ) {
        int blockX = chunkX * 16;
        int blockZ = chunkZ * 16;
        return Message.raw("- " + world + " chunk [" + chunkX + ", " + chunkZ + "] (blocks " + blockX + " to " + (blockX + 15) + ", " + blockZ + " to " + (blockZ + 15) + ")").color(GRAY);
    }

    // Preview messages
    public static Message previewUnclaimed(int chunkX, int chunkZ) {
        return Message.raw("Previewing chunk [" + chunkX + ", " + chunkZ + "] - UNCLAIMED. Use /claim to claim it!").color(GREEN);
    }

    public static Message previewOwnClaim(int chunkX, int chunkZ) {
        return Message.raw("Previewing chunk [" + chunkX + ", " + chunkZ + "] - You own this claim.").color(AQUA);
    }

    public static Message previewOtherClaim(int chunkX, int chunkZ, String ownerName) {
        return Message.raw("Previewing chunk [" + chunkX + ", " + chunkZ + "] - Claimed by " + ownerName).color(YELLOW);
    }

    public static Message showingClaims(int count) {
        return Message.raw("Showing " + count + " claim(s). Use /hideclaims to hide.").color(GREEN);
    }

    public static Message claimsHidden() {
        return Message.raw("Claim visualization hidden.").color(GRAY);
    }

    public static Message noClaimsToShow() {
        return Message.raw("You don't have any claims to show!").color(YELLOW);
    }

    public static Message showingSingleClaim(int chunkX, int chunkZ) {
        return Message.raw("Showing claim at [" + chunkX + ", " + chunkZ + "]. Use /hideclaims to hide.").color(GREEN);
    }

    // Wand selection messages
    public static Message pos1Set(int x, int y, int z) {
        return Message.raw("Position 1 set to [" + x + ", " + y + ", " + z + "]").color(AQUA);
    }

    public static Message pos2Set(int x, int y, int z) {
        return Message.raw("Position 2 set to [" + x + ", " + y + ", " + z + "]").color(AQUA);
    }

    public static Message selectionInfo(int chunkCount) {
        return Message.raw("Selection covers " + chunkCount + " chunk(s). Use /claim to claim them.").color(GRAY);
    }

    public static Message selectionCleared() {
        return Message.raw("Selection cleared.").color(GRAY);
    }

    public static Message noSelection() {
        return Message.raw("No selection! Use the claim wand (stick) to select an area first.").color(RED);
    }

    public static Message incompleteSelection() {
        return Message.raw("Selection incomplete! Set both positions with the claim wand.").color(RED);
    }

    public static Message claimingChunks(int count) {
        return Message.raw("Claiming " + count + " chunk(s)...").color(YELLOW);
    }

    public static Message chunksClaimedSuccess(int claimed, int total) {
        if (claimed == total) {
            return Message.raw("Successfully claimed " + claimed + " chunk(s)!").color(GREEN);
        } else {
            return Message.raw("Claimed " + claimed + " of " + total + " chunk(s).").color(YELLOW);
        }
    }

    public static Message chunkSkippedAlreadyOwn(int chunkX, int chunkZ) {
        return Message.raw("  Skipped [" + chunkX + ", " + chunkZ + "] - already yours").color(GRAY);
    }

    public static Message chunkSkippedOtherOwner(int chunkX, int chunkZ) {
        return Message.raw("  Skipped [" + chunkX + ", " + chunkZ + "] - claimed by another player").color(RED);
    }

    public static Message wandGiven() {
        return Message.raw("You received a claim wand! Left-click to set pos1, right-click to set pos2.").color(GREEN);
    }

    public static Message wandAlreadyHave() {
        return Message.raw("You already have a claim wand in your inventory!").color(YELLOW);
    }

    public static Message tooManyChunksInSelection(int selected, int available) {
        return Message.raw("Selection has " + selected + " chunks but you can only claim " + available + " more!").color(RED);
    }

    public static Message wandUsageTip() {
        return Message.raw("Tip: Left-click = pos1, Right-click = pos2, then /claim").color(GRAY);
    }

    public static Message selectionToolTip() {
        return Message.raw("Tip: Use the selection tool to select an area, then /claim to claim all chunks in it.").color(GRAY);
    }

    // Claim mode messages
    public static Message claimModeEnabled() {
        return Message.raw("Claim mode enabled! Left-click = pos1, Right-click = pos2").color(GREEN);
    }

    public static Message claimModeInstructions() {
        return Message.raw("Select an area, then run /claim again to confirm. Use /claim cancel to exit.").color(GRAY);
    }

    public static Message claimModeCancelled() {
        return Message.raw("Claim mode cancelled.").color(YELLOW);
    }

    public static Message claimModeNoSelection() {
        return Message.raw("No selection made! Click blocks to set corners, or /claim cancel to exit.").color(RED);
    }

    public static Message claimModeIncomplete() {
        return Message.raw("Selection incomplete! Set both corners first. (Left-click = pos1, Right-click = pos2)").color(RED);
    }
}
