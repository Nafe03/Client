package dev.anarchy.waifuhax.client.systems.modules.render;

import dev.anarchy.waifuhax.api.AbstractSetting;
import dev.anarchy.waifuhax.api.settings.BooleanSetting;
import dev.anarchy.waifuhax.api.settings.EnumSetting;
import dev.anarchy.waifuhax.api.settings.annotations.CategorySetting;
import dev.anarchy.waifuhax.api.systems.modules.AbstractModule;
import dev.anarchy.waifuhax.client.events.TickEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public class Fullbright extends AbstractModule {

    public enum Mode {
        GAMMA,
        LUMINANCE,
        BOTH
    }

    @CategorySetting(name = "General")
    public final AbstractSetting<Mode> mode = new EnumSetting<>("Mode", "Fullbright mode", Mode.BOTH);
    public final AbstractSetting<Boolean> removeShadows = new BooleanSetting("Remove Shadows", "Removes entity shadows", true);

    private double originalGamma = 1.0;
    private boolean hadNightVision = false;

    @Override
    public void onActivate(boolean live) {
        super.onActivate(live);

        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.options != null) {
            originalGamma = client.options.getGamma().getValue();
            
            if (mode.getValue() == Mode.GAMMA || mode.getValue() == Mode.BOTH) {
                client.options.getGamma().setValue(16.0);
            }
        }

        if (client != null && client.player != null) {
            hadNightVision = client.player.hasStatusEffect(StatusEffects.NIGHT_VISION);
        }
    }

    @EventHandler
    private void onTick(TickEvent event) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return;

        // Keep gamma at maximum if using gamma mode
        if (mode.getValue() == Mode.GAMMA || mode.getValue() == Mode.BOTH) {
            if (client.options != null) {
                client.options.getGamma().setValue(16.0);
            }
        }

        // Apply night vision effect if using luminance mode
        if ((mode.getValue() == Mode.LUMINANCE || mode.getValue() == Mode.BOTH) && client.player != null) {
            if (!client.player.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
                client.player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.NIGHT_VISION,
                    Integer.MAX_VALUE,
                    0,
                    false,
                    false,
                    false
                ));
            }
        }
    }

    @Override
    public void onDeactivate(boolean live) {
        super.onDeactivate(live);

        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.options != null) {
            client.options.getGamma().setValue(originalGamma);
        }

        // Remove night vision if it wasn't there before
        if (client != null && client.player != null) {
            if (!hadNightVision && client.player.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
                client.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
            }
        }
    }

    @Override
    public String getDescription() {
        return "Makes everything bright and removes shadows";
    }

    public boolean shouldRemoveShadows() {
        return isEnabled.getValue() && removeShadows.getValue();
    }
}