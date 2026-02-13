package dev.anarchy.waifuhax.client.systems.modules.combat;

import dev.anarchy.waifuhax.api.AbstractSetting;
import dev.anarchy.waifuhax.api.settings.BooleanSetting;
import dev.anarchy.waifuhax.api.settings.EnumSetting;
import dev.anarchy.waifuhax.api.settings.FloatSetting;
import dev.anarchy.waifuhax.api.settings.IntegerSetting;
import dev.anarchy.waifuhax.api.settings.annotations.CategorySetting;
import dev.anarchy.waifuhax.api.systems.modules.AbstractModule;
import dev.anarchy.waifuhax.api.util.InventoryUtils;
import dev.anarchy.waifuhax.client.events.TickEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * FINAL FIX: KillAura module with Crit-Only feature
 */
public class KillAura extends AbstractModule {
    
    public enum Priority {
        CLOSEST,
        LOWEST_HEALTH,
        HIGHEST_HEALTH,
        CLOSEST_ANGLE,
        ARMOR
    }
    
    public enum RotationMode {
        NONE,
        SNAP,
        SMOOTH,
        SERVER_SIDE
    }
    
    @CategorySetting(name = "General")
    public final AbstractSetting<Float> range = new FloatSetting("Range", "Attack range", 4.5f, 1.0f, 6.0f);
    public final AbstractSetting<Float> wallRange = new FloatSetting("Wall Range", "Range through walls", 3.0f, 0.0f, 6.0f);
    public final AbstractSetting<Integer> delay = new IntegerSetting("Delay", "Attack delay (ticks)", 10, 0, 20);
    public final AbstractSetting<Boolean> onlyWeapon = new BooleanSetting("Only Weapon", "Only attack with weapons", false);
    public final AbstractSetting<Boolean> pauseOnEat = new BooleanSetting("Pause on Eat", "Don't attack while eating", true);
    public final AbstractSetting<Boolean> pauseOnMine = new BooleanSetting("Pause on Mine", "Don't attack while mining", false);
    
    @CategorySetting(name = "Targeting")
    public final AbstractSetting<Priority> priority = new EnumSetting<>("Priority", "Target priority", Priority.CLOSEST_ANGLE);
    public final AbstractSetting<Boolean> players = new BooleanSetting("Players", "Attack players", true);
    public final AbstractSetting<Boolean> mobs = new BooleanSetting("Mobs", "Attack mobs", false);
    public final AbstractSetting<Boolean> animals = new BooleanSetting("Animals", "Attack animals", false);
    public final AbstractSetting<Boolean> friends = new BooleanSetting("Friends", "Attack friends", false);
    public final AbstractSetting<Boolean> invisible = new BooleanSetting("Invisible", "Attack invisible entities", false);
    public final AbstractSetting<Boolean> naked = new BooleanSetting("Naked", "Attack naked players", true);
    
    @CategorySetting(name = "Rotation")
    public final AbstractSetting<RotationMode> rotation = new EnumSetting<>("Rotation", "Rotation mode", RotationMode.SMOOTH);
    public final AbstractSetting<Float> rotationSpeed = new FloatSetting("Rotation Speed", "Rotation speed", 20.0f, 1.0f, 180.0f)
            .showIf(() -> rotation.getValue() == RotationMode.SMOOTH);
    
    @CategorySetting(name = "Combat")
    public final AbstractSetting<Boolean> critOnly = new BooleanSetting("Crit Only", "Only attack when you can deal critical hits", false);
    public final AbstractSetting<Boolean> ignoreWalls = new BooleanSetting("Ignore Walls", "Ignore wall check for crits", false)
            .showIf(critOnly::getValue);
    
    @CategorySetting(name = "Misc")
    public final AbstractSetting<Boolean> multiAura = new BooleanSetting("Multi Aura", "Attack multiple targets", false);
    public final AbstractSetting<Integer> maxTargets = new IntegerSetting("Max Targets", "Max targets for multi aura", 3, 1, 10)
            .showIf(multiAura::getValue);
    public final AbstractSetting<Boolean> autoSwitch = new BooleanSetting("Auto Switch", "Switch to weapon", false);
    public final AbstractSetting<Boolean> swingClient = new BooleanSetting("Swing Client", "Client-side swing", true);
    
    private Entity currentTarget = null;
    private int ticksSinceAttack = 0;
    
