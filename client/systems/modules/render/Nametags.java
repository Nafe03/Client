package dev.anarchy.waifuhax.client.systems.modules.render;

import dev.anarchy.waifuhax.api.AbstractSetting;
import dev.anarchy.waifuhax.api.settings.BooleanSetting;
import dev.anarchy.waifuhax.api.settings.FloatSetting;
import dev.anarchy.waifuhax.api.settings.IntegerSetting;
import dev.anarchy.waifuhax.api.systems.modules.AbstractModule;

public class Nametags extends AbstractModule {
    
    public final AbstractSetting<Boolean> players = new BooleanSetting("Players", "Show player nametags", true);
    public final AbstractSetting<Boolean> mobs = new BooleanSetting("Mobs", "Show mob nametags", true);
    public final AbstractSetting<Boolean> items = new BooleanSetting("Items", "Show item nametags", false);
    
    public final AbstractSetting<Float> scale = new FloatSetting("Scale", "Nametag scale", 1.0f, 0.5f, 3.0f);
    public final AbstractSetting<Float> maxDistance = new FloatSetting("Max Distance", "Maximum render distance", 64.0f, 16.0f, 256.0f);
    
    public final AbstractSetting<Boolean> showHealth = new BooleanSetting("Show Health", "Display health", true);
    public final AbstractSetting<Boolean> showDistance = new BooleanSetting("Show Distance", "Display distance", true);
    public final AbstractSetting<Boolean> showPing = new BooleanSetting("Show Ping", "Display ping (players only)", false);
    public final AbstractSetting<Boolean> showArmor = new BooleanSetting("Show Armor", "Display armor durability", true);
    public final AbstractSetting<Boolean> showEnchants = new BooleanSetting("Show Enchants", "Display enchantments", false);
    
    public final AbstractSetting<Boolean> background = new BooleanSetting("Background", "Draw background", true);
    public final AbstractSetting<Integer> bgAlpha = new IntegerSetting("Background Alpha", "Background transparency", 100, 0, 255);
    
    public final AbstractSetting<Boolean> throughWalls = new BooleanSetting("Through Walls", "Render through walls", true);
    public final AbstractSetting<Boolean> scaleWithDistance = new BooleanSetting("Scale With Distance", "Scale based on distance", false);
    
    // Public getters for renderer mixin to use
    public boolean showPlayers() {
        return isEnabled.getValue() && players.getValue();
    }
    
    public boolean showMobs() {
        return isEnabled.getValue() && mobs.getValue();
    }
    
    public boolean showItems() {
        return isEnabled.getValue() && items.getValue();
    }
    
    public float getScale() {
        return scale.getValue();
    }
    
    public float getMaxDistance() {
        return maxDistance.getValue();
    }
    
    public boolean shouldShowHealth() {
        return showHealth.getValue();
    }
    
    public boolean shouldShowDistance() {
        return showDistance.getValue();
    }
    
    public boolean shouldShowPing() {
        return showPing.getValue();
    }
    
    public boolean shouldShowArmor() {
        return showArmor.getValue();
    }
    
    public boolean shouldShowEnchants() {
        return showEnchants.getValue();
    }
    
    public boolean hasBackground() {
        return background.getValue();
    }
    
    public int getBackgroundAlpha() {
        return bgAlpha.getValue();
    }
    
    public boolean renderThroughWalls() {
        return throughWalls.getValue();
    }
    
    public boolean shouldScaleWithDistance() {
        return scaleWithDistance.getValue();
    }
    
    @Override
    public String getDescription() {
        return "Enhanced entity nametags with health, distance, and more";
    }
}