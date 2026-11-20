package com.icees.visuals.cosmetics;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

/** Shared solid box primitive for cosmetics. */
public final class MeshPrims {
	private MeshPrims() {}

	public static void box(PoseStack matrices, VertexConsumer vc, int light,
		float x0, float y0, float z0, float x1, float y1, float z1,
		float r, float g, float b, float a) {
		PoseStack.Pose pose = matrices.last();
		Matrix4f m = pose.pose();
		int overlay = OverlayTexture.NO_OVERLAY;
		float aa = Mth.clamp(a, 0f, 1f);
		quad(m, pose, vc, light, overlay, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1, r, g, b, aa);
		quad(m, pose, vc, light, overlay, x1, y0, z0, x0, y0, z0, x0, y1, z0, x1, y1, z0, r, g, b, aa);
		quad(m, pose, vc, light, overlay, x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0, r, g, b, aa);
		quad(m, pose, vc, light, overlay, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1, r, g, b, aa);
		quad(m, pose, vc, light, overlay, x1, y0, z1, x1, y0, z0, x1, y1, z0, x1, y1, z1, r, g, b, aa);
		quad(m, pose, vc, light, overlay, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0, r, g, b, aa);
	}

	private static void quad(Matrix4f m, PoseStack.Pose pose, VertexConsumer vc, int light, int overlay,
		float x0, float y0, float z0, float x1, float y1, float z1,
		float x2, float y2, float z2, float x3, float y3, float z3,
		float r, float g, float b, float a) {
		vc.addVertex(m, x0, y0, z0).setColor(r, g, b, a).setUv(0f, 1f).setOverlay(overlay).setLight(light).setNormal(pose, 0, 1, 0);
		vc.addVertex(m, x1, y1, z1).setColor(r, g, b, a).setUv(1f, 1f).setOverlay(overlay).setLight(light).setNormal(pose, 0, 1, 0);
		vc.addVertex(m, x2, y2, z2).setColor(r, g, b, a).setUv(1f, 0f).setOverlay(overlay).setLight(light).setNormal(pose, 0, 1, 0);
		vc.addVertex(m, x3, y3, z3).setColor(r, g, b, a).setUv(0f, 0f).setOverlay(overlay).setLight(light).setNormal(pose, 0, 1, 0);
	}
}
