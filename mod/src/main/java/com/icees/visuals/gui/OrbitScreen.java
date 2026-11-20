package com.icees.visuals.gui;

import com.icees.visuals.config.IceesConfig;
import com.icees.visuals.cosmetics.KnifeMaterial;
import com.icees.visuals.cosmetics.KnifeStyle;
import com.icees.visuals.effects.FallTheme;
import com.icees.visuals.keybind.IceesKeybinds;
import com.icees.visuals.overlay.OverlayMaterial;
import com.icees.visuals.sky.SkyStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
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

/** Soft glass Orbit — no vanilla dirt, blur sheets, or stone buttons. */
public class OrbitScreen extends Screen {
	private static final FontDescription TITLE_FONT =
		new FontDescription.Resource(Identifier.fromNamespaceAndPath("icees-visuals", "title"));
	private static final Identifier TEX_BLOOM = Identifier.fromNamespaceAndPath("icees-visuals", "textures/gui/bloom.png");
	private static final Identifier TEX_PANEL = Identifier.fromNamespaceAndPath("icees-visuals", "textures/gui/panel.png");
	private static final Identifier TEX_ROW = Identifier.fromNamespaceAndPath("icees-visuals", "textures/gui/row.png");
	private static final Identifier TEX_ROW_ON = Identifier.fromNamespaceAndPath("icees-visuals", "textures/gui/row_on.png");
	private static final Identifier TEX_ROW_HOVER = Identifier.fromNamespaceAndPath("icees-visuals", "textures/gui/row_hover.png");
	private static final Identifier TEX_SCRIM = Identifier.fromNamespaceAndPath("icees-visuals", "textures/gui/scrim.png");

	private enum Tab { SKIES, COSMETICS, KNIVES, THEMES, OVERLAY, CPVP, GUI, KEYBIND }

	private Tab tab = Tab.SKIES;
	private int panelX, panelY;
	private final int panelW = 520;
	private final int panelH = 460;
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
		addTab(tx, ty, "Skies", Tab.SKIES); tx += 48;
		addTab(tx, ty, "Cosmetics", Tab.COSMETICS); tx += 78;
		addTab(tx, ty, "Knives", Tab.KNIVES); tx += 52;
		addTab(tx, ty, "Themes", Tab.THEMES); tx += 56;
		addTab(tx, ty, "Overlay", Tab.OVERLAY); tx += 58;
		addTab(tx, ty, "CPvP", Tab.CPVP); tx += 44;
		addTab(tx, ty, "GUI", Tab.GUI); tx += 36;
		addTab(tx, ty, "Key", Tab.KEYBIND);

		int x = panelX + 28;
		int y = panelY + 92;
		int w = panelW - 56;

