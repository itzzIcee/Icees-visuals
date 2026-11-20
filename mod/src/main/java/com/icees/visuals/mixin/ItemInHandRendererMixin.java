package com.icees.visuals.mixin;

import com.icees.visuals.config.IceesConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Hide the vanilla first-person sword when Icee's knife cosmetic is active,
 * so the custom knife sits in the hand instead (arm still renders).
 */
@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {
	@ModifyVariable(method = "renderArmWithItem", at = @At("HEAD"), argsOnly = true, ordinal = 0)
	private ItemStack icees$replaceSwordWithEmpty(
		ItemStack stack,
		AbstractClientPlayer player,
		float partialTick,
		float pitch,
		InteractionHand hand,
		float swingProgress,
		ItemStack original,
		float equipProgress,
		PoseStack poseStack,
		SubmitNodeCollector collector,
		int light
	) {
		if (!IceesConfig.knife) return stack;
		if (hand != InteractionHand.MAIN_HAND) return stack;
		if (!stack.isEmpty() && stack.is(ItemTags.SWORDS)) {
			return ItemStack.EMPTY;
		}
		return stack;
	}
}
