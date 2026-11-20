package de.iceesvisuals;

import net.minecraft.client.texture.NativeImage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates animated weapon textures (butterfly knife, combat knife, glow sword)
 * and item model files for the resource pack. The animation is a vanilla animated
 * texture (.mcmeta), so no rendering hooks are needed.
 */
public final class WeaponSkins {
    public static final String[] SKINS = {
            "Vanilla", "Butterfly Knife (spinning)", "Butterfly Knife (static)", "Combat Knife", "Glow Sword"};
    public static final String[] BASE_ITEMS = {
            "netherite_sword", "diamond_sword", "iron_sword", "golden_sword", "stone_sword", "wooden_sword"};
    public static final String[] PALETTE_NAMES = {"Ice", "Fire", "Void", "Gold", "Toxic"};
    public static final String[] SPEED_NAMES = {"Slow", "Normal", "Fast"};

    // {blade, handle}
    private static final int[][] PALETTES = {
            {0xFF9FE8FF, 0xFF2B5C8A},
            {0xFFFFB347, 0xFF7A1F0E},
            {0xFFC77DFF, 0xFF2A1040},
            {0xFFFFE066, 0xFF5C4410},
            {0xFF8CFF6B, 0xFF1F4D1A}
    };
    private static final int[] FRAMETIME = {3, 2, 1};
    private static final int SIZE = 32;
    private static final int METAL = 0xFFC8C8C8;
    private static final int OUTLINE = 0xFF101018;

    private record Seg(double x0, double y0, double x1, double y1, double r0, double r1, int color) {}

    private WeaponSkins() {}

    public static void write(Path pack, ThemeManager.Theme t) throws IOException {
        if (t.weaponSkin <= 0 || t.weaponSkin >= SKINS.length) return;
        int skin = t.weaponSkin;
        int[] pal = PALETTES[Math.floorMod(t.palette, PALETTES.length)];
        int frames = skin == 1 ? 16 : skin == 4 ? 8 : 1;

        Path texDir = pack.resolve("assets/iceesvisuals/textures/item");
        Files.createDirectories(texDir);

        NativeImage img = new NativeImage(NativeImage.Format.RGBA, SIZE, SIZE * frames, true);
        try {
            for (int y = 0; y < SIZE * frames; y++)
                for (int x = 0; x < SIZE; x++) img.setColorArgb(x, y, 0);
            for (int k = 0; k < frames; k++) drawFrame(img, k * SIZE, skin, k, frames, pal[0], pal[1]);
            img.writeTo(texDir.resolve("weapon.png"));
        } finally {
            img.close();
        }

        if (frames > 1) {
            int ft = FRAMETIME[Math.floorMod(t.speed, FRAMETIME.length)];
            Files.writeString(texDir.resolve("weapon.png.mcmeta"),
                    "{\"animation\":{\"frametime\":" + ft + ",\"interpolate\":false}}", StandardCharsets.UTF_8);
        }

        Path modelDir = pack.resolve("assets/iceesvisuals/models/item");
        Files.createDirectories(modelDir);
        Files.writeString(modelDir.resolve("weapon.json"),
                "{\"parent\":\"minecraft:item/handheld\",\"textures\":{\"layer0\":\"iceesvisuals:item/weapon\"}}",
                StandardCharsets.UTF_8);

        Path itemsDir = pack.resolve("assets/minecraft/items");
        Files.createDirectories(itemsDir);
        String base = BASE_ITEMS[Math.floorMod(t.weaponItem, BASE_ITEMS.length)];
        Files.writeString(itemsDir.resolve(base + ".json"),
                "{\"model\":{\"type\":\"minecraft:model\",\"model\":\"iceesvisuals:item/weapon\"}}",
                StandardCharsets.UTF_8);
    }

