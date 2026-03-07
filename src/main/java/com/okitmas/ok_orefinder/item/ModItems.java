package com.okitmas.ok_orefinder.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;

import com.okitmas.ok_orefinder.Ok_OreFinder;
import net.neoforged.neoforge.registries.DeferredHolder;


public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, Ok_OreFinder.MOD_ID);

    public static final DeferredHolder<Item, Item> DIAMOND_ORE_FINDER =
            ITEMS.register("diamond_ore_finder",() -> new OreFinderItem(ItemLevel.DIAMOND_ITEM,
                    new Item.Properties()
                    .durability(100)
                    .rarity(Rarity.UNCOMMON)
            ));

    public static final DeferredHolder<Item, Item> IRON_ORE_FINDER =
            ITEMS.register("iron_ore_finder",() -> new OreFinderItem(ItemLevel.IRON_ITEM,
                    new Item.Properties()
                    .durability(32)
                    .rarity(Rarity.UNCOMMON)
            ));

    public static final DeferredHolder<Item, Item> NETHERITE_ORE_FINDER =
            ITEMS.register("netherite_ore_finder",() -> new OreFinderItem(ItemLevel.NETHERITE_ITEM,
                    new Item.Properties()
                            .durability(256)
                            .rarity(Rarity.RARE)
                            .fireResistant()
            ));
}