		switch (tab) {
			case SKIES -> {
				addHit(x, y, w, 22, "Custom Sky  ·  " + onOff(IceesConfig.customSky), IceesConfig.customSky,
					() -> IceesConfig.customSky = !IceesConfig.customSky);
				y += 26;
				int col = 0;
				for (SkyStyle style : SkyStyle.values()) {
					int bx = x + (col % 2) * (w / 2 + 8);
					int by = y + (col / 2) * 26;
					boolean on = IceesConfig.skyStyle == style;
					SkyStyle selected = style;
					addHit(bx, by, w / 2 - 8, 22, (on ? "●  " : "○  ") + style.title, on, () -> {
						IceesConfig.skyStyle = selected;
						IceesConfig.customSky = true;
					});
					col++;
				}
			}
			case COSMETICS -> {
				addHit(x, y, w, 18, "Wings  ·  " + onOff(IceesConfig.wings) + "  ·  " + IceesConfig.wingStyle.title, IceesConfig.wings,
					() -> IceesConfig.wings = !IceesConfig.wings);
				y += 20;
				addHit(x, y, w, 16, "Wing form  →", false, () -> IceesConfig.wingStyle = IceesConfig.wingStyle.next());
				y += 18;
				addHit(x, y, w, 18, "Halo  ·  " + onOff(IceesConfig.halo) + "  ·  " + IceesConfig.haloStyle.title, IceesConfig.halo,
					() -> IceesConfig.halo = !IceesConfig.halo);
				y += 20;
				addHit(x, y, w, 16, "Halo form  →", false, () -> IceesConfig.haloStyle = IceesConfig.haloStyle.next());
				y += 18;
				addHit(x, y, w, 18, "Hat  ·  " + onOff(IceesConfig.hat) + "  ·  " + IceesConfig.hatStyle.title, IceesConfig.hat,
					() -> IceesConfig.hat = !IceesConfig.hat);
				y += 20;
				addHit(x, y, w, 16, "Hat form  →  " + IceesConfig.hatStyle.title, false,
					() -> IceesConfig.hatStyle = IceesConfig.hatStyle.next());
				y += 18;
				addHit(x, y, w, 18, "Cape  ·  " + onOff(IceesConfig.cape) + "  ·  " + IceesConfig.capeStyle.title, IceesConfig.cape,
					() -> IceesConfig.cape = !IceesConfig.cape);
				y += 20;
				addHit(x, y, w, 16, "Cape form  →", false, () -> IceesConfig.capeStyle = IceesConfig.capeStyle.next());
				y += 18;
				addHit(x, y, w, 18, "Aura  ·  " + onOff(IceesConfig.aura) + "  ·  " + IceesConfig.auraStyle.title, IceesConfig.aura,
					() -> IceesConfig.aura = !IceesConfig.aura);
				y += 20;
				addHit(x, y, w, 16, "Aura form  →", false, () -> IceesConfig.auraStyle = IceesConfig.auraStyle.next());
				y += 18;
				addHit(x, y, w, 16, "Jump Circles  ·  " + onOff(IceesConfig.jumpCircles), IceesConfig.jumpCircles,
					() -> IceesConfig.jumpCircles = !IceesConfig.jumpCircles);
				y += 20;
				addHit(x, y, w / 2 - 4, 18, "Size  ·  " + IceesConfig.cosmeticSize + " / 5", false, () -> {
					IceesConfig.cosmeticSize = IceesConfig.cosmeticSize >= 5 ? 1 : IceesConfig.cosmeticSize + 1;
					IceesConfig.cosmeticThickness = IceesConfig.cosmeticSize;
				});
				addHit(x + w / 2 + 4, y, w / 2 - 4, 18, "Brightness  ·  " + IceesConfig.cosmeticBrightness + " / 5", false, () -> {
					IceesConfig.cosmeticBrightness = IceesConfig.cosmeticBrightness >= 5 ? 1 : IceesConfig.cosmeticBrightness + 1;
				});
				y += 20;
				addHit(x, y, w, 18, "Custom Color  ·  " + onOff(IceesConfig.customColor), IceesConfig.customColor,
					() -> IceesConfig.customColor = !IceesConfig.customColor);
				y += 20;
				addHit(x, y, w / 3 - 4, 18, "Hue  ·  " + IceesConfig.cosmeticHue, false, () -> {
					IceesConfig.cosmeticHue = (IceesConfig.cosmeticHue + 15) % 360;
					IceesConfig.customColor = true;
				});
				addHit(x + w / 3 + 2, y, w / 3 - 4, 18, "Sat  ·  " + IceesConfig.cosmeticSat, false, () -> {
					IceesConfig.cosmeticSat = IceesConfig.cosmeticSat >= 100 ? 0 : IceesConfig.cosmeticSat + 10;
					IceesConfig.customColor = true;
				});
				addHit(x + 2 * w / 3 + 4, y, w / 3 - 4, 18, "Val  ·  " + IceesConfig.cosmeticVal, false, () -> {
					IceesConfig.cosmeticVal = IceesConfig.cosmeticVal >= 100 ? 20 : IceesConfig.cosmeticVal + 10;
					IceesConfig.customColor = true;
				});
			}
			case KNIVES -> {
				addHit(x, y, w, 18, "Knives  ·  " + onOff(IceesConfig.knife) + "  (only with sword)", IceesConfig.knife,
					() -> IceesConfig.knife = !IceesConfig.knife);
				y += 20;
				addHit(x, y, w, 16, "Form  →  " + IceesConfig.knifeStyle.title, false, () -> {
					IceesConfig.knifeStyle = IceesConfig.knifeStyle.next();
					IceesConfig.knife = true;
				});
				y += 18;
				addHit(x, y, w, 16, "Material  →  " + IceesConfig.knifeMaterial.title, false,
					() -> IceesConfig.knifeMaterial = IceesConfig.knifeMaterial.next());
				y += 18;
				addHit(x, y, w / 2 - 4, 16, "Size  ·  " + IceesConfig.knifeSize + " / 5", false, () -> {
					IceesConfig.knifeSize = IceesConfig.knifeSize >= 5 ? 1 : IceesConfig.knifeSize + 1;
				});
				addHit(x + w / 2 + 4, y, w / 2 - 4, 16, "Custom Color  ·  " + onOff(IceesConfig.knifeCustomColor), IceesConfig.knifeCustomColor,
					() -> IceesConfig.knifeCustomColor = !IceesConfig.knifeCustomColor);
				y += 18;
				addHit(x, y, w / 3 - 4, 16, "Hue  ·  " + IceesConfig.knifeHue, false, () -> {
					IceesConfig.knifeHue = (IceesConfig.knifeHue + 15) % 360;
					IceesConfig.knifeCustomColor = true;
				});
				addHit(x + w / 3 + 2, y, w / 3 - 4, 16, "Sat  ·  " + IceesConfig.knifeSat, false, () -> {
					IceesConfig.knifeSat = IceesConfig.knifeSat >= 100 ? 0 : IceesConfig.knifeSat + 10;
					IceesConfig.knifeCustomColor = true;
				});
				addHit(x + 2 * w / 3 + 4, y, w / 3 - 4, 16, "Val  ·  " + IceesConfig.knifeVal, false, () -> {
					IceesConfig.knifeVal = IceesConfig.knifeVal >= 100 ? 20 : IceesConfig.knifeVal + 10;
					IceesConfig.knifeCustomColor = true;
				});
				y += 22;
				int col = 0;
				for (KnifeStyle style : KnifeStyle.values()) {
					int bx = x + (col % 2) * (w / 2 + 6);
					int by = y + (col / 2) * 18;
					boolean on = IceesConfig.knifeStyle == style;
					KnifeStyle selected = style;
					addHit(bx, by, w / 2 - 6, 16, (on ? "●  " : "○  ") + style.title, on, () -> {
						IceesConfig.knifeStyle = selected;
						IceesConfig.knife = true;
					});
					col++;
				}
			}
			case THEMES -> {
				for (FallTheme theme : FallTheme.values()) {
					boolean on = IceesConfig.fallTheme == theme;
					FallTheme selected = theme;
					addHit(x, y, w, 22, (on ? "●  " : "○  ") + theme.title, on, () -> IceesConfig.fallTheme = selected);
					y += 24;
				}
				y += 8;
				addHit(x, y, w, 22, "Fall Intensity  ·  " + IceesConfig.fallIntensity + " / 5", false, () -> {
					IceesConfig.fallIntensity = IceesConfig.fallIntensity >= 5 ? 1 : IceesConfig.fallIntensity + 1;
				});
				y += 26;
				addHit(x, y, w, 22, "Movement Trail  ·  " + onOff(IceesConfig.trail), IceesConfig.trail,
					() -> IceesConfig.trail = !IceesConfig.trail);
			}
			case OVERLAY -> {
				addHit(x, y, w, 22, "Block Overlay  ·  " + onOff(IceesConfig.blockOverlay), IceesConfig.blockOverlay,
					() -> IceesConfig.blockOverlay = !IceesConfig.blockOverlay);
				y += 26;
				addHit(x, y, w, 20, "Material  →  " + IceesConfig.overlayMaterial.title, false,
					() -> IceesConfig.overlayMaterial = IceesConfig.overlayMaterial.next());
				y += 24;
				addHit(x, y, w, 20, "Brightness  ·  " + IceesConfig.overlayBrightness + " / 5", false, () -> {
					IceesConfig.overlayBrightness = IceesConfig.overlayBrightness >= 5 ? 1 : IceesConfig.overlayBrightness + 1;
				});
				y += 24;
				addHit(x, y, w, 20, "Custom Color  ·  " + onOff(IceesConfig.overlayCustomColor), IceesConfig.overlayCustomColor,
					() -> IceesConfig.overlayCustomColor = !IceesConfig.overlayCustomColor);
				y += 24;
				addHit(x, y, w / 3 - 4, 20, "Hue  ·  " + IceesConfig.overlayHue, false, () -> {
					IceesConfig.overlayHue = (IceesConfig.overlayHue + 15) % 360;
					IceesConfig.overlayCustomColor = true;
				});
				addHit(x + w / 3 + 2, y, w / 3 - 4, 20, "Sat  ·  " + IceesConfig.overlaySat, false, () -> {
					IceesConfig.overlaySat = IceesConfig.overlaySat >= 100 ? 0 : IceesConfig.overlaySat + 10;
					IceesConfig.overlayCustomColor = true;
				});
				addHit(x + 2 * w / 3 + 4, y, w / 3 - 4, 20, "Val  ·  " + IceesConfig.overlayVal, false, () -> {
					IceesConfig.overlayVal = IceesConfig.overlayVal >= 100 ? 20 : IceesConfig.overlayVal + 10;
					IceesConfig.overlayCustomColor = true;
				});
				y += 28;
				int col = 0;
				for (OverlayMaterial mat : OverlayMaterial.values()) {
					int bx = x + (col % 2) * (w / 2 + 6);
					int by = y + (col / 2) * 22;
					boolean on = IceesConfig.overlayMaterial == mat;
					OverlayMaterial selected = mat;
					addHit(bx, by, w / 2 - 6, 20, (on ? "●  " : "○  ") + mat.title, on, () -> {
						IceesConfig.overlayMaterial = selected;
						IceesConfig.blockOverlay = true;
					});
					col++;
				}
			}
			case CPVP -> {
				addHit(x, y, w, 22, "Motion Blur  ·  " + onOff(IceesConfig.motionBlur), IceesConfig.motionBlur,
					() -> IceesConfig.motionBlur = !IceesConfig.motionBlur);
				y += 26;
				addHit(x, y, w, 20, "Blur Strength  ·  " + IceesConfig.motionBlurStrength + " / 5", false, () -> {
					IceesConfig.motionBlurStrength = IceesConfig.motionBlurStrength >= 5 ? 1 : IceesConfig.motionBlurStrength + 1;
					IceesConfig.motionBlur = true;
				});
				y += 26;
				addHit(x, y, w, 22, "No Hurt Cam  ·  " + onOff(IceesConfig.noHurtCam), IceesConfig.noHurtCam,
					() -> IceesConfig.noHurtCam = !IceesConfig.noHurtCam);
				y += 26;
				addHit(x, y, w, 22, "Fullbright  ·  " + onOff(IceesConfig.fullbright), IceesConfig.fullbright, () -> {
					IceesConfig.fullbright = !IceesConfig.fullbright;
					IceesConfig.applyFullbright(Minecraft.getInstance());
				});
				y += 26;
				addHit(x, y, w, 22, "Low Particles (FPS)  ·  " + onOff(IceesConfig.lowParticles), IceesConfig.lowParticles,
					() -> IceesConfig.lowParticles = !IceesConfig.lowParticles);
			}
			case GUI -> {
				addHit(x, y, w, 22, "Custom GUI Color  ·  " + onOff(IceesConfig.guiCustomColor), IceesConfig.guiCustomColor,
					() -> IceesConfig.guiCustomColor = !IceesConfig.guiCustomColor);
				y += 26;
				addHit(x, y, w, 22, "Rainbow GUI  ·  " + onOff(IceesConfig.guiRainbow), IceesConfig.guiRainbow,
					() -> IceesConfig.guiRainbow = !IceesConfig.guiRainbow);
				y += 26;
				addHit(x, y, w / 3 - 4, 22, "Hue  ·  " + IceesConfig.guiHue, false, () -> {
					IceesConfig.guiHue = (IceesConfig.guiHue + 15) % 360;
					IceesConfig.guiCustomColor = true;
					IceesConfig.guiRainbow = false;
				});
				addHit(x + w / 3 + 2, y, w / 3 - 4, 22, "Sat  ·  " + IceesConfig.guiSat, false, () -> {
					IceesConfig.guiSat = IceesConfig.guiSat >= 100 ? 0 : IceesConfig.guiSat + 10;
					IceesConfig.guiCustomColor = true;
					IceesConfig.guiRainbow = false;
				});
				addHit(x + 2 * w / 3 + 4, y, w / 3 - 4, 22, "Val  ·  " + IceesConfig.guiVal, false, () -> {
					IceesConfig.guiVal = IceesConfig.guiVal >= 100 ? 20 : IceesConfig.guiVal + 10;
					IceesConfig.guiCustomColor = true;
					IceesConfig.guiRainbow = false;
				});
				y += 32;
				addHit(x, y, w, 22, "Reset GUI to Sakura pink", false, () -> {
					IceesConfig.guiCustomColor = false;
					IceesConfig.guiRainbow = false;
					IceesConfig.guiHue = 330;
					IceesConfig.guiSat = 55;
					IceesConfig.guiVal = 100;
				});
				y += 36;
				// live colour preview swatch
				float[] accent = IceesConfig.guiAccentRgb();
				addHit(x, y, w, 28, "Accent preview  ·  H" + IceesConfig.guiHue + " S" + IceesConfig.guiSat + " V" + IceesConfig.guiVal,
					true, () -> {});
			}
			case KEYBIND -> {
				String keyName = listeningKey ? "press any key…" : keyLabel(IceesConfig.orbitKeyCode);
				addHit(x, y, w, 30, "Orbit Menu Key  ·  " + keyName, listeningKey, () -> listeningKey = true);
				y += 40;
				addHit(x, y, w, 24, "Reset to Right Shift", false, () -> {
					IceesKeybinds.resetDefault();
					listeningKey = false;
				});
			}
		}
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

