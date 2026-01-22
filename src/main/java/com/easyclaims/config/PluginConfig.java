package com.easyclaims.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Configuration manager for EasyClaims plugin.
 * Supports in-game configuration via /easyclaims command.
 */
public class PluginConfig {
    private final Path configFile;
    private final Gson gson;
    private ConfigData config;

    public PluginConfig(Path dataDirectory) {
        this.configFile = dataDirectory.resolve("config.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.config = new ConfigData();
        load();
    }

    /**
     * Load configuration from file, with migration from old field names.
     */
    public void load() {
        if (Files.exists(configFile)) {
            try {
                String json = Files.readString(configFile);
                JsonObject obj = JsonParser.parseString(json).getAsJsonObject();

                // Migrate old field names to new ones
                boolean needsMigration = false;

                if (obj.has("startingChunks") && !obj.has("startingClaims")) {
                    obj.addProperty("startingClaims", obj.get("startingChunks").getAsInt());
                    obj.remove("startingChunks");
                    needsMigration = true;
                }
                if (obj.has("chunksPerHour") && !obj.has("claimsPerHour")) {
                    obj.addProperty("claimsPerHour", obj.get("chunksPerHour").getAsInt());
                    obj.remove("chunksPerHour");
                    needsMigration = true;
                }
                if (obj.has("maxClaimsPerPlayer") && !obj.has("maxClaims")) {
                    obj.addProperty("maxClaims", obj.get("maxClaimsPerPlayer").getAsInt());
                    obj.remove("maxClaimsPerPlayer");
                    needsMigration = true;
                }
                if (obj.has("playtimeUpdateIntervalSeconds") && !obj.has("playtimeSaveInterval")) {
                    obj.addProperty("playtimeSaveInterval", obj.get("playtimeUpdateIntervalSeconds").getAsInt());
                    obj.remove("playtimeUpdateIntervalSeconds");
                    needsMigration = true;
                }
                if (obj.has("allowPlayerPvpToggle") && !obj.has("pvpInPlayerClaims")) {
                    // Migration: old allowPlayerPvpToggle=true meant PvE (players could disable PvP)
                    // New pvpInPlayerClaims: true=PvP server, false=PvE server
                    // So we invert the value: allowPlayerPvpToggle=true -> pvpInPlayerClaims=false
                    obj.addProperty("pvpInPlayerClaims", !obj.get("allowPlayerPvpToggle").getAsBoolean());
                    obj.remove("allowPlayerPvpToggle");
                    needsMigration = true;
                }

                // Parse the (possibly migrated) config
                ConfigData loaded = gson.fromJson(obj, ConfigData.class);
                if (loaded != null) {
                    config = loaded;
                }

                // Save migrated config with new field names
                if (needsMigration) {
                    save();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            save();
        }
    }

    /**
     * Reload configuration from file.
     */
    public void reload() {
        load();
    }

    /**
     * Save current configuration to file.
     */
    public void save() {
        try {
            Files.createDirectories(configFile.getParent());
            Files.writeString(configFile, gson.toJson(config));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ===== GETTERS =====

    public double getClaimsPerHour() {
        return config.claimsPerHour;
    }

    public int getStartingClaims() {
        return config.startingClaims;
    }

    public int getMaxClaims() {
        return config.maxClaims;
    }

    public int getPlaytimeSaveInterval() {
        return config.playtimeSaveInterval;
    }

    public int getClaimBufferSize() {
        return config.claimBufferSize;
    }

    /**
     * Whether PvP is enabled in player claims.
     * true = PvP server (fighting allowed in player claims)
     * false = PvE server (no fighting in player claims)
     * Note: Admin claims have their own per-claim setting.
     */
    public boolean isPvpInPlayerClaims() {
        return config.pvpInPlayerClaims;
    }

    // ===== SETTERS (auto-save) =====

    public void setClaimsPerHour(double value) {
        config.claimsPerHour = Math.max(0.0, value);
        save();
    }

    public void setStartingClaims(int value) {
        config.startingClaims = Math.max(0, value);
        save();
    }

    public void setMaxClaims(int value) {
        config.maxClaims = Math.max(1, value);
        save();
    }

    public void setPlaytimeSaveInterval(int value) {
        config.playtimeSaveInterval = Math.max(10, value);
        save();
    }

    public void setClaimBufferSize(int value) {
        config.claimBufferSize = Math.max(0, value);  // 0 = disabled
        save();
    }

    public void setPvpInPlayerClaims(boolean value) {
        config.pvpInPlayerClaims = value;
        save();
    }

    // ===== LEGACY GETTERS (for compatibility) =====

    /** @deprecated Use getClaimsPerHour() */
    public double getChunksPerHour() {
        return getClaimsPerHour();
    }

    /** @deprecated Use getStartingClaims() */
    public int getStartingChunks() {
        return getStartingClaims();
    }

    /** @deprecated Use getMaxClaims() */
    public int getMaxClaimsPerPlayer() {
        return getMaxClaims();
    }

    /** @deprecated Use getPlaytimeSaveInterval() */
    public int getPlaytimeUpdateIntervalSeconds() {
        return getPlaytimeSaveInterval();
    }

    // ===== CALCULATION METHODS =====

    /**
     * Calculate how many chunks a player can claim based on their playtime.
     */
    public int calculateMaxClaims(double playtimeHours) {
        int fromPlaytime = (int) (playtimeHours * config.claimsPerHour);
        int total = config.startingClaims + fromPlaytime;
        return Math.min(total, config.maxClaims);
    }

    /**
     * Calculate hours needed to unlock the next claim.
     */
    public double hoursUntilNextClaim(double currentHours, int currentClaims) {
        if (currentClaims >= config.maxClaims) {
            return -1; // Already at max
        }

        int fromPlaytime = currentClaims - config.startingClaims;
        if (fromPlaytime < 0) {
            return 0; // Still have starting claims available
        }

        // Guard against division by zero
        if (config.claimsPerHour <= 0) {
            return -1; // No claims earned through playtime
        }

        double hoursNeeded = (fromPlaytime + 1) / config.claimsPerHour;
        return Math.max(0, hoursNeeded - currentHours);
    }

    /**
     * Configuration data structure for JSON serialization.
     * Uses clear, user-friendly field names.
     */
    private static class ConfigData {
        int startingClaims = 4;
        double claimsPerHour = 2.0;
        int maxClaims = 50;
        int playtimeSaveInterval = 60;
        int claimBufferSize = 2;  // Buffer zone in chunks around claims where others can't claim
        boolean pvpInPlayerClaims = true;  // true = PvP server, false = PvE server
    }
}
