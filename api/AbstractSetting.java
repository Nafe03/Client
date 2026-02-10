package dev.anarchy.waifuhax.api;

import dev.anarchy.waifuhax.api.gui.components.impl.UIOption;
import dev.anarchy.waifuhax.api.systems.modules.AbstractModule;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.json.JSONObject;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public abstract class AbstractSetting<T> {

    protected final List<String> aliases = new ArrayList<>();
    public Runnable onModified = null;
    protected Supplier<Boolean> shouldShow = null;

    @Getter
    @Setter
    private String name, description;

    @Getter
    @Setter
    private T value;

    @Getter
    @Setter
    private boolean shouldDraw = true;

    public AbstractSetting(String name, String description, T defaultValue, Runnable onModified, String... aliases) {
        this.name = name;
        this.description = description;
        this.value = defaultValue;
        this.aliases.addAll(List.of(aliases));
        this.aliases.add(getDisplayName());
        this.onModified = onModified;
    }

    public AbstractSetting<T> showIf(Supplier<Boolean> shouldShow) {
        this.shouldShow = shouldShow;
        return this;
    }

    public boolean shouldShow() {
        return shouldShow == null || shouldShow.get();
    }

    public String getDisplayName() {
        return name.split(" ")[0];
    }

    public List<String> getAliases() {
        return new ArrayList<String>(aliases);
    }

    public boolean matchAnyAliases(String optionName) {

        AtomicBoolean found = new AtomicBoolean(false);

        aliases.forEach(alias -> {
            if (alias.equalsIgnoreCase(optionName)) {found.set(true);}
        });

        return found.get();
    }

    @Nullable
    public abstract T fromString(String string);

    public void save(JSONObject saveData) {
        saveData.put(name, value);
    }

    public abstract void load(JSONObject savedSettings);

    public abstract void render(AbstractModule module);

    public int size() {
        return MinecraftClient.getInstance().textRenderer.getWidth(getName() + " : " + getValue());
    }

    public void draw(DrawContext ctx, UIOption uiOption) {
        if (ctx != null)
            ctx.drawText(MinecraftClient.getInstance().textRenderer, Text.of(getName() + " : " + getValue()).asOrderedText(), (int) uiOption.getAbsolutePos().x + 4, (int) uiOption.getAbsolutePos().y + 2, 0xFFFFFFFF, true);
    }
}
