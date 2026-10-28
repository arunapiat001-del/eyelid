package com.deathdim.eyelid;

import net.minecraft.client.gui.DrawContext;

public final class EyeAnimation {
    public static final int CLOSE = 0;
    public static final int OPEN = 1;
    private static final int BLACK = 0xFF000000;

    private static int state = 0;
    private static long startMs = 0;
    private static long durMs = 1000;

    private EyeAnimation() {}

    public static void handle(int action, int ticks) {
        durMs = Math.max(1, ticks) * 50L;
        startMs = System.currentTimeMillis();
        state = (action == CLOSE) ? 1 : 3;
    }

    public static void reset() {
        state = 0;
    }

    private static float cover() {
        if (state == 0) return 0f;
        if (state == 2) return 1f;
        float t = Math.min(1f, (System.currentTimeMillis() - startMs) / (float) durMs);
        float e = t * t * (3f - 2f * t);
        if (state == 1) {
            if (t >= 1f) state = 2;
            return e;
        }
        if (t >= 1f) state = 0;
        return 1f - e;
    }

    public static void render(DrawContext context) {
        float cover = cover();
        if (cover <= 0f) return;
        int w = context.getScaledWindowWidth();
        int h = context.getScaledWindowHeight();
        if (cover >= 1f) {
            context.fill(0, 0, w, h, BLACK);
            return;
        }
        int step = 2;
        for (int x = 0; x < w; x += step) {
            float nx = (2f * (x + step / 2f) / w) - 1f;
            float d = nx * nx;
            float c = Math.max(0f, Math.min(1f, cover * 1.5f - 0.5f * d));
            int lid = (int) Math.ceil(c * h / 2f);
            if (lid <= 0) continue;
            int x2 = Math.min(w, x + step);
            context.fill(x, 0, x2, lid, BLACK);
            context.fill(x, h - lid, x2, h, BLACK);
        }
    }
}
