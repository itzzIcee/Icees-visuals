package de.iceesvisuals;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourcePackManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

public final class ThemeManager {
    public static final String PACK_ID = "file/IceesVisuals";

    public static final class Theme {
        public String name;
        public int accent = 0xFF55AAFF;      // ARGB menu accent color
        public int background = 0xE0101018;  // ARGB menu background
        public List<String> textures = new ArrayList<>(); // e.g. "minecraft:block/stone"
        public int sky = -1;                 // index into SKY_FILES, -1 = none
        public int weaponSkin = 0;           // index into WeaponSkins.SKINS, 0 = vanilla
        public int weaponItem = 1;           // index into WeaponSkins.BASE_ITEMS
        public int palette = 0;              // index into WeaponSkins palettes
        public int speed = 1;                // index into WeaponSkins speeds

        public Theme() {}
        public Theme(String name) { this.name = name; }
    }

    private static final class Data {
        List<Theme> themes = new ArrayList<>();
        int current = 0;
    }

    public static final String[] SKY_FILES = {"planet", "rauch", "nebel", "marmor", "sturm", "feuer"};
    public static final String[] SKY_NAMES = {"Purple Planet", "Grey Smoke", "Purple Nebula", "Purple Marble", "Storm", "Fire"};
    private static final String SKY_ID = "minecraft:environment/end_sky";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Data data = new Data();

    private static Path root() {
        return FabricLoader.getInstance().getConfigDir().resolve("iceesvisuals");
    }

    private static Path themeDir(Theme t) {
        return root().resolve("themes").resolve(t.name.replaceAll("[^A-Za-z0-9_-]", "_"));
    }

    public static void load() {
        try {
            Path f = root().resolve("themes.json");
            if (Files.exists(f)) {
                Data d = GSON.fromJson(Files.readString(f, StandardCharsets.UTF_8), Data.class);
                if (d != null && d.themes != null) data = d;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (data.themes.isEmpty()) data.themes.add(new Theme("Default"));
        data.current = Math.max(0, Math.min(data.current, data.themes.size() - 1));
    }

    public static void save() {
        try {
            Files.createDirectories(root());
            Files.writeString(root().resolve("themes.json"), GSON.toJson(data), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Theme current() { return data.themes.get(data.current); }

    public static void cycle(int dir) {
        int n = data.themes.size();
        data.current = ((data.current + dir) % n + n) % n;
        save();
    }

    public static void create(String name) throws IOException {
        name = name.trim();
        if (name.isEmpty()) throw new IOException("Name is empty");
        for (Theme t : data.themes) if (t.name.equalsIgnoreCase(name)) throw new IOException("Theme already exists");
        data.themes.add(new Theme(name));
        data.current = data.themes.size() - 1;
        save();
    }

    public static void deleteCurrent() throws IOException {
        if (data.themes.size() <= 1) throw new IOException("You can't delete the last theme");
        Theme t = current();
        deleteRecursive(themeDir(t));
        data.themes.remove(t);
        data.current = Math.max(0, data.current - 1);
        save();
    }

    /** Copies a PNG into the theme and remembers the texture ID. */
    public static String addTexture(Theme t, String rawId, String rawPath) throws IOException {
        String id = normalizeId(rawId);
        Path src = Path.of(rawPath.trim().replace("\"", ""));
        if (!Files.isRegularFile(src) || !src.toString().toLowerCase(Locale.ROOT).endsWith(".png")) {
            throw new IOException("PNG file not found");
        }
        String[] p = id.split(":", 2);
        Path dst = themeDir(t).resolve(p[0]).resolve(p[1] + ".png");
        Files.createDirectories(dst.getParent());
        Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING);
        if (!t.textures.contains(id)) t.textures.add(id);
        save();
        return id;
    }

    public static boolean removeTexture(Theme t, String rawId) throws IOException {
        String id = normalizeId(rawId);
        if (!t.textures.remove(id)) return false;
        String[] p = id.split(":", 2);
        Files.deleteIfExists(themeDir(t).resolve(p[0]).resolve(p[1] + ".png"));
        save();
        return true;
    }

    private static String normalizeId(String raw) throws IOException {
        String id = raw.trim().toLowerCase(Locale.ROOT);
        if (id.endsWith(".png")) id = id.substring(0, id.length() - 4);
        if (!id.contains(":")) id = "minecraft:" + id;
        if (!id.matches("[a-z0-9_.-]+:[a-z0-9_./-]+") || id.contains("..")) {
            throw new IOException("Invalid texture ID (e.g. block/stone)");
        }
        return id;
    }

    /** Sets one of the bundled example skies as the End sky of the theme (index -1 removes it). */
    public static void setSky(Theme t, int index) throws IOException {
        Path dst = themeDir(t).resolve("minecraft").resolve("environment").resolve("end_sky.png");
        if (index < 0) {
            Files.deleteIfExists(dst);
            t.textures.remove(SKY_ID);
        } else {
            try (InputStream in = ThemeManager.class.getResourceAsStream(
                    "/assets/iceesvisuals/skies/" + SKY_FILES[index] + ".png")) {
                if (in == null) throw new IOException("Sky file missing from the mod");
                Files.createDirectories(dst.getParent());
                Files.copy(in, dst, StandardCopyOption.REPLACE_EXISTING);
            }
            if (!t.textures.contains(SKY_ID)) t.textures.add(SKY_ID);
        }
        t.sky = index;
        save();
    }

    /** Builds the "IceesVisuals" resource pack from the theme and reloads all resources. */
    public static void apply(MinecraftClient mc, Theme t) throws IOException {
        Path pack = FabricLoader.getInstance().getGameDir().resolve("resourcepacks").resolve("IceesVisuals");
        deleteRecursive(pack);
        Files.createDirectories(pack);
        Files.writeString(pack.resolve("pack.mcmeta"),
                "{\"pack\":{\"description\":\"Icee's Visuals: " + t.name.replace("\"", "") + "\",\"min_format\":65,\"max_format\":75}}",
                StandardCharsets.UTF_8);

        for (String id : t.textures) {
            String[] p = id.split(":", 2);
            Path src = themeDir(t).resolve(p[0]).resolve(p[1] + ".png");
            if (!Files.exists(src)) continue;
            Path dst = pack.resolve("assets").resolve(p[0]).resolve("textures").resolve(p[1] + ".png");
            Files.createDirectories(dst.getParent());
            Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING);
        }

        WeaponSkins.write(pack, t);

        mc.execute(() -> {
            ResourcePackManager m = mc.getResourcePackManager();
            m.scanPacks();
            m.enable(PACK_ID);
            mc.options.refreshResourcePacks(m);
        });
    }

    public static void disablePack(MinecraftClient mc) {
        mc.execute(() -> {
            ResourcePackManager m = mc.getResourcePackManager();
            m.scanPacks();
            m.disable(PACK_ID);
            mc.options.refreshResourcePacks(m);
        });
    }

    private static void deleteRecursive(Path p) throws IOException {
        if (!Files.exists(p)) return;
        try (Stream<Path> s = Files.walk(p)) {
            for (Path x : (Iterable<Path>) s.sorted(Comparator.reverseOrder())::iterator) Files.delete(x);
        }
    }
}
