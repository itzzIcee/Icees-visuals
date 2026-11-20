package com.icees.visuals.sky;

import com.icees.visuals.config.IceesConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

/**
 * Procedural dome sky — sakura-tinted living skies drawn every frame.
 */
public final class SakuraSkyRenderer {
	private SakuraSkyRenderer() {
	}

	public static void render(MatrixStack matrices, float tickDelta) {
		if (!IceesConfig.customSky) {
			return;
		}

		SkyStyle style = IceesConfig.skyStyle;
		MinecraftClient client = MinecraftClient.getInstance();
		float time = (client.world != null ? client.world.getTime() : 0) + tickDelta;

		matrices.push();
		RenderSystem.depthMask(false);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShader(GameRenderer::getPositionColorProgram);

		// Slow spin so the sky feels alive
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(time * 0.15f));
		Matrix4f matrix = matrices.peek().getPositionMatrix();

		drawDome(matrix, style, 1.0f, 64f);
		drawBands(matrix, style, time);
		if (style == SkyStyle.GALAXY || style == SkyStyle.SAKURA) {
			drawPlanet(matrix, style, time);
		}
		if (IceesConfig.petals || style == SkyStyle.SAKURA) {
			drawPetals(matrix, time);
		}
		drawStars(matrix, style, time);

		RenderSystem.depthMask(true);
		RenderSystem.disableBlend();
		matrices.pop();
	}

	private static void drawDome(Matrix4f matrix, SkyStyle style, float alpha, float radius) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);

		float topR = style.r * 0.25f;
		float topG = style.g * 0.18f;
		float topB = style.b * 0.30f;
		buffer.vertex(matrix, 0, radius, 0).color(topR, topG, topB, alpha);

		int segments = 48;
		for (int i = 0; i <= segments; i++) {
			float a = (float) (i * Math.PI * 2 / segments);
			float x = (float) Math.cos(a) * radius;
			float z = (float) Math.sin(a) * radius;
			float y = -radius * 0.15f;
			buffer.vertex(matrix, x, y, z).color(style.r, style.g, style.b, alpha * 0.85f);
		}
		BufferRenderer.drawWithGlobalProgram(buffer.end());
	}

	private static void drawBands(Matrix4f matrix, SkyStyle style, float time) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
		int rings = 10;
		for (int i = 0; i <= rings; i++) {
			float t = i / (float) rings;
			float y = 40f - t * 70f + (float) Math.sin(time * 0.02 + t * 4) * 3f;
			float radius = 20f + t * 55f;
			float pulse = 0.35f + 0.25f * (float) Math.sin(time * 0.04 + t * 3);
			float r = lerp(style.r * 0.4f, style.r, t);
			float g = lerp(style.g * 0.3f, style.g, t);
			float b = lerp(style.b * 0.5f, Math.min(1f, style.b + 0.1f), t);
			for (int s = 0; s <= 32; s++) {
				float a = (float) (s * Math.PI * 2 / 32);
				float x = (float) Math.cos(a + t) * radius;
				float z = (float) Math.sin(a + t) * radius;
				buffer.vertex(matrix, x, y, z).color(r, g, b, pulse * (1f - t * 0.5f));
				buffer.vertex(matrix, x * 0.92f, y - 4f, z * 0.92f).color(r, g, b, 0f);
			}
		}
		BufferRenderer.drawWithGlobalProgram(buffer.end());
	}

	private static void drawPlanet(Matrix4f matrix, SkyStyle style, float time) {
		float cx = 28f;
		float cy = 18f;
		float cz = -10f;
		float radius = 10f;
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
		buffer.vertex(matrix, cx, cy, cz).color(0.15f, 0.08f, 0.14f, 0.95f);
		for (int i = 0; i <= 28; i++) {
			float a = (float) (i * Math.PI * 2 / 28);
			float x = cx + (float) Math.cos(a) * radius;
			float y = cy + (float) Math.sin(a) * radius * 0.85f;
			buffer.vertex(matrix, x, y, cz).color(style.r * 0.5f, style.g * 0.3f, style.b * 0.55f, 0.9f);
		}
		BufferRenderer.drawWithGlobalProgram(buffer.end());

		// rings
		BufferBuilder ring = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
		for (int i = 0; i <= 48; i++) {
			float a = (float) (i * Math.PI * 2 / 48) + time * 0.01f;
			float x = cx + (float) Math.cos(a) * radius * 1.7f;
			float y = cy + (float) Math.sin(a) * radius * 0.35f;
			ring.vertex(matrix, x, y, cz).color(1f, 0.75f, 0.88f, 0.65f);
		}
		BufferRenderer.drawWithGlobalProgram(ring.end());
	}

	private static void drawPetals(Matrix4f matrix, float time) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
		for (int i = 0; i < 36; i++) {
			float seed = i * 17.13f;
			float x = ((seed * 13) % 90) - 45f;
			float z = ((seed * 29) % 90) - 45f;
			float y = 35f - ((time * 0.35f + seed) % 70f);
			float s = 0.6f + (i % 5) * 0.15f;
			float a = 0.35f + (i % 4) * 0.1f;
			buffer.vertex(matrix, x, y, z).color(1f, 0.72f, 0.82f, a);
			buffer.vertex(matrix, x + s, y - s * 0.4f, z).color(1f, 0.55f, 0.72f, a * 0.5f);
			buffer.vertex(matrix, x - s * 0.3f, y - s, z + s * 0.2f).color(1f, 0.85f, 0.9f, 0f);
		}
		BufferRenderer.drawWithGlobalProgram(buffer.end());
	}

	private static void drawStars(Matrix4f matrix, SkyStyle style, float time) {
		if (style == SkyStyle.RAIN) {
			return;
		}
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
		int count = style == SkyStyle.GALAXY ? 80 : 45;
		for (int i = 0; i < count; i++) {
			float sx = hash(i, 1) * 100f - 50f;
			float sy = hash(i, 2) * 55f + 5f;
			float sz = hash(i, 3) * 100f - 50f;
			float twinkle = 0.45f + 0.55f * (float) Math.sin(time * 0.1f + i);
			float s = 0.25f + hash(i, 4) * 0.35f;
			buffer.vertex(matrix, sx, sy, sz).color(1f, 0.92f, 0.96f, twinkle);
			buffer.vertex(matrix, sx + s, sy, sz).color(1f, 0.92f, 0.96f, 0f);
			buffer.vertex(matrix, sx, sy + s, sz).color(1f, 0.92f, 0.96f, 0f);
		}
		BufferRenderer.drawWithGlobalProgram(buffer.end());
	}

	private static float hash(int i, int salt) {
		float x = (float) Math.sin(i * 12.9898 + salt * 78.233) * 43758.5453f;
		return x - (float) Math.floor(x);
	}

	private static float lerp(float a, float b, float t) {
		return a + (b - a) * t;
	}
}
