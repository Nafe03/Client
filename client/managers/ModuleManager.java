package dev.anarchy.waifuhax.client.managers;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.anarchy.waifuhax.api.BaseManager;
import dev.anarchy.waifuhax.api.managers.LoadAfter;
import dev.anarchy.waifuhax.api.systems.modules.AbstractModule;
import dev.anarchy.waifuhax.api.systems.modules.Category;
import dev.anarchy.waifuhax.api.systems.modules.annotations.ListHidden;
import dev.anarchy.waifuhax.api.systems.modules.annotations.ManualInstanciating;
import dev.anarchy.waifuhax.api.systems.modules.annotations.NoCommand;
import dev.anarchy.waifuhax.client.WaifuHax;
import dev.anarchy.waifuhax.client.systems.modules.misc.GlobalOptions;
import lombok.SneakyThrows;
import net.minecraft.command.CommandSource;
import org.reflections.Reflections;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@LoadAfter(manager = CommandManager.class)
public class ModuleManager extends BaseManager {

    private static final Set<AbstractModule> moduleSet = new HashSet<>();

    private static void registerCommandToDispatcher(AbstractModule cmd) {
        LiteralArgumentBuilder<CommandSource> builder = LiteralArgumentBuilder.literal(cmd.getName().toLowerCase());
        cmd.build(builder);
        CommandManager.getInstance().dispatcher.register(builder);
    }

    public static Set<AbstractModule> getModulesOfCategory(Category cat) {
        return cat.getMods();
    }

    public static <T extends AbstractModule> T getModule(Class<T> moduleClass) {
        return moduleSet.stream()
                .filter(mod -> mod.getClass().equals(moduleClass))
                .map(moduleClass::cast)
                .findFirst()
                .orElse(null);
    }

    public static AbstractModule getModule(String moduleClass) {
        return moduleSet.stream()
                .filter(mod -> mod.getClass().getSimpleName().equalsIgnoreCase(moduleClass))
                .findFirst()
                .orElse(null);
    }

    public static List<AbstractModule> getEnabledModules() {
        return moduleSet.stream()
                .filter(mod -> mod.isEnabled.getValue() && mod.getClass().getAnnotation(ListHidden.class) == null)
                .sorted(Comparator.comparing(AbstractModule::getName))
                .collect(Collectors.toList());
    }

    public static List<AbstractModule> getModules() {
        return new ArrayList<>(moduleSet);
    }

    public static void onKey(int key) {
        moduleSet.forEach(mod -> {
            if (mod.keycode.getValue().equals(key)) {
                mod.toggle();
                mod.save();
            }
        });
    }

    public static void saveAll() {
        moduleSet.forEach(AbstractModule::save);
    }

    public static void loadAll() {
        moduleSet.forEach(mod -> mod.init(true));
    }

    @SneakyThrows
    private void instanciate(Class<? extends AbstractModule> mod) {
        AbstractModule module = mod.getDeclaredConstructor().newInstance();
        moduleSet.add(module);
        module.init();
        final String category = Arrays.stream(module.getClass().getPackageName().split("\\.")).toList().getLast();
        Category.create(category).addModule(module);
        if (!mod.isAnnotationPresent(NoCommand.class)) {
            registerCommandToDispatcher(module);
        }
    }

    public void initCategories() {
        new Reflections(WaifuHax.class.getPackage().getName() + ".systems.modules")
                .getSubTypesOf(AbstractModule.class)
                .stream().sorted(Comparator.comparing(Class::getSimpleName))
                .forEach(module -> {
                    final String category = Arrays.stream(module.getPackageName().split("\\.")).toList().getLast();
                    Category.create(category);
                });
    }

    @SneakyThrows
    public void init() {
        AtomicReference<File> file = new AtomicReference<>(new File("WaifuHax"));

        if (!file.get().exists()) {file.get().mkdir();}

        initCategories();

        instanciate(GlobalOptions.class);
        new Reflections(WaifuHax.class.getPackage().getName() + ".systems.modules")
                .getSubTypesOf(AbstractModule.class)
                .stream().sorted(Comparator.comparing(Class::getSimpleName))
                .filter(mod -> !mod.isAnnotationPresent(ManualInstanciating.class))
                .forEach(this::instanciate);
    }

}
