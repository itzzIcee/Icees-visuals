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
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom sakura glass Orbit menu — no vanilla button textures.
 * Brand mark uses the Great Vibes title font: "Icee's visuals".
 */
public class OrbitScreen extends Screen {
	private static final FontDescription TITLE_FONT =
		new FontDescription.Resource(Identifier.fromNamespaceAndPath("icees-visuals", "title"));

	private enum Tab { SKIES, COSMETICS, KNIVES, THEMES, KEYBIND }

	private Tab tab = Tab.SKIES;
	private int panelX;
	private int panelY;
	private final int panelW = 480;
	private final int panelH = 320;
	private boolean listeningKey;
	private final List<Hit> hits = new ArrayList<>();
	private final List<TabHit> tabHits = new ArrayList<>();
	private long openMs = System.currentTimeMillis();

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

	private void rebuildHits() {
		hits.clear();
		tabHits.clear();
		panelX = (this.width - panelW) / 2;
		panelY = (this.height - panelH) / 2;

		int tx = panelX + 18;
		int ty = panelY + 58;
		addTab(tx, ty, "Skies", Tab.SKIES); tx += 58;
		addTab(tx, ty, "Cosmetics", Tab.COSMETICS); tx += 88;
		addTab(tx, ty, "Knives", Tab.KNIVES); tx += 64;
		addTab(tx, ty, "Themes", Tab.THEMES); tx += 72;
		addTab(tx, ty, "Key", Tab.KEYBIND);

		int x = panelX + 18;
		int y = panelY + 82;
		int w = panelW - 36;

		switch (tab) {
			case SKIES -> {
				addHit(x, y, w, 22, "Custom Sky  ·  " + onOff(IceesConfig.customSky), IceesConfig.customSky, () -> {
					IceesConfig.customSky = !IceesConfig.customSky;
				});
				y += 28;
				int col = 0;
				for (SkyStyle style : SkyStyle.values()) {
					int bx = x + (col % 2) * (w / 2 + 6);
					int by = y + (col / 2) * 26;
					boolean on = IceesConfig.skyStyle == style;
					SkyStyle selected = style;
					addHit(bx, by, w / 2 - 6, 22, (on ? "●  " : "○  ") + style.title, on, () -> {
						IceesConfig.skyStyle = selected;
						IceesConfig.customSky = true;
					});
					col++;
				}
			}
			case COSMETICS -> {
				addHit(x, y, w, 22, "Wings  ·  " + onOff(IceesConfig.wings) + "  ·  " + IceesConfig.wingStyle.title, IceesConfig.wings,
					() -> IceesConfig.wings = !IceesConfig.wings);
				y += 26;
				addHit(x, y, w, 22, "Next wing  →", false, () -> IceesConfig.wingStyle = IceesConfig.wingStyle.next());
				y += 28;
				addHit(x, y, w, 22, "Halo  ·  " + onOff(IceesConfig.halo) + "  ·  " + IceesConfig.haloStyle.title, IceesConfig.halo,
					() -> IceesConfig.halo = !IceesConfig.halo);
				y += 26;
				addHit(x, y, w, 22, "Next halo  →", false, () -> IceesConfig.haloStyle = IceesConfig.haloStyle.next());
				y += 28;
				addHit(x, y, w, 22, "Cape  ·  " + onOff(IceesConfig.cape) + "  ·  " + IceesConfig.capeStyle.title, IceesConfig.cape,
					() -> IceesConfig.cape = !IceesConfig.cape);
				y += 26;
				addHit(x, y, w, 22, "Next cape  →", false, () -> IceesConfig.capeStyle = IceesConfig.capeStyle.next());
				y += 28;
				addHit(x, y, w / 2 - 4, 22, "Aura  ·  " + onOff(IceesConfig.aura), IceesConfig.aura, () -> IceesConfig.aura = !IceesConfig.aura);
				addHit(x + w / 2 + 4, y, w / 2 - 4, 22, "Jump Circles  ·  " + onOff(IceesConfig.jumpCircles), IceesConfig.jumpCircles,
					() -> IceesConfig.jumpCircles = !IceesConfig.jumpCircles);
			}
			case KNIVES -> {
				addHit(x, y, w, 22, "Knife Animations  ·  " + onOff(IceesConfig.knife), IceesConfig.knife, () -> IceesConfig.knife = !IceesConfig.knife);
				y += 30;
				for (KnifeStyle style : KnifeStyle.values()) {
					boolean on = IceesConfig.knifeStyle == style;
					KnifeStyle selected = style;
					addHit(x, y, w, 22, (on ? "●  " : "○  ") + style.title, on, () -> {
						IceesConfig.knifeStyle = selected;
						IceesConfig.knife = true;
					});
					y += 24;
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
				y += 28;
				addHit(x, y, w, 22, "Movement Trail  ·  " + onOff(IceesConfig.trail), IceesConfig.trail, () -> IceesConfig.trail = !IceesConfig.trail);
			}
			case KEYBIND -> {
				String keyName = listeningKey ? "press any key…" : keyLabel(IceesConfig.orbitKeyCode);
				addHit(x, y, w, 28, "Orbit Menu Key  ·  " + keyName, listeningKey, () -> listeningKey = true);
				y += 36;
				addHit(x, y, w, 22, "Reset to Right Shift", false, () -> {
					IceesKeybinds.resetDefault();
					listeningKey = false;
				});
			}
		}
	}

	private void addTab(int x, int y, String label, Tab value) {
		int w = this.font.width(label) + 12;
		tabHits.add(new TabHit(x, y, w, 14, value, label));
	}

	private void addHit(int x, int y, int w, int h, String label, boolean active, Runnable action) {
		hits.add(new Hit(x, y, w, h, () -> {
			action.run();
			IceesConfig.save();
			rebuildHits();
		}, label, active));
	}

	private static String onOff(boolean v) {
		return v ? "ON" : "OFF";
	}

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
			case GLFW.GLFW_KEY_CAPS_LOCK -> "CAPS";
			case GLFW.GLFW_KEY_INSERT -> "INSERT";
			default -> "KEY " + code;
		};
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		float t = (System.currentTimeMillis() - openMs) / 1000f;
		float appear = Math.min(1f, t * 2.4f);

