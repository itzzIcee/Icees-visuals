package com.icees.visuals.mixin;

import com.icees.visuals.config.IceesConfig;
import com.icees.visuals.cosmetics.FirstPersonKnifeRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Replace the vanilla first-person sword with Icee's 3D knife, keeping the
 * same arm / swing transforms so attacks animate with the hand.
 */
@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
	@Invoker("renderPlayerArm")
	protected abstract void icees$renderPlayerArm(PoseStack poseStack, SubmitNodeCollector collector, int light, float equipProgress, float swingProgress, HumanoidArm arm);

	@Invoker("applyItemArmTransform")
	protected abstract void icees$applyItemArmTransform(PoseStack poseStack, HumanoidArm arm, float equipProgress);

	@Invoker("swingArm")
	protected abstract void icees$swingArm(float swingProgress, PoseStack poseStack, int side, HumanoidArm arm);

	@Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
	private void icees$renderKnifeInHand(
		AbstractClientPlayer player,
		float partialTick,
		float pitch,
		InteractionHand hand,
		float swingProgress,
		ItemStack stack,
		float equipProgress,
		PoseStack poseStack,
		SubmitNodeCollector collector,
		int light,
		CallbackInfo ci
	) {
		if (!IceesConfig.knife) return;
		if (hand != InteractionHand.MAIN_HAND) return;
		if (stack.isEmpty() || !stack.is(ItemTags.SWORDS)) return;
		if (player.isScoping()) return;

		HumanoidArm arm = player.getMainArm();
		int side = arm == HumanoidArm.RIGHT ? 1 : -1;

		poseStack.pushPose();

		// Arm swings with attack (same as empty-hand path)
		if (!player.isInvisible()) {
			poseStack.pushPose();
			this.icees$renderPlayerArm(poseStack, collector, light, equipProgress, swingProgress, arm);
			poseStack.popPose();
		}

		// Knife sits in the item slot and inherits WHACK swing
		this.icees$applyItemArmTransform(poseStack, arm, equipProgress);
		this.icees$swingArm(swingProgress, poseStack, side, arm);
		FirstPersonKnifeRenderer.render(poseStack, light, swingProgress);

		poseStack.popPose();
		ci.cancel();
	}
}
