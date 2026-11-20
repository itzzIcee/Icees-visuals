package com.icees.visuals.overlay;

import com.icees.visuals.config.IceesConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.BlockOutlineRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/** Custom looking-at block outline — colour / brightness / chrome / rainbow. */
public final class BlockOverlayRenderer {
	private BlockOverlayRenderer() {}

	/** @return false to cancel vanilla outline */
	public static boolean render(WorldRenderContext context, BlockOutlineRenderState outline) {
		if (!IceesConfig.blockOverlay || outline == null) return true;
		Minecraft client = Minecraft.getInstance();
		if (client.player == null || client.level == null) return true;

		MultiBufferSource consumers = context.consumers();
		PoseStack matrices = context.matrices();
		if (consumers == null || matrices == null) return true;

		float partial = client.getDeltaTracker().getGameTimeDeltaPartialTick(false);
		float time = client.player.tickCount + partial;
		float[] rgb = IceesConfig.overlayColor(time);
		float bright = IceesConfig.overlayBrightnessFactor();
		float r = Math.min(1f, rgb[0] * bright);
		float g = Math.min(1f, rgb[1] * bright);
		float b = Math.min(1f, rgb[2] * bright);
		float a = Mth.clamp(0.45f + IceesConfig.overlayBrightness * 0.1f, 0.35f, 1f);
		int color = ARGB.colorFromFloat(a, r, g, b);

		Vec3 cam = client.gameRenderer.getMainCamera().position();
		BlockPos pos = outline.pos();
		double ox = pos.getX() - cam.x;
		double oy = pos.getY() - cam.y;
		double oz = pos.getZ() - cam.z;

		float width = Math.max(2.5f, client.getWindow().getAppropriateLineWidth() + IceesConfig.overlayBrightness * 0.6f);
		VertexConsumer lines = consumers.getBuffer(RenderTypes.lines());
		ShapeRenderer.renderShape(matrices, lines, outline.shape(), ox, oy, oz, color, width);

		// Extra chrome/rainbow shimmer pass
		if (IceesConfig.overlayMaterial == OverlayMaterial.CHROME
			|| IceesConfig.overlayMaterial == OverlayMaterial.RAINBOW
			|| IceesConfig.overlayMaterial == OverlayMaterial.SILVER) {
			float shimmer = 0.6f + 0.4f * (0.5f + 0.5f * Mth.sin(time * 0.45f));
			int shine = ARGB.colorFromFloat(0.55f, shimmer, shimmer, Math.min(1f, shimmer + 0.05f));
			ShapeRenderer.renderShape(matrices, lines, outline.shape(), ox, oy, oz, shine, width + 1.5f);
		}
		return false;
	}
}
