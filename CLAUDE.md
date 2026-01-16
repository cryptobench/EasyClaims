# EasyClaims Plugin - Development Context

## Project Overview
A chunk-based land claiming plugin for Hytale servers with playtime-based limits, trust system, and anti-griefing buffer zones.

## How to Explore the Hytale Server API

The Hytale server API is in `Server/HytaleServer.jar`. Since there's no official documentation, use these commands to discover available classes and methods:

### List All Classes in a Package
```bash
cd "/Users/golemgrid/Library/Application Support/Hytale/install/release/package/game/latest/Server"

# Find all event-related classes
jar -tf HytaleServer.jar | grep -i "event"

# Find player-related classes
jar -tf HytaleServer.jar | grep -i "player"

# Find block-related classes
jar -tf HytaleServer.jar | grep -i "block"

# Find all classes in a specific package
jar -tf HytaleServer.jar | grep "com/hypixel/hytale/server/core/event/events/"
```

### Inspect a Class (Methods, Fields, Signatures)
```bash
# Basic class inspection
javap -classpath HytaleServer.jar com.hypixel.hytale.server.core.event.events.player.PlayerInteractEvent

# With more detail (private members too)
javap -p -classpath HytaleServer.jar com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent
```

### Example: Discovering Block Events
```bash
# Step 1: Find all ECS events
jar -tf HytaleServer.jar | grep "event/events/ecs"

# Output shows:
# com/hypixel/hytale/server/core/event/events/ecs/BreakBlockEvent.class
# com/hypixel/hytale/server/core/event/events/ecs/PlaceBlockEvent.class
# com/hypixel/hytale/server/core/event/events/ecs/DamageBlockEvent.class
# com/hypixel/hytale/server/core/event/events/ecs/UseBlockEvent.class

# Step 2: Inspect the class to see methods
javap -classpath HytaleServer.jar com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent

# Output shows:
# public class BreakBlockEvent extends CancellableEcsEvent {
#   public Vector3i getTargetBlock();
#   public BlockType getBlockType();
#   public void setCancelled(boolean);
# }
```

### Example: Finding How to Register Systems
```bash
# Find registry-related classes
jar -tf HytaleServer.jar | grep -i "registry"

# Inspect ComponentRegistryProxy (found in PluginBase.getEntityStoreRegistry())
javap -classpath HytaleServer.jar com.hypixel.hytale.component.ComponentRegistryProxy

# Shows methods like:
# registerSystem(ISystem)
# registerEntityEventType(Class)
# registerWorldEventType(Class)
```

### Example: Understanding Event Hierarchy
```bash
# Check if an event implements IBaseEvent (can use EventRegistry)
javap -classpath HytaleServer.jar com.hypixel.hytale.server.core.event.events.player.PlayerInteractEvent
# Shows: implements IBaseEvent, ICancellable  ✓ Can use EventRegistry

javap -classpath HytaleServer.jar com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent
# Shows: extends CancellableEcsEvent  ✗ Cannot use EventRegistry, needs EntityEventSystem
```

### Key Packages to Explore
```
com.hypixel.hytale.server.core.plugin        # JavaPlugin, PluginBase
com.hypixel.hytale.server.core.event.events  # All events
com.hypixel.hytale.server.core.command       # Command system
com.hypixel.hytale.server.core.entity        # Entity/Player classes
com.hypixel.hytale.component                 # ECS system (Store, Ref, Query)
com.hypixel.hytale.component.system          # EntityEventSystem, EcsEvent
com.hypixel.hytale.event                     # EventRegistry
com.hypixel.hytale.math.vector               # Vector3d, Vector3f, Vector3i
com.hypixel.hytale.protocol                  # InteractionType, etc.
```

## Hytale Server Plugin Development

### Plugin Structure
```
plugins/PluginName/
├── src/main/java/com/yourplugin/
│   ├── YourPlugin.java           # Extends JavaPlugin
│   ├── commands/                  # Extend AbstractPlayerCommand
│   ├── listeners/                 # Event handlers
│   ├── systems/                   # ECS EntityEventSystems
│   └── data/                      # Data classes
├── src/main/resources/
│   └── manifest.json              # MUST use PascalCase fields
├── pom.xml
└── target/PluginName-1.0.0.jar   # Goes in Server/mods/
```

### manifest.json (PascalCase Required!)
```json
{
    "Group": "cryptobench",
    "Name": "EasyClaims",
    "Version": "1.0.0",
    "Main": "com.easyclaims.EasyClaims",
    "Description": "Description here",
    "Authors": [],
    "Dependencies": {}
}
```

