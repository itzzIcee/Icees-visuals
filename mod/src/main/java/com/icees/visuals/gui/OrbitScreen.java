package com.icees.visuals.gui;

import com.icees.visuals.config.IceesConfig;
import com.icees.visuals.cosmetics.KnifeStyle;
import com.icees.visuals.sky.SkyStyle;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

/**
 * Orbit menu (Right Shift) — sakura pink module panel.
 */
public class OrbitScreen extends Screen {
	private enum Tab { SKIES, COSMETICS, KNIVES, EFFECTS }

	private Tab tab = Tab.SKIES;
	private int panelX;
	private int panelY;
	private final int panelW = 440;
	private final int panelH = 300;

	public OrbitScreen() {
		super(Text.translatable("gui.icees-visuals.orbit"));
	}

	@Override
	protected void init() {
		rebuild();
	}

	private void rebuild() {
		clearChildren();
		panelX = (this.width - panelW) / 2;
		panelY = (this.height - panelH) / 2;
		int x = panelX + 16;
		int y = panelY + 56;
		int w = panelW - 32;

		switch (tab) {
			case SKIES -> {
				add(row(x, y, w, "Custom Sky: " + onOff(IceesConfig.customSky), () -> {
					IceesConfig.customSky = !IceesConfig.customSky;
				}));
				y += 26;
				int col = 0;
				for (SkyStyle style : SkyStyle.values()) {
					int bx = x + (col % 2) * (w / 2 + 4);
					int by = y + (col / 2) * 24;
					boolean on = IceesConfig.skyStyle == style;
					add(row(bx, by, w / 2 - 4, (on ? "● " : "○ ") + style.title, () -> {
						IceesConfig.skyStyle = style;
						IceesConfig.customSky = true;
					}));
					col++;
				}
			}
			case COSMETICS -> {
				add(row(x, y, w, "Wings: " + onOff(IceesConfig.wings) + " · " + IceesConfig.wingStyle.title, () -> IceesConfig.wings = !IceesConfig.wings));
				y += 24;
				add(row(x, y, w, "Next wing →", () -> IceesConfig.wingStyle = IceesConfig.wingStyle.next()));
				y += 26;
				add(row(x, y, w, "Halo: " + onOff(IceesConfig.halo) + " · " + IceesConfig.haloStyle.title, () -> IceesConfig.halo = !IceesConfig.halo));
				y += 24;
				add(row(x, y, w, "Next halo →", () -> IceesConfig.haloStyle = IceesConfig.haloStyle.next()));
				y += 26;
				add(row(x, y, w, "Cape: " + onOff(IceesConfig.cape) + " · " + IceesConfig.capeStyle.title, () -> IceesConfig.cape = !IceesConfig.cape));
				y += 24;
				add(row(x, y, w, "Next cape →", () -> IceesConfig.capeStyle = IceesConfig.capeStyle.next()));
				y += 26;
				add(row(x, y, w, "Aura: " + onOff(IceesConfig.aura), () -> IceesConfig.aura = !IceesConfig.aura));
				y += 24;
				add(row(x, y, w, "Jump Circles: " + onOff(IceesConfig.jumpCircles), () -> IceesConfig.jumpCircles = !IceesConfig.jumpCircles));
			}
			case KNIVES -> {
				add(row(x, y, w, "Knife Animations: " + onOff(IceesConfig.knife), () -> IceesConfig.knife = !IceesConfig.knife));
				y += 28;
				for (KnifeStyle style : KnifeStyle.values()) {
					boolean on = IceesConfig.knifeStyle == style;
					add(row(x, y, w, (on ? "● " : "○ ") + style.title, () -> {
						IceesConfig.knifeStyle = style;
						IceesConfig.knife = true;
					}));
					y += 22;
				}
			}
			case EFFECTS -> {
				add(row(x, y, w, "Sakura Petals: " + onOff(IceesConfig.petals), () -> IceesConfig.petals = !IceesConfig.petals));
				y += 26;
				add(row(x, y, w, "Movement Trail: " + onOff(IceesConfig.trail), () -> IceesConfig.trail = !IceesConfig.trail));
				y += 26;
				add(row(x, y, w, "Accent warmth +", () -> IceesConfig.accentHue = (IceesConfig.accentHue + 0.04f) % 1f));
			}
		}
	}

