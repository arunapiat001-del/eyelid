package com.deathdim.eyelid;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.util.Identifier;

public class EyelidClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        PayloadTypeRegistry.playS2C().register(EyePayload.ID, EyePayload.CODEC);
        ClientPlayNetworking.registerGlobalReceiver(EyePayload.ID,
                (payload, context) -> EyeAnimation.handle(payload.action(), payload.ticks()));

        HudElementRegistry.attachElementAfter(
                VanillaHudElements.MISC_OVERLAYS,
                Identifier.of("eyelid", "overlay"),
                (context, tickCounter) -> EyeAnimation.render(context));

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> EyeAnimation.reset());

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal("eyetest")
                        .then(ClientCommandManager.literal("close").executes(c -> {
                            EyeAnimation.handle(EyeAnimation.CLOSE, 40);
                            return 1;
                        }))
                        .then(ClientCommandManager.literal("open").executes(c -> {
                            EyeAnimation.handle(EyeAnimation.OPEN, 40);
                            return 1;
                        }))));
    }
}
