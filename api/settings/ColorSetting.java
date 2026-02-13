package dev.anarchy.waifuhax.api.settings;

import dev.anarchy.waifuhax.api.AbstractSetting;
import dev.anarchy.waifuhax.api.systems.modules.AbstractModule;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.awt.*;

public class ColorSetting extends AbstractSetting<Color> {

    public ColorSetting(String name, String description, Color defaultValue, String... aliases) {
        super(name, description, defaultValue, null);
    }

    private float insureBound(float input) {
        if (input < 0) {input = 0;}
        if (input > 1) {input = 1;}
        return (input);
    }

    @Override
    public @Nullable Color fromString(String string) {
        return null;
    }

    @Override
    public void load(JSONObject savedSettings) {

    }

    @Override
    public void render(AbstractModule module) {

    }
}
