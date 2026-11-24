package com.icees.visuals.gui;

import com.icees.visuals.config.CosmeticColor;
import com.icees.visuals.config.CosmeticGradient;
import com.icees.visuals.config.CosmeticTint;
import com.icees.visuals.config.IceesConfig;
import com.icees.visuals.config.VisualPresets;
import com.icees.visuals.effects.CustomThemeStyle;
import com.icees.visuals.effects.FallTheme;
import com.icees.visuals.effects.SkyStyle;
import com.icees.visuals.keybind.IceesKeybinds;
import com.icees.visuals.overlay.OverlayMaterial;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

/** Soft glass Orbit — cosmetics preview, fine theme/cosmetic controls. */
public class OrbitScreen extends Screen {
	private static final FontDescription TITLE_FONT =
		new FontDescription.Resource(Identifier.fromNamespaceAndPath("icees-visuals", "title"));
	private static final Identifier TEX_BLOOM = Identifier.fromNamespaceAndPath("icees-visuals", "textures/gui/bloom.png");
	private static final Identifier TEX_PANEL = Identifier.fromNamespaceAndPath("icees-visuals", "textures/gui/panel.png");
	private static final Identifier TEX_ROW = Identifier.fromNamespaceAndPath("icees-visuals", "textures/gui/row.png");
	private static final Identifier TEX_ROW_ON = Identifier.fromNamespaceAndPath("icees-visuals", "textures/gui/row_on.png");
	private static final Identifier TEX_ROW_HOVER = Identifier.fromNamespaceAndPath("icees-visuals", "textures/gui/row_hover.png");
	private static final Identifier TEX_SCRIM = Identifier.fromNamespaceAndPath("icees-visuals", "textures/gui/scrim.png");

	private enum Tab { COSMETICS, THEMES, SKYS, CONFIGS, OVERLAY, CPVP, GUI, KEYBIND }
	private enum CosmeticPick { WINGS, HALO, HAT, CAPE, AURA, JUMP, TRAIL }

	private Tab tab = Tab.COSMETICS;
	private CosmeticPick pick = CosmeticPick.WINGS;
	private int configSlot = 0;
	private int panelX, panelY;
	private final int panelW = 640;
	private final int panelH = 520;
	private boolean listeningKey;
	private final List<Hit> hits = new ArrayList<>();
	private final List<TabHit> tabHits = new ArrayList<>();
	private final long openMs = System.currentTimeMillis();
	private record Hit(int x, int y, int w, int h, Runnable action, String label, boolean active) {}
	private record TabHit(int x, int y, int w, int h, Tab tab, String label) {}

	public OrbitScreen() {
		super(Component.translatable("gui.icees-visuals.orbit"));
	}

	@Override
	protected void init() {
		listeningKey = false;
		CosmeticPreviewGate.active = true;
		rebuildHits();
	}

	@Override
	public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		graphics.fill(0, 0, width, height, SakuraTheme.scrim());
		graphics.blit(RenderPipelines.GUI_TEXTURED, TEX_SCRIM, 0, 0, 0f, 0f, width, height, 256, 256, SakuraTheme.scrimTint());
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private void rebuildHits() {
		hits.clear();
		tabHits.clear();
		panelX = (this.width - panelW) / 2;
		panelY = (this.height - panelH) / 2;

		int tx = panelX + 20;
		int ty = panelY + 64;
		addTab(tx, ty, "Cosmetics", Tab.COSMETICS); tx += 78;
		addTab(tx, ty, "Themes", Tab.THEMES); tx += 56;
		addTab(tx, ty, "Skys", Tab.SKYS); tx += 48;
		addTab(tx, ty, "Configs", Tab.CONFIGS); tx += 62;
		addTab(tx, ty, "Overlay", Tab.OVERLAY); tx += 58;
		addTab(tx, ty, "CPvP", Tab.CPVP); tx += 44;
		addTab(tx, ty, "GUI", Tab.GUI); tx += 36;
		addTab(tx, ty, "Key", Tab.KEYBIND);

		int x = panelX + 28;
		int y = panelY + 92;
		int w = panelW - 56;

		switch (tab) {
			case COSMETICS -> buildCosmetics(x, y, w);
			case THEMES -> buildThemes(x, y, w);
			case SKYS -> buildSkys(x, y, w);
			case CONFIGS -> buildConfigs(x, y, w);
			case OVERLAY -> buildOverlay(x, y, w);
			case CPVP -> buildCpvp(x, y, w);
			case GUI -> buildGui(x, y, w);
			case KEYBIND -> buildKeybind(x, y, w);
		}
	}

