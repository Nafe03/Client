package dev.anarchy.waifuhax.api.settings;

import dev.anarchy.waifuhax.api.AbstractSetting;
import dev.anarchy.waifuhax.api.gui.components.UIElement;
import dev.anarchy.waifuhax.api.gui.components.impl.UIButton;
import dev.anarchy.waifuhax.api.gui.components.impl.UIOption;
import dev.anarchy.waifuhax.api.systems.modules.AbstractModule;
import net.minecraft.client.gui.DrawContext;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Optional;

public class EnumSetting<T extends Enum<T>> extends AbstractSetting<T> {

    private String[] options;
    private int index = 0;

    public EnumSetting(String name, String description, T defaultValue, String... aliases) {
        super(name, description, defaultValue, null, aliases);
        options = Arrays.stream(defaultValue.getDeclaringClass().getEnumConstants())
                .map(Object::toString)
                .toArray(String[]::new);
    }

    @Nullable
    @Override
    public T fromString(String string) {
        for (int i = 0; i < options.length; i++) {
            if (options[i].equalsIgnoreCase(string)) {
                index = i;
                break;
            }
        }
        setValue(getValue().getDeclaringClass().getEnumConstants()[index]);
        return null;
    }

    @Override
    public void load(JSONObject savedSettings) {
        if (savedSettings.has(getName())) {
            for (int i = 0; i < options.length; i++) {
                if (options[i].equals(savedSettings.getString(getName()))) {
                    index = i;
                    break;
                }
            }
            setValue(getValue().getDeclaringClass().getEnumConstants()[index]);
        }
    }

    @Override
    public void draw(DrawContext ctx, UIOption uiOption) {
        Optional<UIElement> buttonOpt = uiOption.getChildRecursive(getName());
        
        if (!buttonOpt.isPresent() || !(buttonOpt.get() instanceof UIButton)) {
            // Create button - only once!
            uiOption.clear();
            
            UIButton button = new UIButton(getName() + ": " + getValue().toString());
            button.setIdentifier(getName());
            button.setSize(110, 20);
            button.setMargin(2, 4, 2, 4);
            
            button.addMouseEvent(event -> {
                if (event.button == 0) {
                    cycleValue();
                    button.setText(getName() + ": " + getValue().toString());
                    if (onModified != null) {
                        onModified.run();
                    }
                }
            });
            
            uiOption.setVerticalLayout();
            uiOption.setPadding(0);
            uiOption.setSpacing(0);
            uiOption.addChild(button);
        } else {
            // Just update text - super fast!
            UIButton button = (UIButton) buttonOpt.get();
            String expectedText = getName() + ": " + getValue().toString();
            if (!button.getText().equals(expectedText)) {
                button.setText(expectedText);
            }
        }
    }
    
    private void cycleValue() {
        index = (index + 1) % options.length;
        setValue(getValue().getDeclaringClass().getEnumConstants()[index]);
    }

    @Override
    public int size() {
        return 110;
    }

    @Override
    public void render(AbstractModule module) {
        // Not used
    }
    
    @Override
    public String toString() {
        return getValue().toString();
    }
}