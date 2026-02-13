package dev.anarchy.waifuhax.client.systems.modules.movement;

import dev.anarchy.waifuhax.api.WHLogger;
import dev.anarchy.waifuhax.api.settings.FloatSetting;
import dev.anarchy.waifuhax.api.systems.modules.AbstractModule;
import dev.anarchy.waifuhax.api.util.CombatUtils;
import dev.anarchy.waifuhax.api.util.TpUtils;
import dev.anarchy.waifuhax.client.events.TickEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public class Follow extends AbstractModule {

    public FloatSetting maxDistanceSetting = new FloatSetting("TargetDistance", "Target distance.", 100f, 0, 200);
    public FloatSetting yOffsetSetting = new FloatSetting("YOffset", "Y offset to the target.", 0f, -6, 6);
    PlayerEntity target = null;

    @Override
    public void onActivate(boolean live) {
        if (mc.player == null) return;
        target = CombatUtils.GetPlayerTarget(CombatUtils::ClosestCrosshair, null, maxDistanceSetting.getValue(), false, false);
        if (target == null) {
            WHLogger.printToChat("Target not found.");
            toggle();
        }
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (target == null) return;
        TpUtils.Tp(target.getPos().add(0, yOffsetSetting.getValue(), 0));
        mc.player.setVelocity(Vec3d.ZERO);
    }

    @Override
    public String getDescription() {
        return "Also known as teleport aura";
    }
}
