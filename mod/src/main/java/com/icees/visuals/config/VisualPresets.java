package com.icees.visuals.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.icees.visuals.IceesVisualsClient;
import com.icees.visuals.cosmetics.AuraStyle;
import com.icees.visuals.cosmetics.CapeStyle;
import com.icees.visuals.cosmetics.HaloStyle;
import com.icees.visuals.cosmetics.HatStyle;
import com.icees.visuals.cosmetics.JumpCircleStyle;
import com.icees.visuals.cosmetics.ShieldStyle;
import com.icees.visuals.cosmetics.WingStyle;
import com.icees.visuals.effects.FallTheme;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Named slots that store Themes + Cosmetics looks.
 * File: {@code config/icees-visuals-presets.json}
 */
public final class VisualPresets {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("icees-visuals-presets.json");
	public static final int SLOT_COUNT = 5;

	private static final String[] DEFAULT_NAMES = {
		"Soft Pink", "Arena", "Rainy", "Crystal", "Custom"
	};

	private static Store store = new Store();

	private VisualPresets() {}

	public static void load() {
		if (!Files.isRegularFile(PATH)) {
			ensureSlots();
			saveFile();
			return;
		}
		try (Reader reader = Files.newBufferedReader(PATH)) {
			Store loaded = GSON.fromJson(reader, Store.class);
			if (loaded != null && loaded.slots != null) store = loaded;
		} catch (Exception e) {
			IceesVisualsClient.LOGGER.warn("Failed to load visual presets", e);
		}
		ensureSlots();
	}

	private static void ensureSlots() {
		if (store.slots == null || store.slots.length != SLOT_COUNT) {
			Slot[] next = new Slot[SLOT_COUNT];
			for (int i = 0; i < SLOT_COUNT; i++) {
				next[i] = (store.slots != null && i < store.slots.length && store.slots[i] != null)
					? store.slots[i] : emptySlot(DEFAULT_NAMES[i]);
			}
			store.slots = next;
		}
		for (int i = 0; i < SLOT_COUNT; i++) {
			if (store.slots[i] == null) store.slots[i] = emptySlot(DEFAULT_NAMES[i]);
			if (store.slots[i].name == null || store.slots[i].name.isBlank()) {
				store.slots[i].name = DEFAULT_NAMES[i];
			}
		}
	}

	private static Slot emptySlot(String name) {
		Slot s = new Slot();
		s.name = name;
		s.filled = false;
		return s;
	}

	private static void saveFile() {
		try {
			Files.createDirectories(PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(PATH)) {
				GSON.toJson(store, writer);
			}
		} catch (IOException e) {
			IceesVisualsClient.LOGGER.warn("Failed to save visual presets", e);
		}
	}

	public static String name(int slot) {
		ensureSlots();
		return store.slots[clamp(slot)].name;
	}

	public static boolean isFilled(int slot) {
		ensureSlots();
		return store.slots[clamp(slot)].filled;
	}

	public static void cycleName(int slot) {
		ensureSlots();
		Slot s = store.slots[clamp(slot)];
		int idx = 0;
		for (int i = 0; i < DEFAULT_NAMES.length; i++) {
			if (DEFAULT_NAMES[i].equalsIgnoreCase(s.name)) { idx = i; break; }
		}
		s.name = DEFAULT_NAMES[(idx + 1) % DEFAULT_NAMES.length];
		saveFile();
	}

