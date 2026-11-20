package de.iceesvisuals;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.function.IntConsumer;

public class ThemeScreen extends Screen {
    private final Screen parent;
    private boolean shiftWasDown = true; // prevents closing right after opening
    private TextFieldWidget nameField, idField, pathField;
    private String status = "";
    private int panelTop, panelBottom, statusY;
    private int page = 0; // 0 = themes/textures/sky, 1 = weapons

    public ThemeScreen(Screen parent) {
        super(Text.literal("Icee's Visuals"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        ThemeManager.Theme t = ThemeManager.current();
        int w = 260;
        int left = width / 2 - w / 2;
        int y0 = Math.max(8, height / 2 - (page == 0 ? 150 : 100));
        panelTop = y0;

        // Theme selector (both pages)
        addDrawableChild(ButtonWidget.builder(Text.literal("<"), b -> { ThemeManager.cycle(-1); clearAndInit(); })
                .dimensions(left, y0 + 16, 20, 20).build());
        ButtonWidget nameBtn = ButtonWidget.builder(Text.literal("Theme: " + t.name), b -> {})
                .dimensions(left + 22, y0 + 16, w - 44, 20).build();
        nameBtn.active = false;
        addDrawableChild(nameBtn);
        addDrawableChild(ButtonWidget.builder(Text.literal(">"), b -> { ThemeManager.cycle(1); clearAndInit(); })
                .dimensions(left + w - 20, y0 + 16, 20, 20).build());

        int applyY;
        if (page == 0) {
            applyY = y0 + 242;

            // New / delete theme
            nameField = new TextFieldWidget(textRenderer, left, y0 + 40, 130, 20, Text.literal("Name"));
            nameField.setPlaceholder(Text.literal("New theme name"));
            nameField.setMaxLength(24);
            addDrawableChild(nameField);
            addDrawableChild(ButtonWidget.builder(Text.literal("New"), b -> run(() -> {
                ThemeManager.create(nameField.getText());
                clearAndInit();
            })).dimensions(left + 134, y0 + 40, 60, 20).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("Delete"), b -> run(() -> {
                ThemeManager.deleteCurrent();
                clearAndInit();
            })).dimensions(left + 198, y0 + 40, 62, 20).build());

            // Menu accent color
            int[] rgb = {(t.accent >> 16) & 0xFF, (t.accent >> 8) & 0xFF, t.accent & 0xFF};
            String[] names = {"Red", "Green", "Blue"};
            for (int i = 0; i < 3; i++) {
                final int idx = i;
                addDrawableChild(new ColorSlider(left, y0 + 68 + i * 22, w, 20, names[i], rgb[i], v -> {
                    rgb[idx] = v;
                    t.accent = 0xFF000000 | (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
                    ThemeManager.save();
                }));
            }

            // Custom textures
            idField = new TextFieldWidget(textRenderer, left, y0 + 144, w, 20, Text.literal("ID"));
            idField.setPlaceholder(Text.literal("Texture ID, e.g. block/stone"));
            idField.setMaxLength(128);
            addDrawableChild(idField);

            pathField = new TextFieldWidget(textRenderer, left, y0 + 168, w, 20, Text.literal("Path"));
            pathField.setPlaceholder(Text.literal("PNG path, e.g. C:\\Pictures\\stone.png"));
            pathField.setMaxLength(260);
            addDrawableChild(pathField);

            addDrawableChild(ButtonWidget.builder(Text.literal("Add texture"), b -> run(() -> {
                String id = ThemeManager.addTexture(ThemeManager.current(), idField.getText(), pathField.getText());
                status = "Added: " + id;
            })).dimensions(left, y0 + 192, 130, 20).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("Remove texture"), b -> run(() -> {
                status = ThemeManager.removeTexture(ThemeManager.current(), idField.getText())
                        ? "Removed" : "ID is not in this theme";
            })).dimensions(left + 130, y0 + 192, 130, 20).build());

            // Example skies (End sky), click = next sky
            addDrawableChild(ButtonWidget.builder(skyLabel(t), b -> run(() -> {
                int n = ThemeManager.SKY_FILES.length;
                int next = t.sky + 1 >= n ? -1 : t.sky + 1;
                ThemeManager.setSky(ThemeManager.current(), next);
                b.setMessage(skyLabel(t));
                status = next < 0 ? "Sky removed" : "Sky set - now click Apply";
            })).dimensions(left, y0 + 218, w, 20).build());
        } else {
            applyY = y0 + 172;

            addDrawableChild(ButtonWidget.builder(Text.literal("Skin: " + WeaponSkins.SKINS[t.weaponSkin]), b -> {
                t.weaponSkin = (t.weaponSkin + 1) % WeaponSkins.SKINS.length;
                ThemeManager.save();
                b.setMessage(Text.literal("Skin: " + WeaponSkins.SKINS[t.weaponSkin]));
            }).dimensions(left, y0 + 44, w, 20).build());

            addDrawableChild(ButtonWidget.builder(Text.literal("Replaces: " + WeaponSkins.BASE_ITEMS[t.weaponItem]), b -> {
                t.weaponItem = (t.weaponItem + 1) % WeaponSkins.BASE_ITEMS.length;
                ThemeManager.save();
                b.setMessage(Text.literal("Replaces: " + WeaponSkins.BASE_ITEMS[t.weaponItem]));
            }).dimensions(left, y0 + 68, w, 20).build());

            addDrawableChild(ButtonWidget.builder(Text.literal("Colors: " + WeaponSkins.PALETTE_NAMES[t.palette]), b -> {
                t.palette = (t.palette + 1) % WeaponSkins.PALETTE_NAMES.length;
                ThemeManager.save();
                b.setMessage(Text.literal("Colors: " + WeaponSkins.PALETTE_NAMES[t.palette]));
            }).dimensions(left, y0 + 92, w, 20).build());

            addDrawableChild(ButtonWidget.builder(Text.literal("Spin speed: " + WeaponSkins.SPEED_NAMES[t.speed]), b -> {
                t.speed = (t.speed + 1) % WeaponSkins.SPEED_NAMES.length;
                ThemeManager.save();
                b.setMessage(Text.literal("Spin speed: " + WeaponSkins.SPEED_NAMES[t.speed]));
            }).dimensions(left, y0 + 116, w, 20).build());
        }

        statusY = applyY + 26;
        int navY = statusY + 20;
        panelBottom = navY + 26;

        addDrawableChild(ButtonWidget.builder(Text.literal("Apply"), b -> run(() -> {
            ThemeManager.apply(client, ThemeManager.current());
            status = "Applied - reloading resources...";
        })).dimensions(left, applyY, 130, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Disable pack"), b -> {
            ThemeManager.disablePack(client);
            status = "Pack disabled";
        }).dimensions(left + 130, applyY, 130, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal(page == 0 ? "Weapons >" : "< Themes"), b -> {
            page = 1 - page;
            clearAndInit();
        }).dimensions(left, navY, 130, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close())
                .dimensions(left + 130, navY, 130, 20).build());
    }

