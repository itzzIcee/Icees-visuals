package com.icees.visuals.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.icees.visuals.IceesVisualsClient;
import com.icees.visuals.cosmetics.CapeStyle;
import com.icees.visuals.cosmetics.HaloStyle;
import com.icees.visuals.cosmetics.KnifeStyle;
import com.icees.visuals.cosmetics.WingStyle;
import com.icees.visuals.effects.FallTheme;
import com.icees.visuals.sky.SkyStyle;
import net.fabricmc.loader.api.FabricLoader;
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
	public static boolean cape = true;
	public static CapeStyle capeStyle = CapeStyle.SAKURA;
	public static boolean knife = true;
	public static KnifeStyle knifeStyle = KnifeStyle.BUTTERFLY;
	public static boolean aura = true;
	public static boolean trail = false;
	public static boolean jumpCircles = true;
	/** Falling atmosphere theme (Sakura Fall / White Snow / Soft Mist). */
	public static FallTheme fallTheme = FallTheme.SAKURA_FALL;
	/** 1 = light … 5 = heavy */
	public static int fallIntensity = 3;
	/** GLFW key code for Orbit menu (default Right Shift). */
	public static int orbitKeyCode = GLFW.GLFW_KEY_RIGHT_SHIFT;

	private IceesConfig() {}

	public static void load() {
		if (!Files.isRegularFile(PATH)) {
			save();
			return;
		}
		try (Reader reader = Files.newBufferedReader(PATH)) {
			Data data = GSON.fromJson(reader, Data.class);
			if (data == null) return;
			customSky = data.customSky;
			skyStyle = safeEnum(SkyStyle.class, data.skyStyle, SkyStyle.SAKURA);
			wings = data.wings;
			wingStyle = safeEnum(WingStyle.class, data.wingStyle, WingStyle.PETAL);
			halo = data.halo;
			haloStyle = safeEnum(HaloStyle.class, data.haloStyle, HaloStyle.BLOSSOM);
			cape = data.cape;
			capeStyle = safeEnum(CapeStyle.class, data.capeStyle, CapeStyle.SAKURA);
			knife = data.knife;
			knifeStyle = safeEnum(KnifeStyle.class, data.knifeStyle, KnifeStyle.BUTTERFLY);
			aura = data.aura;
			trail = data.trail;
			jumpCircles = data.jumpCircles;
			if (data.fallTheme != null) {
				fallTheme = safeEnum(FallTheme.class, data.fallTheme, FallTheme.SAKURA_FALL);
			} else if (data.petals) {
				fallTheme = FallTheme.SAKURA_FALL;
			} else {
				fallTheme = FallTheme.OFF;
			}
			if (data.fallIntensity >= 1 && data.fallIntensity <= 5) {
				fallIntensity = data.fallIntensity;
			}
			if (data.orbitKeyCode != 0) orbitKeyCode = data.orbitKeyCode;
		} catch (Exception e) {
			IceesVisualsClient.LOGGER.warn("Failed to load config", e);
		}
	}

	public static void save() {
		Data data = new Data();
		data.customSky = customSky;
		data.skyStyle = skyStyle.name();
		data.wings = wings;
		data.wingStyle = wingStyle.name();
		data.halo = halo;
		data.haloStyle = haloStyle.name();
		data.cape = cape;
		data.capeStyle = capeStyle.name();
		data.knife = knife;
		data.knifeStyle = knifeStyle.name();
		data.aura = aura;
		data.trail = trail;
		data.jumpCircles = jumpCircles;
		data.petals = fallTheme == FallTheme.SAKURA_FALL;
		data.fallTheme = fallTheme.name();
		data.fallIntensity = fallIntensity;
		data.orbitKeyCode = orbitKeyCode;
		try {
			Files.createDirectories(PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(PATH)) {
				GSON.toJson(data, writer);
			}
		} catch (IOException e) {
			IceesVisualsClient.LOGGER.warn("Failed to save config", e);
		}
	}

	private static <T extends Enum<T>> T safeEnum(Class<T> type, String name, T fallback) {
		if (name == null) return fallback;
		try {
			return Enum.valueOf(type, name);
		} catch (IllegalArgumentException e) {
			return fallback;
		}
	}

	@SuppressWarnings("unused")
	private static final class Data {
		boolean customSky = true;
		String skyStyle = "SAKURA";
		boolean wings = true;
		String wingStyle = "PETAL";
		boolean halo = true;
		String haloStyle = "BLOSSOM";
		boolean cape = true;
		String capeStyle = "SAKURA";
		boolean knife = true;
		String knifeStyle = "BUTTERFLY";
		boolean aura = true;
		boolean trail = false;
		boolean jumpCircles = true;
		boolean petals = true;
		String fallTheme = "SAKURA_FALL";
		int fallIntensity = 3;
		int orbitKeyCode = 344;
	}
}
