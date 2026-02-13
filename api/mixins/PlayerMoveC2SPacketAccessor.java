/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package dev.anarchy.waifuhax.api.mixins;

import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerMoveC2SPacket.class)
public interface PlayerMoveC2SPacketAccessor {

    @Mutable
    @Accessor("x")
    void setX(double x);

    @Mutable
    @Accessor("y")
    void setY(double y);

    @Mutable
    @Accessor("z")
    void setZ(double z);

    @Mutable
    @Accessor("changePosition")
    void setChangePosition(boolean changePosition);

    @Mutable
    @Accessor("yaw")
    void setYaw(float yaw);

    @Mutable
    @Accessor("pitch")
    void setPitch(float pitch);

    @Mutable
    @Accessor("changeLook")
    void setChangeLook(boolean changeLook);

    @Mutable
    @Accessor("onGround")
    void setOnGround(boolean onGround);
}
