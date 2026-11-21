package com.icees.visuals.mixin;

import com.icees.visuals.cosmetics.CosmeticRenderer;
import com.icees.visuals.gui.OrbitScreen;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.GuiEntityRenderer;
import net.minecraft.client.gui.render.state.pip.GuiEntityRenderState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Draw Icee cosmetics into the Orbit skin preview (same PIP texture as the entity). */
@Mixin(GuiEntityRenderer.class)
public abstract class GuiEntityRendererMixin {
	@Shadow
	protected MultiBufferSource.BufferSource bufferSource;

	@Inject(method = "renderToTexture(Lnet/minecraft/client/gui/render/state/pip/GuiEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;)V", at = @At("TAIL"))
	private void icees$previewCosmetics(GuiEntityRenderState state, PoseStack poseStack, CallbackInfo ci) {
		Minecraft client = Minecraft.getInstance();
		if (!(client.screen instanceof OrbitScreen)) return;
		if (!(state.renderState() instanceof LivingEntityRenderState living)) return;
		CosmeticRenderer.renderInGuiPreview(poseStack, this.bufferSource, living.bodyRot);
	}
}