    private static void drawFrame(NativeImage img, int yOff, int skin, int k, int frames, int blade, int handle) {
        List<Seg> s = new ArrayList<>();
        double rot = Math.toRadians(-45); // blade tip points to the top right
        double phase = 2 * Math.PI * k / frames;

        switch (skin) {
            case 1 -> { // spinning butterfly knife: rotates and opens/closes its handles
                rot += phase;
                double h = 1.4 + 3.0 * (0.5 + 0.5 * Math.sin(phase * 2));
                butterfly(s, h, blade, handle);
            }
            case 2 -> butterfly(s, 1.4, blade, handle);
            case 3 -> {
                s.add(new Seg(-2, 0, 14, 0, 2.0, 0.6, blade));
                s.add(new Seg(-3, -3, -3, 3, 0.9, 0.9, METAL));
                s.add(new Seg(-3, 0, -14, 0, 1.7, 1.7, handle));
            }
            case 4 -> { // glow sword: blade pulses towards white
                double glow = 0.5 + 0.5 * Math.sin(phase);
                int b = lerp(blade, 0xFFFFFFFF, glow * 0.6);
                s.add(new Seg(-2, 0, 15, 0, 1.8, 0.9, b));
                s.add(new Seg(-3, -4, -3, 4, 0.9, 0.9, METAL));
                s.add(new Seg(-3, 0, -11, 0, 1.5, 1.5, handle));
                s.add(new Seg(-12, 0, -12, 0, 1.8, 1.8, METAL));
            }
            default -> { }
        }
        render(img, yOff, s, rot, true);
        render(img, yOff, s, rot, false);
    }

    private static void butterfly(List<Seg> s, double h, int blade, int handle) {
        s.add(new Seg(-1, 0, 14, 0, 1.8, 0.6, blade));
        s.add(new Seg(-3, -h, -14, -h, 1.5, 1.5, handle));
        s.add(new Seg(-3, h, -14, h, 1.5, 1.5, handle));
        s.add(new Seg(-3, 0, -3, 0, 1.2, 1.2, METAL));
    }

    private static void render(NativeImage img, int yOff, List<Seg> segs, double rot, boolean outline) {
        double c = Math.cos(rot), sn = Math.sin(rot);
        for (Seg g : segs) {
            double len = Math.hypot(g.x1() - g.x0(), g.y1() - g.y0());
            int steps = (int) Math.max(1, Math.ceil(len * 2));
            for (int i = 0; i <= steps; i++) {
                double f = (double) i / steps;
                double lx = g.x0() + (g.x1() - g.x0()) * f;
                double ly = g.y0() + (g.y1() - g.y0()) * f;
                double r = g.r0() + (g.r1() - g.r0()) * f + (outline ? 0.9 : 0);
                double px = 16 + lx * c - ly * sn;
                double py = 16 + lx * sn + ly * c;
                disk(img, yOff, px, py, r, outline ? OUTLINE : g.color());
            }
        }
    }

    private static void disk(NativeImage img, int yOff, double px, double py, double r, int color) {
        for (int x = (int) Math.floor(px - r); x <= (int) Math.ceil(px + r); x++) {
            for (int y = (int) Math.floor(py - r); y <= (int) Math.ceil(py + r); y++) {
                if (x < 0 || y < 0 || x >= SIZE || y >= SIZE) continue;
                double dx = x + 0.5 - px, dy = y + 0.5 - py;
                if (dx * dx + dy * dy <= r * r) img.setColorArgb(x, yOff + y, color);
            }
        }
    }

    private static int lerp(int a, int b, double t) {
        int ar = (a >> 16) & 255, ag = (a >> 8) & 255, ab = a & 255;
        int br = (b >> 16) & 255, bg = (b >> 8) & 255, bb = b & 255;
        int r = (int) Math.round(ar + (br - ar) * t);
        int g = (int) Math.round(ag + (bg - ag) * t);
        int bl = (int) Math.round(ab + (bb - ab) * t);
        return 0xFF000000 | (r << 16) | (g << 8) | bl;
    }
}