	/** Snapshot current themes + cosmetics into this slot. */
	public static void saveCurrent(int slot) {
		ensureSlots();
		Slot s = store.slots[clamp(slot)];
		s.filled = true;
		s.wings = IceesConfig.wings; s.wingStyle = IceesConfig.wingStyle.name();
		s.halo = IceesConfig.halo; s.haloStyle = IceesConfig.haloStyle.name();
		s.hat = IceesConfig.hat; s.hatStyle = IceesConfig.hatStyle.name();
		s.cape = IceesConfig.cape; s.capeStyle = IceesConfig.capeStyle.name();
		s.aura = IceesConfig.aura; s.auraStyle = IceesConfig.auraStyle.name();
		s.shield = IceesConfig.shield; s.shieldStyle = IceesConfig.shieldStyle.name();
		s.trail = IceesConfig.trail; s.jumpCircles = IceesConfig.jumpCircles;
		s.jumpCircleStyle = IceesConfig.jumpCircleStyle.name();
		s.jumpCircleSize = IceesConfig.jumpCircleSize;
		s.jumpCircleSpeed = IceesConfig.jumpCircleSpeed;
		s.jumpCircleCount = IceesConfig.jumpCircleCount;
		s.wingSize = IceesConfig.wingSize; s.haloSize = IceesConfig.haloSize;
		s.hatSize = IceesConfig.hatSize; s.capeSize = IceesConfig.capeSize;
		s.auraSize = IceesConfig.auraSize; s.shieldSize = IceesConfig.shieldSize;
		s.cosmeticBrightness = IceesConfig.cosmeticBrightness;
		s.wingTint = IceesConfig.wingTint.copy();
		s.haloTint = IceesConfig.haloTint.copy();
		s.capeTint = IceesConfig.capeTint.copy();
		s.auraTint = IceesConfig.auraTint.copy();
		s.shieldTint = IceesConfig.shieldTint.copy();
		s.hatTint = IceesConfig.hatTint.copy();
		s.trailTint = IceesConfig.trailTint.copy();
		s.jumpTint = IceesConfig.jumpTint.copy();
		s.fallTheme = IceesConfig.fallTheme.name();
		s.fallIntensity = IceesConfig.fallIntensity;
		s.fallSize = IceesConfig.fallSize;
		s.fallSpeed = IceesConfig.fallSpeed;
		s.fallSpread = IceesConfig.fallSpread;
		s.fallRangeChunks = IceesConfig.fallRangeChunks;
		s.fallOpacity = IceesConfig.fallOpacity;
		saveFile();
	}

	/** Apply a filled slot onto live config (themes + cosmetics). */
	public static boolean loadSlot(int slot) {
		ensureSlots();
		Slot s = store.slots[clamp(slot)];
		if (!s.filled) return false;
		IceesConfig.wings = s.wings;
		IceesConfig.wingStyle = enumOr(WingStyle.class, s.wingStyle, WingStyle.PETAL);
		IceesConfig.halo = s.halo;
		IceesConfig.haloStyle = enumOr(HaloStyle.class, s.haloStyle, HaloStyle.BLOSSOM);
		IceesConfig.hat = s.hat;
		IceesConfig.hatStyle = enumOr(HatStyle.class, s.hatStyle, HatStyle.STRAW_RED);
		IceesConfig.cape = s.cape;
		IceesConfig.capeStyle = enumOr(CapeStyle.class, s.capeStyle, CapeStyle.SAKURA);
		IceesConfig.aura = s.aura;
		IceesConfig.auraStyle = enumOr(AuraStyle.class, s.auraStyle, AuraStyle.ORBIT);
		IceesConfig.shield = false; // shield cosmetic removed
		IceesConfig.shieldStyle = enumOr(ShieldStyle.class, s.shieldStyle, ShieldStyle.SAKURA);
		IceesConfig.trail = s.trail;
		IceesConfig.jumpCircles = s.jumpCircles;
		IceesConfig.jumpCircleStyle = enumOr(JumpCircleStyle.class, s.jumpCircleStyle, JumpCircleStyle.RIPPLE);
		if (s.jumpCircleSize >= 1) IceesConfig.jumpCircleSize = IceesConfig.clamp10(s.jumpCircleSize);
		if (s.jumpCircleSpeed >= 1) IceesConfig.jumpCircleSpeed = IceesConfig.clamp10(s.jumpCircleSpeed);
		if (s.jumpCircleCount >= 1) IceesConfig.jumpCircleCount = IceesConfig.clamp10(s.jumpCircleCount);
		IceesConfig.wingSize = IceesConfig.clamp10(s.wingSize > 0 ? s.wingSize : 5);
		IceesConfig.haloSize = IceesConfig.clamp10(s.haloSize > 0 ? s.haloSize : 5);
		IceesConfig.hatSize = IceesConfig.clamp10(s.hatSize > 0 ? s.hatSize : 5);
		IceesConfig.capeSize = IceesConfig.clamp10(s.capeSize > 0 ? s.capeSize : 5);
		IceesConfig.auraSize = IceesConfig.clamp10(s.auraSize > 0 ? s.auraSize : 5);
		IceesConfig.shieldSize = IceesConfig.clamp10(s.shieldSize > 0 ? s.shieldSize : 5);
		if (s.cosmeticBrightness >= 1) IceesConfig.cosmeticBrightness = IceesConfig.clamp10(s.cosmeticBrightness);
		applyTint(IceesConfig.wingTint, s.wingTint);
		applyTint(IceesConfig.haloTint, s.haloTint);
		applyTint(IceesConfig.capeTint, s.capeTint);
		applyTint(IceesConfig.auraTint, s.auraTint);
		applyTint(IceesConfig.shieldTint, s.shieldTint);
		applyTint(IceesConfig.hatTint, s.hatTint);
		applyTint(IceesConfig.trailTint, s.trailTint != null ? s.trailTint : s.auraTint);
		applyTint(IceesConfig.jumpTint, s.jumpTint != null ? s.jumpTint : s.auraTint);
		IceesConfig.fallTheme = enumOr(FallTheme.class, s.fallTheme, FallTheme.SAKURA_FALL);
		if (s.fallIntensity >= 1) IceesConfig.fallIntensity = IceesConfig.clamp10(s.fallIntensity);
		if (s.fallSize >= 1) IceesConfig.fallSize = IceesConfig.clamp10(s.fallSize);
		if (s.fallSpeed >= 1) IceesConfig.fallSpeed = IceesConfig.clamp10(s.fallSpeed);
		if (s.fallSpread >= 1) IceesConfig.fallSpread = IceesConfig.clamp10(s.fallSpread);
		if (s.fallRangeChunks >= 1) IceesConfig.fallRangeChunks = IceesConfig.clampChunks(s.fallRangeChunks);
		if (s.fallOpacity >= 1) IceesConfig.fallOpacity = IceesConfig.clamp10(s.fallOpacity);
		IceesConfig.save();
		return true;
	}

