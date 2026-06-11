package com.owner.lightbuilddragon.network;

import com.owner.lightbuilddragon.LightBuildDragonMod;
import com.owner.lightbuilddragon.entity.LightBuildDragonEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Optional;

public final class DragonControlPackets {
    public static final Identifier CONTROL = LightBuildDragonMod.id("dragon_control");
    public static final Identifier SHOOT_FIRE_CHARGE = LightBuildDragonMod.id("shoot_fire_charge");
    public static final Identifier MELEE_ATTACK = LightBuildDragonMod.id("melee_attack");

    private DragonControlPackets() {
    }

    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(CONTROL, (server, player, handler, buf, responseSender) -> {
            boolean wantsUp = buf.readBoolean();
            server.execute(() -> getControlledDragon(player).ifPresent(dragon -> dragon.setRiderWantsUp(wantsUp)));
        });

        ServerPlayNetworking.registerGlobalReceiver(SHOOT_FIRE_CHARGE, (server, player, handler, buf, responseSender) ->
                server.execute(() -> getControlledDragon(player).ifPresent(dragon -> dragon.shootFireCharge(player)))
        );

        ServerPlayNetworking.registerGlobalReceiver(MELEE_ATTACK, (server, player, handler, buf, responseSender) ->
                server.execute(() -> getControlledDragon(player).ifPresent(dragon -> dragon.controlledMeleeAttack(player)))
        );
    }

    private static Optional<LightBuildDragonEntity> getControlledDragon(ServerPlayerEntity player) {
        Entity vehicle = player.getVehicle();
        if (vehicle instanceof LightBuildDragonEntity dragon && dragon.isOwner(player)) {
            return Optional.of(dragon);
        }
        return Optional.empty();
    }
}
