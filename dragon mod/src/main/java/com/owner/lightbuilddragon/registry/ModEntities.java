package com.owner.lightbuilddragon.registry;

import com.owner.lightbuilddragon.LightBuildDragonMod;
import com.owner.lightbuilddragon.entity.LightBuildDragonEntity;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.BiomeKeys;

public final class ModEntities {
    public static final EntityType<LightBuildDragonEntity> LIGHT_BUILD_DRAGON = Registry.register(
            Registries.ENTITY_TYPE,
            LightBuildDragonMod.id("light_build_dragon"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, LightBuildDragonEntity::new)
                    .dimensions(EntityDimensions.fixed(2.4F, 2.2F))
                    .trackRangeBlocks(12)
                    .trackedUpdateRate(2)
                    .build()
    );

    private ModEntities() {
    }

    public static void register() {
        FabricDefaultAttributeRegistry.register(
                LIGHT_BUILD_DRAGON,
                LightBuildDragonEntity.createLightBuildDragonAttributes()
        );

        SpawnRestriction.register(
                LIGHT_BUILD_DRAGON,
                SpawnRestriction.Location.ON_GROUND,
                Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                LightBuildDragonEntity::canSpawn
        );

        BiomeModifications.addSpawn(
                BiomeSelectors.includeByKey(BiomeKeys.DESERT),
                SpawnGroup.CREATURE,
                LIGHT_BUILD_DRAGON,
                4,
                1,
                1
        );
    }
}
