package com.owner.lightbuilddragon.client.render;

import com.owner.lightbuilddragon.LightBuildDragonMod;
import com.owner.lightbuilddragon.entity.LightBuildDragonEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public final class LightBuildDragonModel extends GeoModel<LightBuildDragonEntity> {
    @Override
    public Identifier getModelResource(LightBuildDragonEntity animatable) {
        return LightBuildDragonMod.id("geo/light_build_dragon.geo.json");
    }

    @Override
    public Identifier getTextureResource(LightBuildDragonEntity animatable) {
        return LightBuildDragonMod.id("textures/entity/light_build_dragon.png");
    }

    @Override
    public Identifier getAnimationResource(LightBuildDragonEntity animatable) {
        return LightBuildDragonMod.id("animations/light_build_dragon.animation.json");
    }
}
