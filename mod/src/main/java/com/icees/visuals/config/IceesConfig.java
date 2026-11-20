package com.icees.visuals.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.icees.visuals.IceesVisualsClient;
import com.icees.visuals.cosmetics.AuraStyle;
import com.icees.visuals.cosmetics.CapeStyle;
import com.icees.visuals.cosmetics.HaloStyle;
import com.icees.visuals.cosmetics.HatStyle;
import com.icees.visuals.cosmetics.KnifeMaterial;
import com.icees.visuals.cosmetics.KnifeStyle;
import com.icees.visuals.cosmetics.WingStyle;
import com.icees.visuals.effects.FallTheme;
import com.icees.visuals.overlay.OverlayMaterial;
import com.icees.visuals.sky.SkyStyle;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class IceesConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("icees-visuals.json");

	public static boolean customSky = true;
	public static SkyStyle skyStyle = SkyStyle.SAKURA;
	public static boolean wings = true;
	public static WingStyle wingStyle = WingStyle.PETAL;
	public static boolean halo = true;
	public static HaloStyle haloStyle = HaloStyle.BLOSSOM;
	public static boolean hat = true;
	public static HatStyle hatStyle = HatStyle.STRAW_RED;
	public static boolean cape = true;
	public static CapeStyle capeStyle = CapeStyle.SAKURA;
	public static boolean knife = true;
	public static KnifeStyle knifeStyle = KnifeStyle.BUTTERFLY;
	public static KnifeMaterial knifeMaterial = KnifeMaterial.STEEL;
	public static int knifeSize = 3;
	public static boolean knifeCustomColor = false;
	public static int knifeHue = 210;
	public static int knifeSat = 15;
	public static int knifeVal = 90;
	public static boolean aura = true;
	public static AuraStyle auraStyle = AuraStyle.ORBIT;
	public static boolean trail = false;
	public static boolean jumpCircles = true;
	public static FallTheme fallTheme = FallTheme.SAKURA_FALL;
	public static int fallIntensity = 3;
	public static int cosmeticBrightness = 4;
	public static int cosmeticSize = 3;
	public static int cosmeticThickness = 3;
	public static boolean customColor = true;
	public static int cosmeticHue = 330;
	public static int cosmeticSat = 70;
	public static int cosmeticVal = 100;

	/* —— Block Overlay —— */
	public static boolean blockOverlay = true;
	public static OverlayMaterial overlayMaterial = OverlayMaterial.SAKURA;
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
		return 0.35f + cosmeticBrightness * 0.18f;
	}

	public static float thicknessFactor() {
		int s = Math.max(cosmeticSize, cosmeticThickness);
		return 0.55f + s * 0.22f;
	}

	public static float sizeFactor() {
		return thicknessFactor();
	}

	public static float knifeSizeFactor() {
		return 0.65f + knifeSize * 0.22f;
	}

	public static float overlayBrightnessFactor() {
		return 0.55f + overlayBrightness * 0.18f;
	}

	public static boolean knifeActive(Player player) {
		if (!knife || player == null) return false;
		ItemStack main = player.getMainHandItem();
		return !main.isEmpty() && main.is(ItemTags.SWORDS);
	}

	public static float[] color(float baseR, float baseG, float baseB) {
		if (!customColor) return new float[]{baseR, baseG, baseB};
		return hsvToRgb(cosmeticHue, cosmeticSat / 100f, cosmeticVal / 100f);
	}

	public static float[] knifeBladeColor(float time) {
		return switch (knifeMaterial) {
			case RAINBOW -> {
				float h = (time * 8f + (knifeCustomColor ? knifeHue : 0)) % 360f;
				if (h < 0) h += 360f;
				yield hsvToRgb(h, 0.85f, 1f);
			}
			case CHROME -> {
				float shimmer = 0.82f + 0.18f * (0.5f + 0.5f * (float) Math.sin(time * 0.35f));
				if (knifeCustomColor) {
					float[] c = hsvToRgb(knifeHue, Math.min(0.35f, knifeSat / 100f), knifeVal / 100f);
					yield new float[]{
						Math.min(1f, c[0] * 0.45f + shimmer * 0.7f),
						Math.min(1f, c[1] * 0.45f + shimmer * 0.72f),
						Math.min(1f, c[2] * 0.45f + shimmer * 0.78f)
					};
				}
				yield new float[]{shimmer, shimmer * 1.02f, Math.min(1f, shimmer * 1.08f)};
			}
			case GOLD -> knifeCustomColor ? hsvToRgb(knifeHue, knifeSat / 100f, knifeVal / 100f) : new float[]{0.95f, 0.78f, 0.28f};
			case SAKURA -> knifeCustomColor ? hsvToRgb(knifeHue, knifeSat / 100f, knifeVal / 100f) : new float[]{1.00f, 0.62f, 0.78f};
			case OBSIDIAN -> knifeCustomColor ? hsvToRgb(knifeHue, knifeSat / 100f, knifeVal / 100f) : new float[]{0.22f, 0.18f, 0.28f};
			case STEEL -> knifeCustomColor ? hsvToRgb(knifeHue, knifeSat / 100f, knifeVal / 100f) : new float[]{0.72f, 0.76f, 0.82f};
		};
	}

	public static float[] overlayColor(float time) {
		return switch (overlayMaterial) {
			case RAINBOW -> {
				float h = (time * 10f + (overlayCustomColor ? overlayHue : 0)) % 360f;
				if (h < 0) h += 360f;
				yield hsvToRgb(h, 0.9f, 1f);
			}
			case CHROME -> {
				float shimmer = 0.78f + 0.22f * (0.5f + 0.5f * (float) Math.sin(time * 0.4f));
				yield new float[]{shimmer, shimmer * 1.02f, Math.min(1f, shimmer * 1.08f)};
			}
			case SILVER -> overlayCustomColor
				? hsvToRgb(overlayHue, Math.min(0.25f, overlaySat / 100f), overlayVal / 100f)
				: new float[]{0.82f, 0.86f, 0.92f};
			case GOLD -> overlayCustomColor
				? hsvToRgb(overlayHue, overlaySat / 100f, overlayVal / 100f)
				: new float[]{0.95f, 0.78f, 0.28f};
			case SAKURA -> overlayCustomColor
				? hsvToRgb(overlayHue, overlaySat / 100f, overlayVal / 100f)
				: new float[]{1.00f, 0.62f, 0.78f};
			case SOLID -> overlayCustomColor
				? hsvToRgb(overlayHue, overlaySat / 100f, overlayVal / 100f)
				: new float[]{1f, 1f, 1f};
		};
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
			customSky = data.customSky;
			skyStyle = safeEnum(SkyStyle.class, data.skyStyle, SkyStyle.SAKURA);
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
			knife = data.knife;
			knifeStyle = safeEnum(KnifeStyle.class, data.knifeStyle, KnifeStyle.BUTTERFLY);
			knifeMaterial = safeEnum(KnifeMaterial.class, data.knifeMaterial, KnifeMaterial.STEEL);
			if (data.knifeSize >= 1 && data.knifeSize <= 5) knifeSize = data.knifeSize;
			knifeCustomColor = data.knifeCustomColor;
			knifeHue = Math.floorMod(data.knifeHue, 360);
			if (data.knifeSat >= 0 && data.knifeSat <= 100) knifeSat = data.knifeSat;
			if (data.knifeVal >= 0 && data.knifeVal <= 100) knifeVal = data.knifeVal;
			aura = data.aura;
			auraStyle = safeEnum(AuraStyle.class, data.auraStyle, AuraStyle.ORBIT);
			trail = data.trail;
			jumpCircles = data.jumpCircles;
			if (data.fallTheme != null) fallTheme = safeEnum(FallTheme.class, data.fallTheme, FallTheme.SAKURA_FALL);
			else fallTheme = data.petals ? FallTheme.SAKURA_FALL : FallTheme.OFF;
			if (data.fallIntensity >= 1 && data.fallIntensity <= 5) fallIntensity = data.fallIntensity;
			if (data.cosmeticBrightness >= 1 && data.cosmeticBrightness <= 5) cosmeticBrightness = data.cosmeticBrightness;
			if (data.cosmeticSize >= 1 && data.cosmeticSize <= 5) cosmeticSize = data.cosmeticSize;
			if (data.cosmeticThickness >= 1 && data.cosmeticThickness <= 5) cosmeticThickness = data.cosmeticThickness;
			cosmeticSize = Math.max(cosmeticSize, cosmeticThickness);
			cosmeticThickness = cosmeticSize;
			customColor = data.customColor;
			cosmeticHue = Math.floorMod(data.cosmeticHue, 360);
			if (data.cosmeticSat >= 0 && data.cosmeticSat <= 100) cosmeticSat = data.cosmeticSat;
			if (data.cosmeticVal >= 0 && data.cosmeticVal <= 100) cosmeticVal = data.cosmeticVal;

			blockOverlay = data.blockOverlay;
			overlayMaterial = safeEnum(OverlayMaterial.class, data.overlayMaterial, OverlayMaterial.SAKURA);
			if (data.overlayBrightness >= 1 && data.overlayBrightness <= 5) overlayBrightness = data.overlayBrightness;
			overlayCustomColor = data.overlayCustomColor;
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
		data.customSky = customSky; data.skyStyle = skyStyle.name();
		data.wings = wings; data.wingStyle = wingStyle.name();
		data.halo = halo; data.haloStyle = haloStyle.name();
		data.hat = hat; data.hatStyle = hatStyle.name();
		data.cape = cape; data.capeStyle = capeStyle.name();
		data.knife = knife; data.knifeStyle = knifeStyle.name();
		data.knifeMaterial = knifeMaterial.name();
		data.knifeSize = knifeSize;
		data.knifeCustomColor = knifeCustomColor;
		data.knifeHue = knifeHue; data.knifeSat = knifeSat; data.knifeVal = knifeVal;
		data.aura = aura; data.auraStyle = auraStyle.name();
		data.trail = trail; data.jumpCircles = jumpCircles;
		data.petals = fallTheme == FallTheme.SAKURA_FALL;
		data.fallTheme = fallTheme.name(); data.fallIntensity = fallIntensity;
		data.cosmeticBrightness = cosmeticBrightness;
		data.cosmeticSize = cosmeticSize; data.cosmeticThickness = cosmeticThickness;
		data.customColor = customColor;
		data.cosmeticHue = cosmeticHue; data.cosmeticSat = cosmeticSat; data.cosmeticVal = cosmeticVal;
		data.blockOverlay = blockOverlay;
		data.overlayMaterial = overlayMaterial.name();
		data.overlayBrightness = overlayBrightness;
		data.overlayCustomColor = overlayCustomColor;
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
	}

	/** Old single STRAW style maps to the red-band voxel hat. */
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
		boolean customSky = true; String skyStyle = "SAKURA";
		boolean wings = true; String wingStyle = "PETAL";
		boolean halo = true; String haloStyle = "BLOSSOM";
		boolean hat = true; String hatStyle = "STRAW_RED";
		boolean cape = true; String capeStyle = "SAKURA";
		boolean knife = true; String knifeStyle = "BUTTERFLY";
		String knifeMaterial = "STEEL"; int knifeSize = 3;
		boolean knifeCustomColor = false; int knifeHue = 210; int knifeSat = 15; int knifeVal = 90;
		boolean aura = true; String auraStyle = "ORBIT";
		boolean trail = false; boolean jumpCircles = true;
		boolean petals = true; String fallTheme = "SAKURA_FALL"; int fallIntensity = 3;
		int cosmeticBrightness = 4; int cosmeticSize = 3; int cosmeticThickness = 3;
		boolean customColor = true; int cosmeticHue = 330; int cosmeticSat = 70; int cosmeticVal = 100;
		boolean blockOverlay = true; String overlayMaterial = "SAKURA"; int overlayBrightness = 3;
		boolean overlayCustomColor = false; int overlayHue = 330; int overlaySat = 70; int overlayVal = 100;
		boolean motionBlur = false; int motionBlurStrength = 2;
		boolean noHurtCam = true; boolean fullbright = false; boolean lowParticles = false;
		boolean guiCustomColor = false; boolean guiRainbow = false;
		int guiHue = 330; int guiSat = 55; int guiVal = 100;
		int orbitKeyCode = 344;
	}
}
