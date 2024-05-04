package net.minecraft.server.network;

import java.util.function.Consumer;
import net.minecraft.network.protocol.Packet;

public interface ConfigurationTask {

    void start(Consumer<Packet<?>> consumer);

    ConfigurationTask.a type();

    public static record a(String id) {

        public String toString() {
            return this.id;
        }
    }
}
