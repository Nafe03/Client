package dev.anarchy.waifuhax.client.systems.modules.render;

import dev.anarchy.waifuhax.api.AbstractSetting;
import dev.anarchy.waifuhax.api.settings.BooleanSetting;
import dev.anarchy.waifuhax.api.settings.EnumSetting;
import dev.anarchy.waifuhax.api.settings.FloatSetting;
import dev.anarchy.waifuhax.api.settings.IntegerSetting;
import dev.anarchy.waifuhax.api.settings.StringSetting;

import dev.anarchy.waifuhax.api.systems.modules.AbstractModule;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import dev.anarchy.waifuhax.renderer.Renderer3D;
import dev.anarchy.waifuhax.renderer.ShapeMode;
import dev.anarchy.waifuhax.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * ESP - Entity ESP
 * Shows entities through walls with customizable rendering modes
 */
public class ESP extends AbstractModule {

    public enum Mode {
        Box,
        Wireframe,
        _2D,
        Shader,
        Glow
    }

    public enum ColorMode {
        EntityType,
        Distance,
        Health
    }

    // General settings
    public final AbstractSetting<Mode> mode = new EnumSetting<>("Mode", "Rendering mode", Mode.Box);
    public final AbstractSetting<Boolean> highlightTarget = new BooleanSetting("Highlight Target", "Highlights the currently targeted entity differently", false);
    public final AbstractSetting<Boolean> targetHitbox = new BooleanSetting("Target Hitbox", "Draw the hitbox of the target entity", true);
    public final AbstractSetting<Integer> outlineWidth = new IntegerSetting("Outline Width", "The width of the shader outline", 2, 1, 10);
    public final AbstractSetting<Float> glowMultiplier = new FloatSetting("Glow Multiplier", "Multiplier for glow effect", 3.5f, 0f, 10f);
    public final AbstractSetting<Boolean> ignoreSelf = new BooleanSetting("Ignore Self", "Ignores yourself drawing the shader", true);
    public final AbstractSetting<ShapeMode> shapeMode = new EnumSetting<>("Shape Mode", "How the shapes are rendered", ShapeMode.Both);
    public final AbstractSetting<Float> fillOpacity = new FloatSetting("Fill Opacity", "The opacity of the shape fill", 0.3f, 0f, 1f);
    public final AbstractSetting<Float> fadeDistance = new FloatSetting("Fade Distance", "The distance from an entity where the color begins to fade", 3f, 0f, 12f);

    // Entity filters - comma-separated list of entity type names
    public final AbstractSetting<String> entities = new StringSetting("Entities", "Comma-separated entity types (PLAYER, CREEPER, SKELETON, etc.)", "PLAYER");


    // Color settings
    public final AbstractSetting<ColorMode> colorMode = new EnumSetting<>("Color Mode", "Determines the colors used for entities", ColorMode.EntityType);
    public final AbstractSetting<Boolean> friendOverride = new BooleanSetting("Show Friend Colors", "Whether or not to override the distance/health color of friends with the friend color", true);

    // Static colors for EntityType mode
    public final AbstractSetting<Integer> playersR = new IntegerSetting("Players Red", "The other player's color red component", 255, 0, 255);
    public final AbstractSetting<Integer> playersG = new IntegerSetting("Players Green", "The other player's color green component", 255, 0, 255);
    public final AbstractSetting<Integer> playersB = new IntegerSetting("Players Blue", "The other player's color blue component", 255, 0, 255);

    public final AbstractSetting<Integer> animalsR = new IntegerSetting("Animals Red", "The animal's color red component", 25, 0, 255);
    public final AbstractSetting<Integer> animalsG = new IntegerSetting("Animals Green", "The animal's color green component", 255, 0, 255);
    public final AbstractSetting<Integer> animalsB = new IntegerSetting("Animals Blue", "The animal's color blue component", 25, 0, 255);

    public final AbstractSetting<Integer> waterAnimalsR = new IntegerSetting("Water Animals Red", "The water animal's color red component", 25, 0, 255);
    public final AbstractSetting<Integer> waterAnimalsG = new IntegerSetting("Water Animals Green", "The water animal's color green component", 25, 0, 255);
    public final AbstractSetting<Integer> waterAnimalsB = new IntegerSetting("Water Animals Blue", "The water animal's color blue component", 255, 0, 255);

