package com.deathdim.eyelid;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;

public class RebirthClient implements ClientModInitializer {
    private static final int LIFT = 0;
    private static final int MAX_HOLD = 1200; // pengaman: lepas otomatis setelah 60 detik

    // 0 = diam, 1 = animasi, 2 = menahan pitch
    private static int state = 0, dur = 1, elapsed = 0, hold = 0;
    private static float from = 0f, to = 0f;
    private static boolean release = false;

    @Override
    public void onInitializeClient() {
        PayloadTypeRegistry.playS2C().register(RebirthPayload.ID, RebirthPayload.CODEC);
        ClientPlayNetworking.registerGlobalReceiver(RebirthPayload.ID,
                (p, ctx) -> start(p.action(), p.ticks(), p.pitch()));
        ClientTickEvents.END_CLIENT_TICK.register(RebirthClient::tick);
        ClientPlayConnectionEvents.DISCONNECT.register((h, c) -> state = 0);

        // Test tanpa plugin: /rebirthtest lift  dan  /rebirthtest lower
        ClientCommandRegistrationCallback.EVENT.register((d, r) ->
                d.register(ClientCommandManager.literal("rebirthtest")
                        .then(ClientCommandManager.literal("lift").executes(c -> {
                            start(LIFT, 20, -75);
                            return 1;
                        }))
                        .then(ClientCommandManager.literal("lower").executes(c -> {
                            start(1, 60, 0);
                            return 1;
                        }))));
    }

    private static void start(int action, int ticks, int pitch) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        from = mc.player.getPitch();
        to = (action == LIFT) ? MathHelper.clamp(pitch, -90, 90) : 0f;
        release = (action != LIFT);
        dur = Math.max(1, ticks);
        elapsed = 0;
        hold = 0;
        state = 1;
    }

    private static void tick(MinecraftClient mc) {
        if (state == 0 || mc.player == null) return;
        if (state == 1) {
            elapsed++;
            float t = Math.min(1f, elapsed / (float) dur);
            float e = t * t * (3f - 2f * t);
            mc.player.setPitch(from + (to - from) * e);
            if (t >= 1f) {
                if (release) state = 0;
                else { state = 2; hold = 0; }
            }
        } else if (++hold > MAX_HOLD) {
            state = 0;
        } else {
            mc.player.setPitch(to);
        }
    }
}
