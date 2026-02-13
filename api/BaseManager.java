package dev.anarchy.waifuhax.api;

import dev.anarchy.waifuhax.api.managers.IManager;
import lombok.Getter;

public abstract class BaseManager implements IManager {

    @Getter
    private boolean isInit = false;

    protected BaseManager() {
        this.isInit = true;
    }
}
