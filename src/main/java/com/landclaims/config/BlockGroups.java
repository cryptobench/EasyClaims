package com.landclaims.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * Configurable block groups for granular permission checking.
 * Block IDs can be exact matches or partial matches (contains).
 */
public class BlockGroups {
    private final Path configFile;
    private final Gson gson;

    // Block groups - these determine what trust level is needed
    private Set<String> useBlocks;         // Blocks that need USE trust level
    private Set<String> containerBlocks;   // Blocks that need CONTAINER trust level
    private Set<String> workstationBlocks; // Blocks that need WORKSTATION trust level

    // Patterns to match (if block ID contains any of these)
    private Set<String> usePatterns;
    private Set<String> containerPatterns;
    private Set<String> workstationPatterns;

    public BlockGroups(Path dataDirectory) {
        this.configFile = dataDirectory.resolve("block_groups.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();

        initDefaults();
        load();
    }

    private void initDefaults() {
        // ===== USE LEVEL BLOCKS (doors, trapdoors, gates) =====
        // These allow basic interaction without full access

        useBlocks = new HashSet<>();
        // Hytale door blocks by decoration set
        useBlocks.add("Door_Ancient");
        useBlocks.add("Door_Crude");
        useBlocks.add("Door_Desert");
        useBlocks.add("Door_Feran");
        useBlocks.add("Door_Frozen_Castle");
        useBlocks.add("Door_Human_Ruins");
        useBlocks.add("Door_Jungle");
        useBlocks.add("Door_Kweebec");
        useBlocks.add("Door_Lumberjack");
        useBlocks.add("Door_Tavern");
        useBlocks.add("Door_Temple_Dark");
        useBlocks.add("Door_Temple_Light");
        useBlocks.add("Door_Temple_Emerald");
        useBlocks.add("Door_Temple_Wind");
        useBlocks.add("Door_Village");
        useBlocks.add("Door_Wooden");
        // Trapdoors
        useBlocks.add("Trapdoor_Ancient");
        useBlocks.add("Trapdoor_Crude");
        useBlocks.add("Trapdoor_Desert");
        useBlocks.add("Trapdoor_Feran");
        useBlocks.add("Trapdoor_Frozen_Castle");
        useBlocks.add("Trapdoor_Jungle");
        useBlocks.add("Trapdoor_Lumberjack");
        useBlocks.add("Trapdoor_Temple_Dark");
        useBlocks.add("Trapdoor_Temple_Light");
        useBlocks.add("Trapdoor_Village");

        // Patterns for USE level - matches if block ID contains any of these (case-insensitive)
        usePatterns = new HashSet<>();
        usePatterns.add("door");        // All door variants
        usePatterns.add("trapdoor");    // All trapdoor variants
        usePatterns.add("gate");        // Fence gates
        usePatterns.add("button");      // Buttons
        usePatterns.add("lever");       // Levers
        usePatterns.add("switch");      // Switches
        usePatterns.add("bell");        // Bells

        // ===== CONTAINER LEVEL BLOCKS (chests, storage) =====
        // These allow access to storage containers

        containerBlocks = new HashSet<>();
        // Hytale chest blocks by decoration set
        containerBlocks.add("Chest_Ancient");
        containerBlocks.add("Chest_Small_Ancient");
        containerBlocks.add("Chest_Crude");
        containerBlocks.add("Chest_Small_Crude");
        containerBlocks.add("Chest_Desert");
        containerBlocks.add("Chest_Small_Desert");
        containerBlocks.add("Chest_Large_Desert");
        containerBlocks.add("Chest_Feran");
        containerBlocks.add("Chest_Small_Feran");
        containerBlocks.add("Chest_Large_Feran");
        containerBlocks.add("Chest_Frozen_Castle");
        containerBlocks.add("Chest_Large_Frozen_Castle");
        containerBlocks.add("Chest_Human_Ruins");
        containerBlocks.add("Chest_Small_Human_Ruins");
        containerBlocks.add("Chest_Large_Human_Ruins");
        containerBlocks.add("Chest_Jungle");
        containerBlocks.add("Chest_Small_Jungle");
        containerBlocks.add("Chest_Large_Jungle");
        containerBlocks.add("Chest_Kweebec");
        containerBlocks.add("Chest_Lumberjack");
        containerBlocks.add("Chest_Small_Lumberjack");
        containerBlocks.add("Chest_Large_Lumberjack");
        containerBlocks.add("Chest_Tavern");
        containerBlocks.add("Chest_Temple_Dark");
        containerBlocks.add("Chest_Temple_Light");
        containerBlocks.add("Chest_Village");
        containerBlocks.add("Chest_Legendary");
        containerBlocks.add("Chest_Wooden");
        // Wardrobes
        containerBlocks.add("Wardrobe_Ancient");
        containerBlocks.add("Wardrobe_Crude");
        containerBlocks.add("Wardrobe_Desert");
        containerBlocks.add("Wardrobe_Feran");
        containerBlocks.add("Wardrobe_Frozen_Castle");
        containerBlocks.add("Wardrobe_Human_Ruins");
        containerBlocks.add("Wardrobe_Jungle");
        containerBlocks.add("Wardrobe_Lumberjack");
        containerBlocks.add("Wardrobe_Temple_Dark");
        containerBlocks.add("Wardrobe_Temple_Light");
        containerBlocks.add("Wardrobe_Village");
        // Coffins (container type)
        containerBlocks.add("Container_Coffin");
        containerBlocks.add("Coffin_Human_Ruins");

        // Patterns for CONTAINER level - matches if block ID contains any of these
        containerPatterns = new HashSet<>();
        containerPatterns.add("chest");       // All chest variants
        containerPatterns.add("wardrobe");    // All wardrobe variants
        containerPatterns.add("coffin");      // Coffins
        containerPatterns.add("barrel");      // Barrels
        containerPatterns.add("crate");       // Crates
        containerPatterns.add("container_");  // Container_ prefixed blocks
        containerPatterns.add("storage");     // Storage blocks
        containerPatterns.add("pot_");        // Pots (Container_Pot_*)

        // ===== WORKSTATION LEVEL BLOCKS (crafting benches) =====
        // These allow use of crafting/processing stations

        workstationBlocks = new HashSet<>();
        // Hytale workstation/bench blocks (from Common/Blocks/Benches/)
        workstationBlocks.add("Bench_Alchemy");
        workstationBlocks.add("Bench_Anvil");
        workstationBlocks.add("Bench_ArcaneTable");
        workstationBlocks.add("Bench_Armor");
        workstationBlocks.add("Bench_Armory");
        workstationBlocks.add("Bench_Builder");
        workstationBlocks.add("Bench_Campfire");
        workstationBlocks.add("Bench_Campfire_Cooking");
        workstationBlocks.add("Bench_Campfire_Billycan");
        workstationBlocks.add("Bench_Carpenter");
        workstationBlocks.add("Bench_Cooking");
        workstationBlocks.add("Bench_Farming");
        workstationBlocks.add("Bench_Furnace");
        workstationBlocks.add("Bench_Furnace_Simple");
        workstationBlocks.add("Bench_Furniture");
        workstationBlocks.add("Bench_Loom");
        workstationBlocks.add("Bench_Lumbermill");
        workstationBlocks.add("Bench_Memory");
        workstationBlocks.add("Bench_Salvage");
        workstationBlocks.add("Bench_Tannery");
        workstationBlocks.add("Bench_Weapon");
        workstationBlocks.add("Bench_Workbench");
        // Alternative naming (without Bench_ prefix)
        workstationBlocks.add("Alchemy");
        workstationBlocks.add("Anvil");
        workstationBlocks.add("ArcaneTable");
        workstationBlocks.add("Armory");
        workstationBlocks.add("Campfire");
        workstationBlocks.add("Carpenter");
        workstationBlocks.add("Cooking");
        workstationBlocks.add("Furnace");
        workstationBlocks.add("Furnace_Simple");
        workstationBlocks.add("Furnace2");
        workstationBlocks.add("Loom");
        workstationBlocks.add("Lumbermill");
        workstationBlocks.add("Salvage");
        workstationBlocks.add("Tannery");
        workstationBlocks.add("Workbench");
        workstationBlocks.add("Workbench2");
        workstationBlocks.add("Workbench3");
        // Deco cauldron (usable)
        workstationBlocks.add("Deco_Cauldron");

        // Patterns for WORKSTATION level - matches if block ID contains any of these
        workstationPatterns = new HashSet<>();
        workstationPatterns.add("workbench");   // All workbench variants
        workstationPatterns.add("furnace");     // All furnace variants
        workstationPatterns.add("anvil");       // Anvils
        workstationPatterns.add("alchemy");     // Alchemy benches
        workstationPatterns.add("armory");      // Armory benches
        workstationPatterns.add("carpenter");   // Carpenter benches
        workstationPatterns.add("cooking");     // Cooking stations
        workstationPatterns.add("farming");     // Farming benches
        workstationPatterns.add("loom");        // Looms
        workstationPatterns.add("lumbermill");  // Lumbermills
        workstationPatterns.add("salvage");     // Salvage stations
        workstationPatterns.add("tannery");     // Tanneries
        workstationPatterns.add("campfire");    // Campfires (cooking)
        workstationPatterns.add("arcanetable"); // Arcane tables
        workstationPatterns.add("cauldron");    // Cauldrons
        workstationPatterns.add("brewing");     // Brewing stands
        workstationPatterns.add("enchant");     // Enchanting tables
        workstationPatterns.add("_bench");      // Generic bench suffix
    }

    private void load() {
        if (Files.exists(configFile)) {
            try {
                String json = Files.readString(configFile);
                BlockGroupsData data = gson.fromJson(json, BlockGroupsData.class);
                if (data != null) {
                    if (data.useBlocks != null) useBlocks = new HashSet<>(data.useBlocks);
                    if (data.usePatterns != null) usePatterns = new HashSet<>(data.usePatterns);
                    if (data.containerBlocks != null) containerBlocks = new HashSet<>(data.containerBlocks);
                    if (data.containerPatterns != null) containerPatterns = new HashSet<>(data.containerPatterns);
                    if (data.workstationBlocks != null) workstationBlocks = new HashSet<>(data.workstationBlocks);
                    if (data.workstationPatterns != null) workstationPatterns = new HashSet<>(data.workstationPatterns);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            save(); // Create default config file
        }
    }

    public void save() {
        BlockGroupsData data = new BlockGroupsData();
        data.useBlocks = useBlocks;
        data.usePatterns = usePatterns;
        data.containerBlocks = containerBlocks;
        data.containerPatterns = containerPatterns;
        data.workstationBlocks = workstationBlocks;
        data.workstationPatterns = workstationPatterns;

        try {
            Files.createDirectories(configFile.getParent());
            Files.writeString(configFile, gson.toJson(data));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Check if a block requires USE trust level (doors, buttons, etc.)
     */
    public boolean isUseBlock(BlockType blockType) {
        if (blockType == null) return false;

        // Check built-in door detection
        if (blockType.isDoor()) return true;

        String id = blockType.getId();
        if (id == null) return false;

        String lowerID = id.toLowerCase();

        // Check exact matches
        if (useBlocks.contains(id) || useBlocks.contains(lowerID)) {
            return true;
        }

        // Check patterns
        for (String pattern : usePatterns) {
            if (lowerID.contains(pattern)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if a block requires CONTAINER trust level (chests, etc.)
     */
    public boolean isContainerBlock(BlockType blockType) {
        if (blockType == null) return false;

        String id = blockType.getId();
        if (id == null) return false;

        String lowerID = id.toLowerCase();

        // Check exact matches
        if (containerBlocks.contains(id) || containerBlocks.contains(lowerID)) {
            return true;
        }

        // Check patterns
        for (String pattern : containerPatterns) {
            if (lowerID.contains(pattern)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if a block requires WORKSTATION trust level (crafting tables, anvils, etc.)
     */
    public boolean isWorkstationBlock(BlockType blockType) {
        if (blockType == null) return false;

        String id = blockType.getId();
        if (id == null) return false;

        String lowerID = id.toLowerCase();

        // Check exact matches
        if (workstationBlocks.contains(id) || workstationBlocks.contains(lowerID)) {
            return true;
        }

        // Check patterns
        for (String pattern : workstationPatterns) {
            if (lowerID.contains(pattern)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Add a block ID to the USE group.
     */
    public void addUseBlock(String blockId) {
        useBlocks.add(blockId);
        save();
    }

    /**
     * Add a pattern to the USE group.
     */
    public void addUsePattern(String pattern) {
        usePatterns.add(pattern.toLowerCase());
        save();
    }

    /**
     * Add a block ID to the CONTAINER group.
     */
    public void addContainerBlock(String blockId) {
        containerBlocks.add(blockId);
        save();
    }

    /**
     * Add a pattern to the CONTAINER group.
     */
    public void addContainerPattern(String pattern) {
        containerPatterns.add(pattern.toLowerCase());
        save();
    }

    /**
     * Add a block ID to the WORKSTATION group.
     */
    public void addWorkstationBlock(String blockId) {
        workstationBlocks.add(blockId);
        save();
    }

    /**
     * Add a pattern to the WORKSTATION group.
     */
    public void addWorkstationPattern(String pattern) {
        workstationPatterns.add(pattern.toLowerCase());
        save();
    }

    public Set<String> getUseBlocks() {
        return new HashSet<>(useBlocks);
    }

    public Set<String> getUsePatterns() {
        return new HashSet<>(usePatterns);
    }

    public Set<String> getContainerBlocks() {
        return new HashSet<>(containerBlocks);
    }

    public Set<String> getContainerPatterns() {
        return new HashSet<>(containerPatterns);
    }

    public Set<String> getWorkstationBlocks() {
        return new HashSet<>(workstationBlocks);
    }

    public Set<String> getWorkstationPatterns() {
        return new HashSet<>(workstationPatterns);
    }

    // JSON data class
    private static class BlockGroupsData {
        Set<String> useBlocks;
        Set<String> usePatterns;
        Set<String> containerBlocks;
        Set<String> containerPatterns;
        Set<String> workstationBlocks;
        Set<String> workstationPatterns;
    }
}
