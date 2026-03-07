package com.okitmas.ok_orefinder;

import com.mojang.logging.LogUtils;
import com.okitmas.ok_orefinder.item.ModCreativeModeTabs;
import com.okitmas.ok_orefinder.item.ModItems;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import org.slf4j.Logger;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.ModContainer;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Ok_OreFinder.MOD_ID)
public class Ok_OreFinder {

    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "ok_orefinder";

    // Directly reference a slf4j logger
    public static final Logger OK_MOD_LOGGER = LogUtils.getLogger();

    public Ok_OreFinder(IEventBus modEventBus, ModContainer modContainer) {
                IEventBus FORGE_EVENT_BUS = NeoForge.EVENT_BUS;
        // Register the commonSetup method for mod loading
        // modEventBus.addListener(this::commonSetup);
        // Register the Deferred Register to the mod event bus so items get registered
        ModItems.ITEMS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so tabs get registered
        ModCreativeModeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        // Register ourselves for server and other game events we are interested in
        FORGE_EVENT_BUS.register(this);
        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);
        // Register our mod's ModConfigSpec so that Forge can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        modEventBus.addListener((ModConfigEvent.Reloading event) -> Config.loadConfig());
        modEventBus.addListener((ModConfigEvent.Loading event) -> Config.loadConfig());
        //        FORGE_EVENT_BUS.addListener((ServerAboutToStartEvent event) -> FinderHelper.startExecutor());
        //        FORGE_EVENT_BUS.addListener((ServerStoppingEvent event) -> FinderHelper.shutdownExecutor());
    }

    //    private void commonSetup(final FMLCommonSetupEvent event)
    //    {
    //        LOGGER.info("ore finder depth: {}", Config.search_depth);
    //
    //        Config.target_ores.forEach((pBlock) -> LOGGER.info("target blocks >> {}", pBlock.toString()));
    //    }
    // Add the ore finder item to the tool and utilities tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.DIAMOND_ORE_FINDER.get());
            event.accept(ModItems.IRON_ORE_FINDER.get());
            event.accept(ModItems.NETHERITE_ORE_FINDER.get());
        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    //    @SubscribeEvent
    //    public void onServerStarting(ServerStartingEvent event)
    //    {
    //        // load(refresh) config
    //        Config.loadConfig();
    //    }
    @SubscribeEvent
    public void onServerAboutToStart(ServerAboutToStartEvent event) {
        FinderHelper.startExecutor();
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        FinderHelper.shutdownExecutor();
    }
    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    //    @EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
    //    public static class ClientModEvents
    //    {
    //        @SubscribeEvent
    //        public static void onClientSetup(FMLClientSetupEvent event)
    //        {
    //            // Some client setup code
    //            LOGGER.info("HELLO FROM CLIENT SETUP");
    //            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    //        }
    //    }
}
