package com.owner.lightbuilddragon;

import com.owner.lightbuilddragon.network.DragonControlPackets;
import com.owner.lightbuilddragon.registry.ModEntities;
import com.owner.lightbuilddragon.registry.ModItems;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.GeckoLib;

public final class LightBuildDragonMod implements ModInitializer {
    public static final String MOD_ID = "lightbuilddragon";

    @Override
    public void onInitialize() {
        GeckoLib.initialize();
        ModEntities.register();
        ModItems.register();
        DragonControlPackets.registerServerReceivers();
    }

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }
}
