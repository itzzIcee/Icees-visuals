package com.icees.visuals.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.icees.visuals.IceesVisualsClient;
import com.icees.visuals.cosmetics.AuraStyle;
import com.icees.visuals.cosmetics.CapeStyle;
import com.icees.visuals.cosmetics.HaloStyle;
import com.icees.visuals.cosmetics.HatStyle;
import com.icees.visuals.cosmetics.ShieldStyle;
import com.icees.visuals.cosmetics.WingStyle;
import com.icees.visuals.effects.FallTheme;
import com.icees.visuals.overlay.OverlayMaterial;
import com.icees.visuals.pvp.HitAnimStyle;
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
	public static FallTheme fallTheme = FallTheme.SAKURA_FALL;
	public static int fallIntensity = 3;
	public static int fallSize = 3;
	public static int cosmeticBrightness = 5;
	public static int cosmeticSize = 4;
	public static int cosmeticThickness = 4;
	/** Kept for config migration — use per-cosmetic tints. */
	@Deprecated
	public static boolean customColor = true;
	public static int cosmeticHue = 330;
	public static int cosmeticSat = 70;
	public static int cosmeticVal = 100;
	/** Per-cosmetic colours (hats stay natural — no tint). */
	public static CosmeticTint wingTint = new CosmeticTint(true, 330, 70, 100);
	public static CosmeticTint haloTint = new CosmeticTint(true, 320, 65, 100);
	public static CosmeticTint capeTint = new CosmeticTint(true, 330, 70, 100);
	public static CosmeticTint auraTint = new CosmeticTint(true, 310, 70, 100);
	public static CosmeticTint shieldTint = new CosmeticTint(true, 330, 55, 100);

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

	/* —— PvP Cosmetics —— */
	public static boolean pvpHitMarkers = true;
	public static HitAnimStyle pvpHitAnim = HitAnimStyle.CROSS;
	public static int pvpHitAnimSize = 3;
	public static boolean pvpCrosshair = false;
	public static int pvpCrosshairSize = 2;
	public static boolean pvpTargetCue = false;
	public static boolean pvpHitColour = true;
	public static int pvpHitHue = 330;
	public static int pvpHitSat = 70;
	public static int pvpHitVal = 100;
	public static boolean pvpHitParticles = true;
	public static boolean pvpMaceParticles = true;
	public static boolean pvpSpearParticles = true;
	public static int pvpParticleSize = 3;
	public static CosmeticTint pvpTint = new CosmeticTint(true, 330, 70, 100);

	/* —— Weapon hit animations / FX —— */
	public static boolean pvpStunslam = true;
	public static int pvpStunslamSize = 3;
	public static boolean pvpMace = true;
	public static int pvpMaceSize = 3;
	public static boolean pvpSpear = true;
	public static int pvpSpearSize = 3;
	public static boolean pvpSword = true;
	public static int pvpSwordSize = 3;
	public static boolean pvpAxe = true;
	public static int pvpAxeSize = 3;

	/* —— Orbit GUI colours —— */
	public static boolean guiCustomColor = false;
	public static boolean guiRainbow = false;
	public static int guiHue = 330;
	public static int guiSat = 55;
	public static int guiVal = 100;

	public static int orbitKeyCode = GLFW.GLFW_KEY_RIGHT_SHIFT;

	private IceesConfig() {}

	public static float brightnessFactor() {
		return 0.65f + cosmeticBrightness * 0.28f;
	}

	public static float thicknessFactor() {
		int s = Math.max(cosmeticSize, cosmeticThickness);
		return 0.85f + s * 0.30f;
	}

	public static float sizeFactor() {
		return thicknessFactor();
	}

	public static float overlayBrightnessFactor() {
		return 0.55f + overlayBrightness * 0.18f;
	}

	/** Legacy global tint — prefer {@link #color(CosmeticTint, float, float, float)}. */
	public static float[] color(float baseR, float baseG, float baseB) {
		return color(wingTint, baseR, baseG, baseB);
	}

	public static float[] color(CosmeticTint tint, float baseR, float baseG, float baseB) {
		if (tint == null) return new float[]{baseR, baseG, baseB};
		return tint.apply(baseR, baseG, baseB);
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
			shield = data.shield;
			shieldStyle = safeEnum(ShieldStyle.class, data.shieldStyle, ShieldStyle.SAKURA);
			trail = data.trail;
			jumpCircles = data.jumpCircles;
			if (data.fallTheme != null) fallTheme = safeEnum(FallTheme.class, data.fallTheme, FallTheme.SAKURA_FALL);
			else fallTheme = data.petals ? FallTheme.SAKURA_FALL : FallTheme.OFF;
			if (data.fallIntensity >= 1 && data.fallIntensity <= 5) fallIntensity = data.fallIntensity;
			if (data.fallSize >= 1 && data.fallSize <= 5) fallSize = data.fallSize;
			if (data.cosmeticBrightness >= 1 && data.cosmeticBrightness <= 5) cosmeticBrightness = data.cosmeticBrightness;
			if (data.cosmeticSize >= 1 && data.cosmeticSize <= 5) cosmeticSize = data.cosmeticSize;
			if (data.cosmeticThickness >= 1 && data.cosmeticThickness <= 5) cosmeticThickness = data.cosmeticThickness;
			cosmeticSize = Math.max(cosmeticSize, cosmeticThickness);
			cosmeticThickness = cosmeticSize;
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

			pvpHitMarkers = data.pvpHitMarkers;
			pvpHitAnim = safeEnum(HitAnimStyle.class, data.pvpHitAnim, HitAnimStyle.CROSS);
			if (data.pvpHitAnimSize >= 1 && data.pvpHitAnimSize <= 5) pvpHitAnimSize = data.pvpHitAnimSize;
			pvpCrosshair = data.pvpCrosshair;
			if (data.pvpCrosshairSize >= 1 && data.pvpCrosshairSize <= 5) pvpCrosshairSize = data.pvpCrosshairSize;
			pvpTargetCue = data.pvpTargetCue;
			pvpHitColour = data.pvpHitColour;
			pvpHitHue = Math.floorMod(data.pvpHitHue, 360);
			if (data.pvpHitSat >= 0 && data.pvpHitSat <= 100) pvpHitSat = data.pvpHitSat;
			if (data.pvpHitVal >= 0 && data.pvpHitVal <= 100) pvpHitVal = data.pvpHitVal;
			pvpHitParticles = data.pvpHitParticles;
			pvpMaceParticles = data.pvpMaceParticles;
			pvpSpearParticles = data.pvpSpearParticles;
			if (data.pvpParticleSize >= 1 && data.pvpParticleSize <= 5) pvpParticleSize = data.pvpParticleSize;
			pvpTint.loadFrom(data.pvpTint, new CosmeticTint(true, 330, 70, 100));
			pvpStunslam = data.pvpStunslam;
			if (data.pvpStunslamSize >= 1 && data.pvpStunslamSize <= 5) pvpStunslamSize = data.pvpStunslamSize;
			pvpMace = data.pvpMace;
			if (data.pvpMaceSize >= 1 && data.pvpMaceSize <= 5) pvpMaceSize = data.pvpMaceSize;
			pvpSpear = data.pvpSpear;
			if (data.pvpSpearSize >= 1 && data.pvpSpearSize <= 5) pvpSpearSize = data.pvpSpearSize;
			pvpSword = data.pvpSword;
			if (data.pvpSwordSize >= 1 && data.pvpSwordSize <= 5) pvpSwordSize = data.pvpSwordSize;
			pvpAxe = data.pvpAxe;
			if (data.pvpAxeSize >= 1 && data.pvpAxeSize <= 5) pvpAxeSize = data.pvpAxeSize;

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
		data.petals = fallTheme == FallTheme.SAKURA_FALL;
		data.fallTheme = fallTheme.name(); data.fallIntensity = fallIntensity; data.fallSize = fallSize;
		data.cosmeticBrightness = cosmeticBrightness;
		data.cosmeticSize = cosmeticSize; data.cosmeticThickness = cosmeticThickness;
		data.customColor = wingTint.enabled;
		data.cosmeticHue = wingTint.hue; data.cosmeticSat = wingTint.sat; data.cosmeticVal = wingTint.val;
		data.wingTint = wingTint.copy();
		data.haloTint = haloTint.copy();
		data.capeTint = capeTint.copy();
		data.auraTint = auraTint.copy();
		data.shieldTint = shieldTint.copy();
		data.blockOverlay = blockOverlay;
		data.overlayMaterial = overlayMaterial.name();
		data.overlayBrightness = overlayBrightness;
		data.overlayCustomColor = overlayMaterial == OverlayMaterial.CUSTOM;
		data.overlayHue = overlayHue; data.overlaySat = overlaySat; data.overlayVal = overlayVal;
		data.motionBlur = motionBlur; data.motionBlurStrength = motionBlurStrength;
		data.noHurtCam = noHurtCam; data.fullbright = fullbright; data.lowParticles = lowParticles;
		data.pvpHitMarkers = pvpHitMarkers;
		data.pvpHitAnim = pvpHitAnim.name(); data.pvpHitAnimSize = pvpHitAnimSize;
		data.pvpCrosshair = pvpCrosshair;
		data.pvpCrosshairSize = pvpCrosshairSize; data.pvpTargetCue = pvpTargetCue;
		data.pvpHitColour = pvpHitColour;
		data.pvpHitHue = pvpHitHue; data.pvpHitSat = pvpHitSat; data.pvpHitVal = pvpHitVal;
		data.pvpHitParticles = pvpHitParticles; data.pvpMaceParticles = pvpMaceParticles;
		data.pvpSpearParticles = pvpSpearParticles; data.pvpParticleSize = pvpParticleSize;
		data.pvpTint = pvpTint.copy();
		data.pvpStunslam = pvpStunslam; data.pvpStunslamSize = pvpStunslamSize;
		data.pvpMace = pvpMace; data.pvpMaceSize = pvpMaceSize;
		data.pvpSpear = pvpSpear; data.pvpSpearSize = pvpSpearSize;
		data.pvpSword = pvpSword; data.pvpSwordSize = pvpSwordSize;
		data.pvpAxe = pvpAxe; data.pvpAxeSize = pvpAxeSize;
		data.guiCustomColor = guiCustomColor; data.guiRainbow = guiRainbow;
		data.guiHue = guiHue; data.guiSat = guiSat; data.guiVal = guiVal;
		data.orbitKeyCode = orbitKeyCode;
		try {
			Files.createDirectories(PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(PATH)) { GSON.toJson(data, writer); }
		} catch (IOException e) {
			IceesVisualsClient.LOGGER.warn("Failed to save config", e);
		}
	}

	/** Only the two straw hats remain — anything else maps to red-band straw. */
	private static HatStyle migrateHat(String name) {
		if (name == null || "STRAW".equals(name) || "STRAW_RED".equals(name)) return HatStyle.STRAW_RED;
		if ("STRAW_BROWN".equals(name)) return HatStyle.STRAW_BROWN;
		return HatStyle.STRAW_RED;
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
		boolean petals = true; String fallTheme = "SAKURA_FALL"; int fallIntensity = 3; int fallSize = 3;
		int cosmeticBrightness = 5; int cosmeticSize = 4; int cosmeticThickness = 4;
		boolean customColor = true; int cosmeticHue = 330; int cosmeticSat = 70; int cosmeticVal = 100;
		CosmeticTint wingTint; CosmeticTint haloTint; CosmeticTint capeTint; CosmeticTint auraTint; CosmeticTint shieldTint;
		boolean blockOverlay = true; String overlayMaterial = "RAINBOW"; int overlayBrightness = 3;
		boolean overlayCustomColor = false; int overlayHue = 330; int overlaySat = 70; int overlayVal = 100;
		boolean motionBlur = false; int motionBlurStrength = 2;
		boolean noHurtCam = true; boolean fullbright = false; boolean lowParticles = false;
		boolean pvpHitMarkers = true; String pvpHitAnim = "CROSS"; int pvpHitAnimSize = 3;
		boolean pvpCrosshair = false; int pvpCrosshairSize = 2;
		boolean pvpTargetCue = false; boolean pvpHitColour = true;
		int pvpHitHue = 330; int pvpHitSat = 70; int pvpHitVal = 100;
		boolean pvpHitParticles = true; boolean pvpMaceParticles = true; boolean pvpSpearParticles = true;
		int pvpParticleSize = 3; CosmeticTint pvpTint;
		boolean pvpStunslam = true; int pvpStunslamSize = 3;
		boolean pvpMace = true; int pvpMaceSize = 3;
		boolean pvpSpear = true; int pvpSpearSize = 3;
		boolean pvpSword = true; int pvpSwordSize = 3;
		boolean pvpAxe = true; int pvpAxeSize = 3;
		boolean guiCustomColor = false; boolean guiRainbow = false;
		int guiHue = 330; int guiSat = 55; int guiVal = 100;
		int orbitKeyCode = 344;
	}
}
