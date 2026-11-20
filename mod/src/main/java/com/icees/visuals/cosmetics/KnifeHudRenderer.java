package com.icees.visuals.cosmetics;

import com.icees.visuals.config.IceesConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.joml.Matrix3x2fStack;

/** Soft first-person knife overlay — always visible when Knives are ON. */
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

		float time = client.player.tickCount + client.getDeltaTracker().getGameTimeDeltaPartialTick(false);
		int w = client.getWindow().getGuiScaledWidth();
		int h = client.getWindow().getGuiScaledHeight();

		float spin = switch (IceesConfig.knifeStyle) {
			case BUTTERFLY, KARAMBIT, CURVED -> time * 8f;
			case BAYONET, LONG_BLADE -> Mth.sin(time * 0.2f) * 25f;
			case STILETTO, SHORT_BLADE -> Mth.sin(time * 0.35f) * 40f;
			case URSUS -> time * 5f + Mth.sin(time * 0.45f) * 18f;
		};

		float cx = w * 0.72f + Mth.sin(time * 0.15f) * 6f;
		float cy = h * 0.62f + Mth.cos(time * 0.18f) * 4f;
		int size = Math.max(48, h / 5);

		Matrix3x2fStack pose = graphics.pose();
		pose.pushMatrix();
		pose.translate(cx, cy);
		pose.rotate((float) Math.toRadians(spin));
		pose.translate(-size / 2f, -size / 2f);

		int glow = ARGB.colorFromFloat(0.35f, 1f, 0.7f, 0.85f);
		int blade = ARGB.colorFromFloat(0.95f, 1f, 0.92f, 0.96f);
		int bloom = Math.max(size + 20, (int) (size * 1.4f));
		pose.translate(-(bloom - size) / 2f, -(bloom - size) / 2f);
		graphics.blit(RenderPipelines.GUI_TEXTURED, GLOW, 0, 0, 0f, 0f, bloom, bloom, 256, 256, glow);
		pose.translate((bloom - size) / 2f, (bloom - size) / 2f);
		graphics.blit(RenderPipelines.GUI_TEXTURED, TEX, 0, 0, 0f, 0f, size, size, 128, 256, blade);
		pose.popMatrix();
	}
}