	private void buildSkys(int x, int y, int w) {
		int col = 0;
		int rowY = y;
		for (SkyStyle sky : SkyStyle.values()) {
			boolean on = IceesConfig.skyStyle == sky;
			SkyStyle selected = sky;
			int bx = x + col * (w / 2 + 4);
			addHit(bx, rowY, w / 2 - 4, 18, (on ? "●  " : "○  ") + sky.title, on, () -> IceesConfig.skyStyle = selected);
			col++;
			if (col >= 2) {
				col = 0;
				rowY += 20;
			}
		}
		if (col != 0) rowY += 20;
		rowY += 10;
		addHit(x, rowY, w, 22,
			"Animated  ·  " + onOff(IceesConfig.skyAnimated),
			IceesConfig.skyAnimated,
			() -> IceesConfig.skyAnimated = !IceesConfig.skyAnimated);
		rowY += 28;
		rowY = addStepper(x, rowY, w, "Anim Speed", IceesConfig.skySpeed, 10, v -> {
			IceesConfig.skySpeed = v;
			IceesConfig.skyAnimated = true;
		});
		addStepper(x, rowY, w, "Brightness", IceesConfig.skyBrightness, 10, v -> IceesConfig.skyBrightness = v);
	}

	/** Save / load Themes + Cosmetics looks into named slots. */
	private void buildConfigs(int x, int y, int w) {
		addHit(x, y, w, 16, "Save themes + cosmetics as a look — load anytime", false, () -> {});
		y += 24;

		for (int i = 0; i < VisualPresets.SLOT_COUNT; i++) {
			int slot = i;
			boolean selected = configSlot == slot;
			boolean filled = VisualPresets.isFilled(slot);
			String status = filled ? "saved" : "empty";
			String mark = selected ? "●" : "○";
			addHit(x, y, w - 200, 20,
				mark + " " + VisualPresets.name(slot) + "  ·  " + status,
				selected || filled,
				() -> configSlot = slot);
			addHit(x + w - 190, y, 58, 20, "Load", filled, () -> {
				if (VisualPresets.loadSlot(slot)) configSlot = slot;
			});
			addHit(x + w - 126, y, 58, 20, "Save", true, () -> {
				VisualPresets.saveCurrent(slot);
				configSlot = slot;
			});
			addHit(x + w - 62, y, 62, 20, "Clear", filled, () -> VisualPresets.clearSlot(slot));
			y += 26;
		}

		y += 8;
		addHit(x, y, w, 20,
			"Rename selected  →  " + VisualPresets.name(configSlot) + "  (click to cycle)",
			false,
			() -> VisualPresets.cycleName(configSlot));
	}

	private void buildThemes(int x, int y, int w) {
		int col = 0;
		int rowY = y;
		for (FallTheme theme : FallTheme.values()) {
			boolean on = IceesConfig.fallTheme == theme;
			FallTheme selected = theme;
			int bx = x + col * (w / 2 + 4);
			addHit(bx, rowY, w / 2 - 4, 18, (on ? "●  " : "○  ") + theme.title, on, () -> IceesConfig.fallTheme = selected);
			col++;
			if (col >= 2) {
				col = 0;
				rowY += 20;
			}
		}
		if (col != 0) rowY += 20;
		rowY += 8;
		if (IceesConfig.fallTheme == FallTheme.CUSTOM) {
			addHit(x, rowY, 24, 18, "‹", false, () -> IceesConfig.customThemeStyle = IceesConfig.customThemeStyle.prev());
			addHit(x + 28, rowY, w - 56, 18, "Style  →  " + IceesConfig.customThemeStyle.title, false,
				() -> IceesConfig.customThemeStyle = IceesConfig.customThemeStyle.next());
			addHit(x + w - 24, rowY, 24, 18, "›", false, () -> IceesConfig.customThemeStyle = IceesConfig.customThemeStyle.next());
			rowY += 22;
			rowY = addStepper(x, rowY, w, "Hue", IceesConfig.customThemeHue, 355, v -> {
				IceesConfig.customThemeHue = Math.floorMod(v, 360);
				IceesConfig.fallTheme = FallTheme.CUSTOM;
			}, 5);
			rowY = addStepper(x, rowY, w, "Sat", IceesConfig.customThemeSat, 100, v -> {
				IceesConfig.customThemeSat = Math.max(0, Math.min(100, v));
				IceesConfig.fallTheme = FallTheme.CUSTOM;
			});
			rowY = addStepper(x, rowY, w, "Val", IceesConfig.customThemeVal, 100, v -> {
				IceesConfig.customThemeVal = Math.max(0, Math.min(100, v));
				IceesConfig.fallTheme = FallTheme.CUSTOM;
			});
			rowY += 4;
		}
		rowY = addStepper(x, rowY, w, "Intensity", IceesConfig.fallIntensity, 10, v -> IceesConfig.fallIntensity = v);
		rowY = addStepper(x, rowY, w, "Size", IceesConfig.fallSize, 10, v -> IceesConfig.fallSize = v);
		rowY = addStepper(x, rowY, w, "Speed", IceesConfig.fallSpeed, 10, v -> IceesConfig.fallSpeed = v);
		rowY = addStepper(x, rowY, w, "Spread", IceesConfig.fallSpread, 10, v -> IceesConfig.fallSpread = v);
		rowY = addChunkRangeStepper(x, rowY, w);
		addStepper(x, rowY, w, "Opacity", IceesConfig.fallOpacity, 10, v -> IceesConfig.fallOpacity = v);
	}

