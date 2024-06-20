package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.arguments.coordinates.ArgumentPosition;
import net.minecraft.core.BlockPosition;
import net.minecraft.world.entity.EnumCreatureType;
import net.minecraft.world.level.SpawnerCreature;

public class DebugMobSpawningCommand {

    public DebugMobSpawningCommand() {}

    public static void register(CommandDispatcher<CommandListenerWrapper> commanddispatcher) {
        LiteralArgumentBuilder<CommandListenerWrapper> literalargumentbuilder = (LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("debugmobspawning").requires((commandlistenerwrapper) -> {
            return commandlistenerwrapper.hasPermission(2);
        });
        EnumCreatureType[] aenumcreaturetype = EnumCreatureType.values();
        int i = aenumcreaturetype.length;

        for (int j = 0; j < i; ++j) {
            EnumCreatureType enumcreaturetype = aenumcreaturetype[j];

            literalargumentbuilder.then(net.minecraft.commands.CommandDispatcher.literal(enumcreaturetype.getName()).then(net.minecraft.commands.CommandDispatcher.argument("at", ArgumentPosition.blockPos()).executes((commandcontext) -> {
                return spawnMobs((CommandListenerWrapper) commandcontext.getSource(), enumcreaturetype, ArgumentPosition.getLoadedBlockPos(commandcontext, "at"));
            })));
        }

        commanddispatcher.register(literalargumentbuilder);
    }

    private static int spawnMobs(CommandListenerWrapper commandlistenerwrapper, EnumCreatureType enumcreaturetype, BlockPosition blockposition) {
        SpawnerCreature.spawnCategoryForPosition(enumcreaturetype, commandlistenerwrapper.getLevel(), blockposition);
        return 1;
    }
}