    @Override
    public String getDescription() {
        return "Automatically attacks nearby entities";
    }
    
    @Override
    public void onActivate(boolean live) {
        currentTarget = null;
        ticksSinceAttack = 0;
    }
    
    @EventHandler
    private void onTick(TickEvent event) {
        if (mc.player == null || mc.world == null) return;
        
        if (pauseOnEat.getValue() && mc.player.isUsingItem()) return;
        if (pauseOnMine.getValue() && mc.interactionManager.isBreakingBlock()) return;
        
        // Check if we can crit if crit-only is enabled
        if (critOnly.getValue() && !canCrit()) {
            return;
        }
        
        if (onlyWeapon.getValue() && !isHoldingWeapon()) {
            if (autoSwitch.getValue()) {
                switchToWeapon();
            } else {
                return;
            }
        }
        
        List<Entity> targets = getTargets();
        if (targets.isEmpty()) {
            currentTarget = null;
            return;
        }
        
        if (multiAura.getValue()) {
            int attacked = 0;
            for (Entity target : targets) {
                if (attacked >= maxTargets.getValue()) break;
                if (attack(target)) attacked++;
            }
        } else {
            currentTarget = targets.get(0);
            attack(currentTarget);
        }
    }
    
    /**
     * Checks if the player can perform a critical hit
     * Critical hits occur when:
     * - Player is falling (negative Y velocity)
     * - Player is not sprinting
     * - Player is not in water/lava
     * - Player is not on a ladder
     * - Player is not riding an entity
     * - Player is not blind
     */
    private boolean canCrit() {
        if (mc.player == null) return false;
        
        // Must be falling
        if (mc.player.getVelocity().y >= 0) return false;
        
        // Must not be sprinting
        if (mc.player.isSprinting()) return false;
        
        // Must not be in liquid (water/lava)
        if (mc.player.isTouchingWater() || mc.player.isInLava()) return false;
        
        // Must not be on a ladder
        if (mc.player.isClimbing()) return false;
        
        // Must not be riding an entity
        if (mc.player.hasVehicle()) return false;
        
        // Must not have blindness effect
        if (mc.player.hasStatusEffect(net.minecraft.entity.effect.StatusEffects.BLINDNESS)) return false;
        
        // If ignoreWalls is disabled, check if player is on ground
        if (!ignoreWalls.getValue() && mc.player.isOnGround()) return false;
        
        return true;
    }
    
    private List<Entity> getTargets() {
        List<Entity> targets = new ArrayList<>();
        
        for (Entity entity : mc.world.getEntities()) {
            if (!isValidTarget(entity)) continue;
            
            double distance = mc.player.distanceTo(entity);
            boolean canSee = mc.player.canSee(entity);
            
            if (canSee && distance > range.getValue()) continue;
            if (!canSee && distance > wallRange.getValue()) continue;
            
            targets.add(entity);
        }
        
        targets.sort(getComparator());
        return targets;
    }
    
    private boolean isValidTarget(Entity entity) {
        if (entity == mc.player) return false;
        if (!(entity instanceof LivingEntity)) return false;
        if (entity.isRemoved() || !entity.isAlive()) return false;
        
        LivingEntity living = (LivingEntity) entity;
        if (living.getHealth() <= 0) return false;
        if (!invisible.getValue() && entity.isInvisible()) return false;
        
        if (entity instanceof PlayerEntity) {
            if (!players.getValue()) return false;
            if (!friends.getValue() && isFriend((PlayerEntity) entity)) return false;
            if (!naked.getValue() && isNaked((PlayerEntity) entity)) return false;
            return true;
        }
        
        if (entity instanceof Monster) return mobs.getValue();
        if (entity instanceof AnimalEntity) return animals.getValue();
        
        return false;
    }
    
    private boolean attack(Entity target) {
        if (ticksSinceAttack < delay.getValue()) {
            ticksSinceAttack++;
            return false;
        }
        
        if (rotation.getValue() != RotationMode.NONE) {
            rotateToTarget(target);
        }
        
        if (swingClient.getValue()) {
            mc.player.swingHand(Hand.MAIN_HAND);
        }
        
        mc.player.networkHandler.sendPacket(
            PlayerInteractEntityC2SPacket.attack(target, mc.player.isSneaking())
        );
        
        ticksSinceAttack = 0;
        return true;
    }
    
