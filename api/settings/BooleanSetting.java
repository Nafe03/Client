package dev.anarchy.waifuhax.api.settings;

import dev.anarchy.waifuhax.api.AbstractSetting;
import dev.anarchy.waifuhax.api.gui.components.UIElement;
import dev.anarchy.waifuhax.api.gui.components.impl.UICheckBox;
import dev.anarchy.waifuhax.api.gui.components.impl.UIOption;
import dev.anarchy.waifuhax.api.gui.components.impl.UIText;
import dev.anarchy.waifuhax.api.systems.modules.AbstractModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.Optional;

public class BooleanSetting extends AbstractSetting<Boolean> {

    public BooleanSetting(String name, String description, boolean defaultValue, String... aliases) {
        super(name, description, defaultValue, null, aliases);
    }

    public BooleanSetting(String name, String description, boolean defaultValue, Runnable onModified, String... aliases) {
        super(name, description, defaultValue, onModified, aliases);
    }

    @Nullable
    @Override
    public Boolean fromString(String string) {
        return string.equalsIgnoreCase("true") || string.equalsIgnoreCase("1");
    }

    @Override
    public void load(JSONObject savedSettings) {
        if (savedSettings.has(getName())) {
            setValue(savedSettings.getBoolean(getName()));
        }
    }

    @Override
    public void draw(DrawContext ctx, UIOption uiOption) {
        // Check if UI exists
        Optional<UIElement> labelOpt = uiOption.getChildRecursive(getName() + "_label");
        Optional<UIElement> boxOpt = uiOption.getChildRecursive(getName() + "_checkbox");
        
        if (!labelOpt.isPresent() || !boxOpt.isPresent()) {
            // Create UI - only once!
            uiOption.clear();
            
            // Label
            UIText label = new UIText();
            label.setText(getName());
            label.setIdentifier(getName() + "_label");
            label.setMargin(0, 0, 0, 4);
            label.setColor(0xFFC0C0D0);
            
            // Checkbox
            UICheckBox checkbox = new UICheckBox();
            checkbox.setIdentifier(getName() + "_checkbox");
            checkbox.setActive(getValue());
            checkbox.setMargin(0, 4, 0, 0);
            checkbox.addMouseEvent(event -> {
                if (event.button == 0) {
                    setValue(!getValue());
                    checkbox.setActive(getValue());
                    if (onModified != null) {
                        onModified.run();
                    }
                }
            });
            
            // Horizontal layout
            uiOption.setHorizontalLayout();
            uiOption.setPadding(0);
            uiOption.setSpacing(6);
            uiOption.addChild(label);
            uiOption.addChild(checkbox);
        } else {
            // Just update state - super fast!
            UICheckBox checkbox = boxOpt.get().as(UICheckBox.class);
            if (checkbox.state != getValue()) {
                checkbox.setActive(getValue());
            }
        }
    }

    @Override
    public int size() {
        return 110;
    }

    @Override
    public void render(AbstractModule module) {
        // Not used
    }
}