package dev.anarchy.waifuhax.client.events;
/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

import meteordevelopment.orbit.ICancellable;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;

public class PacketEvent {

    public static class Receive implements ICancellable {

        public Packet<?> packet;
        public ClientConnection connection;
        private boolean cancelled = false;

        public Receive(Packet<?> packet, ClientConnection connection) {
            this.setCancelled(false);
            this.packet = packet;
            this.connection = connection;
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public void setCancelled(boolean cancelled) {
            this.cancelled = cancelled;
        }
    }

    public static class Send implements ICancellable {

        public Packet<?> packet;
        public ClientConnection connection;
        private boolean cancelled = false;

        public Send(Packet<?> packet, ClientConnection connection) {
            this.setCancelled(false);
            this.packet = packet;
            this.connection = connection;
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public void setCancelled(boolean cancelled) {
            this.cancelled = cancelled;
        }
    }

    public static class Sent {

        public Packet<?> packet;
        public ClientConnection connection;

        public Sent(Packet<?> packet, ClientConnection connection) {
            this.packet = packet;
            this.connection = connection;
        }
    }
}