	public static void clearSlot(int slot) {
		ensureSlots();
		String name = store.slots[clamp(slot)].name;
		store.slots[clamp(slot)] = emptySlot(name != null ? name : DEFAULT_NAMES[clamp(slot)]);
		saveFile();
	}

	private static void applyTint(CosmeticTint target, CosmeticTint src) {
		if (src == null) return;
		target.enabled = src.enabled;
		target.hue = Math.floorMod(src.hue, 360);
		target.sat = Math.max(0, Math.min(100, src.sat));
		target.val = Math.max(0, Math.min(100, src.val));
	}

	private static <T extends Enum<T>> T enumOr(Class<T> type, String name, T fallback) {
		if (name == null) return fallback;
		try { return Enum.valueOf(type, name); } catch (IllegalArgumentException e) { return fallback; }
	}

	private static int clamp(int slot) {
		return Math.max(0, Math.min(SLOT_COUNT - 1, slot));
	}

	private static final class Store {
		Slot[] slots = new Slot[SLOT_COUNT];
	}

	@SuppressWarnings("unused")
	private static final class Slot {
		String name = "Custom";
		boolean filled;
		boolean wings = true; String wingStyle = "PETAL";
		boolean halo = true; String haloStyle = "BLOSSOM";
		boolean hat = true; String hatStyle = "STRAW_RED";
		boolean cape = true; String capeStyle = "SAKURA";
		boolean aura = true; String auraStyle = "ORBIT";
		boolean shield = false; String shieldStyle = "SAKURA";
		boolean trail = false; boolean jumpCircles = true;
		String jumpCircleStyle = "RIPPLE";
		int jumpCircleSize = 5; int jumpCircleSpeed = 5; int jumpCircleCount = 4;
		int wingSize = 5; int haloSize = 5; int hatSize = 5; int capeSize = 5; int auraSize = 5; int shieldSize = 5;
		int cosmeticBrightness = 7;
		CosmeticTint wingTint; CosmeticTint haloTint; CosmeticTint capeTint; CosmeticTint auraTint; CosmeticTint shieldTint; CosmeticTint hatTint;
		CosmeticTint trailTint; CosmeticTint jumpTint;
		String fallTheme = "SAKURA_FALL";
		int fallIntensity = 5; int fallSize = 5; int fallSpeed = 5; int fallSpread = 5;
		int fallRangeChunks = 3; int fallOpacity = 7;
	}
}