	private void buildOverlay(int x, int y, int w) {
		addHit(x, y, w, 20, "Block Overlay  ·  " + onOff(IceesConfig.blockOverlay), IceesConfig.blockOverlay,
			() -> IceesConfig.blockOverlay = !IceesConfig.blockOverlay);
		y += 24;
		addHit(x, y, w / 2 - 4, 20, (IceesConfig.overlayMaterial == OverlayMaterial.RAINBOW ? "●  " : "○  ") + "Rainbow",
			IceesConfig.overlayMaterial == OverlayMaterial.RAINBOW, () -> {
				IceesConfig.overlayMaterial = OverlayMaterial.RAINBOW;
				IceesConfig.overlayCustomColor = false;
				IceesConfig.blockOverlay = true;
			});
		addHit(x + w / 2 + 4, y, w / 2 - 4, 20, (IceesConfig.overlayMaterial == OverlayMaterial.CUSTOM ? "●  " : "○  ") + "Custom",
			IceesConfig.overlayMaterial == OverlayMaterial.CUSTOM, () -> {
				IceesConfig.overlayMaterial = OverlayMaterial.CUSTOM;
				IceesConfig.overlayCustomColor = true;
				IceesConfig.blockOverlay = true;
			});
		y += 26;
		y = addStepper(x, y, w, "Brightness", IceesConfig.overlayBrightness, 10, v -> IceesConfig.overlayBrightness = v);
		y = addStepper(x, y, w, "Hue", IceesConfig.overlayHue, 355, v -> {
			IceesConfig.overlayHue = Math.floorMod(v, 360);
			IceesConfig.overlayMaterial = OverlayMaterial.CUSTOM;
			IceesConfig.overlayCustomColor = true;
		}, 5);
		y = addStepper(x, y, w, "Sat", IceesConfig.overlaySat, 100, v -> {
			IceesConfig.overlaySat = Math.max(0, Math.min(100, v));
			IceesConfig.overlayMaterial = OverlayMaterial.CUSTOM;
			IceesConfig.overlayCustomColor = true;
		});
		addStepper(x, y, w, "Val", IceesConfig.overlayVal, 100, v -> {
			IceesConfig.overlayVal = Math.max(0, Math.min(100, v));
			IceesConfig.overlayMaterial = OverlayMaterial.CUSTOM;
			IceesConfig.overlayCustomColor = true;
		});
	}

	private void buildCpvp(int x, int y, int w) {
		addHit(x, y, w, 20, "Motion Blur  ·  " + onOff(IceesConfig.motionBlur), IceesConfig.motionBlur,
			() -> IceesConfig.motionBlur = !IceesConfig.motionBlur);
		y += 24;
		y = addStepper(x, y, w, "Blur Strength", IceesConfig.motionBlurStrength, 10, v -> {
			IceesConfig.motionBlurStrength = v;
			IceesConfig.motionBlur = true;
		});
		addHit(x, y, w, 20, "No Hurt Cam  ·  " + onOff(IceesConfig.noHurtCam), IceesConfig.noHurtCam,
			() -> IceesConfig.noHurtCam = !IceesConfig.noHurtCam);
		y += 24;
		addHit(x, y, w, 20, "Fullbright  ·  " + onOff(IceesConfig.fullbright), IceesConfig.fullbright, () -> {
			IceesConfig.fullbright = !IceesConfig.fullbright;
			IceesConfig.applyFullbright(Minecraft.getInstance());
		});
		y += 24;
		addHit(x, y, w, 20, "Low Particles (FPS)  ·  " + onOff(IceesConfig.lowParticles), IceesConfig.lowParticles,
			() -> IceesConfig.lowParticles = !IceesConfig.lowParticles);
	}

