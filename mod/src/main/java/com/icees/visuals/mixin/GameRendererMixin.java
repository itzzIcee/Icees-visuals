package com.icees.visuals.mixin;

import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Reserved for future sakura post-FX (haze / blossom tint).
 * Kept so the mixin config stays stable across builds.
 */
@Mixin(GameRenderer.class)
public class GameRendererMixin {
}
