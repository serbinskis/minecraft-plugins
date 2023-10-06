package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.List;
import java.util.function.Function;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.network.chat.ChatComponentUtils;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.EntityHuman;

public class CommandList {

    public CommandList() {}

    public static void register(CommandDispatcher<CommandListenerWrapper> commanddispatcher) {
        commanddispatcher.register((LiteralArgumentBuilder) ((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("list").executes((commandcontext) -> {
            return listPlayers((CommandListenerWrapper) commandcontext.getSource());
        })).then(net.minecraft.commands.CommandDispatcher.literal("uuids").executes((commandcontext) -> {
            return listPlayersWithUuids((CommandListenerWrapper) commandcontext.getSource());
        })));
    }

    private static int listPlayers(CommandListenerWrapper commandlistenerwrapper) {
        return format(commandlistenerwrapper, EntityHuman::getDisplayName);
    }

    private static int listPlayersWithUuids(CommandListenerWrapper commandlistenerwrapper) {
        return format(commandlistenerwrapper, (entityplayer) -> {
            return IChatBaseComponent.translatable("commands.list.nameAndId", entityplayer.getName(), entityplayer.getGameProfile().getId());
        });
    }

    private static int format(CommandListenerWrapper commandlistenerwrapper, Function<EntityPlayer, IChatBaseComponent> function) {
        PlayerList playerlist = commandlistenerwrapper.getServer().getPlayerList();
        // CraftBukkit start
        List<EntityPlayer> players = playerlist.getPlayers();
        if (commandlistenerwrapper.getBukkitSender() instanceof org.bukkit.entity.Player) {
            org.bukkit.entity.Player sender = (org.bukkit.entity.Player) commandlistenerwrapper.getBukkitSender();
            players = players.stream().filter((ep) -> sender.canSee(ep.getBukkitEntity())).collect(java.util.stream.Collectors.toList());
        }
        List<EntityPlayer> list = players;
        // CraftBukkit end
        IChatBaseComponent ichatbasecomponent = ChatComponentUtils.formatList(list, function);

        commandlistenerwrapper.sendSuccess(() -> {
            return IChatBaseComponent.translatable("commands.list.players", list.size(), playerlist.getMaxPlayers(), ichatbasecomponent);
        }, false);
        return list.size();
    }
}
