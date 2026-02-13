package dev.anarchy.waifuhax.client;

import dev.anarchy.waifuhax.api.managers.ManagerManager;
import meteordevelopment.orbit.EventBus;
import meteordevelopment.orbit.IEventBus;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;

import java.lang.invoke.MethodHandles;

public class WaifuHax implements ClientModInitializer {

    public static final IEventBus EVENT_BUS = new EventBus();

    public static ModMetadata MOD_META;
    public static String VERSION;

    @Override
    public void onInitializeClient() {

        MOD_META = FabricLoader.getInstance().getModContainer("waifuhax").orElseThrow().getMetadata();
        VERSION = MOD_META.getVersion().getFriendlyString();

        EVENT_BUS.registerLambdaFactory("dev.anarchy.waifuhax", (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));
        EVENT_BUS.subscribe(this);

        new ManagerManager().init();
    }

}
