package dev.anarchy.waifuhax.client.systems.modules.world;

import dev.anarchy.waifuhax.api.settings.EnumSetting;
import dev.anarchy.waifuhax.api.systems.modules.AbstractModule;

public class AntiBlockRotate extends AbstractModule {

    public EnumSetting mode = new EnumSetting("Mode", "What type of rotation will be used", Modes.NO_ROTATION);

    @Override
    public String getDescription() {
        return "Prevent the texture rotation coordinate exploit";
    }

    public enum Modes {
        NO_ROTATION,
        CUSTOM_SEED,
    }
}
