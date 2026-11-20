package com.icees.visuals.cosmetics;

import com.icees.visuals.config.IceesConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.joml.Matrix3x2fStack;

/** First-person knife only — HUD overlay (body cosmetics stay third-person). */
public final class KnifeHudRenderer {
	private static final Identifier TEX = Identifier.fromNamespaceAndPath("icees-visuals", "textures/cosmetic/knife.png");
	private static final Identifier GLOW = Identifier.fromNamespaceAndPath("icees-visuals", "textures/cosmetic/aura.png");

	private KnifeHudRenderer() {}

	public static void render(GuiGraphics graphics) {
		if (!IceesConfig.knife) return;
		Minecraft client = Minecraft.getInstance();
		if (client.player == null || client.level == null) return;
		if (client.screen != null) return;
		if (!client.options.getCameraType().isFirstPerson()) return;
		if (client.options.hideGui) return;

		float time = client.player.tickCount + client.getDeltaTracker().getGameTimeDeltaPartialTick(false);
		int w = client.getWindow().getGuiScaledWidth();
		int h = client.getWindow().getGuiScaledHeight();
		float bright = IceesConfig.brightnessFactor();
		float thick = IceesConfig.thicknessFactor();

		float spin = switch (IceesConfig.knifeStyle) {
			case BUTTERFLY, KARAMBIT, CURVED -> time * 8f;
			case BAYONET, LONG_BLADE -> Mth.sin(time * 0.2f) * 25f;
			case STILETTO, SHORT_BLADE -> Mth.sin(time * 0.35f) * 40f;
			case URSUS -> time * 5f + Mth.sin(time * 0.45f) * 18f;
		};

		float cx = w * 0.74f + Mth.sin(time * 0.15f) * 8f;
		float cy = h * 0.64f + Mth.cos(time * 0.18f) * 5f;
		int size = Math.max(56, (int) ((h / 4.2f) * thick));

		Matrix3x2fStack pose = graphics.pose();
		pose.pushMatrix();
		pose.translate(cx, cy);
		pose.rotate((float) Math.toRadians(spin));
		pose.translate(-size / 2f, -size / 2f);

		int bloom = Math.max(size + 28, (int) (size * 1.55f));
		int glow = ARGB.colorFromFloat(Mth.clamp(0.55f * bright, 0f, 1f), 1f, 0.65f, 0.85f);
		int blade = ARGB.colorFromFloat(Mth.clamp(0.98f * bright, 0f, 1f), 1f, 0.94f, 0.98f);
		pose.translate(-(bloom - size) / 2f, -(bloom - size) / 2f);
		graphics.blit(RenderPipelines.GUI_TEXTURED, GLOW, 0, 0, 0f, 0f, bloom, bloom, 256, 256, glow);
		pose.translate((bloom - size) / 2f, (bloom - size) / 2f);
		graphics.blit(RenderPipelines.GUI_TEXTURED, TEX, 0, 0, 0f, 0f, size, size, 128, 256, blade);
		pose.popMatrix();
	}
}
