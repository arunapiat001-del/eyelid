package com.deathdim.eyelid;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;

public class CameraOrbit implements ClientModInitializer {
    public static final double RADIUS = 3.5;      // jarak kamera dari pemain (blok)
    public static final double HEIGHT = 0.7;      // tinggi kamera di atas titik fokus
    private static final double SPEED = 0.8;      // kecepatan putar (radian/detik)

    private static boolean active = false, stopping = false, angleInit = false;
    private static long startMs = 0, stopMs = 0, inMs = 1, outMs = 1;
    private static double angle0 = 0;
    private static Perspective previous = Perspective.FIRST_PERSON;

    @Override
    public void onInitializeClient() {
        PayloadTypeRegistry.playS2C().register(CameraPayload.ID, CameraPayload.CODEC);
        ClientPlayNetworking.registerGlobalReceiver(CameraPayload.ID, (p, ctx) -> {
            if (p.action() == 0) start(p.ticks());
            else stop(p.ticks());
        });
        ClientPlayConnectionEvents.DISCONNECT.register((h, c) -> reset());

        // Test: /orbittest start  dan  /orbittest stop
        ClientCommandRegistrationCallback.EVENT.register((d, r) ->
                d.register(ClientCommandManager.literal("orbittest")
                        .then(ClientCommandManager.literal("start").executes(c -> {
                            start(10);
                            return 1;
                        }))
                        .then(ClientCommandManager.literal("stop").executes(c -> {
                            stop(20);
                            return 1;
                        }))));
    }

    public static void start(int ticks) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        if (!active) {
            previous = mc.options.getPerspective();
            mc.options.setPerspective(Perspective.THIRD_PERSON_BACK);
        }
        active = true;
        stopping = false;
        angleInit = false;
        startMs = System.currentTimeMillis();
        inMs = Math.max(1, ticks) * 50L;
    }

    public static void stop(int ticks) {
        if (!active) return;
        stopping = true;
        stopMs = System.currentTimeMillis();
        outMs = Math.max(1, ticks) * 50L;
    }

    public static void reset() {
        if (active) MinecraftClient.getInstance().options.setPerspective(previous);
        active = false;
        stopping = false;
    }

    private static float smooth(float t) {
        return t * t * (3f - 2f * t);
    }

    /** 0 = kamera normal, 1 = kamera orbit penuh. */
    public static float weight() {
        if (!active) return 0f;
        long now = System.currentTimeMillis();
        float w = smooth(Math.min(1f, (now - startMs) / (float) inMs));
        if (stopping) {
            float o = Math.min(1f, (now - stopMs) / (float) outMs);
            if (o >= 1f) {
                reset();
                return 0f;
            }
            w *= 1f - smooth(o);
        }
        return w;
    }

    /** Titik awal orbit diambil dari posisi kamera normal (di belakang pemain). */
    public static void initAngle(double dx, double dz) {
        if (angleInit || dx * dx + dz * dz < 1.0) return;
        angle0 = Math.atan2(dz, dx);
        angleInit = true;
    }

    public static double angle() {
        return angle0 + (System.currentTimeMillis() - startMs) / 1000.0 * SPEED;
    }
}
