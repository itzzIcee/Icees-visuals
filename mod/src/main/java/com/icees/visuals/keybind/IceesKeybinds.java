package com.icees.visuals.keybind;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class IceesKeybinds {
	public static KeyBinding OPEN_ORBIT;

	private IceesKeybinds() {
	}

	public static void register() {
		OPEN_ORBIT = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"key.icees-visuals.orbit",
			InputUtil.Type.KEYSYM,
			GLFW.GLFW_KEY_RIGHT_SHIFT,
			"key.categories.icees-visuals"
		));
	}
}
