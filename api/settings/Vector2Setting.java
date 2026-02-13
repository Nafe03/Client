package dev.anarchy.waifuhax.api.settings;

import dev.anarchy.waifuhax.api.AbstractSetting;
import dev.anarchy.waifuhax.api.WHLogger;
import dev.anarchy.waifuhax.api.systems.modules.AbstractModule;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.json.JSONObject;
public class Vector2Setting extends AbstractSetting<Vector2f> {

    public Vector2Setting(String name, String description, Vector2f defaultValue, Runnable onModified, String... aliases) {
        super(name, description, defaultValue, onModified, aliases);
    }

    @Override
    public @Nullable Vector2f fromString(String string) {
        if (string.split(" ").length != 2)
            throw new RuntimeException("Vector2f " + getName() + " is not properly formated ! report this asap");
        return new Vector2f(Float.parseFloat(string.split(" ")[0]), Float.parseFloat(string.split(" ")[1]));
    }

    @Override
    public void load(JSONObject savedSettings) {
        if (savedSettings.has(getName())) {
            JSONObject vec = savedSettings.getJSONObject(getName());
            WHLogger.print("Test loading a vec2f: %s, %s", vec.getFloat("x"), vec.getFloat("y"));
            setValue(new Vector2f(vec.getFloat("x"), vec.getFloat("y")));
        }
    }

    @Override
    public void save(JSONObject saveData) {
        saveData.put(getName(), new JSONObject().put("x", getValue().x).put("y", getValue().y));
    }

    @Override
    public void render(AbstractModule module) {

    }
}
