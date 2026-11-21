package com.icees.visuals.mixin;

import com.icees.visuals.cosmetics.CosmeticRenderer;
import com.icees.visuals.gui.CosmeticPreviewGate;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.render.pip.GuiEntityRenderer;
import net.minecraft.client.gui.render.state.pip.GuiEntityRenderState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Draws real 3D cosmetics into the Orbit skin preview PIP texture.
 * Soft require + gate so this never blocks launch (owo / other GUIs).
 */
@Mixin(GuiEntityRenderer.class)
public abstract class GuiEntityRendererMixin {
	@Inject(
		method = "renderToTexture(Lnet/minecraft/client/gui/render/state/pip/GuiEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;)V",
		at = @At("TAIL"),
		require = 0
	)
	private void icees$previewCosmetics(GuiEntityRenderState state, PoseStack poseStack, CallbackInfo ci) {
		if (!CosmeticPreviewGate.active) return;
		try {
			if (!(state.renderState() instanceof LivingEntityRenderState living)) return;
			MultiBufferSource.BufferSource buffers = ((PipBufferAccessor) (Object) this).icees$getBufferSource();
			if (buffers == null) return;
			CosmeticRenderer.renderInGuiPreview(poseStack, buffers, living);
		} catch (Throwable ignored) {
			// Never break inventory / owo entity previews
		}
	}
}
