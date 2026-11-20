package com.icees.visuals.mixin;

import com.icees.visuals.config.IceesConfig;
import net.minecraft.client.renderer.CloudRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Belt-and-suspenders cloud tint so sky styles read when looking up. */
@Mixin(CloudRenderer.class)
public class CloudRendererMixin {
	@ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true, ordinal = 0)
	private int icees$forceCloudColor(int color) {
		if (!IceesConfig.customSky) return color;
		return IceesConfig.skyStyle.toCloudArgb();
	}
}
