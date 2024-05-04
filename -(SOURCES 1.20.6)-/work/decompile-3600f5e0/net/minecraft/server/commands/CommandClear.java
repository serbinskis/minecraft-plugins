package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.function.Predicate;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.arguments.ArgumentEntity;
import net.minecraft.commands.arguments.item.ArgumentItemPredicate;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.item.ItemStack;

public class CommandClear {

    private static final DynamicCommandExceptionType ERROR_SINGLE = new DynamicCommandExceptionType((object) -> {
        return IChatBaseComponent.translatableEscape("clear.failed.single", object);
    });
    private static final DynamicCommandExceptionType ERROR_MULTIPLE = new DynamicCommandExceptionType((object) -> {
        return IChatBaseComponent.translatableEscape("clear.failed.multiple", object);
    });

    public CommandClear() {}

    public static void register(CommandDispatcher<CommandListenerWrapper> commanddispatcher, CommandBuildContext commandbuildcontext) {
        commanddispatcher.register((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("clear").requires((commandlistenerwrapper) -> {
            return commandlistenerwrapper.hasPermission(2);
        })).executes((commandcontext) -> {
            return clearUnlimited((CommandListenerWrapper) commandcontext.getSource(), Collections.singleton(((CommandListenerWrapper) commandcontext.getSource()).getPlayerOrException()), (itemstack) -> {
                return true;
            });
        })).then(((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("targets", ArgumentEntity.players()).executes((commandcontext) -> {
            return clearUnlimited((CommandListenerWrapper) commandcontext.getSource(), ArgumentEntity.getPlayers(commandcontext, "targets"), (itemstack) -> {
                return true;
            });
        })).then(((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("item", ArgumentItemPredicate.itemPredicate(commandbuildcontext)).executes((commandcontext) -> {
            return clearUnlimited((CommandListenerWrapper) commandcontext.getSource(), ArgumentEntity.getPlayers(commandcontext, "targets"), ArgumentItemPredicate.getItemPredicate(commandcontext, "item"));
        })).then(net.minecraft.commands.CommandDispatcher.argument("maxCount", IntegerArgumentType.integer(0)).executes((commandcontext) -> {
            return clearInventory((CommandListenerWrapper) commandcontext.getSource(), ArgumentEntity.getPlayers(commandcontext, "targets"), ArgumentItemPredicate.getItemPredicate(commandcontext, "item"), IntegerArgumentType.getInteger(commandcontext, "maxCount"));
        })))));
    }

    private static int clearUnlimited(CommandListenerWrapper commandlistenerwrapper, Collection<EntityPlayer> collection, Predicate<ItemStack> predicate) throws CommandSyntaxException {
        return clearInventory(commandlistenerwrapper, collection, predicate, -1);
    }

    private static int clearInventory(CommandListenerWrapper commandlistenerwrapper, Collection<EntityPlayer> collection, Predicate<ItemStack> predicate, int i) throws CommandSyntaxException {
        int j = 0;
        Iterator iterator = collection.iterator();

        while (iterator.hasNext()) {
            EntityPlayer entityplayer = (EntityPlayer) iterator.next();

            j += entityplayer.getInventory().clearOrCountMatchingItems(predicate, i, entityplayer.inventoryMenu.getCraftSlots());
            entityplayer.containerMenu.broadcastChanges();
            entityplayer.inventoryMenu.slotsChanged(entityplayer.getInventory());
        }

        if (j == 0) {
            if (collection.size() == 1) {
                throw CommandClear.ERROR_SINGLE.create(((EntityPlayer) collection.iterator().next()).getName());
            } else {
                throw CommandClear.ERROR_MULTIPLE.create(collection.size());
            }
        } else {
            if (i == 0) {
                if (collection.size() == 1) {
                    commandlistenerwrapper.sendSuccess(() -> {
                        return IChatBaseComponent.translatable("commands.clear.test.single", j, ((EntityPlayer) collection.iterator().next()).getDisplayName());
                    }, true);
                } else {
                    commandlistenerwrapper.sendSuccess(() -> {
                        return IChatBaseComponent.translatable("commands.clear.test.multiple", j, collection.size());
                    }, true);
                }
            } else if (collection.size() == 1) {
                commandlistenerwrapper.sendSuccess(() -> {
                    return IChatBaseComponent.translatable("commands.clear.success.single", j, ((EntityPlayer) collection.iterator().next()).getDisplayName());
                }, true);
            } else {
                commandlistenerwrapper.sendSuccess(() -> {
                    return IChatBaseComponent.translatable("commands.clear.success.multiple", j, collection.size());
                }, true);
            }

            return j;
        }
    }
}
