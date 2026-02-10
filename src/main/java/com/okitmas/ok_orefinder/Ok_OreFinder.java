package com.okitmas.ok_orefinder;


import com.mojang.logging.LogUtils;
import com.okitmas.ok_orefinder.item.ModCreativeModeTabs;
import com.okitmas.ok_orefinder.item.ModItems;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Ok_OreFinder.MOD_ID)
public class Ok_OreFinder
{
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "ok_orefinder";
    // Directly reference a slf4j logger
    public static final Logger OK_MOD_LOGGER = LogUtils.getLogger();

    public Ok_OreFinder(FMLJavaModLoadingContext context)
    {
        IEventBus MOD_EVENT_BUS = context.getModEventBus();
        IEventBus FORGE_EVENT_BUS = MinecraftForge.EVENT_BUS;

        // Register the commonSetup method for mod loading
        // MOD_EVENT_BUS.addListener(this::commonSetup);

        // Register the Deferred Register to the mod event bus so items get registered
        ModItems.ITEMS.register(MOD_EVENT_BUS);
        // Register the Deferred Register to the mod event bus so tabs get registered
        ModCreativeModeTabs.CREATIVE_MODE_TABS.register(MOD_EVENT_BUS);

        // Register ourselves for server and other game events we are interested in
        FORGE_EVENT_BUS.register(this);

        // Register the item to a creative tab
        MOD_EVENT_BUS.addListener(this::addCreative);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        MOD_EVENT_BUS.addListener((ModConfigEvent.Reloading event) -> Config.loadConfig());
        MOD_EVENT_BUS.addListener((ModConfigEvent.Loading event) -> Config.loadConfig());
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
    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
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
//    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
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
