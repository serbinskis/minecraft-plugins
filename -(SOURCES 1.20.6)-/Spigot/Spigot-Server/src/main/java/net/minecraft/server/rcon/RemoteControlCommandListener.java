package net.minecraft.server.rcon;

import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.ICommandListener;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2F;
import net.minecraft.world.phys.Vec3D;

// CraftBukkit start
import java.net.SocketAddress;
import org.bukkit.craftbukkit.command.CraftRemoteConsoleCommandSender;
// CraftBukkit end
public class RemoteControlCommandListener implements ICommandListener {

    private static final String RCON = "Rcon";
    private static final IChatBaseComponent RCON_COMPONENT = IChatBaseComponent.literal("Rcon");
    private final StringBuffer buffer = new StringBuffer();
    private final MinecraftServer server;
    // CraftBukkit start
    public final SocketAddress socketAddress;
    private final CraftRemoteConsoleCommandSender remoteConsole = new CraftRemoteConsoleCommandSender(this);

    public RemoteControlCommandListener(MinecraftServer minecraftserver, SocketAddress socketAddress) {
        this.socketAddress = socketAddress;
        // CraftBukkit end
        this.server = minecraftserver;
    }

    public void prepareForCommand() {
        this.buffer.setLength(0);
    }

    public String getCommandResponse() {
        return this.buffer.toString();
    }

    public CommandListenerWrapper createCommandSourceStack() {
        WorldServer worldserver = this.server.overworld();

        return new CommandListenerWrapper(this, Vec3D.atLowerCornerOf(worldserver.getSharedSpawnPos()), Vec2F.ZERO, worldserver, 4, "Rcon", RemoteControlCommandListener.RCON_COMPONENT, this.server, (Entity) null);
    }

    // CraftBukkit start - Send a String
    public void sendMessage(String message) {
        this.buffer.append(message);
    }

    @Override
    public org.bukkit.command.CommandSender getBukkitSender(CommandListenerWrapper wrapper) {
        return this.remoteConsole;
    }
    // CraftBukkit end

    @Override
    public void sendSystemMessage(IChatBaseComponent ichatbasecomponent) {
        this.buffer.append(ichatbasecomponent.getString());
    }

    @Override
    public boolean acceptsSuccess() {
        return true;
    }

    @Override
    public boolean acceptsFailure() {
        return true;
    }

    @Override
    public boolean shouldInformAdmins() {
        return this.server.shouldRconBroadcast();
    }
}