### Main Plugin Class
```java
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

public class YourPlugin extends JavaPlugin {
    public YourPlugin(JavaPluginInit init) { super(init); }

    @Override
    public void setup() {
        // Register commands, events, systems
        getCommandRegistry().registerCommand(new YourCommand());
        getEventRegistry().registerGlobal(EventType.class, this::handler);
        getEntityStoreRegistry().registerSystem(new YourSystem());
    }

    @Override
    public void start() { }

    @Override
    public void shutdown() { }
}
```

### Available Registries (from PluginBase)
```java
getEventRegistry()           // For IBaseEvent events (PlayerInteractEvent, etc.)
getEntityStoreRegistry()     // For ECS systems and components on entities
getChunkStoreRegistry()      // For ECS systems and components on chunks
getCommandRegistry()         // For commands
getBlockStateRegistry()      // For block states
getEntityRegistry()          // For entity types
getTaskRegistry()            // For scheduled tasks
getDataDirectory()           // Plugin data folder: mods/Group_PluginName/
```

## Two Event Systems in Hytale

### 1. Standard Events (EventRegistry)
Events implementing `IBaseEvent<KeyType>`. Registered via EventRegistry.

```java
// Registration
eventRegistry.registerGlobal(PlayerInteractEvent.class, this::onPlayerInteract);
eventRegistry.register(PlayerConnectEvent.class, this::onConnect);  // Instance-specific

// Handler
private void onPlayerInteract(PlayerInteractEvent event) {
    Player player = event.getPlayer();
    Vector3i block = event.getTargetBlock();
    InteractionType action = event.getActionType();
    event.setCancelled(true);  // Cancel the event
}
```

**Available Player Events:**
- `PlayerInteractEvent` - player interactions (has getPlayer(), getTargetBlock(), getActionType())
- `PlayerConnectEvent` - player joins
- `PlayerDisconnectEvent` - player leaves
- `PlayerChatEvent` - chat messages

### 2. ECS Events (EntityEventSystem)
Events extending `EcsEvent`. **Cannot use EventRegistry** - need EntityEventSystem.

```java
public class MyProtectionSystem extends EntityEventSystem<EntityStore, BreakBlockEvent> {

    public MyProtectionSystem() {
        super(BreakBlockEvent.class);
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Query.any();  // Required - match all entities
    }

    @Override
    public void handle(int entityIndex, ArchetypeChunk<EntityStore> chunk,
                       Store<EntityStore> store, CommandBuffer<EntityStore> commandBuffer,
                       BreakBlockEvent event) {
        Vector3i block = event.getTargetBlock();
        event.setCancelled(true);  // Cancel the event
    }
}

// Registration in plugin setup()
getEntityStoreRegistry().registerSystem(new MyProtectionSystem());
```

**Available ECS Block Events:**
- `BreakBlockEvent` - block destroyed (getTargetBlock(), setCancelled())
- `DamageBlockEvent` - block taking damage (getTargetBlock(), getDamage(), setDamage(), setCancelled())
- `PlaceBlockEvent` - block placed (getTargetBlock(), setCancelled())
- `UseBlockEvent.Pre` - block used/chest opened (getTargetBlock(), getContext(), setCancelled())

**Key Difference:** ECS events don't have player info directly! Must track via PlayerInteractEvent.

## InteractionType Enum
```java
Primary         // Left click - break/attack
Secondary       // Right click - place/use
Use             // Use action
Pick            // Pick block (middle click)
Pickup          // Pick up item
Ability1/2/3    // Abilities
// ... and more
```

## Commands
```java
public class MyCommand extends AbstractPlayerCommand {
    private final OptionalArg<String> arg;

    public MyCommand() {
        super("commandname", "Description");
        this.arg = withOptionalArg("argname", "desc", ArgTypes.STRING);
        // Or: withRequiredArg(...)
        requirePermission("myplugin.use");
    }

    @Override
    protected void execute(CommandContext ctx, Store<EntityStore> store,
                          Ref<EntityStore> playerRef, PlayerRef playerData, World world) {
        String value = arg.get(ctx);
        Player player = store.getComponent(playerRef, Player.getComponentType());
        playerData.sendMessage(Message.raw("Hello").color(new Color(85, 255, 85)));
    }
}
```

## Permissions System

### Adding Permission Requirements to Commands
Use `requirePermission()` in command constructors to require a permission:
```java
public class MyCommand extends AbstractPlayerCommand {
    public MyCommand() {
        super("mycommand", "Description");
        requirePermission("myplugin.use");  // Players need this permission to use the command
    }
}
```

