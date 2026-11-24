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
import com.icees.visuals.effects.CustomThemeStyle;
import com.icees.visuals.effects.FallTheme;
import com.icees.visuals.effects.SkyStyle;
import com.icees.visuals.overlay.OverlayMaterial;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.ARGB;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class IceesConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("icees-visuals.json");

	public static boolean wings = true;
	public static WingStyle wingStyle = WingStyle.PETAL;
	public static boolean halo = true;
	public static HaloStyle haloStyle = HaloStyle.BLOSSOM;
	public static boolean hat = true;
	public static HatStyle hatStyle = HatStyle.STRAW_RED;
	public static boolean cape = true;
	public static CapeStyle capeStyle = CapeStyle.SAKURA;
	public static boolean aura = true;
	public static AuraStyle auraStyle = AuraStyle.ORBIT;
	public static boolean shield = false;
	public static ShieldStyle shieldStyle = ShieldStyle.SAKURA;
	public static boolean trail = false;
	public static boolean jumpCircles = true;
	public static JumpCircleStyle jumpCircleStyle = JumpCircleStyle.RIPPLE;
	public static int jumpCircleSize = 5;
	public static int jumpCircleSpeed = 5;
	public static int jumpCircleCount = 4;
	public static FallTheme fallTheme = FallTheme.SAKURA_FALL;
	public static int fallIntensity = 5;
	public static int fallSize = 5;
	public static int fallSpeed = 5;
	public static int fallSpread = 5;
	/** How far themes fill around you, in chunks (1–8). */
	public static int fallRangeChunks = 3;
	public static int fallOpacity = 7;
	/** Custom fall theme colour + particle style (used when fallTheme == CUSTOM). */
	public static CustomThemeStyle customThemeStyle = CustomThemeStyle.PETALS;
	public static int customThemeHue = 330;
	public static int customThemeSat = 70;
	public static int customThemeVal = 100;

	/* —— Custom Skies —— */
	public static SkyStyle skyStyle = SkyStyle.OFF;
	public static boolean skyAnimated = true;
	public static int skySpeed = 5;
	public static int skyBrightness = 8;

	public static int cosmeticBrightness = 7;
	/** Legacy global size — migrated into per-cosmetic sizes. */
	public static int cosmeticSize = 5;
	public static int cosmeticThickness = 5;
	public static int wingSize = 5;
	public static int haloSize = 5;
	public static int hatSize = 5;
	public static int capeSize = 5;
	public static int auraSize = 5;
	public static int shieldSize = 5;
	/** Kept for config migration — use per-cosmetic tints. */
	@Deprecated
	public static boolean customColor = true;
	public static int cosmeticHue = 330;
	public static int cosmeticSat = 70;
	public static int cosmeticVal = 100;
	/** Per-cosmetic colours. */
	public static CosmeticTint wingTint = new CosmeticTint(true, 330, 70, 100);
	public static CosmeticTint haloTint = new CosmeticTint(true, 320, 65, 100);
	public static CosmeticTint capeTint = new CosmeticTint(true, 330, 70, 100);
	public static CosmeticTint auraTint = new CosmeticTint(true, 310, 70, 100);
	public static CosmeticTint shieldTint = new CosmeticTint(true, 330, 55, 100);
	public static CosmeticTint hatTint = new CosmeticTint(true, 40, 55, 90);
	public static CosmeticTint trailTint = new CosmeticTint(true, 200, 70, 100);
	public static CosmeticTint jumpTint = new CosmeticTint(true, 310, 70, 100);

	/* —— Block Overlay —— */
	public static boolean blockOverlay = true;
	public static OverlayMaterial overlayMaterial = OverlayMaterial.RAINBOW;
	public static int overlayBrightness = 3;
	public static boolean overlayCustomColor = false;
	public static int overlayHue = 330;
	public static int overlaySat = 70;
	public static int overlayVal = 100;

	/* —— CPvP Essentials —— */
	public static boolean motionBlur = false;
	public static int motionBlurStrength = 2;
	public static boolean noHurtCam = true;
	public static boolean fullbright = false;
	public static boolean lowParticles = false;


	/* —— Orbit GUI colours —— */
	public static boolean guiCustomColor = false;
	public static boolean guiRainbow = false;
	public static int guiHue = 330;
	public static int guiSat = 55;
	public static int guiVal = 100;

	public static int orbitKeyCode = GLFW.GLFW_KEY_RIGHT_SHIFT;

	private IceesConfig() {}

	public static float brightnessFactor() {
		return 0.5f + clamp10(cosmeticBrightness) * 0.15f;
	}

	public static float thicknessFactor() {
		return sizeFactor(cosmeticSize);
	}

	public static float sizeFactor(int level) {
		return 0.65f + clamp10(level) * 0.16f;
	}

	public static int clamp10(int v) {
		return Math.max(1, Math.min(10, v));
	}

	public static float fallSpeedFactor() {
		return 0.45f + clamp10(fallSpeed) * 0.12f;
	}

	public static float fallSpreadFactor() {
		return 0.55f + clamp10(fallSpread) * 0.12f;
	}

	public static int clampChunks(int v) {
		return Math.max(1, Math.min(8, v));
	}

	/** Theme fill radius in blocks (chunks × 16). */
	public static float fallRangeBlocks() {
		return clampChunks(fallRangeChunks) * 16f;
	}

	public static float fallOpacityFactor() {
		return 0.35f + clamp10(fallOpacity) * 0.065f;
	}

	public static float sizeFactor() {
		return thicknessFactor();
	}

	public static int sizeOf(String which) {
		return switch (which) {
			case "wing" -> wingSize;
			case "halo" -> haloSize;
			case "hat" -> hatSize;
			case "cape" -> capeSize;
			case "aura" -> auraSize;
			case "shield" -> shieldSize;
			case "jump" -> jumpCircleSize;
			default -> cosmeticSize;
		};
	}

	public static void setSizeOf(String which, int v) {
		v = clamp10(v);
		switch (which) {
			case "wing" -> wingSize = v;
			case "halo" -> haloSize = v;
			case "hat" -> hatSize = v;
			case "cape" -> capeSize = v;
			case "aura" -> auraSize = v;
			case "shield" -> shieldSize = v;
			case "jump" -> jumpCircleSize = v;
			default -> { cosmeticSize = v; cosmeticThickness = v; }
		}
	}

	public static float overlayBrightnessFactor() {
		return 0.55f + overlayBrightness * 0.18f;
	}

	/** Legacy global tint — prefer {@link #color(CosmeticTint, float, float, float)}. */
	public static float[] color(float baseR, float baseG, float baseB) {
		return color(wingTint, baseR, baseG, baseB);
	}

	public static float[] color(CosmeticTint tint, float baseR, float baseG, float baseB) {
		return color(tint, baseR, baseG, baseB, 0.5f);
	}

	/** @param t 0..1 along gradient (or animated phase) */
	public static float[] color(CosmeticTint tint, float baseR, float baseG, float baseB, float t) {
		if (tint == null) return new float[]{baseR, baseG, baseB};
		return tint.apply(baseR, baseG, baseB, t);
	}

	public static float[] overlayColor(float time) {
		if (overlayMaterial == OverlayMaterial.CUSTOM) {
			return hsvToRgb(overlayHue, overlaySat / 100f, overlayVal / 100f);
		}
		float h = (time * 10f) % 360f;
		if (h < 0) h += 360f;
		return hsvToRgb(h, 0.9f, 1f);
	}

	/** Accent RGB for Orbit GUI (0–1). */
	public static float[] guiAccentRgb() {
		if (guiRainbow) {
			float h = (System.currentTimeMillis() / 40f) % 360f;
			return hsvToRgb(h, 0.7f, 1f);
		}
		if (guiCustomColor) {
			return hsvToRgb(guiHue, guiSat / 100f, guiVal / 100f);
		}
		return new float[]{1.00f, 0.62f, 0.75f};
	}

	public static int guiArgb(float a, float r, float g, float b) {
		return ARGB.colorFromFloat(a, r, g, b);
	}

	public static float[] hsvToRgb(float h, float s, float v) {
		h = ((h % 360f) + 360f) % 360f;
		float c = v * s;
		float x = c * (1f - Math.abs((h / 60f) % 2f - 1f));
		float m = v - c;
		float r = 0, g = 0, b = 0;
		if (h < 60) { r = c; g = x; }
		else if (h < 120) { r = x; g = c; }
		else if (h < 180) { g = c; b = x; }
		else if (h < 240) { g = x; b = c; }
		else if (h < 300) { r = x; b = c; }
		else { r = c; b = x; }
		return new float[]{r + m, g + m, b + m};
	}

	public static void applyFullbright(net.minecraft.client.Minecraft client) {
		if (client == null || client.options == null) return;
		try {
			if (fullbright) client.options.gamma().set(16.0);
			else client.options.gamma().set(1.0);
		} catch (Throwable ignored) {}
	}

	public static void load() {
		if (!Files.isRegularFile(PATH)) { save(); return; }
		try (Reader reader = Files.newBufferedReader(PATH)) {
			Data data = GSON.fromJson(reader, Data.class);
			if (data == null) return;
			wings = data.wings;
			wingStyle = safeEnum(WingStyle.class, data.wingStyle, WingStyle.PETAL);
			halo = data.halo;
			haloStyle = safeEnum(HaloStyle.class, data.haloStyle, HaloStyle.BLOSSOM);
			if (data.hatStyle != null) {
				hat = data.hat;
				hatStyle = migrateHat(data.hatStyle);
			} else {
				hat = true;
				hatStyle = HatStyle.STRAW_RED;
			}
			cape = data.cape;
			capeStyle = safeEnum(CapeStyle.class, data.capeStyle, CapeStyle.SAKURA);
			aura = data.aura;
			auraStyle = safeEnum(AuraStyle.class, data.auraStyle, AuraStyle.ORBIT);
			shield = false; // shield cosmetic removed
			shieldStyle = safeEnum(ShieldStyle.class, data.shieldStyle, ShieldStyle.SAKURA);
			trail = data.trail;
			jumpCircles = data.jumpCircles;
			jumpCircleStyle = safeEnum(JumpCircleStyle.class, data.jumpCircleStyle, JumpCircleStyle.RIPPLE);
			if (data.jumpCircleSize >= 1) jumpCircleSize = clamp10(data.jumpCircleSize);
			if (data.jumpCircleSpeed >= 1) jumpCircleSpeed = clamp10(data.jumpCircleSpeed);
			if (data.jumpCircleCount >= 1) jumpCircleCount = clamp10(data.jumpCircleCount);
			if (data.fallTheme != null) fallTheme = safeEnum(FallTheme.class, data.fallTheme, FallTheme.SAKURA_FALL);
			else fallTheme = data.petals ? FallTheme.SAKURA_FALL : FallTheme.OFF;
			if (data.fallIntensity >= 1) fallIntensity = clamp10(data.fallIntensity);
			if (data.fallSize >= 1) fallSize = clamp10(data.fallSize);
			if (data.fallSpeed >= 1) fallSpeed = clamp10(data.fallSpeed);
			if (data.fallSpread >= 1) fallSpread = clamp10(data.fallSpread);
			if (data.fallRangeChunks >= 1) fallRangeChunks = clampChunks(data.fallRangeChunks);
			if (data.fallOpacity >= 1) fallOpacity = clamp10(data.fallOpacity);
			customThemeStyle = safeEnum(CustomThemeStyle.class, data.customThemeStyle, CustomThemeStyle.PETALS);
			customThemeHue = Math.floorMod(data.customThemeHue, 360);
			if (data.customThemeSat >= 0 && data.customThemeSat <= 100) customThemeSat = data.customThemeSat;
			if (data.customThemeVal >= 0 && data.customThemeVal <= 100) customThemeVal = data.customThemeVal;
			skyStyle = safeEnum(SkyStyle.class, data.skyStyle, SkyStyle.OFF);
			skyAnimated = data.skyAnimated;
			if (data.skySpeed >= 1) skySpeed = clamp10(data.skySpeed);
			if (data.skyBrightness >= 1) skyBrightness = clamp10(data.skyBrightness);
			if (data.cosmeticBrightness >= 1) cosmeticBrightness = clamp10(data.cosmeticBrightness);
			if (data.cosmeticSize >= 1) cosmeticSize = clamp10(data.cosmeticSize);
			if (data.cosmeticThickness >= 1) cosmeticThickness = clamp10(data.cosmeticThickness);
			cosmeticSize = Math.max(cosmeticSize, cosmeticThickness);
			cosmeticThickness = cosmeticSize;
			wingSize = clamp10(data.wingSize > 0 ? data.wingSize : cosmeticSize);
			haloSize = clamp10(data.haloSize > 0 ? data.haloSize : cosmeticSize);
			hatSize = clamp10(data.hatSize > 0 ? data.hatSize : cosmeticSize);
			capeSize = clamp10(data.capeSize > 0 ? data.capeSize : cosmeticSize);
			auraSize = clamp10(data.auraSize > 0 ? data.auraSize : cosmeticSize);
			shieldSize = clamp10(data.shieldSize > 0 ? data.shieldSize : cosmeticSize);
			customColor = data.customColor;
			cosmeticHue = Math.floorMod(data.cosmeticHue, 360);
			if (data.cosmeticSat >= 0 && data.cosmeticSat <= 100) cosmeticSat = data.cosmeticSat;
			if (data.cosmeticVal >= 0 && data.cosmeticVal <= 100) cosmeticVal = data.cosmeticVal;
			CosmeticTint legacy = new CosmeticTint(customColor, cosmeticHue, cosmeticSat, cosmeticVal);
			wingTint.loadFrom(data.wingTint, legacy);
			haloTint.loadFrom(data.haloTint, legacy);
			capeTint.loadFrom(data.capeTint, legacy);
			auraTint.loadFrom(data.auraTint, legacy);
			shieldTint.loadFrom(data.shieldTint, legacy);
			hatTint.loadFrom(data.hatTint, new CosmeticTint(true, 40, 55, 90));
			trailTint.loadFrom(data.trailTint, auraTint.copy());
			jumpTint.loadFrom(data.jumpTint, auraTint.copy());

			blockOverlay = data.blockOverlay;
			overlayMaterial = OverlayMaterial.fromSaved(data.overlayMaterial);
			if (data.overlayBrightness >= 1 && data.overlayBrightness <= 5) overlayBrightness = data.overlayBrightness;
			overlayCustomColor = data.overlayCustomColor || overlayMaterial == OverlayMaterial.CUSTOM;
			if (overlayCustomColor) overlayMaterial = OverlayMaterial.CUSTOM;
			else overlayMaterial = OverlayMaterial.RAINBOW;
			overlayHue = Math.floorMod(data.overlayHue, 360);
			if (data.overlaySat >= 0 && data.overlaySat <= 100) overlaySat = data.overlaySat;
			if (data.overlayVal >= 0 && data.overlayVal <= 100) overlayVal = data.overlayVal;

			motionBlur = data.motionBlur;
			if (data.motionBlurStrength >= 1 && data.motionBlurStrength <= 5) motionBlurStrength = data.motionBlurStrength;
			noHurtCam = data.noHurtCam;
			fullbright = data.fullbright;
			lowParticles = data.lowParticles;


			guiCustomColor = data.guiCustomColor;
			guiRainbow = data.guiRainbow;
			guiHue = Math.floorMod(data.guiHue, 360);
			if (data.guiSat >= 0 && data.guiSat <= 100) guiSat = data.guiSat;
			if (data.guiVal >= 0 && data.guiVal <= 100) guiVal = data.guiVal;

			if (data.orbitKeyCode != 0) orbitKeyCode = data.orbitKeyCode;
		} catch (Exception e) {
			IceesVisualsClient.LOGGER.warn("Failed to load config", e);
		}
	}

	public static void save() {
		cosmeticThickness = cosmeticSize;
		Data data = new Data();
		data.wings = wings; data.wingStyle = wingStyle.name();
		data.halo = halo; data.haloStyle = haloStyle.name();
		data.hat = hat; data.hatStyle = hatStyle.name();
		data.cape = cape; data.capeStyle = capeStyle.name();
		data.aura = aura; data.auraStyle = auraStyle.name();
		data.shield = shield; data.shieldStyle = shieldStyle.name();
		data.trail = trail; data.jumpCircles = jumpCircles;
		data.jumpCircleStyle = jumpCircleStyle.name();
		data.jumpCircleSize = jumpCircleSize; data.jumpCircleSpeed = jumpCircleSpeed; data.jumpCircleCount = jumpCircleCount;
		data.petals = fallTheme == FallTheme.SAKURA_FALL;
		data.fallTheme = fallTheme.name();
		data.fallIntensity = fallIntensity; data.fallSize = fallSize;
		data.fallSpeed = fallSpeed; data.fallSpread = fallSpread;
		data.fallRangeChunks = fallRangeChunks; data.fallOpacity = fallOpacity;
		data.customThemeStyle = customThemeStyle.name();
		data.customThemeHue = customThemeHue;
		data.customThemeSat = customThemeSat;
		data.customThemeVal = customThemeVal;
		data.skyStyle = skyStyle.name();
		data.skyAnimated = skyAnimated;
		data.skySpeed = skySpeed;
		data.skyBrightness = skyBrightness;
		data.cosmeticBrightness = cosmeticBrightness;
		data.cosmeticSize = cosmeticSize; data.cosmeticThickness = cosmeticThickness;
		data.wingSize = wingSize; data.haloSize = haloSize; data.hatSize = hatSize;
		data.capeSize = capeSize; data.auraSize = auraSize; data.shieldSize = shieldSize;
		data.customColor = wingTint.enabled;
		data.cosmeticHue = wingTint.hue; data.cosmeticSat = wingTint.sat; data.cosmeticVal = wingTint.val;
		data.wingTint = wingTint.copy();
		data.haloTint = haloTint.copy();
		data.capeTint = capeTint.copy();
		data.auraTint = auraTint.copy();
		data.shieldTint = shieldTint.copy();
		data.hatTint = hatTint.copy();
		data.trailTint = trailTint.copy();
		data.jumpTint = jumpTint.copy();
		data.blockOverlay = blockOverlay;
		data.overlayMaterial = overlayMaterial.name();
		data.overlayBrightness = overlayBrightness;
		data.overlayCustomColor = overlayMaterial == OverlayMaterial.CUSTOM;
		data.overlayHue = overlayHue; data.overlaySat = overlaySat; data.overlayVal = overlayVal;
		data.motionBlur = motionBlur; data.motionBlurStrength = motionBlurStrength;
		data.noHurtCam = noHurtCam; data.fullbright = fullbright; data.lowParticles = lowParticles;
		data.guiCustomColor = guiCustomColor; data.guiRainbow = guiRainbow;
		data.guiHue = guiHue; data.guiSat = guiSat; data.guiVal = guiVal;
		data.orbitKeyCode = orbitKeyCode;
		try {
			Files.createDirectories(PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(PATH)) { GSON.toJson(data, writer); }
		} catch (IOException e) {
			IceesVisualsClient.LOGGER.warn("Failed to save config", e);
		}
		try {
			com.icees.visuals.network.CosmeticClientSync.onConfigChanged();
		} catch (Throwable ignored) {
			// Sync is best-effort (e.g. not connected yet).
		}
	}

	private static HatStyle migrateHat(String name) {
		if (name == null || "STRAW".equals(name)) return HatStyle.STRAW_RED;
		return safeEnum(HatStyle.class, name, HatStyle.STRAW_RED);
	}

	private static <T extends Enum<T>> T safeEnum(Class<T> type, String name, T fallback) {
		if (name == null) return fallback;
		try { return Enum.valueOf(type, name); } catch (IllegalArgumentException e) { return fallback; }
	}

	@SuppressWarnings("unused")
	private static final class Data {
		boolean wings = true; String wingStyle = "PETAL";
		boolean halo = true; String haloStyle = "BLOSSOM";
		boolean hat = true; String hatStyle = "STRAW_RED";
		boolean cape = true; String capeStyle = "SAKURA";
		boolean aura = true; String auraStyle = "ORBIT";
		boolean shield = false; String shieldStyle = "SAKURA";
		boolean trail = false; boolean jumpCircles = true;
		String jumpCircleStyle = "RIPPLE"; int jumpCircleSize = 5; int jumpCircleSpeed = 5; int jumpCircleCount = 4;
		boolean petals = true; String fallTheme = "SAKURA_FALL";
		int fallIntensity = 5; int fallSize = 5; int fallSpeed = 5; int fallSpread = 5;
		int fallRangeChunks = 3; int fallOpacity = 7;
		String customThemeStyle = "PETALS"; int customThemeHue = 330; int customThemeSat = 70; int customThemeVal = 100;
		String skyStyle = "OFF"; boolean skyAnimated = true; int skySpeed = 5; int skyBrightness = 8;
		int cosmeticBrightness = 7; int cosmeticSize = 5; int cosmeticThickness = 5;
		int wingSize = 5; int haloSize = 5; int hatSize = 5; int capeSize = 5; int auraSize = 5; int shieldSize = 5;
		boolean customColor = true; int cosmeticHue = 330; int cosmeticSat = 70; int cosmeticVal = 100;
		CosmeticTint wingTint; CosmeticTint haloTint; CosmeticTint capeTint; CosmeticTint auraTint; CosmeticTint shieldTint; CosmeticTint hatTint;
		CosmeticTint trailTint; CosmeticTint jumpTint;
		boolean blockOverlay = true; String overlayMaterial = "RAINBOW"; int overlayBrightness = 3;
		boolean overlayCustomColor = false; int overlayHue = 330; int overlaySat = 70; int overlayVal = 100;
		boolean motionBlur = false; int motionBlurStrength = 2;
		boolean noHurtCam = true; boolean fullbright = false; boolean lowParticles = false;
		boolean guiCustomColor = false; boolean guiRainbow = false;
		int guiHue = 330; int guiSat = 55; int guiVal = 100;
		int orbitKeyCode = 344;
	}
}
