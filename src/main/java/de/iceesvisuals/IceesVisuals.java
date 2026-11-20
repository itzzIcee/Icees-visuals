package de.iceesvisuals;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.gui.screen.TitleScreen;
import org.lwjgl.glfw.GLFW;

public class IceesVisuals implements ClientModInitializer {
    private static boolean wasDown = false;

    @Override
    public void onInitializeClient() {
        ThemeManager.load();

        // Right Shift opens the menu in-game or on the title screen
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.getWindow() == null) return;
            boolean down = GLFW.glfwGetKey(client.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
            if (down && !wasDown && (client.currentScreen == null || client.currentScreen instanceof TitleScreen)) {
                client.setScreen(new ThemeScreen(client.currentScreen));
            }
            wasDown = down;
        });
    }
}