    public final AbstractSetting<Integer> monstersR = new IntegerSetting("Monsters Red", "The monster's color red component", 255, 0, 255);
    public final AbstractSetting<Integer> monstersG = new IntegerSetting("Monsters Green", "The monster's color green component", 25, 0, 255);
    public final AbstractSetting<Integer> monstersB = new IntegerSetting("Monsters Blue", "The monster's color blue component", 25, 0, 255);

    public final AbstractSetting<Integer> ambientR = new IntegerSetting("Ambient Red", "The ambient's color red component", 25, 0, 255);
    public final AbstractSetting<Integer> ambientG = new IntegerSetting("Ambient Green", "The ambient's color green component", 25, 0, 255);
    public final AbstractSetting<Integer> ambientB = new IntegerSetting("Ambient Blue", "The ambient's color blue component", 25, 0, 255);

    public final AbstractSetting<Integer> miscR = new IntegerSetting("Misc Red", "The misc color red component", 175, 0, 255);
    public final AbstractSetting<Integer> miscG = new IntegerSetting("Misc Green", "The misc color green component", 175, 0, 255);
    public final AbstractSetting<Integer> miscB = new IntegerSetting("Misc Blue", "The misc color blue component", 175, 0, 255);

    // Target colors
    public final AbstractSetting<Integer> targetR = new IntegerSetting("Target Red", "The target color red component", 200, 0, 255);
    public final AbstractSetting<Integer> targetG = new IntegerSetting("Target Green", "The target color green component", 200, 0, 255);
    public final AbstractSetting<Integer> targetB = new IntegerSetting("Target Blue", "The target color blue component", 200, 0, 255);

    public final AbstractSetting<Integer> targetHitboxR = new IntegerSetting("Target Hitbox Red", "The target hitbox color red component", 100, 0, 255);
    public final AbstractSetting<Integer> targetHitboxG = new IntegerSetting("Target Hitbox Green", "The target hitbox color green component", 200, 0, 255);
    public final AbstractSetting<Integer> targetHitboxB = new IntegerSetting("Target Hitbox Blue", "The target hitbox color blue component", 200, 0, 255);

    private final Renderer3D renderer = new Renderer3D();
    private int count;

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (mode.getValue() == Mode._2D) return;

        count = 0;

        Entity target = null;
        if (highlightTarget.getValue() && targetHitbox.getValue() && mc.crosshairTarget instanceof net.minecraft.util.hit.EntityHitResult hr) {
            target = hr.getEntity();
        }

        for (Entity entity : mc.world.getEntities()) {
            if (target != entity && shouldSkip(entity)) continue;
            if (target == entity || mode.getValue() == Mode.Box || mode.getValue() == Mode.Wireframe) {
                drawBoundingBox(event, entity);
            }
            count++;
        }
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        if (mode.getValue() != Mode._2D) return;

