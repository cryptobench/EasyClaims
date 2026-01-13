package com.landclaims;

import com.landclaims.commands.ClaimCommand;
import com.landclaims.commands.ClaimHelpCommand;
import com.landclaims.commands.ClaimsCommand;
import com.landclaims.commands.PlaytimeCommand;
import com.landclaims.commands.TrustCommand;
import com.landclaims.commands.UnclaimCommand;
import com.landclaims.commands.UntrustCommand;
import com.landclaims.config.PluginConfig;
import com.landclaims.data.ClaimStorage;
import com.landclaims.data.PlaytimeStorage;
import com.landclaims.listeners.ClaimProtectionListener;
import com.landclaims.managers.ClaimManager;
import com.landclaims.managers.PlaytimeManager;
import com.landclaims.systems.BlockBreakProtectionSystem;
import com.landclaims.systems.BlockDamageProtectionSystem;
import com.landclaims.systems.BlockPlaceProtectionSystem;
import com.landclaims.systems.BlockUseProtectionSystem;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.event.events.ecs.DamageBlockEvent;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;

/**
 * LandClaims - A chunk-based land claiming plugin with playtime-based limits.
 *
 * Features:
 * - Claim chunks to protect your builds
 * - More playtime = more claim chunks available
 * - Trust other players in your claims
 * - Full protection for claimed areas
 */
public class LandClaims extends JavaPlugin {

    private PluginConfig config;
    private ClaimStorage claimStorage;
    private PlaytimeStorage playtimeStorage;
    private ClaimManager claimManager;
    private PlaytimeManager playtimeManager;
    private ClaimProtectionListener protectionListener;

    public LandClaims(JavaPluginInit init) {
        super(init);
    }

    @Override
    public void setup() {
        // Initialize configuration
        config = new PluginConfig(getDataDirectory());

        // Initialize storage
        claimStorage = new ClaimStorage(getDataDirectory());
        playtimeStorage = new PlaytimeStorage(getDataDirectory());

        // Initialize managers
        claimManager = new ClaimManager(claimStorage, playtimeStorage, config);
        playtimeManager = new PlaytimeManager(playtimeStorage, config);

        // Register commands
        getCommandRegistry().registerCommand(new ClaimCommand(this));
        getCommandRegistry().registerCommand(new UnclaimCommand(this));
        getCommandRegistry().registerCommand(new ClaimsCommand(this));
        getCommandRegistry().registerCommand(new TrustCommand(this));
        getCommandRegistry().registerCommand(new UntrustCommand(this));
        getCommandRegistry().registerCommand(new PlaytimeCommand(this));
        getCommandRegistry().registerCommand(new ClaimHelpCommand(this));

        // Register protection event listeners (for PlayerInteractEvent)
        protectionListener = new ClaimProtectionListener(this);
        protectionListener.register(getEventRegistry());

        // Register ECS block protection systems
        getLogger().atInfo().log("Registering ECS block protection systems...");
        try {
            // Register entity event types first - required before systems can receive these events
            getEntityStoreRegistry().registerEntityEventType(DamageBlockEvent.class);
            getEntityStoreRegistry().registerEntityEventType(BreakBlockEvent.class);
            getEntityStoreRegistry().registerEntityEventType(PlaceBlockEvent.class);
            getEntityStoreRegistry().registerEntityEventType(UseBlockEvent.Pre.class);
            getLogger().atInfo().log("Registered entity event types");

            // Now register the systems that handle these events
            getEntityStoreRegistry().registerSystem(new BlockDamageProtectionSystem(claimManager, getLogger()));
            getLogger().atInfo().log("Registered BlockDamageProtectionSystem");
            getEntityStoreRegistry().registerSystem(new BlockBreakProtectionSystem(claimManager, getLogger()));
            getLogger().atInfo().log("Registered BlockBreakProtectionSystem");
            getEntityStoreRegistry().registerSystem(new BlockPlaceProtectionSystem(claimManager, getLogger()));
            getLogger().atInfo().log("Registered BlockPlaceProtectionSystem");
            getEntityStoreRegistry().registerSystem(new BlockUseProtectionSystem(claimManager, getLogger()));
            getLogger().atInfo().log("Registered BlockUseProtectionSystem");
            getLogger().atInfo().log("All ECS systems registered successfully!");
        } catch (Exception e) {
            getLogger().atSevere().withCause(e).log("ERROR registering ECS systems");
        }
    }

    @Override
    public void start() {
        // Plugin started successfully
    }

    @Override
    public void shutdown() {
        // Shutdown playtime manager (saves all sessions)
        if (playtimeManager != null) {
            playtimeManager.shutdown();
        }

        // Save all claim data
        if (claimStorage != null) {
            claimStorage.saveAll();
        }
    }

    public PluginConfig getPluginConfig() {
        return config;
    }

    public ClaimManager getClaimManager() {
        return claimManager;
    }

    public PlaytimeManager getPlaytimeManager() {
        return playtimeManager;
    }

    /**
     * Called when a player joins - start tracking their playtime.
     * Note: This should be hooked into the server's player join event.
     */
    public void onPlayerJoin(java.util.UUID playerId) {
        playtimeManager.onPlayerJoin(playerId);
    }

    /**
     * Called when a player leaves - save their playtime.
     * Note: This should be hooked into the server's player leave event.
     */
    public void onPlayerLeave(java.util.UUID playerId) {
        playtimeManager.onPlayerLeave(playerId);
    }
}
