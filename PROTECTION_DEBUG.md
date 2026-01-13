# LandClaims Block Protection - Debug Notes

## Problem Statement
Untrusted players can still:
- Break blocks in claimed chunks
- Place blocks in claimed chunks
- Open chests in claimed chunks

## Hytale Server API Discovery

### Event Systems
There are TWO separate event systems in Hytale:

1. **Standard Events** (`com.hypixel.hytale.event`)
   - Implement `IBaseEvent<KeyType>`
   - Registered via `EventRegistry.registerGlobal(Class, Consumer)`
   - Examples: `PlayerInteractEvent`, `PlayerConnectEvent`, `PlayerDisconnectEvent`
   - These CAN be cancelled with `event.setCancelled(true)`

2. **ECS Events** (`com.hypixel.hytale.component.system`)
   - Extend `EcsEvent` (does NOT implement `IBaseEvent`)
   - Examples: `BreakBlockEvent`, `PlaceBlockEvent`, `DamageBlockEvent`, `UseBlockEvent.Pre`
   - These are cancellable via `ICancellableEcsEvent.setCancelled(true)`
   - **CANNOT be registered via EventRegistry** - need `EntityEventSystem`

### Key Classes Discovered

```
PlayerInteractEvent (player events - works with EventRegistry)
├── getPlayer() -> Player
├── getTargetBlock() -> Vector3i
├── getActionType() -> InteractionType (Primary, Secondary, Use, etc.)
├── setCancelled(boolean)
└── implements IBaseEvent<String>, ICancellable

BreakBlockEvent (ECS events - needs EntityEventSystem)
├── getTargetBlock() -> Vector3i
├── getBlockType() -> BlockType
├── setCancelled(boolean)
└── extends CancellableEcsEvent (NOT IBaseEvent)

DamageBlockEvent (ECS)
├── getTargetBlock() -> Vector3i
├── getDamage() / setDamage(float)
├── setCancelled(boolean)
└── extends CancellableEcsEvent

PlaceBlockEvent (ECS)
├── getTargetBlock() -> Vector3i
├── setCancelled(boolean)
└── extends CancellableEcsEvent

UseBlockEvent.Pre (ECS - for chests, doors, etc.)
├── getTargetBlock() -> Vector3i
├── getContext() -> InteractionContext
├── setCancelled(boolean)
└── extends UseBlockEvent, implements ICancellableEcsEvent
```

### InteractionType Enum Values
```java
Primary         // Left click (break/attack)
Secondary       // Right click (place/use)
Use             // Use action
Pick            // Pick block
Pickup          // Pick up item
// ... and many more
```

## Approaches Tried

### Approach 1: PlayerInteractEvent Only (Original)
**File:** `ClaimProtectionListener.java`

```java
eventRegistry.registerGlobal(PlayerInteractEvent.class, this::onPlayerInteract);

private void onPlayerInteract(PlayerInteractEvent event) {
    // Check claim, cancel if protected
    if (!claimManager.canInteract(playerId, worldName, x, z)) {
        event.setCancelled(true);
    }
}
```

**Result:** ❌ Did not prevent block breaking/placing. PlayerInteractEvent cancellation may not stop subsequent ECS events.

### Approach 2: Register ECS Events via EventRegistry
**Attempted:**
```java
eventRegistry.registerGlobal(BreakBlockEvent.class, this::onBreakBlock);
eventRegistry.registerGlobal(DamageBlockEvent.class, this::onDamageBlock);
eventRegistry.registerGlobal(PlaceBlockEvent.class, this::onPlaceBlock);
eventRegistry.registerGlobal(UseBlockEvent.Pre.class, this::onUseBlock);
```

**Result:** ❌ Does not work. ECS events don't implement `IBaseEvent`, so `registerGlobal` silently fails or throws type errors.

### Approach 3: EntityEventSystem Implementations (Current)
**Files Created:**
- `systems/BlockBreakProtectionSystem.java`
- `systems/BlockDamageProtectionSystem.java`
- `systems/BlockPlaceProtectionSystem.java`
- `systems/BlockUseProtectionSystem.java`

**Implementation:**
```java
public class BlockBreakProtectionSystem extends EntityEventSystem<EntityStore, BreakBlockEvent> {

    public BlockBreakProtectionSystem(ClaimManager claimManager) {
        super(BreakBlockEvent.class);
        this.claimManager = claimManager;
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Query.any();  // Match all entities
    }

    @Override
    public void handle(int entityIndex, ArchetypeChunk<EntityStore> chunk,
                       Store<EntityStore> store, CommandBuffer<EntityStore> commandBuffer,
                       BreakBlockEvent event) {
        // Protection logic
        if (shouldProtect) {
            event.setCancelled(true);
        }
    }
}
```

