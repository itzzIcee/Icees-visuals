package com.icees.visuals.cosmetics;

import com.icees.visuals.config.IceesConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/**
 * First-person knife — realistic 3D mesh inheriting vanilla arm swing.
 */
public final class FirstPersonKnifeRenderer {
	private static final Identifier TEX = Identifier.fromNamespaceAndPath("icees-visuals", "textures/cosmetic/knife.png");

	private FirstPersonKnifeRenderer() {}

	public static void render(PoseStack matrices, int light, float swingProgress) {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null) return;

		float time = client.player.tickCount + client.getDeltaTracker().getGameTimeDeltaPartialTick(false);
		float[] rgb = IceesConfig.knifeBladeColor(time);
		float size = IceesConfig.knifeSizeFactor();
		float bright = IceesConfig.brightnessFactor();

		MultiBufferSource.BufferSource buffers = client.renderBuffers().bufferSource();
		VertexConsumer vc = buffers.getBuffer(RenderTypes.eyes(TEX));

		matrices.pushPose();
		matrices.translate(0.0f, 0.05f, 0.02f);
		matrices.mulPose(Axis.XP.rotationDegrees(-12f - swingProgress * 10f));
		matrices.mulPose(Axis.ZP.rotationDegrees(4f));
		matrices.scale(size, size, size);

		float r = Math.min(1f, rgb[0] * bright);
		float g = Math.min(1f, rgb[1] * bright);
		float b = Math.min(1f, rgb[2] * bright);
		KnifeModels.draw(matrices, vc, light, IceesConfig.knifeStyle, r, g, b, time);

		matrices.popPose();
		buffers.endLastBatch();
	}
}
