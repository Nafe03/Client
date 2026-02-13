package dev.anarchy.waifuhax.api.systems.modules;

import dev.anarchy.waifuhax.api.gui.WHWindow;
import dev.anarchy.waifuhax.api.gui.components.impl.UICheckBox;
import dev.anarchy.waifuhax.api.gui.components.impl.UIModuleSelector;
import dev.anarchy.waifuhax.api.systems.modules.annotations.ListHidden;
import dev.anarchy.waifuhax.client.WaifuHax;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec2f;
import org.apache.commons.lang3.StringUtils;
import org.joml.Vector2f;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Slf4j
public class Category {

    private String filename;
    private String name;
    private Vec2f pos = new Vec2f(0, 0);

    private static final HashSet<Category> categories = new HashSet<>();
    private final HashSet<AbstractModule> mods = new HashSet<>();

    private final WHWindow ClickGUI = new WHWindow("TMP", new Vector2f(0, 0));

    private Category() {
        WaifuHax.EVENT_BUS.unsubscribe(ClickGUI);

        ClickGUI.getRoot().getChildRecursive("WindowHeader").get().addMouseEvent((event) -> {
            pos = new Vec2f(ClickGUI.getRoot().getPos().x, ClickGUI.getRoot().getPos().y);
            save();
        });
        ClickGUI.getRoot().getChildRecursive("IsPinned").get().setEnabled(false);
    }

    private static String readFileToString(String filePath) {
        Path path = Paths.get(filePath);

        try {
            byte[] fileBytes = Files.readAllBytes(path);
            return new String(fileBytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SneakyThrows
    public void save() {
        JSONObject data = new JSONObject();
        data.put("pos", new JSONObject().put("x", pos.x).put("y", pos.y));

        File file = new File(filename);

        if (!file.exists())
            file.createNewFile();

        FileWriter writer = new FileWriter(filename);
        writer.write(data.toString(4));
        writer.close();
    }

    public static Category create(String name) {
        Optional<Category> possibleCat = categories.stream().filter(cat -> cat.getName().equalsIgnoreCase(name)).findFirst();
        if (possibleCat.isPresent()) {return possibleCat.get();}
        Category cat = new Category();
        cat.name = name;
        cat.filename = "./WaifuHax/" + name + ".json";
        File modDir = new File("./WaifuHax/" + cat.name.toUpperCase());
        if (!modDir.exists())
            modDir.mkdir();
        if (new File(cat.filename).exists()) {
            JSONObject obj = new JSONObject(readFileToString(cat.filename));
            if (obj.has("pos")) {
                cat.pos = new Vec2f(obj.getJSONObject("pos").getFloat("x"), obj.getJSONObject("pos").getFloat("y"));
            }
        }
        categories.add(cat);
        cat.ClickGUI.getRoot().setPos(cat.pos.x, cat.pos.y);
        return cat;
    }

    public static void disableAll() {
        categories.forEach(cat -> WaifuHax.EVENT_BUS.unsubscribe(cat.ClickGUI));
    }

    public static void enableAll() {
        categories.forEach(cat -> {
            if (!cat.getMods().isEmpty()) {
                cat.ClickGUI.setWindowSize(cat.minWidth(), (int) (4.5f + (15 * cat.properSize())));
                WaifuHax.EVENT_BUS.subscribe(cat.ClickGUI);
            }
        });
    }

    public void addModule(AbstractModule mod) {
        if (!mod.getClass().isAnnotationPresent(ListHidden.class)) {
            ClickGUI.getRoot()
                    .getChildRecursive("WindowFrameForeground")
                    .get()
                    .addChild(new UIModuleSelector()
                            .setModule(mod)
                            .setIdentifier(mod.getName())
                            .setPos(3, 4.5f + (15 * properSize()))
                    );
        }
        mods.add(mod);
        if (!mod.getClass().isAnnotationPresent(ListHidden.class)) {
            ClickGUI.setTitle(formatedName());
        }
    }

    public Set<AbstractModule> getMods() {
        return new HashSet<>(mods);
    }

    public int properSize() {
        return mods.stream()
                .filter((mod) -> !mod.getClass()
                    .isAnnotationPresent(ListHidden.class))
                .toList()
                .size();
    }

    public String formatedName() {
        return StringUtils.capitalize(name) + "§f (" + properSize() + ")";
    }

    public String getName() {
        return name.toUpperCase(Locale.ROOT);
    }

    public int minWidth() {
        int header = (MinecraftClient.getInstance().textRenderer.getWidth(formatedName()) + 4 + (UICheckBox.SIZE * 2) + ((UICheckBox.SIZE * 3) / 4));
        for (AbstractModule mod : mods) {
            header = Math.max(header, 4 + MinecraftClient.getInstance().textRenderer.getWidth(mod.getName()) + MinecraftClient.getInstance().textRenderer.getWidth("00"));
        }
        return header;
    }
}
