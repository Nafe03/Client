package dev.anarchy.waifuhax.api.settings;

import dev.anarchy.waifuhax.api.AbstractSetting;
import dev.anarchy.waifuhax.api.gui.components.UIElement;
import dev.anarchy.waifuhax.api.gui.components.impl.UIOption;
import dev.anarchy.waifuhax.api.gui.components.impl.UISlider;
import dev.anarchy.waifuhax.api.systems.modules.AbstractModule;
import lombok.AccessLevel;
import lombok.Getter;
import net.minecraft.client.gui.DrawContext;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.Optional;

public class IntegerSetting extends AbstractSetting<Integer> {

    @Getter(AccessLevel.PUBLIC)
    private final int min, max;
    
    private int lastKnownValue = 0;

    public IntegerSetting(String name, String description, Integer defaultValue, int min, int max, String... aliases) {
        super(name, description, defaultValue, null, aliases);
        this.max = max;
        this.min = min;
        this.lastKnownValue = defaultValue;
    }

    @Nullable
    @Override
    public Integer fromString(String string) {
        try {
            return Integer.parseInt(string);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    @Override
    public void load(JSONObject savedSettings) {
        if (savedSettings.has(getName())) {
            int loadedValue = savedSettings.getInt(getName());
            setValue(loadedValue);
            lastKnownValue = loadedValue;
        }
    }

    @Override
    public void draw(DrawContext ctx, UIOption uiOption) {
        Optional<UIElement> sliderOpt = uiOption.getChildRecursive(getName());
        
        if (!sliderOpt.isPresent() || !(sliderOpt.get() instanceof UISlider)) {
            // Create slider - only once!
            uiOption.clear();
            
            UISlider slider = new UISlider(getName(), min, max, getValue().floatValue());
            slider.setIdentifier(getName());
            slider.setSize(110, 24);
            slider.setMargin(2, 4, 2, 4);
            uiOption.addChild(slider);
            
            uiOption.setVerticalLayout();
            uiOption.setPadding(0);
            uiOption.setSpacing(0);
            
            lastKnownValue = getValue();
        } else {
            // Sync values - optimized!
            UISlider slider = (UISlider) sliderOpt.get();
            int sliderValueInt = Math.round(slider.getValue());
            int settingValue = getValue();
            
            // Only update if changed
            if (slider.isDragging()) {
                // Slider is being dragged - update setting
                if (sliderValueInt != lastKnownValue) {
                    setValue(sliderValueInt);
                    lastKnownValue = sliderValueInt;
                    
                    // Snap slider to integer
                    slider.setValue(sliderValueInt);
                    
                    if (onModified != null) {
                        onModified.run();
                    }
                }
            } else if (settingValue != sliderValueInt) {
                // Setting changed externally - update slider
                slider.setValue(settingValue);
                lastKnownValue = settingValue;
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

    @Override
    public String toString() {
        return getValue().toString();
    }
}