package com.icees.visuals.gui;

import com.icees.visuals.config.IceesConfig;
import com.icees.visuals.cosmetics.KnifeStyle;
import com.icees.visuals.effects.FallTheme;
import com.icees.visuals.keybind.IceesKeybinds;
import com.icees.visuals.sky.SkyStyle;
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

	private enum Tab { SKIES, COSMETICS, KNIVES, THEMES, KEYBIND }

	private Tab tab = Tab.SKIES;
	private int panelX, panelY;
	private final int panelW = 500;
	private final int panelH = 330;
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

	/** Kill vanilla menu dirt / blur / panorama. */
	@Override
	public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		graphics.fill(0, 0, width, height, 0xAA080510);
		graphics.blit(RenderPipelines.GUI_TEXTURED, TEX_SCRIM, 0, 0, 0f, 0f, width, height, 256, 256, ARGB.colorFromFloat(0.55f, 1f, 0.7f, 0.85f));
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

		int tx = panelX + 28;
		int ty = panelY + 64;
		addTab(tx, ty, "Skies", Tab.SKIES); tx += 60;
		addTab(tx, ty, "Cosmetics", Tab.COSMETICS); tx += 92;
		addTab(tx, ty, "Knives", Tab.KNIVES); tx += 68;
		addTab(tx, ty, "Themes", Tab.THEMES); tx += 74;
		addTab(tx, ty, "Key", Tab.KEYBIND);

		int x = panelX + 28;
		int y = panelY + 92;
		int w = panelW - 56;

		switch (tab) {
			case SKIES -> {
				addHit(x, y, w, 26, "Custom Sky  ·  " + onOff(IceesConfig.customSky), IceesConfig.customSky,
					() -> IceesConfig.customSky = !IceesConfig.customSky);
				y += 32;
				int col = 0;
				for (SkyStyle style : SkyStyle.values()) {
					int bx = x + (col % 2) * (w / 2 + 8);
					int by = y + (col / 2) * 28;
					boolean on = IceesConfig.skyStyle == style;
					SkyStyle selected = style;
					addHit(bx, by, w / 2 - 8, 24, (on ? "●  " : "○  ") + style.title, on, () -> {
						IceesConfig.skyStyle = selected;
						IceesConfig.customSky = true;
					});
					col++;
				}
			}
			case COSMETICS -> {
				addHit(x, y, w, 24, "Wings  ·  " + onOff(IceesConfig.wings) + "  ·  " + IceesConfig.wingStyle.title, IceesConfig.wings,
					() -> IceesConfig.wings = !IceesConfig.wings);
				y += 28;
				addHit(x, y, w, 24, "Next wing  →", false, () -> IceesConfig.wingStyle = IceesConfig.wingStyle.next());
				y += 30;
				addHit(x, y, w, 24, "Halo  ·  " + onOff(IceesConfig.halo) + "  ·  " + IceesConfig.haloStyle.title, IceesConfig.halo,
					() -> IceesConfig.halo = !IceesConfig.halo);
				y += 28;
				addHit(x, y, w, 24, "Next halo  →", false, () -> IceesConfig.haloStyle = IceesConfig.haloStyle.next());
				y += 30;
				addHit(x, y, w, 24, "Cape  ·  " + onOff(IceesConfig.cape) + "  ·  " + IceesConfig.capeStyle.title, IceesConfig.cape,
					() -> IceesConfig.cape = !IceesConfig.cape);
				y += 28;
				addHit(x, y, w, 24, "Next cape  →", false, () -> IceesConfig.capeStyle = IceesConfig.capeStyle.next());
				y += 30;
				addHit(x, y, w / 2 - 6, 24, "Aura  ·  " + onOff(IceesConfig.aura), IceesConfig.aura, () -> IceesConfig.aura = !IceesConfig.aura);
				addHit(x + w / 2 + 6, y, w / 2 - 6, 24, "Jump Circles  ·  " + onOff(IceesConfig.jumpCircles), IceesConfig.jumpCircles,
					() -> IceesConfig.jumpCircles = !IceesConfig.jumpCircles);
			}
			case KNIVES -> {
				addHit(x, y, w, 26, "Knife Animations  ·  " + onOff(IceesConfig.knife), IceesConfig.knife,
					() -> IceesConfig.knife = !IceesConfig.knife);
				y += 34;
				for (KnifeStyle style : KnifeStyle.values()) {
					boolean on = IceesConfig.knifeStyle == style;
					KnifeStyle selected = style;
					addHit(x, y, w, 24, (on ? "●  " : "○  ") + style.title, on, () -> {
						IceesConfig.knifeStyle = selected;
						IceesConfig.knife = true;
					});
					y += 26;
				}
			}
			case THEMES -> {
				for (FallTheme theme : FallTheme.values()) {
					boolean on = IceesConfig.fallTheme == theme;
					FallTheme selected = theme;
					addHit(x, y, w, 24, (on ? "●  " : "○  ") + theme.title, on, () -> IceesConfig.fallTheme = selected);
					y += 26;
				}
				y += 10;
				addHit(x, y, w, 24, "Fall Intensity  ·  " + IceesConfig.fallIntensity + " / 5", false, () -> {
					IceesConfig.fallIntensity = IceesConfig.fallIntensity >= 5 ? 1 : IceesConfig.fallIntensity + 1;
				});
				y += 30;
				addHit(x, y, w, 24, "Movement Trail  ·  " + onOff(IceesConfig.trail), IceesConfig.trail,
					() -> IceesConfig.trail = !IceesConfig.trail);
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
		tabHits.add(new TabHit(x, y, this.font.width(label) + 14, 16, value, label));
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

		int bloom = ARGB.colorFromFloat(0.45f * appear, 1f, 0.55f, 0.72f);
		graphics.blit(RenderPipelines.GUI_TEXTURED, TEX_BLOOM,
			panelX - 110, panelY - 90, 0f, 0f, panelW + 220, panelH + 180, 512, 512, bloom);

		graphics.blit(RenderPipelines.GUI_TEXTURED, TEX_PANEL,
			panelX - 12, panelY - 12, 0f, 0f, panelW + 24, panelH + 24, 512, 512, -1);

		Component brand = Component.literal("Icee's visuals").withStyle(Style.EMPTY.withFont(TITLE_FONT));
		graphics.drawString(this.font, brand, panelX + 30, panelY + 16, SakuraTheme.TITLE_GLOW, false);
		graphics.drawString(this.font, brand, panelX + 28, panelY + 14, SakuraTheme.TITLE, false);
		graphics.drawString(this.font, "soft orbit", panelX + 30, panelY + 42, SakuraTheme.MUTED, false);

		for (TabHit th : tabHits) {
			boolean on = tab == th.tab;
			boolean hover = inside(mouseX, mouseY, th.x, th.y, th.w, th.h);
			int color = on ? SakuraTheme.ACCENT_HOT : hover ? SakuraTheme.TEXT : SakuraTheme.MUTED;
			graphics.drawString(this.font, th.label, th.x, th.y, color, false);
			if (on) {
				graphics.fill(th.x, th.y + 12, th.x + this.font.width(th.label), th.y + 13, SakuraTheme.ACCENT);
			}
		}

		for (Hit hit : hits) {
			boolean hover = inside(mouseX, mouseY, hit.x, hit.y, hit.w, hit.h);
			Identifier rowTex = hit.active ? TEX_ROW_ON : hover ? TEX_ROW_HOVER : TEX_ROW;
			graphics.blit(RenderPipelines.GUI_TEXTURED, rowTex, hit.x, hit.y, 0f, 0f, hit.w, hit.h, 512, 64, -1);
			graphics.drawString(this.font, hit.label, hit.x + 14, hit.y + (hit.h - 8) / 2, SakuraTheme.TEXT, false);
		}

		String tip = switch (tab) {
			case SKIES -> IceesConfig.skyStyle.description;
			case COSMETICS -> "Soft wings, halos & capes — client-side glow.";
			case KNIVES -> IceesConfig.knifeStyle.tricks;
			case THEMES -> IceesConfig.fallTheme.description;
			case KEYBIND -> listeningKey ? "Waiting for a key…" : "Click the row, then press a new key.";
		};
		graphics.drawString(this.font, tip, panelX + 28, panelY + panelH - 22, SakuraTheme.MUTED, false);
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
