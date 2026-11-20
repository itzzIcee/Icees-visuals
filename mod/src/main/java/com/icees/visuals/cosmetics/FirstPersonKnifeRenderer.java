package com.icees.visuals.cosmetics;

import com.icees.visuals.config.IceesConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/**
 * First-person knife — flat M9-style bayonet sitting in the grip like the reference.
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

		MultiBufferSource.BufferSource buffers = client.renderBuffers().bufferSource();
		// entityTranslucent-ish glow still readable; eyes made shards look like lasers
		VertexConsumer vc = buffers.getBuffer(RenderTypes.entityCutoutNoCull(TEX));

		matrices.pushPose();
		// Hold like the M9 ref: blade across the lower-right FOV, tip forward-up
		matrices.translate(0.12f, -0.02f, -0.35f);
		matrices.mulPose(Axis.YP.rotationDegrees(75f));
		matrices.mulPose(Axis.XP.rotationDegrees(8f + swingProgress * 25f));
		matrices.mulPose(Axis.ZP.rotationDegrees(-12f - swingProgress * 8f));
		matrices.scale(size * 1.15f, size * 1.15f, size * 1.15f);

		float bright = 0.85f + IceesConfig.brightnessFactor() * 0.15f;
		float r = Math.min(1f, rgb[0] * bright);
		float g = Math.min(1f, rgb[1] * bright);
		float b = Math.min(1f, rgb[2] * bright);
		KnifeModels.draw(matrices, vc, Math.max(light, LightTexture.pack(12, 12)), IceesConfig.knifeStyle, r, g, b, time);

		matrices.popPose();
		buffers.endLastBatch();
	}
}
