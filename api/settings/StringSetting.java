package dev.anarchy.waifuhax.api.settings;

import dev.anarchy.waifuhax.api.AbstractSetting;
import dev.anarchy.waifuhax.api.systems.modules.AbstractModule;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class StringSetting extends AbstractSetting<String> {

    public StringSetting(String name, String description, String defaultValue, String... aliases) {
        super(name, description, defaultValue, null, aliases);
    }

    @Nullable
    @Override
    public String fromString(String string) {
        return string;
    }

    @Override
    public void load(JSONObject savedSettings) {
        if (savedSettings.has(getName())) {
            setValue(savedSettings.getString(getName()));
        }
    }

    @Override
    public void render(AbstractModule module) {
    }
}
