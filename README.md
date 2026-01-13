# LandClaims

A chunk-based land claiming plugin for Hytale servers with playtime-based claim limits and granular trust permissions.

## Features

- **Chunk-based claims** - Claim 16x16 chunk areas to protect your builds
- **Playtime rewards** - More playtime = more claim chunks available
- **Granular trust system** - Give players different permission levels (doors only, chests, full build access)
- **Configurable block groups** - Define which blocks require which permission level
- **Full protection** - Prevents breaking, placing, damaging, and using blocks in claimed areas
- **Backward compatible** - Existing claim data is automatically migrated

## Installation

1. Build the plugin or download from releases
2. Place `LandClaims-1.0.0.jar` in your Hytale server's `mods/` folder
3. Restart the server
4. Config files will be created in `mods/Community_LandClaims/`

## Commands

### Claiming

| Command | Permission | Description |
|---------|------------|-------------|
| `/claim` | `landclaims.claim` | Claim the chunk you're standing in |
| `/unclaim` | `landclaims.unclaim` | Unclaim the chunk you're standing in |
| `/unclaimall` | `landclaims.unclaimall` | Remove all your claims |
| `/claims` | `landclaims.list` | List all your claims with coordinates |

### Trust System

| Command | Permission | Description |
|---------|------------|-------------|
| `/trust <player> [level]` | `landclaims.trust` | Trust a player with optional permission level |
| `/untrust <player>` | `landclaims.untrust` | Remove trust from a player |
| `/trustlist` | `landclaims.trustlist` | List all trusted players and their levels |

### Info

| Command | Permission | Description |
|---------|------------|-------------|
| `/playtime` | `landclaims.playtime` | Show your playtime and available claims |
| `/claimhelp` | `landclaims.help` | Show help information |

## Trust Levels

Trust levels are hierarchical - higher levels include all permissions from lower levels.

| Level | Command | Description | Allows |
|-------|---------|-------------|--------|
| 1 | `/trust Player use` | Basic interaction | Doors, buttons, levers, trapdoors, fence gates, pressure plates |
| 2 | `/trust Player container` | Storage access | + Chests, barrels, shulker boxes, hoppers, dispensers |
| 3 | `/trust Player workstation` | Crafting access | + Crafting tables, furnaces, anvils, brewing stands, enchanting tables |
| 4 | `/trust Player damage` | Block damage | + Can damage/mine blocks (but not fully break) |
| 5 | `/trust Player build` | Full access | + Can break and place blocks (default if no level specified) |

### Examples

```
/trust Steve use         # Steve can only use doors and buttons
/trust Alex container    # Alex can open chests but not build
/trust Bob build         # Bob has full access (same as /trust Bob)
/trust Steve workstation # Update Steve's access to workstation level
```

## Player Lookup

The `/trust` and `/untrust` commands accept:
- **Player names** - For online players (case-insensitive)
- **UUIDs** - For offline players

```
/trust PlayerName build     # Trust online player by name
/trust 12345678-1234-...    # Trust offline player by UUID
/untrust PlayerName         # Works if online OR if previously trusted
```

## Configuration

### Main Config: `config.json`

Located in `mods/Community_LandClaims/config.json`:

```json
{
  "chunksPerHour": 2,
  "startingChunks": 4,
  "maxClaimsPerPlayer": 50,
  "playtimeUpdateIntervalSeconds": 60
}
```

| Setting | Description |
|---------|-------------|
| `chunksPerHour` | How many additional chunks per hour of playtime |
| `startingChunks` | Chunks available to new players |
| `maxClaimsPerPlayer` | Maximum claims regardless of playtime |
| `playtimeUpdateIntervalSeconds` | How often to save playtime data |

### Block Groups: `block_groups.json`

Defines which blocks require which trust level. Uses pattern matching (if block ID contains the pattern).

```json
{
  "useBlocks": [],
  "usePatterns": [
    "door", "button", "lever", "pressure_plate",
    "gate", "trapdoor", "switch", "bell"
  ],
  "containerBlocks": [],
  "containerPatterns": [
    "chest", "barrel", "crate", "container",
    "storage", "hopper", "dispenser", "dropper", "shulker"
  ],
  "workstationBlocks": [],
  "workstationPatterns": [
    "crafting", "workbench", "furnace", "smoker", "blast",
    "anvil", "grindstone", "loom", "cartography", "smithing",
    "stonecutter", "brewing", "enchant", "cauldron", "composter", "lectern"
  ]
}
```

**Adding Custom Blocks:**

As you discover Hytale block IDs, add them to the appropriate list:

- `useBlocks` / `containerBlocks` / `workstationBlocks` - Exact block ID matches
- `usePatterns` / `containerPatterns` / `workstationPatterns` - Partial matches (if ID contains pattern)

## Data Storage

All data is stored in `mods/Community_LandClaims/`:

```
mods/Community_LandClaims/
├── config.json           # Main configuration
├── block_groups.json     # Block permission groups
├── playtime/
│   └── <uuid>.json       # Per-player playtime data
└── claims/
    ├── index.json        # Chunk ownership index
    └── <uuid>.json       # Per-player claims and trusted players
```

### Claim Data Format

Each player's claims file (`claims/<uuid>.json`):

```json
{
  "claims": [
    {
      "world": "default",
      "chunkX": 10,
      "chunkZ": -5,
      "claimedAt": 1704067200000
    }
  ],
  "trustedPlayersData": {
    "uuid-of-trusted-player": {
      "name": "PlayerName",
      "level": "container"
    }
  }
}
```

## Permissions Setup

Grant permissions to your server's permission groups:

```
perm group add Adventure landclaims.claim
perm group add Adventure landclaims.unclaim
perm group add Adventure landclaims.unclaimall
perm group add Adventure landclaims.list
perm group add Adventure landclaims.trust
perm group add Adventure landclaims.untrust
perm group add Adventure landclaims.trustlist
perm group add Adventure landclaims.playtime
perm group add Adventure landclaims.help
```

## How Protection Works

The plugin uses Hytale's ECS event system to intercept block interactions:

| Event | Required Trust Level |
|-------|---------------------|
| `UseBlockEvent.Pre` | Depends on block type (use/container/workstation) |
| `DamageBlockEvent` | `damage` |
| `BreakBlockEvent` | `build` |
| `PlaceBlockEvent` | `build` |

When a player tries to interact with a claimed chunk:
1. If unclaimed or player is owner: Allowed
2. If player has required trust level: Allowed
3. Otherwise: Cancelled with message

## Building from Source

Requires Java 25 and Maven.

```bash
cd plugins/LandClaims
mvn clean package
```

The built JAR will be in `target/LandClaims-1.0.0.jar`. Copy to your server's `mods/` folder.

## Debugging

Enable debug logging to see block events:
- Check server console for `[LandClaims]` messages
- Block IDs are logged when interactions are checked
- Use this to discover block IDs for your `block_groups.json`

## Migration

The plugin automatically migrates old data formats:
- **v1** (List of UUIDs) → Converted to BUILD trust level
- **v2** (UUID → name map) → Converted to BUILD trust level
- **v3** (Current format with trust levels) → Used as-is

## License

MIT