	private void add(ClickableWidget widget) {
		addDrawableChild(widget);
	}

	private ButtonWidget row(int x, int y, int w, String label, Runnable action) {
		return ButtonWidget.builder(Text.literal(label), b -> {
			action.run();
			IceesConfig.save();
			rebuild();
		}).dimensions(x, y, w, 20).build();
	}

	private static String onOff(boolean v) {
		return v ? "ON" : "OFF";
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		context.fill(0, 0, width, height, SakuraTheme.BG);
		context.fill(panelX - 18, panelY - 18, panelX + panelW + 18, panelY + panelH + 18, 0x33FF8FB8);
		context.fill(panelX, panelY, panelX + panelW, panelY + panelH, SakuraTheme.PANEL);
		context.fill(panelX, panelY, panelX + panelW, panelY + 3, SakuraTheme.ACCENT_DEEP);
		context.fill(panelX, panelY + 3, panelX + panelW, panelY + 4, SakuraTheme.ACCENT_HOT);

		context.drawText(textRenderer, "Icees · Orbit", panelX + 16, panelY + 12, SakuraTheme.ACCENT_HOT, false);
		context.drawText(textRenderer, "sakura design", panelX + 16, panelY + 26, SakuraTheme.MUTED, false);

		drawTab(context, panelX + 16, panelY + 40, "Skies", Tab.SKIES, mouseX, mouseY);
		drawTab(context, panelX + 88, panelY + 40, "Cosmetics", Tab.COSMETICS, mouseX, mouseY);
		drawTab(context, panelX + 186, panelY + 40, "Knives", Tab.KNIVES, mouseX, mouseY);
		drawTab(context, panelX + 258, panelY + 40, "Effects", Tab.EFFECTS, mouseX, mouseY);

		String tip = switch (tab) {
			case SKIES -> IceesConfig.skyStyle.description;
			case COSMETICS -> "Wings, halos & capes — client-side only.";
			case KNIVES -> IceesConfig.knifeStyle.tricks;
			case EFFECTS -> "Petals and warmth over the world.";
		};
		context.drawText(textRenderer, tip, panelX + 16, panelY + panelH - 20, SakuraTheme.MUTED, false);

		super.render(context, mouseX, mouseY, delta);
	}

	private void drawTab(DrawContext context, int x, int y, String label, Tab value, int mouseX, int mouseY) {
		boolean on = tab == value;
		boolean hover = mouseX >= x && mouseX <= x + textRenderer.getWidth(label) + 10 && mouseY >= y && mouseY <= y + 12;
		int color = on ? SakuraTheme.ACCENT_HOT : hover ? SakuraTheme.TEXT : SakuraTheme.MUTED;
		context.drawText(textRenderer, label, x, y, color, false);
		if (on) {
			context.fill(x, y + 10, x + textRenderer.getWidth(label), y + 11, SakuraTheme.ACCENT);
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (clickTab(mouseX, mouseY, panelX + 16, "Skies", Tab.SKIES)) return true;
		if (clickTab(mouseX, mouseY, panelX + 88, "Cosmetics", Tab.COSMETICS)) return true;
		if (clickTab(mouseX, mouseY, panelX + 186, "Knives", Tab.KNIVES)) return true;
		if (clickTab(mouseX, mouseY, panelX + 258, "Effects", Tab.EFFECTS)) return true;
		return super.mouseClicked(mouseX, mouseY, button);
	}

	private boolean clickTab(double mouseX, double mouseY, int x, String label, Tab value) {
		int y = panelY + 40;
		if (mouseX >= x && mouseX <= x + textRenderer.getWidth(label) + 10 && mouseY >= y && mouseY <= y + 12) {
			tab = value;
			rebuild();
			return true;
		}
		return false;
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	@Override
	public void close() {
		IceesConfig.save();
		super.close();
	}
}
