package dev.anarchy.waifuhax.api.systems.modules;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import dev.anarchy.waifuhax.api.AbstractSetting;
import dev.anarchy.waifuhax.api.WHLogger;
import dev.anarchy.waifuhax.api.gui.WHWindow;
import dev.anarchy.waifuhax.api.gui.components.UIElement;
import dev.anarchy.waifuhax.api.gui.components.impl.UICheckBox;
import dev.anarchy.waifuhax.api.gui.components.impl.UIEmpty;
import dev.anarchy.waifuhax.api.gui.components.impl.UIOption;
import dev.anarchy.waifuhax.api.settings.*;
import dev.anarchy.waifuhax.api.settings.annotations.CategorySetting;
import dev.anarchy.waifuhax.api.systems.commands.arguments.AnyArgumentType;
import dev.anarchy.waifuhax.api.systems.commands.arguments.SettingArgumentType;
import dev.anarchy.waifuhax.api.systems.modules.annotations.AutoDisable;
import dev.anarchy.waifuhax.api.systems.modules.annotations.AutoEnable;
import dev.anarchy.waifuhax.api.systems.modules.annotations.ManualInstanciating;
import dev.anarchy.waifuhax.api.util.PathUtils;
import dev.anarchy.waifuhax.client.WaifuHax;
import dev.anarchy.waifuhax.client.systems.modules.misc.GlobalOptions;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.CommandSource;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;


@ManualInstanciating
public abstract class AbstractModule {

    protected static final MinecraftClient mc = MinecraftClient.getInstance();

    private final WHWindow settings = new WHWindow("settings", new Vector2f(25, 25));

    @Setter(AccessLevel.PUBLIC)
    protected static ClientPlayerEntity player;
    public final KeybindSetting keycode = new KeybindSetting("Module Key", "Toggle button.", -1, -1, Integer.MAX_VALUE);

    @Getter
    private Category category;

    public AbstractModule() {
        WaifuHax.EVENT_BUS.unsubscribe(settings);
        category = Category.create(Arrays.stream(getClass().getPackageName().split("\\.")).toList().getLast());
        UIElement fore = settings.getRoot().getChildRecursive("WindowFrameForeground").get();
        fore.addChild(new UIEmpty().setIdentifier("ModuleContainer").setPos(new Vector3f(0, 0, 0)));
        settings.getRoot().getChildRecursive("IsPinned").get().addMouseEvent((event) -> {
            if (event.button == 0) {
                ((UICheckBox)settings.getRoot().getChildRecursive("IsPinned").get()).setActive(false);
                isDrawingSettings = false;
                WaifuHax.EVENT_BUS.unsubscribe(settings);
            }
        });
    }

    public int minWidth() {
        int header = (MinecraftClient.getInstance().textRenderer.getWidth(settings.getName()) + 4 + (UICheckBox.SIZE * 2) + ((UICheckBox.SIZE * 3) / 4));
        for (AbstractSetting setting : getSettings()) {
            header = Math.max(header, 4 + setting.size());
        }
        return header;
    }

    @Getter(AccessLevel.PUBLIC)
    private final String name = this.getClass().getSimpleName();
    
    // Manual getter for name (Lombok not working)
    public String getName() { return name; }
    
    private String path;// = PathUtils.join("WaifuHax", category.name(), getName() + ".json");

    @CategorySetting
    public final BooleanSetting isEnabled = new BooleanSetting("Enabled", "", false, this::onToggle, "e", "isenabled");

    private static boolean isTrue(String input) {
        return input.equalsIgnoreCase("true") || input.equalsIgnoreCase("1");
    }

    private boolean isDrawingSettings = false;