    private Text skyLabel(ThemeManager.Theme t) {
        return Text.literal("Sky: " + (t.sky < 0 ? "None (click to cycle)" : ThemeManager.SKY_NAMES[t.sky]));
    }

    private interface Action { void run() throws Exception; }

    private void run(Action a) {
        try {
            a.run();
        } catch (Exception e) {
            status = "Error: " + e.getMessage();
        }
    }

    @Override
    public void tick() {
        boolean down = GLFW.glfwGetKey(client.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
        if (down && !shiftWasDown) close();
        shiftWasDown = down;
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    // Custom background in theme colors (instead of the blur)
    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ThemeManager.Theme t = ThemeManager.current();
        ctx.fill(0, 0, width, height, 0x88000000);
        int left = width / 2 - 140, right = width / 2 + 140;
        ctx.fill(left, panelTop - 6, right, panelBottom, t.background);
        ctx.fill(left, panelTop - 6, right, panelTop - 4, t.accent);
        ctx.fill(left, panelBottom - 2, right, panelBottom, t.accent);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        ThemeManager.Theme t = ThemeManager.current();
        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("Icee's Visuals"), width / 2, panelTop + 3, t.accent);
        if (page == 0) {
            ctx.drawTextWithShadow(textRenderer, Text.literal("Textures in theme: " + t.textures.size()),
                    width / 2 - 130, panelTop + 132, 0xFFAAAAAA);
        } else {
            ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("Skins are animated textures made by the mod."),
                    width / 2, panelTop + 144, 0xFFAAAAAA);
            ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("Click Apply to rebuild the resource pack."),
                    width / 2, panelTop + 156, 0xFFAAAAAA);
        }
        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal(status), width / 2, statusY, 0xFFFFFFFF);
    }

    private static class ColorSlider extends SliderWidget {
        private final String label;
        private final IntConsumer onChange;

        ColorSlider(int x, int y, int w, int h, String label, int init, IntConsumer onChange) {
            super(x, y, w, h, Text.empty(), init / 255.0);
            this.label = label;
            this.onChange = onChange;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Text.literal(label + ": " + (int) Math.round(value * 255)));
        }

        @Override
        protected void applyValue() {
            onChange.accept((int) Math.round(value * 255));
        }
    }
}
