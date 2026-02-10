package dev.anarchy.waifuhax.client.systems.modules.movement;

import meteordevelopment.orbit.EventHandler;
import dev.anarchy.waifuhax.client.events.TickEvent;
import dev.anarchy.waifuhax.api.settings.BooleanSetting;
import dev.anarchy.waifuhax.api.settings.FloatSetting;
import dev.anarchy.waifuhax.api.systems.modules.AbstractModule;
import net.minecraft.util.math.Vec3d;

public class Fly extends AbstractModule {

    public final FloatSetting speed = new FloatSetting("Speed", "The fly speed.", 0.1f, 0.01f, 1.0f);
    public final BooleanSetting antiKick = new BooleanSetting("AntiKick", "Prevent kick for flying.", true);

    @Override
    public String getDescription() {
        return "Fly module for servers with no anticheat";
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (mc.player == null) return;

        // Start with zero velocity
        Vec3d velocity = Vec3d.ZERO;

        // Horizontal movement
        float yaw = mc.player.getYaw();
        float forward = mc.player.input.movementForward;
        float sideways = mc.player.input.movementSideways;
        if (forward != 0 || sideways != 0) {
            double dir = Math.sqrt(forward * forward + sideways * sideways);
            double angle = Math.toRadians(yaw) + Math.atan2(sideways, forward) - Math.PI / 2;
            double vx = Math.cos(angle) * dir * speed.getValue();
            double vz = Math.sin(angle) * dir * speed.getValue();
            velocity = new Vec3d(vx, 0, vz);
        }

        // Vertical movement
        if (mc.player.input.jumping) {
            velocity = velocity.add(0, speed.getValue(), 0);
        }
        if (mc.player.input.sneaking) {
            velocity = velocity.add(0, -speed.getValue(), 0);
        }

        // Anti-kick
        if (antiKick.getValue() && !mc.player.input.jumping && !mc.player.input.sneaking) {
            velocity = velocity.add(0, -0.031, 0);
        }

        mc.player.setVelocity(velocity);
    }
}