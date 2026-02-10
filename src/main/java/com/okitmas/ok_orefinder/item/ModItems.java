package com.okitmas.ok_orefinder.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import com.okitmas.ok_orefinder.Ok_OreFinder;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Ok_OreFinder.MOD_ID);

    public static final RegistryObject<Item> DIAMOND_ORE_FINDER =
            ITEMS.register("diamond_ore_finder",() -> new OreFinderItem(ItemLevel.DIAMOND_ITEM,
                    new Item.Properties()
                    .durability(100)
                    .rarity(Rarity.UNCOMMON)
            ));

    public static final RegistryObject<Item> IRON_ORE_FINDER =
            ITEMS.register("iron_ore_finder",() -> new OreFinderItem(ItemLevel.IRON_ITEM,
                    new Item.Properties()
                    .durability(32)
                    .rarity(Rarity.UNCOMMON)
            ));

    public static final RegistryObject<Item> NETHERITE_ORE_FINDER =
            ITEMS.register("netherite_ore_finder",() -> new OreFinderItem(ItemLevel.NETHERITE_ITEM,
                    new Item.Properties()
                            .durability(256)
                            .rarity(Rarity.RARE)
                            .fireResistant()
            ));
}