    private void rotateToTarget(Entity target) {
        float[] rotations = calculateAngle(mc.player.getEyePos(), target.getPos());
        float targetYaw = rotations[0];
        float targetPitch = rotations[1];
        
        switch (rotation.getValue()) {
            case SNAP:
                mc.player.setYaw(targetYaw);
                mc.player.setPitch(targetPitch);
                break;
                
            case SMOOTH:
                float yawDiff = targetYaw - mc.player.getYaw();
                float pitchDiff = targetPitch - mc.player.getPitch();
                
                while (yawDiff > 180) yawDiff -= 360;
                while (yawDiff < -180) yawDiff += 360;
                
                float speed = rotationSpeed.getValue();
                float yawChange = Math.max(-speed, Math.min(speed, yawDiff));
                float pitchChange = Math.max(-speed, Math.min(speed, pitchDiff));
                
                mc.player.setYaw(mc.player.getYaw() + yawChange);
                mc.player.setPitch(mc.player.getPitch() + pitchChange);
                break;
                
            case SERVER_SIDE:
            case NONE:
                break;
        }
    }
    
    private float[] calculateAngle(Vec3d from, Vec3d to) {
        double diffX = to.x - from.x;
        double diffY = to.y - from.y;
        double diffZ = to.z - from.z;
        double dist = Math.sqrt(diffX * diffX + diffZ * diffZ);
        
        float yaw = (float) (Math.atan2(diffZ, diffX) * 180.0 / Math.PI) - 90.0f;
        float pitch = (float) -(Math.atan2(diffY, dist) * 180.0 / Math.PI);
        
        return new float[]{yaw, pitch};
    }
    
    private Comparator<Entity> getComparator() {
        return (e1, e2) -> {
            switch (priority.getValue()) {
                case CLOSEST:
                    return Double.compare(mc.player.distanceTo(e1), mc.player.distanceTo(e2));
                    
                case LOWEST_HEALTH:
                    float h1 = e1 instanceof LivingEntity ? ((LivingEntity) e1).getHealth() : 0;
                    float h2 = e2 instanceof LivingEntity ? ((LivingEntity) e2).getHealth() : 0;
                    return Float.compare(h1, h2);
                    
                case HIGHEST_HEALTH:
                    h1 = e1 instanceof LivingEntity ? ((LivingEntity) e1).getHealth() : 0;
                    h2 = e2 instanceof LivingEntity ? ((LivingEntity) e2).getHealth() : 0;
                    return Float.compare(h2, h1);
                    
                case CLOSEST_ANGLE:
                    float[] rot1 = calculateAngle(mc.player.getEyePos(), e1.getPos());
                    float[] rot2 = calculateAngle(mc.player.getEyePos(), e2.getPos());
                    float angle1 = Math.abs(rot1[0] - mc.player.getYaw()) + Math.abs(rot1[1] - mc.player.getPitch());
                    float angle2 = Math.abs(rot2[0] - mc.player.getYaw()) + Math.abs(rot2[1] - mc.player.getPitch());
                    return Float.compare(angle1, angle2);
                    
                case ARMOR:
                    float armor1 = e1 instanceof LivingEntity ? ((LivingEntity) e1).getArmor() : 0;
                    float armor2 = e2 instanceof LivingEntity ? ((LivingEntity) e2).getArmor() : 0;
                    return Float.compare(armor1, armor2);
                    
                default:
                    return 0;
            }
        };
    }
    
    private boolean isFriend(PlayerEntity player) {
        return false;
    }
    
    private boolean isNaked(PlayerEntity player) {
        // Check armor slots (boots, leggings, chestplate, helmet = indices 0-3)
        for (int i = 0; i < 4; i++) {
            ItemStack stack = player.getInventory().getStack(36 + i); // Armor slots start at 36
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }
    
    private boolean isHoldingWeapon() {
        String item = mc.player.getMainHandStack().getItem().toString();
        return item.contains("sword") || item.contains("axe");
    }
    
    private void switchToWeapon() {
        for (int i = 0; i < 9; i++) {
            String item = mc.player.getInventory().getStack(i).getItem().toString();
            if (item.contains("sword") || item.contains("axe")) {
                InventoryUtils.selectSlot(i, false);
                return;
            }
        }
    }
}