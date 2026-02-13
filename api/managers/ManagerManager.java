package dev.anarchy.waifuhax.api.managers;

import dev.anarchy.waifuhax.api.BaseManager;
import dev.anarchy.waifuhax.client.WaifuHax;
import lombok.SneakyThrows;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ManagerManager extends BaseManager {

    private static final List<BaseManager> managers = new ArrayList<>();

    public static Optional<BaseManager> getManager(Class<? extends BaseManager> clazz) {
        return managers.stream()
                .filter(instance -> instance.getClass().equals(clazz))
                .findFirst();
    }

    private boolean isManagerLoaded(Class<? extends BaseManager> clazz) {
        return managers.stream().anyMatch(instance -> instance.getClass().equals(clazz));
    }

    @SneakyThrows
    private void initManager(Class<? extends BaseManager> clazz) {
        // if a manager needs another to be loaded in order to work
        // we will first create the parent manager.
        // it is done recursively, but is safe because most managers
        // are independent of the rest, and it is also not guaranteed
        if (clazz.getAnnotation(LoadAfter.class) != null) {
            Class<? extends BaseManager> parentClass = clazz.getAnnotation(LoadAfter.class).manager();
            if (!isManagerLoaded(parentClass)) {initManager(parentClass);}
        }
        if (!isManagerLoaded(clazz)) {
            managers.add(clazz.getDeclaredConstructor().newInstance());
            getManager(clazz).ifPresent(IManager::init);
        }
    }

    @Override
    public void init() {
        new Reflections(WaifuHax.class.getPackage().getName() + ".managers")
                .getSubTypesOf(BaseManager.class)
                .forEach(this::initManager);
    }
}
