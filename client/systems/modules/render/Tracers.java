package dev.anarchy.waifuhax.client.systems.modules.render;

import dev.anarchy.waifuhax.api.AbstractSetting;
import dev.anarchy.waifuhax.api.settings.BooleanSetting;
import dev.anarchy.waifuhax.api.settings.EnumSetting;
import dev.anarchy.waifuhax.api.settings.FloatSetting;
import dev.anarchy.waifuhax.api.settings.IntegerSetting;
import dev.anarchy.waifuhax.api.systems.modules.AbstractModule;
import dev.anarchy.waifuhax.client.events.RenderEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

public class Tracers extends AbstractModule {
    
    public enum TargetPoint {
        FEET,
        BODY,
        HEAD,
        EYES
    }
    
    public enum ColorMode {
        STATIC,
        DISTANCE,
        HEALTH,
        RAINBOW
    }
    
    // Settings
    public final AbstractSetting<TargetPoint> target = new EnumSetting<>("Target", "Where to draw line to", TargetPoint.BODY);
    public final AbstractSetting<ColorMode> colorMode = new EnumSetting<>("Color Mode", "How to color tracers", ColorMode.DISTANCE);
    public final AbstractSetting<Float> lineWidth = new FloatSetting("Line Width", "Width of tracer lines", 2.0f, 0.1f, 5.0f);
    public final AbstractSetting<Float> maxDistance = new FloatSetting("Max Distance", "Maximum render distance", 64.0f, 16.0f, 256.0f);
    
    // Entity filters
    public final AbstractSetting<Boolean> players = new BooleanSetting("Players", "Show players", true);
    public final AbstractSetting<Boolean> hostiles = new BooleanSetting("Hostiles", "Show hostile mobs", true);
    public final AbstractSetting<Boolean> passives = new BooleanSetting("Passives", "Show passive mobs", false);
    public final AbstractSetting<Boolean> items = new BooleanSetting("Items", "Show dropped items", false);
    public final AbstractSetting<Boolean> crystals = new BooleanSetting("Crystals", "Show end crystals", true);
    public final AbstractSetting<Boolean> others = new BooleanSetting("Others", "Show other entities", false);
    
    // Color settings
    public final AbstractSetting<Integer> playerR = new IntegerSetting("Player Red", "Player color red", 0, 0, 255);
    public final AbstractSetting<Integer> playerG = new IntegerSetting("Player Green", "Player color green", 255, 0, 255);
    public final AbstractSetting<Integer> playerB = new IntegerSetting("Player Blue", "Player color blue", 255, 0, 255);
    
    public final AbstractSetting<Integer> hostileR = new IntegerSetting("Hostile Red", "Hostile color red", 255, 0, 255);
    public final AbstractSetting<Integer> hostileG = new IntegerSetting("Hostile Green", "Hostile color green", 0, 0, 255);
    public final AbstractSetting<Integer> hostileB = new IntegerSetting("Hostile Blue", "Hostile color blue", 0, 0, 255);
    
    private long startTime;
    
    @Override
    public void onActivate(boolean live) {
        super.onActivate(live);
        startTime = System.currentTimeMillis();
    }
    
    @EventHandler
    private void onRender(RenderEvent.World event) {
        if (mc.player == null || mc.world == null) return;
        
        MatrixStack matrices = event.getMatrices();
        Vec3d camPos = mc.gameRenderer.getCamera().getPos();
        Vec3d startPos = getStartPosition(camPos);
        
        matrices.push();
        matrices.translate(-camPos.x, -camPos.y, -camPos.z);
        
        // Setup OpenGL
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glLineWidth(lineWidth.getValue());
        
        GL11.glBegin(GL11.GL_LINES);
        
        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!shouldRender(entity)) continue;
            
            double distance = mc.player.distanceTo(entity);
            if (distance > maxDistance.getValue()) continue;
            
            Vec3d endPos = getTargetPosition(entity);
            float[] color = getColor(entity, distance);
            
            GL11.glColor4f(color[0], color[1], color[2], color[3]);
            GL11.glVertex3d(startPos.x, startPos.y, startPos.z);
            GL11.glVertex3d(endPos.x, endPos.y, endPos.z);
        }
        
        GL11.glEnd();
        GL11.glPopAttrib();
        
        matrices.pop();
    }
    
    private Vec3d getStartPosition(Vec3d camPos) {
        // Start from player's eyes
        return mc.player.getEyePos();
    }
    
    private Vec3d getTargetPosition(Entity entity) {
        return switch (target.getValue()) {
            case FEET -> entity.getPos();
            case BODY -> entity.getPos().add(0, entity.getHeight() / 2, 0);
            case HEAD -> entity.getPos().add(0, entity.getHeight() - 0.2, 0);
            case EYES -> entity.getEyePos();
        };
    }
    
    private boolean shouldRender(Entity entity) {
        if (entity instanceof PlayerEntity) return players.getValue();
        if (entity instanceof HostileEntity) return hostiles.getValue();
        if (entity instanceof PassiveEntity) return passives.getValue();
        if (entity instanceof ItemEntity) return items.getValue();
        if (entity instanceof EndCrystalEntity) return crystals.getValue();
        return others.getValue();
    }
    
    private float[] getColor(Entity entity, double distance) {
        return switch (colorMode.getValue()) {
            case DISTANCE -> getDistanceColor(distance);
            case HEALTH -> getHealthColor(entity);
            case RAINBOW -> getRainbowColor();
            case STATIC -> getStaticColor(entity);
        };
    }
    
    private float[] getStaticColor(Entity entity) {
        if (entity instanceof PlayerEntity) {
            return new float[]{playerR.getValue() / 255f, playerG.getValue() / 255f, playerB.getValue() / 255f, 1.0f};
        } else if (entity instanceof HostileEntity) {
            return new float[]{hostileR.getValue() / 255f, hostileG.getValue() / 255f, hostileB.getValue() / 255f, 1.0f};
        }
        return new float[]{1.0f, 1.0f, 1.0f, 1.0f};
    }
    
    private float[] getDistanceColor(double distance) {
        float ratio = (float) Math.min(distance / maxDistance.getValue(), 1.0);
        return new float[]{ratio, 1.0f - ratio, 0.0f, 1.0f};
    }
    
    private float[] getHealthColor(Entity entity) {
        if (entity instanceof PlayerEntity player) {
            float health = player.getHealth() / player.getMaxHealth();
            return new float[]{1.0f - health, health, 0.0f, 1.0f};
        }
        return new float[]{1.0f, 1.0f, 1.0f, 1.0f};
    }
    
    private float[] getRainbowColor() {
        long time = System.currentTimeMillis() - startTime;
        float hue = (time % 3000) / 3000.0f;
        int rgb = java.awt.Color.HSBtoRGB(hue, 1.0f, 1.0f);
        return new float[]{
            ((rgb >> 16) & 0xFF) / 255f,
            ((rgb >> 8) & 0xFF) / 255f,
            (rgb & 0xFF) / 255f,
            1.0f
        };
    }
    
    @Override
    public String getDescription() {
        return "Draw lines to entities";
    }
}