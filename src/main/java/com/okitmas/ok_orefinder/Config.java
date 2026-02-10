package com.okitmas.ok_orefinder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.okitmas.ok_orefinder.Ok_OreFinder.OK_MOD_LOGGER;

// Demonstrates to use Forge's config APIs
@Mod.EventBusSubscriber(modid = Ok_OreFinder.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.IntValue DIAMOND_BFS_DEPTH = BUILDER
            .comment("The depth of Broad First Search Method(right click without shift).",
                    "Don't set it too large! It will make your PC lagging!")
            .defineInRange("bfs_depth_for_diamond", 16, 1, 127);

    public static final ForgeConfigSpec.IntValue SEARCH_COOLDOWN = BUILDER
            .comment("The Cooldown of using Ore Finder")
            .defineInRange("search_cooldown",20,20,72000);

    public static final ForgeConfigSpec.IntValue DIAMOND_DOWNWARDS_DEPTH = BUILDER
            .comment("The depth of downward searching method.(right click with shift)")
            .defineInRange("downwards_depth_for_diamond",64,1,319);

    public static final ForgeConfigSpec.IntValue DOWNWARDS_LAYER_SIZE = BUILDER
            .comment("The size(Manhattan Distance) of each layers in downward searching method(right click with shift).")
            .defineInRange("downwards_layer_size",8,1,24);

    // a list of strings that are treated as resource locations for blocks
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> DIAMOND_TARGET_ORES = BUILDER
            .comment("Diamond Ore Finder will search for these blocks(ores).")
            .defineList("target_ores_for_diamond", () -> List.of("minecraft:diamond_ore",
                            "minecraft:deepslate_diamond_ore",
                            "minecraft:ancient_debris"),
                    Config::validateBlocksName);

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> SKIP_BLOCKS = BUILDER
            .comment("Which block should be passed when using Broad First Search Method(right click without shift)")
            .defineList("skip_blocks", () -> List.of("minecraft:air",
                            "minecraft:bedrock",
                            "minecraft:water",
                            "minecraft:lava"),
                    Config::validateBlocksName);

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> IRON_TARGET_ORES = BUILDER
            .comment("Iron Ore Finder will search for these blocks(ores).")
            .defineList("target_ores_for_iron", () -> List.of("minecraft:gold_ore",
                            "minecraft:deepslate_gold_ore",
                            "minecraft:diamond_ore",
                            "minecraft:deepslate_diamond_ore"),
                    Config::validateBlocksName);

    public static final ForgeConfigSpec.IntValue IRON_BFS_DEPTH = BUILDER
            .comment("The depth of Broad First Search Method(right click without shift).",
                    "Don't set it too large! It will make your PC lagging!")
            .defineInRange("bfs_depth_for_iron", 8, 1, 63);

    public static final ForgeConfigSpec.IntValue IRON_DOWNWARDS_DEPTH = BUILDER
            .comment("The depth of downward searching method.(right click with shift)")
            .defineInRange("downwards_depth_for_iron",32,1,128);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    private static int diamond_bfs_depth;
    private static int search_cooldown;
    private static int diamond_downwards_depth;
    private static int downwards_layer_size;
    private static Set<Block> diamond_target_ores;
    private static Set<Block> skip_blocks;
    private static Set<Block> iron_target_ores;
    private static int iron_bfs_depth;
    private static int iron_downwards_depth;

    private static boolean validateBlocksName(final Object obj)
    {
        return obj instanceof final String itemName && ForgeRegistries.BLOCKS.containsKey(ResourceLocation.tryParse(itemName));
    }

    public static void loadConfig() {
        OK_MOD_LOGGER.debug("start to load mod config");
        diamond_bfs_depth = DIAMOND_BFS_DEPTH.get();
        search_cooldown = SEARCH_COOLDOWN.get();
        diamond_downwards_depth = DIAMOND_DOWNWARDS_DEPTH.get();
        downwards_layer_size = DOWNWARDS_LAYER_SIZE.get();

        // convert the list of strings into a set of items
        diamond_target_ores = DIAMOND_TARGET_ORES.get().stream()
                .map(blockName -> ForgeRegistries.BLOCKS.getValue(ResourceLocation.tryParse(blockName)))
                .collect(Collectors.toSet());

        skip_blocks = SKIP_BLOCKS.get().stream()
                .map(blockName -> ForgeRegistries.BLOCKS.getValue(ResourceLocation.tryParse(blockName)))
                .collect(Collectors.toSet());

        iron_target_ores = IRON_TARGET_ORES.get().stream()
                .map(blockName -> ForgeRegistries.BLOCKS.getValue(ResourceLocation.tryParse(blockName)))
                .collect(Collectors.toSet());
        iron_bfs_depth = IRON_BFS_DEPTH.get();
        iron_downwards_depth = IRON_DOWNWARDS_DEPTH.get();
    }

    public static int getDiamond_bfs_depth() {return diamond_bfs_depth;}
    public static int getSearch_cooldown() {return search_cooldown;}
    public static int getDiamond_downwards_depth() {return diamond_downwards_depth;}
    public static int getDownwards_layer_size() {return downwards_layer_size;}
    public static Set<Block> getDiamond_target_ores() {return diamond_target_ores;}
    public static Set<Block> getSkip_blocks() {return skip_blocks;}
    public static Set<Block> getIron_target_ores() {return iron_target_ores;}
    public static int getIron_bfs_depth() {return iron_bfs_depth;}
    public static int getIron_downwards_depth() {return iron_downwards_depth;}

    // load config when server staring(at main method)
//    @SubscribeEvent
//    static void onLoad(final ModConfigEvent event)
//    {
//        loadConfig();
//    }
}
