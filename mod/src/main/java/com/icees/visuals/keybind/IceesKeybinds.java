package com.icees.visuals.keybind;

import com.icees.visuals.config.IceesConfig;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class IceesKeybinds {
	public static KeyMapping OPEN_ORBIT;

	private IceesKeybinds() {}

	public static void register() {
		OPEN_ORBIT = KeyBindingHelper.registerKeyBinding(new KeyMapping(
			"key.icees-visuals.orbit",
			InputConstants.Type.KEYSYM,
			IceesConfig.orbitKeyCode,
			KeyMapping.Category.MISC
		));
	}

	public static void rebind(int glfwKeyCode) {
		IceesConfig.orbitKeyCode = glfwKeyCode;
		OPEN_ORBIT.setKey(InputConstants.Type.KEYSYM.getOrCreate(glfwKeyCode));
		KeyMapping.resetMapping();
		IceesConfig.save();
	}

	public static void resetDefault() {
		rebind(GLFW.GLFW_KEY_RIGHT_SHIFT);
	}
}