### Checking Permissions Manually
```java
// On Player object (in command execute methods)
Player player = store.getComponent(playerRef, Player.getComponentType());
if (player.hasPermission("myplugin.admin")) {
    // Do admin stuff
}

// Via PermissionsModule (when you only have UUID)
import com.hypixel.hytale.server.core.permissions.PermissionsModule;

if (PermissionsModule.get().hasPermission(playerData.getUuid(), "myplugin.admin")) {
    // Do admin stuff
}
```

### Managing Permissions (Server Console Commands)
```bash
# Add permission to a group
perm group add <GroupName> <permission>
perm group add Adventure easyclaims.use
perm group add Adventure easyclaims.admin

# Remove permission from a group
perm group remove <GroupName> <permission>

# List group permissions
perm group list <GroupName>

# Add user to a group
perm user group add <username> <GroupName>

# Add permission directly to user
perm user add <username> <permission>

# Test if you have a permission
perm test <permission>
```

### Permission Format & Wildcards
```
myplugin.use          # Exact permission
myplugin.*            # Wildcard - grants all myplugin.* permissions
*                     # All permissions (admin/OP)
-myplugin.use         # Negates/denies a specific permission
-*                    # Denies all permissions
```

### permissions.json Structure
Located at `Server/permissions.json`:
```json
{
  "users": {
    "uuid-here": {
      "groups": ["Adventure", "Builder"]
    }
  },
  "groups": {
    "Default": [],
    "Adventure": ["easyclaims.use"],
    "OP": ["*"]
  }
}
```

**Important:** Group names are case-sensitive!

### EasyClaims Permissions
| Permission | Description |
|------------|-------------|
| `easyclaims.use` | Basic access to /easyclaims command |
| `easyclaims.admin` | Admin commands (config, set, reload, unclaim) |

## Messages (No Minecraft Color Codes!)
```java
import com.hypixel.hytale.server.core.Message;
import java.awt.Color;

// Correct
playerData.sendMessage(Message.raw("Success!").color(new Color(85, 255, 85)));

// WRONG - shows literal "§a"
playerData.sendMessage(Message.raw("§aSuccess!"));

// Common colors
Color GREEN = new Color(85, 255, 85);
Color RED = new Color(255, 85, 85);
Color YELLOW = new Color(255, 255, 85);
Color GOLD = new Color(255, 170, 0);
Color GRAY = new Color(170, 170, 170);
```

## Player Position & Teleportation
```java
// Get position
TransformComponent transform = store.getComponent(playerRef, TransformComponent.getComponentType());
Vector3d position = transform.getPosition();
Vector3f rotation = transform.getRotation();

// Teleport (MUST run on world thread)
world.execute(() -> {
    Vector3d pos = new Vector3d(x, y, z);
    Vector3f rot = new Vector3f(yaw, pitch, 0);
    Teleport teleport = new Teleport(world, pos, rot);
    store.addComponent(playerRef, Teleport.getComponentType(), teleport);
});
```

## Data Storage
```java
Path dataDir = getDataDirectory();  // mods/Group_PluginName/
Gson gson = new GsonBuilder().setPrettyPrinting().create();  // Gson provided by Hytale
```

## Building & Installation
```bash
mvn clean package
# Copy target/PluginName-1.0.0.jar to Server/mods/
```

## Git Commits & Release Notes

**IMPORTANT:** The CI workflow automatically creates GitHub releases from commits to master/main. The commit message is used as the release notes in the "What's Changed" section.

### Commit Message Requirements
When committing changes, write descriptive commit messages that serve as release notes:

```
<Short summary of change>

<Detailed description of what changed and why>
- List specific changes
- Include any breaking changes
- Mention new features or bug fixes
```

### Example Good Commit Message
```
Add item pickup protection for claimed areas

Protect flowers, bottles, and other harvestable items from being picked up
by untrusted players in claimed chunks.

- Add ItemPickupProtectionSystem for InteractivelyPickupItemEvent
- Add cannotPickupItemsHere() message
- Register system in main plugin class
```

### Why This Matters
- Each push to master triggers a new release
- The commit message becomes the release notes
- Users see "What's Changed" in the GitHub release
- Good commit messages = good release documentation

## Key Imports
```java
// Plugin
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

// Events
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.server.core.event.events.player.*;
import com.hypixel.hytale.server.core.event.events.ecs.*;

// ECS Systems
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

// Commands
import com.hypixel.hytale.server.core.command.system.*;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;

// Entity/Player
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;

// Math
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;

// Messages
import com.hypixel.hytale.server.core.Message;

// Permissions
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
```