    public void toggleSettingsDraw() {
        if (!isDrawingSettings) {
            UIElement container = settings.getRoot().getChildRecursive("ModuleContainer").orElse(null);
            if (container == null)
                return;
            container.clear();
            AtomicInteger y = new AtomicInteger(5);
            getSettings().forEach(setting -> {
                container.addChild(new UIOption().setSetting(setting).setPos(0, y.get()).setIdentifier("Setting-" + setting.getName()));
                y.addAndGet(18);
            });
            settings.setTitle(name + " settings");
            settings.setWindowSize(minWidth(), y.get() + 5);
            container.setSize(minWidth(), y.get() + 5);
            container.executeOnChilds((element) -> {
                if (element instanceof UIOption option) {
                    option.setSize(minWidth(), 12);
                    option.init();
                }
                if (element instanceof UICheckBox box) {
                    box.setPos(minWidth() - (UICheckBox.SIZE / 4) - UICheckBox.SIZE - 5, box.getPos().y + 2);
                }
            });
            WaifuHax.EVENT_BUS.subscribe(settings);
        }
        else
            WaifuHax.EVENT_BUS.unsubscribe(settings);
        isDrawingSettings = !isDrawingSettings;
    }

    // yes, this is pasted from Meteor
    // Helper methods to painlessly infer the CommandSource generic type argument
    protected static <T> RequiredArgumentBuilder<CommandSource, T> argument(final String name, final ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }

    protected static LiteralArgumentBuilder<CommandSource> literal(final String name) {
        return LiteralArgumentBuilder.literal(name);

    }