        // 2D rendering would require additional setup, for now just count entities
        count = 0;
        for (Entity entity : mc.world.getEntities()) {
            if (shouldSkip(entity)) continue;
            count++;
        }
    }

    private void drawBoundingBox(Render3DEvent event, Entity entity) {
        Color color = getColor(entity);
        if (color != null) {
            double x = MathHelper.lerp(event.tickDelta, entity.lastRenderX, entity.getX()) - entity.getX();
            double y = MathHelper.lerp(event.tickDelta, entity.lastRenderY, entity.getY()) - entity.getY();
            double z = MathHelper.lerp(event.tickDelta, entity.lastRenderZ, entity.getZ()) - entity.getZ();

            ShapeMode shape = shapeMode.getValue();
            if (drawAsTarget(entity) && mode.getValue() != Mode.Box) {
                shape = ShapeMode.Lines;
                color = new Color(targetHitboxR.getValue(), targetHitboxG.getValue(), targetHitboxB.getValue(), 255);
            }

            Box box = entity.getBoundingBox();
            renderer.box(x + box.minX, y + box.minY, z + box.minZ, x + box.maxX, y + box.maxY, z + box.maxZ,
                        new Color(0, 0, 0, (int)(fillOpacity.getValue() * 255)), color, shape, 0);
        }
    }

    public boolean drawAsTarget(Entity entity) {
        return highlightTarget.getValue() && mc.crosshairTarget instanceof net.minecraft.util.hit.EntityHitResult hr && hr.getEntity() == entity;
    }

    public boolean shouldSkip(Entity entity) {
        if (drawAsTarget(entity)) return false;
        if (!shouldRenderEntityType(entity.getType())) return true;
        if (entity == mc.player && ignoreSelf.getValue()) return true;
        if (entity == mc.getCameraEntity() && mc.options.getPerspective().isFirstPerson()) return true;
        return !isInRenderDistance(entity);
    }
    
    private boolean shouldRenderEntityType(EntityType<?> type) {
        String config = entities.getValue().toUpperCase();
        if (config.isEmpty() || config.equals("ALL")) return true;
        
        String typeName = EntityType.getId(type).getPath().toUpperCase();
        String[] allowedTypes = config.split(",");
        for (String allowed : allowedTypes) {
            if (allowed.trim().toUpperCase().equals(typeName)) return true;
        }
        return false;
    }


    private boolean isInRenderDistance(Entity entity) {
        double dist = mc.player.distanceTo(entity);
        return dist <= mc.options.getViewDistance().getValue() * 16;
    }

    public Color getColor(Entity entity) {
        Color color;
        double alpha = 1;

        if (drawAsTarget(entity)) {
            color = new Color(targetR.getValue(), targetG.getValue(), targetB.getValue(), 255);
        } else {
            if (!shouldRenderEntityType(entity.getType())) return null;

            alpha = getFadeAlpha(entity);
            if (alpha == 0) return null;

            color = getEntityTypeColor(entity);
        }

        return new Color(color.r, color.g, color.b, (int)(color.a * alpha));
    }

    private double getFadeAlpha(Entity entity) {
        double dist = mc.player.distanceTo(entity);
        double fadeDist = fadeDistance.getValue();
        double alpha = 1;
        if (dist <= fadeDist * fadeDist) alpha = Math.sqrt(dist) / fadeDist;
        if (alpha <= 0.075) alpha = 0;
        return alpha;
    }

    public Color getEntityTypeColor(Entity entity) {
        if (colorMode.getValue() == ColorMode.EntityType) {
            if (entity instanceof PlayerEntity) {
                return new Color(playersR.getValue(), playersG.getValue(), playersB.getValue(), 255);
            } else {
                return switch (entity.getType().getSpawnGroup()) {
                    case CREATURE -> new Color(animalsR.getValue(), animalsG.getValue(), animalsB.getValue(), 255);
                    case WATER_AMBIENT, WATER_CREATURE, UNDERGROUND_WATER_CREATURE -> new Color(waterAnimalsR.getValue(), waterAnimalsG.getValue(), waterAnimalsB.getValue(), 255);
                    case MONSTER -> new Color(monstersR.getValue(), monstersG.getValue(), monstersB.getValue(), 255);
                    case AMBIENT -> new Color(ambientR.getValue(), ambientG.getValue(), ambientB.getValue(), 255);
                    default -> new Color(miscR.getValue(), miscG.getValue(), miscB.getValue(), 255);
                };
            }
        }

        if (colorMode.getValue() == ColorMode.Health) {
            if (entity instanceof PlayerEntity player) {
                float health = player.getHealth() / player.getMaxHealth();
                return new Color((int)((1.0f - health) * 255), (int)(health * 255), 0, 255);
            }
            return new Color(255, 255, 255, 255);
        } else {
            // Distance color
            double dist = mc.player.distanceTo(entity);
            float ratio = (float) Math.min(dist / (mc.options.getViewDistance().getValue() * 16), 1.0);
            return new Color((int)(ratio * 255), (int)((1.0f - ratio) * 255), 0, 255);
        }
    }

    public String getDescription() {
        return "Renders entities through walls.";
    }


    public String getInfoString() {
        return Integer.toString(count);
    }

    public boolean isShader() {
        return isEnabled.getValue() && mode.getValue() == Mode.Shader;
    }

    public boolean isGlow() {
        return isEnabled.getValue() && mode.getValue() == Mode.Glow;
    }

}
