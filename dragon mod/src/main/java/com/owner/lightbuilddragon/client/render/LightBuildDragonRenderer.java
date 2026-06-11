package com.owner.lightbuilddragon.client.render;

import com.owner.lightbuilddragon.entity.LightBuildDragonEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public final class LightBuildDragonRenderer extends GeoEntityRenderer<LightBuildDragonEntity> {
    public LightBuildDragonRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new LightBuildDragonModel());
        this.shadowRadius = 1.2F;
    }
}