**Registration:**
```java
// In LandClaims.setup()
getEntityStoreRegistry().registerSystem(new BlockDamageProtectionSystem(claimManager));
getEntityStoreRegistry().registerSystem(new BlockBreakProtectionSystem(claimManager));
getEntityStoreRegistry().registerSystem(new BlockPlaceProtectionSystem(claimManager));
getEntityStoreRegistry().registerSystem(new BlockUseProtectionSystem(claimManager));
```

**Result:** ❓ Not yet confirmed working. Systems compile and register, but protection may still not work.

## Player Tracking System

Since ECS events don't contain player information directly, we track players via `PlayerInteractEvent`:

```java
// When PlayerInteractEvent fires, store the player info
Map<String, PlayerInteraction> pendingInteractions; // "x,y,z" -> player data
Map<UUID, PlayerInteraction> playerLastInteraction; // player -> last interaction

// In ECS event handlers, look up who caused the event
PlayerInteraction interaction = ClaimProtectionListener.getInteraction(blockKey);
if (interaction != null) {
    // We know which player did this
} else {
    // Fallback: block all access to claimed chunks
}
```

## Claim Data Structure

**Index file:** `mods/Community_LandClaims/claims/index.json`
```json
{
  "default": {
    "-57,-2": "5864bd32-4d9a-4b11-8ed5-2eeb909f8fd9",
    "-58,-2": "5864bd32-4d9a-4b11-8ed5-2eeb909f8fd9"
  }
}
```

**Per-player claims:** `mods/Community_LandClaims/claims/{uuid}.json`
```json
{
  "claims": [
    {"world": "default", "chunkX": -58, "chunkZ": -2, "claimedAt": 1768331474584}
  ],
  "trustedPlayers": []
}
```

## Chunk Coordinate Math
```java
int chunkX = (int) Math.floor(worldX / 16);
int chunkZ = (int) Math.floor(worldZ / 16);

// Chunk -57 covers blocks x: -912 to -897
// Chunk -2 covers blocks z: -32 to -17
```

## Things to Investigate

### 1. Are ECS Systems Being Called?
Add logging to verify systems are actually receiving events:
```java
@Override
public void handle(..., BreakBlockEvent event) {
    System.out.println("[LandClaims] BreakBlockEvent fired at " + event.getTargetBlock());
    // ... rest of logic
}
```

### 2. Is getEntityStoreRegistry().registerSystem() Correct?
Maybe systems need to be registered differently. Check if there's:
- A specific system group to register with
- A world-level registration
- An event type registration requirement

### 3. Does Query.any() Work?
Maybe we need a more specific query, or maybe systems with `Query.any()` aren't triggered for block events.

### 4. Is PlayerInteractEvent Actually Firing?
Add logging to verify the tracking is working:
```java
private void onPlayerInteract(PlayerInteractEvent event) {
    System.out.println("[LandClaims] PlayerInteractEvent: " +
        event.getPlayer().getUuid() + " at " + event.getTargetBlock() +
        " action=" + event.getActionType());
}
```

### 5. Alternative: World Event System
There's also `WorldEventType` in addition to `EntityEventType`. Block events might be world events:
```java
getEntityStoreRegistry().registerWorldEventType(BreakBlockEvent.class);
// vs
getEntityStoreRegistry().registerEntityEventType(BreakBlockEvent.class);
```

### 6. Check BlockHealthModule
The built-in `BlockHealthModule` handles block damage/breaking. Maybe we need to hook into that system or extend it.

### 7. Permission-Based Protection
Maybe there's a built-in permission system for block protection:
```java
player.hasPermission("landclaims.bypass");
// Or block-level permissions?
```

## Server API Methods Available

From `PluginBase`:
```java
getEventRegistry()           // For IBaseEvent events
getEntityStoreRegistry()     // For ECS components/systems on entities
getChunkStoreRegistry()      // For ECS components/systems on chunks
getBlockStateRegistry()      // For block states
getEntityRegistry()          // For entity types
getTaskRegistry()            // For scheduled tasks
```

## Files Modified

1. `src/main/java/com/landclaims/LandClaims.java` - Main plugin, registers systems
2. `src/main/java/com/landclaims/listeners/ClaimProtectionListener.java` - Player event handling & tracking
3. `src/main/java/com/landclaims/systems/BlockBreakProtectionSystem.java` - NEW
4. `src/main/java/com/landclaims/systems/BlockDamageProtectionSystem.java` - NEW
5. `src/main/java/com/landclaims/systems/BlockPlaceProtectionSystem.java` - NEW
6. `src/main/java/com/landclaims/systems/BlockUseProtectionSystem.java` - NEW

## Next Steps to Try

1. **Add extensive logging** to all event handlers to see what's being called
2. **Check server console** for any errors during plugin load
3. **Try WorldEventType** instead of EntityEventType for block events
4. **Look at BlockHealthModule** source/behavior for clues
5. **Test if PlayerInteractEvent cancellation** actually prevents anything
6. **Check if there's a priority system** for event handlers
7. **Look for other plugins** that implement protection successfully