		Component brand = Component.literal("Icee's visuals").withStyle(Style.EMPTY.withFont(TITLE_FONT));
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

		for (Hit hit : hits) {
			boolean hover = inside(mouseX, mouseY, hit.x, hit.y, hit.w, hit.h);
			Identifier rowTex = hit.active ? TEX_ROW_ON : hover ? TEX_ROW_HOVER : TEX_ROW;
			graphics.blit(RenderPipelines.GUI_TEXTURED, rowTex, hit.x, hit.y, 0f, 0f, hit.w, hit.h, 512, 64, -1);
			graphics.drawString(this.font, hit.label, hit.x + 14, hit.y + (hit.h - 8) / 2, SakuraTheme.text(), false);
			// colour swatch on GUI preview row
			if (tab == Tab.GUI && hit.label.startsWith("Accent preview")) {
				graphics.fill(hit.x + hit.w - 36, hit.y + 6, hit.x + hit.w - 10, hit.y + hit.h - 6, SakuraTheme.accent());
			}
		}

		String tip = switch (tab) {
			case SKIES -> IceesConfig.customSky ? IceesConfig.skyStyle.description : "Custom Sky OFF — vanilla sky restored.";
			case COSMETICS -> "Forms + size + colour. Third person for body cosmetics.";
			case KNIVES -> "Equip a sword. Form / size / material / colour — held in hand.";
			case THEMES -> IceesConfig.fallTheme == FallTheme.OFF ? "Themes off." : IceesConfig.fallTheme.description;
			case OVERLAY -> "Look at a block — outline colour / chrome / rainbow.";
			case CPVP -> "Motion blur, no hurt cam, fullbright, low particles.";
			case GUI -> "Paint the Orbit menu — Hue / Sat / Val or Rainbow.";
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
		IceesConfig.save();
		super.onClose();
	}
}