	private void buildGui(int x, int y, int w) {
		addHit(x, y, w, 20, "Custom GUI Color  ·  " + onOff(IceesConfig.guiCustomColor), IceesConfig.guiCustomColor,
			() -> IceesConfig.guiCustomColor = !IceesConfig.guiCustomColor);
		y += 24;
		addHit(x, y, w, 20, "Rainbow GUI  ·  " + onOff(IceesConfig.guiRainbow), IceesConfig.guiRainbow,
			() -> IceesConfig.guiRainbow = !IceesConfig.guiRainbow);
		y += 24;
		y = addStepper(x, y, w, "Hue", IceesConfig.guiHue, 355, v -> {
			IceesConfig.guiHue = Math.floorMod(v, 360);
			IceesConfig.guiCustomColor = true;
			IceesConfig.guiRainbow = false;
		}, 5);
		y = addStepper(x, y, w, "Sat", IceesConfig.guiSat, 100, v -> {
			IceesConfig.guiSat = Math.max(0, Math.min(100, v));
			IceesConfig.guiCustomColor = true;
			IceesConfig.guiRainbow = false;
		});
		y = addStepper(x, y, w, "Val", IceesConfig.guiVal, 100, v -> {
			IceesConfig.guiVal = Math.max(0, Math.min(100, v));
			IceesConfig.guiCustomColor = true;
			IceesConfig.guiRainbow = false;
		});
		addHit(x, y, w, 20, "Reset GUI to Sakura pink", false, () -> {
			IceesConfig.guiCustomColor = false;
			IceesConfig.guiRainbow = false;
			IceesConfig.guiHue = 330;
			IceesConfig.guiSat = 55;
			IceesConfig.guiVal = 100;
		});
	}

	private void buildKeybind(int x, int y, int w) {
		String keyName = listeningKey ? "press any key…" : keyLabel(IceesConfig.orbitKeyCode);
		addHit(x, y, w, 28, "Orbit Menu Key  ·  " + keyName, listeningKey, () -> listeningKey = true);
		y += 36;
		addHit(x, y, w, 22, "Reset to Right Shift", false, () -> {
			IceesKeybinds.resetDefault();
			listeningKey = false;
		});
	}

	/** Preview left, controls right — pick one cosmetic at a time. */
	private void buildCosmetics(int x, int y, int w) {
		int previewW = 140;
		int cx = x + previewW + 12;
		int cw = w - previewW - 12;

		CosmeticPick[] picks = CosmeticPick.values();
		int slot = cw / picks.length - 2;
		for (int i = 0; i < picks.length; i++) {
			CosmeticPick p = picks[i];
			boolean selected = pick == p;
			boolean enabled = isEnabled(p);
			addHit(cx + i * (slot + 2), y, slot, 18, (selected ? "●" : "○") + shortLabel(p), selected || enabled, () -> pick = p);
		}
		y += 24;

		addHit(cx, y, cw, 20, label(pick) + "  ·  " + onOff(isEnabled(pick)), isEnabled(pick), this::togglePick);
		y += 24;

		if (pick == CosmeticPick.JUMP) {
			addHit(cx, y, 24, 18, "‹", false, () -> IceesConfig.jumpCircleStyle = IceesConfig.jumpCircleStyle.prev());
			addHit(cx + 28, y, cw - 56, 18, "Style  →  " + IceesConfig.jumpCircleStyle.title, false,
				() -> IceesConfig.jumpCircleStyle = IceesConfig.jumpCircleStyle.next());
			addHit(cx + cw - 24, y, 24, 18, "›", false, () -> IceesConfig.jumpCircleStyle = IceesConfig.jumpCircleStyle.next());
			y += 22;
			y = addTintControlsFor(cx, y, cw, CosmeticPick.JUMP);
			y = addStepper(cx, y, cw, "Rings", IceesConfig.jumpCircleCount, 10, v -> IceesConfig.jumpCircleCount = v);
			y = addStepper(cx, y, cw, "Size", IceesConfig.jumpCircleSize, 10, v -> IceesConfig.jumpCircleSize = v);
			y = addStepper(cx, y, cw, "Speed", IceesConfig.jumpCircleSpeed, 10, v -> IceesConfig.jumpCircleSpeed = v);
		} else if (pick == CosmeticPick.TRAIL) {
			addHit(cx, y, cw, 16, "Leaves a soft glow while you walk / run", false, () -> {});
			y += 20;
			y = addTintControlsFor(cx, y, cw, CosmeticPick.TRAIL);
		} else {
			addHit(cx, y, 24, 18, "‹", false, this::prevForm);
			addHit(cx + 28, y, cw - 56, 18, "Form  →  " + formTitle(pick), false, this::cycleForm);
			addHit(cx + cw - 24, y, 24, 18, "›", false, this::cycleForm);
			y += 22;

			y = addTintControlsFor(cx, y, cw, pick);
		}

		y += 4;
		if (pick != CosmeticPick.JUMP && pick != CosmeticPick.TRAIL) {
			y = addStepper(cx, y, cw, "Size", sizeOfPick(), 10, this::setSizeOfPick);
		}
		addStepper(cx, y, cw, "Brightness", IceesConfig.cosmeticBrightness, 10, v -> IceesConfig.cosmeticBrightness = v);
	}

