package com.owner.lightbuilddragon.client;

import com.owner.lightbuilddragon.entity.LightBuildDragonEntity;
import com.owner.lightbuilddragon.network.DragonControlPackets;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.PacketByteBuf;
import org.lwjgl.glfw.GLFW;

public final class LightBuildDragonKeybinds {
    private static KeyBinding fireChargeKey;
    private static KeyBinding meleeAttackKey;

    private LightBuildDragonKeybinds() {
    }

    public static void register() {
        fireChargeKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.lightbuilddragon.fire_charge",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_F,
                "key.categories.lightbuilddragon"
        ));

        meleeAttackKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.lightbuilddragon.melee_attack",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "key.categories.lightbuilddragon"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.getNetworkHandler() == null) {
                return;
            }

            boolean ridingDragon = client.player.getVehicle() instanceof LightBuildDragonEntity;
            if (!ridingDragon) {
                drainQueuedPresses();
                return;
            }

            PacketByteBuf controlBuffer = PacketByteBufs.create();
            controlBuffer.writeBoolean(client.options.jumpKey.isPressed());
            ClientPlayNetworking.send(DragonControlPackets.CONTROL, controlBuffer);

            while (fireChargeKey.wasPressed()) {
                ClientPlayNetworking.send(DragonControlPackets.SHOOT_FIRE_CHARGE, PacketByteBufs.empty());
            }

            while (meleeAttackKey.wasPressed()) {
                ClientPlayNetworking.send(DragonControlPackets.MELEE_ATTACK, PacketByteBufs.empty());
            }
        });
    }

    private static void drainQueuedPresses() {
        while (fireChargeKey.wasPressed()) {
            // Drop key presses when not mounted.
        }
        while (meleeAttackKey.wasPressed()) {
            // Drop key presses when not mounted.
        }
    }
}
