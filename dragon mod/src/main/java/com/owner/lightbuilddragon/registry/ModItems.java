package com.owner.lightbuilddragon.registry;

import com.owner.lightbuilddragon.LightBuildDragonMod;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class ModItems {
    public static final Item LIGHT_BUILD_DRAGON_SPAWN_EGG = Registry.register(
            Registries.ITEM,
            LightBuildDragonMod.id("light_build_dragon_spawn_egg"),
            new SpawnEggItem(ModEntities.LIGHT_BUILD_DRAGON, 0xF5E7A2, 0xFFEFC4, new Item.Settings())
    );

    private ModItems() {
    }

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(entries -> entries.add(LIGHT_BUILD_DRAGON_SPAWN_EGG));
    }
}
