package net.minecraft.server.network.config;

import java.util.function.Consumer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.configuration.ClientboundFinishConfigurationPacket;
import net.minecraft.server.network.ConfigurationTask;

public class JoinWorldTask implements ConfigurationTask {

    public static final ConfigurationTask.a TYPE = new ConfigurationTask.a("join_world");

    public JoinWorldTask() {}

    @Override
    public void start(Consumer<Packet<?>> consumer) {
        consumer.accept(ClientboundFinishConfigurationPacket.INSTANCE);
    }

    @Override
    public ConfigurationTask.a type() {
        return JoinWorldTask.TYPE;
    }
}
