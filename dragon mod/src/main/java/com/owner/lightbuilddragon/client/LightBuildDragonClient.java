package com.owner.lightbuilddragon.client;

import com.owner.lightbuilddragon.client.render.LightBuildDragonRenderer;
import com.owner.lightbuilddragon.registry.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public final class LightBuildDragonClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.LIGHT_BUILD_DRAGON, LightBuildDragonRenderer::new);
        LightBuildDragonKeybinds.register();
    }
}