	private int sizeOfPick() {
		return switch (pick) {
			case WINGS -> IceesConfig.wingSize;
			case HALO -> IceesConfig.haloSize;
			case HAT -> IceesConfig.hatSize;
			case CAPE -> IceesConfig.capeSize;
			case AURA, TRAIL -> IceesConfig.auraSize;
			case JUMP -> IceesConfig.jumpCircleSize;
		};
	}

	private void setSizeOfPick(int v) {
		switch (pick) {
			case WINGS -> IceesConfig.wingSize = IceesConfig.clamp10(v);
			case HALO -> IceesConfig.haloSize = IceesConfig.clamp10(v);
			case HAT -> IceesConfig.hatSize = IceesConfig.clamp10(v);
			case CAPE -> IceesConfig.capeSize = IceesConfig.clamp10(v);
			case AURA, TRAIL -> IceesConfig.auraSize = IceesConfig.clamp10(v);
			case JUMP -> IceesConfig.jumpCircleSize = IceesConfig.clamp10(v);
		}
	}

	/** Named presets, gradients, and custom Hue/Sat/Val for this cosmetic only. */
	private int addTintControlsFor(int x, int y, int w, CosmeticPick which) {
		CosmeticTint tint = tintOf(which);
		CosmeticColor col = CosmeticColor.matching(tint);
		CosmeticGradient grad = CosmeticGradient.matching(tint);
		addHit(x, y, w, 16, "Colour  ·  " + onOff(tint.enabled), tint.enabled, () -> tintOf(which).toggle());
		y += 18;
		addHit(x, y, 24, 18, "‹", false, () -> CosmeticColor.matching(tintOf(which)).prev().apply(tintOf(which)));
		addHit(x + 28, y, w - 56, 18, "Preset  →  " + col.title, false,
			() -> CosmeticColor.matching(tintOf(which)).next().apply(tintOf(which)));
		addHit(x + w - 24, y, 24, 18, "›", false, () -> CosmeticColor.matching(tintOf(which)).next().apply(tintOf(which)));
		y += 22;
		addHit(x, y, 24, 18, "‹", false, () -> CosmeticGradient.matching(tintOf(which)).prev().apply(tintOf(which)));
		addHit(x + 28, y, w - 56, 18, "Gradient  →  " + grad.title, false,
			() -> CosmeticGradient.matching(tintOf(which)).next().apply(tintOf(which)));
		addHit(x + w - 24, y, 24, 18, "›", false, () -> CosmeticGradient.matching(tintOf(which)).next().apply(tintOf(which)));
		y += 22;
		addHit(x, y, w, 14, "Custom colour", false, () -> {});
		y += 16;
		y = addStepper(x, y, w, "Hue", tint.hue, 355, v -> {
			CosmeticTint t = tintOf(which);
			t.hue = Math.floorMod(v, 360);
			t.enabled = true;
		}, 5);
		y = addStepper(x, y, w, "Sat", tint.sat, 100, v -> {
			CosmeticTint t = tintOf(which);
			t.sat = Math.max(0, Math.min(100, v));
			t.enabled = true;
		});
		y = addStepper(x, y, w, "Val", tint.val, 100, v -> {
			CosmeticTint t = tintOf(which);
			t.val = Math.max(0, Math.min(100, v));
			t.enabled = true;
		});
		if (tint.gradient) {
			y = addStepper(x, y, w, "Hue 2", tint.hue2, 355, v -> {
				CosmeticTint t = tintOf(which);
				t.hue2 = Math.floorMod(v, 360);
				t.enabled = true;
				t.gradient = true;
			}, 5);
		}
		return y;
	}

