package com.okitmas.ok_orefinder.item;

import com.okitmas.ok_orefinder.Ok_OreFinder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Ok_OreFinder.MOD_ID);

    public static final RegistryObject<CreativeModeTab> MOD_TAB = CREATIVE_MODE_TABS.register("ore_finder_tab", () -> CreativeModeTab.builder()
            .icon(() -> ModItems.DIAMOND_ORE_FINDER.get().getDefaultInstance())
            .title(Component.translatable("itemGroup.ore_finder_tab"))
            .displayItems((parameters, output) -> {
                output.accept(ModItems.DIAMOND_ORE_FINDER.get());
                output.accept(ModItems.IRON_ORE_FINDER.get());
                output.accept(ModItems.NETHERITE_ORE_FINDER.get());
            })
            .build());
}
