package dev.anarchy.waifuhax.client.systems.modules.misc;

import dev.anarchy.waifuhax.api.systems.modules.AbstractModule;

public class CoordinateSpoofer extends AbstractModule {

    @Override
    public String getDescription() {
        return "Do literally nothing, but can fool people into thinking it's not your actual coordinates if you leak them by accident";
    }
}