	private int addChunkRangeStepper(int x, int y, int w) {
		int value = IceesConfig.clampChunks(IceesConfig.fallRangeChunks);
		addHit(x, y, 24, 16, "−", false, () -> IceesConfig.fallRangeChunks = Math.max(1, value - 1));
		addHit(x + 28, y, w - 56, 16,
			"Range  ·  " + value + " chunk" + (value == 1 ? "" : "s") + "  (" + (value * 16) + "m)",
			false,
			() -> IceesConfig.fallRangeChunks = value >= 8 ? 1 : value + 1);
		addHit(x + w - 24, y, 24, 16, "+", false, () -> IceesConfig.fallRangeChunks = Math.min(8, value + 1));
		return y + 18;
	}

	private int addStepper(int x, int y, int w, String label, int value, int max, IntConsumer set) {
		return addStepper(x, y, w, label, value, max, set, 1);
	}

	private int addStepper(int x, int y, int w, String label, int value, int max, IntConsumer set, int step) {
		final int min = (max >= 100 || max == 355) ? 0 : 1;
		final boolean wrapHue = max == 355;
		addHit(x, y, 24, 16, "−", false, () -> {
			int next = value - step;
			if (wrapHue) set.accept(Math.floorMod(next, 360));
			else set.accept(Math.max(min, next));
		});
		addHit(x + 28, y, w - 56, 16, label + "  ·  " + value + (wrapHue ? "°" : " / " + max), false, () -> {
			int next = value + step;
			if (wrapHue) set.accept(Math.floorMod(next, 360));
			else set.accept(next > max ? min : next);
		});
		addHit(x + w - 24, y, 24, 16, "+", false, () -> {
			int next = value + step;
			if (wrapHue) set.accept(Math.floorMod(next, 360));
			else set.accept(Math.min(max, next));
		});
		return y + 18;
	}

	private static String shortLabel(CosmeticPick p) {
		return switch (p) {
			case WINGS -> "Wing";
			case HALO -> "Halo";
			case HAT -> "Hat";
			case CAPE -> "Cape";
			case AURA -> "Aura";
			case JUMP -> "Jump";
			case TRAIL -> "Trail";
		};
	}

	private static String label(CosmeticPick p) {
		return switch (p) {
			case WINGS -> "Wings";
			case HALO -> "Halo";
			case HAT -> "Hat";
			case CAPE -> "Cape";
			case AURA -> "Aura";
			case JUMP -> "Jump Circles";
			case TRAIL -> "Movement Trail";
		};
	}

	private static boolean isEnabled(CosmeticPick p) {
		return switch (p) {
			case WINGS -> IceesConfig.wings;
			case HALO -> IceesConfig.halo;
			case HAT -> IceesConfig.hat;
			case CAPE -> IceesConfig.cape;
			case AURA -> IceesConfig.aura;
			case JUMP -> IceesConfig.jumpCircles;
			case TRAIL -> IceesConfig.trail;
		};
	}

	private void togglePick() {
		switch (pick) {
			case WINGS -> IceesConfig.wings = !IceesConfig.wings;
			case HALO -> IceesConfig.halo = !IceesConfig.halo;
			case HAT -> IceesConfig.hat = !IceesConfig.hat;
			case CAPE -> IceesConfig.cape = !IceesConfig.cape;
			case AURA -> IceesConfig.aura = !IceesConfig.aura;
			case JUMP -> IceesConfig.jumpCircles = !IceesConfig.jumpCircles;
			case TRAIL -> IceesConfig.trail = !IceesConfig.trail;
		}
	}

	private String formTitle(CosmeticPick p) {
		return switch (p) {
			case WINGS -> IceesConfig.wingStyle.title;
			case HALO -> IceesConfig.haloStyle.title;
			case HAT -> IceesConfig.hatStyle.title;
			case CAPE -> IceesConfig.capeStyle.title;
			case AURA -> IceesConfig.auraStyle.title;
			case JUMP -> IceesConfig.jumpCircleStyle.title;
			case TRAIL -> "Soft Glow";
		};
	}

	private void cycleForm() {
		switch (pick) {
			case WINGS -> IceesConfig.wingStyle = IceesConfig.wingStyle.next();
			case HALO -> IceesConfig.haloStyle = IceesConfig.haloStyle.next();
			case HAT -> IceesConfig.hatStyle = IceesConfig.hatStyle.next();
			case CAPE -> IceesConfig.capeStyle = IceesConfig.capeStyle.next();
			case AURA -> IceesConfig.auraStyle = IceesConfig.auraStyle.next();
			case JUMP -> IceesConfig.jumpCircleStyle = IceesConfig.jumpCircleStyle.next();
			case TRAIL -> {}
		}
	}