    public List<AbstractSetting> getSettings() {
        List<AbstractSetting> tmp = new ArrayList<>();

        Arrays.stream(this.getClass().getFields()).forEach(field -> {
            if (AbstractSetting.class.isAssignableFrom(field.getType())) {
                try {
                    field.setAccessible(true); // Make the field accessible, especially if it is private
                    tmp.add((AbstractSetting) field.get(this));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        return tmp;
    }

    // allow accessing categories
    public LinkedHashMap<String, List<AbstractSetting>> getSettingGroups() {
        LinkedHashMap<String, List<AbstractSetting>> groups = new LinkedHashMap<>();
        AtomicReference<String> currentCategory = new AtomicReference<>("");

        Arrays.stream(this.getClass().getFields()).forEach(field -> {
            if (AbstractSetting.class.isAssignableFrom(field.getType())) {
                try {
                    field.setAccessible(true); // Make the field accessible, especially if it is private
                    if (field.isAnnotationPresent(CategorySetting.class)) {
                        currentCategory.set(field.getAnnotation(CategorySetting.class).name());
                    }
                    groups.computeIfAbsent(currentCategory.get(), k -> new ArrayList<>()).add((AbstractSetting) field.get(this));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        return groups;
    }

    /**
     * 2024-06-14, 18:04 : Likely capable of causing a crash, we'll see how it
     * goes. edit 2024-06-14, 19:03 : Why did I think it was a good idea for
     * FUCK’S SAKE edit 2024-06-14, 20:29 : I'm a fucking genius
     *
     * @param builder : command builder
     */
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("setting", SettingArgumentType.from(getSettings()))
                .then(argument("newValue", AnyArgumentType.get())
                        .executes(ctx -> {
                            AbstractSetting set = SettingArgumentType.getSetting(ctx);
                            String nv = ctx.getArgument("newValue", String.class);

                            boolean failed = false, resetFlag = false;

                            try {
                                if (set instanceof BooleanSetting booleanSetting) {
                                    booleanSetting.setValue(isTrue(nv));
                                    if (booleanSetting.getDisplayName().equalsIgnoreCase("enabled")) {
                                        resetFlag = true;
                                    }
                                }
                                else if (set instanceof FloatSetting floatSetting) {
                                    floatSetting.setValue(Float.parseFloat(nv));
                                }
                                else if (set instanceof IntegerSetting integerSetting) {
                                    integerSetting.setValue(Integer.valueOf(nv));
                                }
                                else if (set instanceof LongSetting longSetting) {
                                    longSetting.setValue(Long.valueOf(nv));
                                }
                                else if (set instanceof StringSetting stringSetting) {
                                    stringSetting.setValue(nv);
                                }
                                else if (set instanceof EnumSetting enumSetting) {
                                    enumSetting.fromString(nv);
                                }
                                else if (set instanceof Vector2Setting) {
                                    WHLogger.printToChat("Position vectors cant be changed this way !");
                                }
                                else {
                                    WHLogger.printToChat("Type %s has not been implemented yet", set.getValue().getClass().getSimpleName());
                                    failed = true;
                                }
                            } catch (NumberFormatException e) {
                                WHLogger.printToChat("An error has occurred due to a miss-input : \n%s", e.getMessage());
                                failed = true;
                            }

                            if (!failed) {
                                WHLogger.printToChat("§7§o%s.%s = %s", getName().toLowerCase(), set.getDisplayName().toLowerCase(), nv.toLowerCase());

                                if (resetFlag) {
                                    if (this instanceof HudElement hlement) {
                                        hlement.onToggle(true);
                                    }
                                    else {
                                        onToggle(true);
                                    }
                                }

                                save();
                            }
                            return 1;
                        })
                )
        );
    }

    public void init() {
        init(false);
    }

    /**
     * In charge of loading module settings
     */
    public void init(boolean live) {
        path = PathUtils.join("./WaifuHax", category.getName(), getName() + ".json");

        File file = new File(path);

        if (file.exists()) {
            load(live);
        }
        else {
            save();
        }

        if (this.getClass().isAnnotationPresent(AutoDisable.class)) {
            isEnabled.setValue(false);
            save();
            onToggle(true);
        }

        if (this.getClass().isAnnotationPresent(AutoEnable.class)) {
            if (!isEnabled.getValue()) {
                isEnabled.setValue(true);
                save();
                onToggle(true);
            }
            isEnabled.setShouldDraw(false);
        }
    }

    private String readFileToString(String filePath) {
        Path path = Paths.get(filePath);

        try {
            byte[] fileBytes = Files.readAllBytes(path);
            return new String(fileBytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void load() {
        load(false);
    }

    private void load(boolean live) {
        JSONObject savedSettings = new JSONObject(readFileToString(path));
        isEnabled.setValue(savedSettings.has("Enabled") && savedSettings.getBoolean("Enabled"));
        keycode.setValue(savedSettings.has("keycode") ? savedSettings.getInt("keycode") : -1);
        WaifuHax.EVENT_BUS.unsubscribe(this);

        getSettings().forEach(setting -> setting.load(savedSettings));
        onToggle(live);
    }

    @SneakyThrows
    public void save() {
        JSONObject saveData = new JSONObject();

        saveData.put("Enabled", isEnabled.getValue());
        saveData.put("keycode", keycode.getValue());

        getSettings().forEach(abstractSetting -> abstractSetting.save(saveData));

        File file = new File(path);

        if (!file.exists()) {file.createNewFile();}

        FileWriter writer = new FileWriter(path);
        writer.write(saveData.toString(4));
        writer.close();
    }

    public void onToggle() {
        onToggle(false);
    }

    // NOTE : LIVE MEAN WE ARE RELOADING ALL MODULES AT ONCE
    public void onToggle(boolean live) {
        if (isEnabled.getValue()) {
            WaifuHax.EVENT_BUS.subscribe(this);
            onActivate(live);
            if (!live && GlobalOptions.getInstance().printToggleMessage.getValue() && mc.world != null) {
                WHLogger.printToChat("%s was §2enabled", getName());
            }
        }
        else {
            WaifuHax.EVENT_BUS.unsubscribe(this);
            onDeactivate(live);
            if (!live && GlobalOptions.getInstance().printToggleMessage.getValue() && mc.world != null) {
                WHLogger.printToChat("%s was §4disabled", getName());
            }
        }
    }

    public void onActivate(boolean live) {
    }

    public void onDeactivate(boolean live) {
    }

    public void toggle() {
        isEnabled.setValue(!isEnabled.getValue());
        onToggle();
    }

    public abstract String getDescription();


}