		// Soft sakura dusk scrim
		graphics.fill(0, 0, width, height, SakuraTheme.SCRIM);
		// Ambient bloom behind panel
		int bloom = (int) (0x22 + appear * 0x33);
		graphics.fill(panelX - 28, panelY - 24, panelX + panelW + 28, panelY + panelH + 24, (bloom << 24) | 0xFF8FB8);
		graphics.fill(panelX - 10, panelY - 8, panelX + panelW + 10, panelY + panelH + 8, SakuraTheme.PANEL_EDGE);

		// Glass panel
		graphics.fill(panelX, panelY, panelX + panelW, panelY + panelH, SakuraTheme.PANEL);
		graphics.fill(panelX, panelY, panelX + panelW, panelY + 2, SakuraTheme.ACCENT_DEEP);
		graphics.fill(panelX, panelY + 2, panelX + panelW, panelY + 3, SakuraTheme.ACCENT_HOT);
		graphics.fill(panelX + 1, panelY + 3, panelX + panelW - 1, panelY + 48, SakuraTheme.GLASS);

		// Brand title — Great Vibes script
		Component brand = Component.literal("Icee's visuals")
			.withStyle(Style.EMPTY.withFont(TITLE_FONT));
		int brandX = panelX + 20;
		int brandY = panelY + 12;
		graphics.drawString(this.font, brand, brandX + 1, brandY + 1, SakuraTheme.TITLE_GLOW, false);
		graphics.drawString(this.font, brand, brandX, brandY, SakuraTheme.TITLE, false);
		graphics.drawString(this.font, "orbit  ·  sakura", panelX + 22, panelY + 38, SakuraTheme.MUTED, false);

		// Tabs
		for (TabHit th : tabHits) {
			boolean on = tab == th.tab;
			boolean hover = inside(mouseX, mouseY, th.x, th.y, th.w, th.h);
			int color = on ? SakuraTheme.ACCENT_HOT : hover ? SakuraTheme.TEXT : SakuraTheme.MUTED;
			graphics.drawString(this.font, th.label, th.x, th.y, color, false);
			if (on) {
				graphics.fill(th.x, th.y + 11, th.x + this.font.width(th.label), th.y + 12, SakuraTheme.ACCENT);
			}
		}

		// Rows
		for (Hit hit : hits) {
			boolean hover = inside(mouseX, mouseY, hit.x, hit.y, hit.w, hit.h);
			int bg = hit.active ? SakuraTheme.ROW_ON : hover ? SakuraTheme.ROW_HOVER : SakuraTheme.ROW;
			graphics.fill(hit.x, hit.y, hit.x + hit.w, hit.y + hit.h, bg);
			if (hit.active || hover) {
				graphics.fill(hit.x, hit.y, hit.x + 2, hit.y + hit.h, SakuraTheme.ACCENT_HOT);
			}
			int ty = hit.y + (hit.h - 8) / 2;
			graphics.drawString(this.font, hit.label, hit.x + 10, ty, SakuraTheme.TEXT, false);
		}

		String tip = switch (tab) {
			case SKIES -> IceesConfig.skyStyle.description;
			case COSMETICS -> "Wings, halos & capes — client-side only.";
			case KNIVES -> IceesConfig.knifeStyle.tricks;
			case THEMES -> IceesConfig.fallTheme.description;
			case KEYBIND -> listeningKey ? "Waiting for a key…" : "Click the row, then press a new key.";
		};
		graphics.drawString(this.font, tip, panelX + 18, panelY + panelH - 18, SakuraTheme.MUTED, false);
	}

	private static boolean inside(double mx, double my, int x, int y, int w, int h) {
		return mx >= x && mx <= x + w && my >= y && my <= y + h;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
		double mx = event.x();
		double my = event.y();
		if (event.button() != 0) {
			return super.mouseClicked(event, bl);
		}
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
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void onClose() {
		IceesConfig.save();
		super.onClose();
	}
}
