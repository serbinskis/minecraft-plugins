package net.minecraft.server.network.config;

import java.util.function.Consumer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundResourcePackPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ConfigurationTask;

public class ServerResourcePackConfigurationTask implements ConfigurationTask {

    public static final ConfigurationTask.a TYPE = new ConfigurationTask.a("server_resource_pack");
    private final MinecraftServer.ServerResourcePackInfo info;

    public ServerResourcePackConfigurationTask(MinecraftServer.ServerResourcePackInfo minecraftserver_serverresourcepackinfo) {
        this.info = minecraftserver_serverresourcepackinfo;
    }

    @Override
    public void start(Consumer<Packet<?>> consumer) {
        consumer.accept(new ClientboundResourcePackPacket(this.info.url(), this.info.hash(), this.info.isRequired(), this.info.prompt()));
    }

    @Override
    public ConfigurationTask.a type() {
        return ServerResourcePackConfigurationTask.TYPE;
    }
}
