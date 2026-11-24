package com.icees.visuals.mixin;

import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PictureInPictureRenderer.class)
public interface PipBufferAccessor {
	@Accessor("bufferSource")
	MultiBufferSource.BufferSource icees$getBufferSource();
}
