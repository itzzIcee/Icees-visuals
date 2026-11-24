package com.icees.visuals.cpvp;

import com.icees.visuals.config.IceesConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

/**
 * Lightweight motion blur — directional smear based on camera turn / move speed.
 * (No post-chain required; works as a HUD overlay.)
 */
public final class MotionBlurRenderer {
	private static float lastYaw, lastPitch;
	private static boolean init;
	private static float smear;

	private MotionBlurRenderer() {}

	public static void render(GuiGraphics graphics) {
		if (!IceesConfig.motionBlur || IceesConfig.motionBlurStrength <= 0) return;
		Minecraft client = Minecraft.getInstance();
		if (client.player == null || client.level == null) return;
		if (client.screen != null) return;
		if (!client.options.getCameraType().isFirstPerson()) return;

		float yaw = client.player.getYRot();
		float pitch = client.player.getXRot();
		if (!init) {
			lastYaw = yaw;
			lastPitch = pitch;
			init = true;
			return;
		}

		float dyaw = Mth.wrapDegrees(yaw - lastYaw);
		float dpitch = pitch - lastPitch;
		lastYaw = yaw;
		lastPitch = pitch;

		float speed = (float) client.player.getDeltaMovement().horizontalDistance() * 12f;
		float turn = Math.abs(dyaw) * 0.35f + Math.abs(dpitch) * 0.45f;
		float target = Mth.clamp((speed + turn) * (IceesConfig.motionBlurStrength * 0.18f), 0f, 0.55f);
		smear = Mth.lerp(0.35f, smear, target);
		if (smear < 0.02f) return;

		int w = client.getWindow().getGuiScaledWidth();
		int h = client.getWindow().getGuiScaledHeight();
		int steps = 3 + IceesConfig.motionBlurStrength;
		float dirX = Mth.clamp(dyaw * 1.8f, -18f, 18f);
		float dirY = Mth.clamp(dpitch * 1.8f, -14f, 14f);

		for (int i = 1; i <= steps; i++) {
			float t = i / (float) steps;
			float a = smear * (1f - t) * 0.22f;
			int color = ARGB.colorFromFloat(a, 0.92f, 0.94f, 1f);
			int ox = Math.round(dirX * t);
			int oy = Math.round(dirY * t);
			// edge trails (not a full opaque wash)
			graphics.fill(ox, oy, ox + 6, h + oy, color);
			graphics.fill(w + ox - 6, oy, w + ox, h + oy, color);
			graphics.fill(ox, oy, w + ox, oy + 4, color);
			graphics.fill(ox, h + oy - 4, w + ox, h + oy, color);
		}
	}
}