	private void prevForm() {
		switch (pick) {
			case WINGS -> {
				var a = com.icees.visuals.cosmetics.WingStyle.values();
				IceesConfig.wingStyle = a[(IceesConfig.wingStyle.ordinal() + a.length - 1) % a.length];
			}
			case HALO -> {
				var a = com.icees.visuals.cosmetics.HaloStyle.values();
				IceesConfig.haloStyle = a[(IceesConfig.haloStyle.ordinal() + a.length - 1) % a.length];
			}
			case HAT -> IceesConfig.hatStyle = IceesConfig.hatStyle.prev();
			case CAPE -> {
				var a = com.icees.visuals.cosmetics.CapeStyle.values();
				IceesConfig.capeStyle = a[(IceesConfig.capeStyle.ordinal() + a.length - 1) % a.length];
			}
			case AURA -> {
				var a = com.icees.visuals.cosmetics.AuraStyle.values();
				IceesConfig.auraStyle = a[(IceesConfig.auraStyle.ordinal() + a.length - 1) % a.length];
			}
			case JUMP -> IceesConfig.jumpCircleStyle = IceesConfig.jumpCircleStyle.prev();
			case TRAIL -> {}
		}
	}

	private CosmeticTint tintOf(CosmeticPick p) {
		return switch (p) {
			case WINGS -> IceesConfig.wingTint;
			case HALO -> IceesConfig.haloTint;
			case CAPE -> IceesConfig.capeTint;
			case AURA -> IceesConfig.auraTint;
			case TRAIL -> IceesConfig.trailTint;
			case JUMP -> IceesConfig.jumpTint;
			case HAT -> IceesConfig.hatTint;
		};
	}

	private void addTab(int x, int y, String label, Tab value) {
		tabHits.add(new TabHit(x, y, this.font.width(label) + 10, 16, value, label));
	}

	private void addHit(int x, int y, int w, int h, String label, boolean active, Runnable action) {
		hits.add(new Hit(x, y, w, h, () -> {
			action.run();
			IceesConfig.save();
			rebuildHits();
		}, label, active));
	}

	private static String onOff(boolean v) { return v ? "ON" : "OFF"; }

