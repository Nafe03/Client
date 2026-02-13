package dev.anarchy.waifuhax.api.settings;

import dev.anarchy.waifuhax.api.gui.components.impl.UIOption;
import dev.anarchy.waifuhax.api.systems.modules.AbstractModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class KeybindSetting extends IntegerSetting {

    public static KeybindSetting currentBind = null;

    public boolean shouldSave = false;

    public KeybindSetting(String name, String description, Integer defaultValue, int min, int max, String... aliases) {
        super(name, description, defaultValue, min, max, aliases);
    }

    @Nullable
    @Override
    public Integer fromString(String string) {
        Integer v = super.fromString(string);
        if (v != null) return v;
        if (string.isEmpty()) return null;
        return (int) string.charAt(0);
    }

    @Override
    public int size() {
        String str;
        if (currentBind == this)
            str = " : (press a key to bind)";
        else
            str = getValue() < 0 ? "NONE" : InputUtil.fromKeyCode(getValue(), -1).getLocalizedText().getString();
        return MinecraftClient.getInstance().textRenderer.getWidth(getName() + " : " + str);
    }

    @Override
    public void draw(DrawContext ctx, UIOption uiOption) {
        if (ctx == null) {
            uiOption.addMouseEvent(event -> {
                if (event.button == 0 && currentBind == null && MinecraftClient.getInstance().currentScreen != null)
                    currentBind = this;
            });
            return;
        }
        if (currentBind == this)
        {
            ctx.drawText(MinecraftClient.getInstance().textRenderer,
                Text.of(getName() + " : (press a key to bind)").asOrderedText(),
                (int) uiOption.getAbsolutePos().x + 4,
                (int) uiOption.getAbsolutePos().y + 2,
                0xFFFFFFFF,
                true);
            if (shouldSave) {
                currentBind = null;
                shouldSave = false;
            }
        }
        else {
            String name = getValue() < 0 ? "NONE" : InputUtil.fromKeyCode(getValue(), -1).getLocalizedText().getString();
            ctx.drawText(MinecraftClient.getInstance().textRenderer,
                    Text.of(getName() + " : " + name).asOrderedText(),
                    (int) uiOption.getAbsolutePos().x + 4,
                    (int) uiOption.getAbsolutePos().y + 2,
                    0xFFFFFFFF,
                    true);
        }
    }

    @Override
    public void render(AbstractModule module) {
        if (!isShouldDraw()) return;
        if (currentBind == this) {
            if (shouldSave) {
                currentBind = null;
                shouldSave = false;
                if (module != null) module.save();
            }
        }
        else {
            String name = getValue() < 0 ? "NONE" : InputUtil.fromKeyCode(getValue(), -1).getLocalizedText().getString();

//            if (ImGui.button(getName() + ": " + name + " ##" + module.getName())) {
            if (currentBind != null && currentBind != this) {
                currentBind.shouldSave = true;
            }
            else {
                currentBind = this;
            }
//            }
        }
    }

}