	private static String keyLabel(int code) {
		String name = GLFW.glfwGetKeyName(code, 0);
		if (name != null) return name.toUpperCase();
		return switch (code) {
			case GLFW.GLFW_KEY_RIGHT_SHIFT -> "RIGHT SHIFT";
			case GLFW.GLFW_KEY_LEFT_SHIFT -> "LEFT SHIFT";
			case GLFW.GLFW_KEY_RIGHT_CONTROL -> "RIGHT CTRL";
			case GLFW.GLFW_KEY_LEFT_CONTROL -> "LEFT CTRL";
			case GLFW.GLFW_KEY_RIGHT_ALT -> "RIGHT ALT";
			case GLFW.GLFW_KEY_LEFT_ALT -> "LEFT ALT";
			case GLFW.GLFW_KEY_TAB -> "TAB";
			default -> "KEY " + code;
		};
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		float appear = Math.min(1f, (System.currentTimeMillis() - openMs) / 420f);
		float[] accent = IceesConfig.guiAccentRgb();

		int bloom = ARGB.colorFromFloat(0.45f * appear, accent[0], accent[1] * 0.7f, accent[2] * 0.85f);
		graphics.blit(RenderPipelines.GUI_TEXTURED, TEX_BLOOM,
			panelX - 110, panelY - 90, 0f, 0f, panelW + 220, panelH + 180, 512, 512, bloom);

		graphics.blit(RenderPipelines.GUI_TEXTURED, TEX_PANEL,
			panelX - 12, panelY - 12, 0f, 0f, panelW + 24, panelH + 24, 512, 512, -1);

		Component brand = Component.literal("Icee's Visuals").withStyle(Style.EMPTY.withFont(TITLE_FONT));
		graphics.drawString(this.font, brand, panelX + 30, panelY + 16, SakuraTheme.titleGlow(), false);
		graphics.drawString(this.font, brand, panelX + 28, panelY + 14, SakuraTheme.title(), false);
		graphics.drawString(this.font, "soft orbit", panelX + 30, panelY + 42, SakuraTheme.muted(), false);

		for (TabHit th : tabHits) {
			boolean on = tab == th.tab;
			boolean hover = inside(mouseX, mouseY, th.x, th.y, th.w, th.h);
			int color = on ? SakuraTheme.accentHot() : hover ? SakuraTheme.text() : SakuraTheme.muted();
			graphics.drawString(this.font, th.label, th.x, th.y, color, false);
			if (on) {
				graphics.fill(th.x, th.y + 12, th.x + this.font.width(th.label), th.y + 13, SakuraTheme.accent());
			}
		}

		if (tab == Tab.COSMETICS) {
			int px = panelX + 24;
			int py = panelY + 112;
			graphics.fill(px, py, px + 128, py + 180, ARGB.colorFromFloat(0.18f, accent[0], accent[1], accent[2]));
			graphics.drawString(this.font, "preview", px + 8, py + 6, SakuraTheme.muted(), false);
			if (this.minecraft != null && this.minecraft.player != null) {
				CosmeticPreviewGate.active = true;
				InventoryScreen.renderEntityInInventoryFollowsMouse(
					graphics, px + 8, py + 22, px + 120, py + 172, 48, 0.0625F, mouseX, mouseY, this.minecraft.player);
			}
			CosmeticPreviewOverlay.drawLabels(graphics, this.font, px + 4, py + 168, 120);
		}

		for (Hit hit : hits) {
			boolean hover = inside(mouseX, mouseY, hit.x, hit.y, hit.w, hit.h);
			Identifier rowTex = hit.active ? TEX_ROW_ON : hover ? TEX_ROW_HOVER : TEX_ROW;
			graphics.blit(RenderPipelines.GUI_TEXTURED, rowTex, hit.x, hit.y, 0f, 0f, hit.w, hit.h, 512, 64, -1);
			graphics.drawString(this.font, hit.label, hit.x + 8, hit.y + (hit.h - 8) / 2, SakuraTheme.text(), false);
		}

		String tip = switch (tab) {
			case COSMETICS -> pick == CosmeticPick.JUMP
				? IceesConfig.jumpCircleStyle.description
				: "Preview shows your cosmetics — pick one, then form / colour.";
			case THEMES -> IceesConfig.fallTheme == FallTheme.OFF ? "Themes off."
				: IceesConfig.fallTheme == FallTheme.CUSTOM
					? "Custom · " + IceesConfig.customThemeStyle.title + " · H" + IceesConfig.customThemeHue
					: IceesConfig.fallTheme.description;
			case SKYS -> IceesConfig.skyStyle == SkyStyle.OFF
				? "Vanilla sky."
				: IceesConfig.skyStyle.description + (IceesConfig.skyAnimated ? "  ·  animated" : "  ·  static");
			case CONFIGS -> "Save writes Themes + Cosmetics into a slot. Load restores them.";
			case OVERLAY -> "Rainbow or Custom Hue·Sat·Val outline.";
			case CPVP -> "Motion blur, no hurt cam, fullbright, low particles.";
			case GUI -> "Paint the Orbit menu — fine Hue / Sat / Val.";
			case KEYBIND -> listeningKey ? "Waiting for a key…" : "Click the row, then press a new key.";
		};
		graphics.drawString(this.font, tip, panelX + 28, panelY + panelH - 22, SakuraTheme.muted(), false);
	}

	private static boolean inside(double mx, double my, int x, int y, int w, int h) {
		return mx >= x && mx <= x + w && my >= y && my <= y + h;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
		double mx = event.x();
		double my = event.y();
		if (event.button() != 0) return super.mouseClicked(event, bl);
		for (TabHit th : tabHits) {
			if (inside(mx, my, th.x, th.y, th.w, th.h)) {
				tab = th.tab;
				listeningKey = false;
				rebuildHits();
				return true;
			}
		}
		for (Hit hit : hits) {
			if (inside(mx, my, hit.x, hit.y, hit.w, hit.h)) {
				hit.action.run();
				rebuildHits();
				return true;
			}
		}
		return super.mouseClicked(event, bl);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (listeningKey) {
			int code = event.key();
			if (code == GLFW.GLFW_KEY_ESCAPE) {
				listeningKey = false;
				rebuildHits();
				return true;
			}
			if (code != GLFW.GLFW_KEY_UNKNOWN) {
				IceesKeybinds.rebind(code);
				listeningKey = false;
				rebuildHits();
				return true;
			}
		}
		return super.keyPressed(event);
	}

	@Override
	public void onClose() {
		CosmeticPreviewGate.active = false;
		IceesConfig.save();
		super.onClose();
	}
}
