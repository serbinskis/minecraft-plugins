package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.AdvancementTree;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.ICompletionProvider;
import net.minecraft.commands.arguments.ArgumentEntity;
import net.minecraft.commands.arguments.ArgumentMinecraftKeyRegistered;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.server.level.EntityPlayer;

public class CommandAdvancement {

    private static final DynamicCommandExceptionType ERROR_NO_ACTION_PERFORMED = new DynamicCommandExceptionType((object) -> {
        return (IChatBaseComponent) object;
    });
    private static final Dynamic2CommandExceptionType ERROR_CRITERION_NOT_FOUND = new Dynamic2CommandExceptionType((object, object1) -> {
        return IChatBaseComponent.translatable("commands.advancement.criterionNotFound", object, object1);
    });
    private static final SuggestionProvider<CommandListenerWrapper> SUGGEST_ADVANCEMENTS = (commandcontext, suggestionsbuilder) -> {
        Collection<AdvancementHolder> collection = ((CommandListenerWrapper) commandcontext.getSource()).getServer().getAdvancements().getAllAdvancements();

        return ICompletionProvider.suggestResource(collection.stream().map(AdvancementHolder::id), suggestionsbuilder);
    };

    public CommandAdvancement() {}

    public static void register(CommandDispatcher<CommandListenerWrapper> commanddispatcher) {
        commanddispatcher.register((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("advancement").requires((commandlistenerwrapper) -> {
            return commandlistenerwrapper.hasPermission(2);
        })).then(net.minecraft.commands.CommandDispatcher.literal("grant").then(((RequiredArgumentBuilder) ((RequiredArgumentBuilder) ((RequiredArgumentBuilder) ((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("targets", ArgumentEntity.players()).then(net.minecraft.commands.CommandDispatcher.literal("only").then(((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("advancement", ArgumentMinecraftKeyRegistered.id()).suggests(CommandAdvancement.SUGGEST_ADVANCEMENTS).executes((commandcontext) -> {
            return perform((CommandListenerWrapper) commandcontext.getSource(), ArgumentEntity.getPlayers(commandcontext, "targets"), CommandAdvancement.Action.GRANT, getAdvancements(commandcontext, ArgumentMinecraftKeyRegistered.getAdvancement(commandcontext, "advancement"), CommandAdvancement.Filter.ONLY));
        })).then(net.minecraft.commands.CommandDispatcher.argument("criterion", StringArgumentType.greedyString()).suggests((commandcontext, suggestionsbuilder) -> {
            return ICompletionProvider.suggest((Iterable) ArgumentMinecraftKeyRegistered.getAdvancement(commandcontext, "advancement").value().criteria().keySet(), suggestionsbuilder);
        }).executes((commandcontext) -> {
            return performCriterion((CommandListenerWrapper) commandcontext.getSource(), ArgumentEntity.getPlayers(commandcontext, "targets"), CommandAdvancement.Action.GRANT, ArgumentMinecraftKeyRegistered.getAdvancement(commandcontext, "advancement"), StringArgumentType.getString(commandcontext, "criterion"));
        }))))).then(net.minecraft.commands.CommandDispatcher.literal("from").then(net.minecraft.commands.CommandDispatcher.argument("advancement", ArgumentMinecraftKeyRegistered.id()).suggests(CommandAdvancement.SUGGEST_ADVANCEMENTS).executes((commandcontext) -> {
            return perform((CommandListenerWrapper) commandcontext.getSource(), ArgumentEntity.getPlayers(commandcontext, "targets"), CommandAdvancement.Action.GRANT, getAdvancements(commandcontext, ArgumentMinecraftKeyRegistered.getAdvancement(commandcontext, "advancement"), CommandAdvancement.Filter.FROM));
        })))).then(net.minecraft.commands.CommandDispatcher.literal("until").then(net.minecraft.commands.CommandDispatcher.argument("advancement", ArgumentMinecraftKeyRegistered.id()).suggests(CommandAdvancement.SUGGEST_ADVANCEMENTS).executes((commandcontext) -> {
            return perform((CommandListenerWrapper) commandcontext.getSource(), ArgumentEntity.getPlayers(commandcontext, "targets"), CommandAdvancement.Action.GRANT, getAdvancements(commandcontext, ArgumentMinecraftKeyRegistered.getAdvancement(commandcontext, "advancement"), CommandAdvancement.Filter.UNTIL));
        })))).then(net.minecraft.commands.CommandDispatcher.literal("through").then(net.minecraft.commands.CommandDispatcher.argument("advancement", ArgumentMinecraftKeyRegistered.id()).suggests(CommandAdvancement.SUGGEST_ADVANCEMENTS).executes((commandcontext) -> {
            return perform((CommandListenerWrapper) commandcontext.getSource(), ArgumentEntity.getPlayers(commandcontext, "targets"), CommandAdvancement.Action.GRANT, getAdvancements(commandcontext, ArgumentMinecraftKeyRegistered.getAdvancement(commandcontext, "advancement"), CommandAdvancement.Filter.THROUGH));
        })))).then(net.minecraft.commands.CommandDispatcher.literal("everything").executes((commandcontext) -> {
            return perform((CommandListenerWrapper) commandcontext.getSource(), ArgumentEntity.getPlayers(commandcontext, "targets"), CommandAdvancement.Action.GRANT, ((CommandListenerWrapper) commandcontext.getSource()).getServer().getAdvancements().getAllAdvancements());
        }))))).then(net.minecraft.commands.CommandDispatcher.literal("revoke").then(((RequiredArgumentBuilder) ((RequiredArgumentBuilder) ((RequiredArgumentBuilder) ((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("targets", ArgumentEntity.players()).then(net.minecraft.commands.CommandDispatcher.literal("only").then(((RequiredArgumentBuilder) net.minecraft.commands.CommandDispatcher.argument("advancement", ArgumentMinecraftKeyRegistered.id()).suggests(CommandAdvancement.SUGGEST_ADVANCEMENTS).executes((commandcontext) -> {
            return perform((CommandListenerWrapper) commandcontext.getSource(), ArgumentEntity.getPlayers(commandcontext, "targets"), CommandAdvancement.Action.REVOKE, getAdvancements(commandcontext, ArgumentMinecraftKeyRegistered.getAdvancement(commandcontext, "advancement"), CommandAdvancement.Filter.ONLY));
        })).then(net.minecraft.commands.CommandDispatcher.argument("criterion", StringArgumentType.greedyString()).suggests((commandcontext, suggestionsbuilder) -> {
            return ICompletionProvider.suggest((Iterable) ArgumentMinecraftKeyRegistered.getAdvancement(commandcontext, "advancement").value().criteria().keySet(), suggestionsbuilder);
        }).executes((commandcontext) -> {
            return performCriterion((CommandListenerWrapper) commandcontext.getSource(), ArgumentEntity.getPlayers(commandcontext, "targets"), CommandAdvancement.Action.REVOKE, ArgumentMinecraftKeyRegistered.getAdvancement(commandcontext, "advancement"), StringArgumentType.getString(commandcontext, "criterion"));
        }))))).then(net.minecraft.commands.CommandDispatcher.literal("from").then(net.minecraft.commands.CommandDispatcher.argument("advancement", ArgumentMinecraftKeyRegistered.id()).suggests(CommandAdvancement.SUGGEST_ADVANCEMENTS).executes((commandcontext) -> {
            return perform((CommandListenerWrapper) commandcontext.getSource(), ArgumentEntity.getPlayers(commandcontext, "targets"), CommandAdvancement.Action.REVOKE, getAdvancements(commandcontext, ArgumentMinecraftKeyRegistered.getAdvancement(commandcontext, "advancement"), CommandAdvancement.Filter.FROM));
        })))).then(net.minecraft.commands.CommandDispatcher.literal("until").then(net.minecraft.commands.CommandDispatcher.argument("advancement", ArgumentMinecraftKeyRegistered.id()).suggests(CommandAdvancement.SUGGEST_ADVANCEMENTS).executes((commandcontext) -> {
            return perform((CommandListenerWrapper) commandcontext.getSource(), ArgumentEntity.getPlayers(commandcontext, "targets"), CommandAdvancement.Action.REVOKE, getAdvancements(commandcontext, ArgumentMinecraftKeyRegistered.getAdvancement(commandcontext, "advancement"), CommandAdvancement.Filter.UNTIL));
        })))).then(net.minecraft.commands.CommandDispatcher.literal("through").then(net.minecraft.commands.CommandDispatcher.argument("advancement", ArgumentMinecraftKeyRegistered.id()).suggests(CommandAdvancement.SUGGEST_ADVANCEMENTS).executes((commandcontext) -> {
            return perform((CommandListenerWrapper) commandcontext.getSource(), ArgumentEntity.getPlayers(commandcontext, "targets"), CommandAdvancement.Action.REVOKE, getAdvancements(commandcontext, ArgumentMinecraftKeyRegistered.getAdvancement(commandcontext, "advancement"), CommandAdvancement.Filter.THROUGH));
        })))).then(net.minecraft.commands.CommandDispatcher.literal("everything").executes((commandcontext) -> {
            return perform((CommandListenerWrapper) commandcontext.getSource(), ArgumentEntity.getPlayers(commandcontext, "targets"), CommandAdvancement.Action.REVOKE, ((CommandListenerWrapper) commandcontext.getSource()).getServer().getAdvancements().getAllAdvancements());
        })))));
    }

    private static int perform(CommandListenerWrapper commandlistenerwrapper, Collection<EntityPlayer> collection, CommandAdvancement.Action commandadvancement_action, Collection<AdvancementHolder> collection1) throws CommandSyntaxException {
        int i = 0;

        EntityPlayer entityplayer;

        for (Iterator iterator = collection.iterator(); iterator.hasNext(); i += commandadvancement_action.perform(entityplayer, (Iterable) collection1)) {
            entityplayer = (EntityPlayer) iterator.next();
        }

        if (i == 0) {
            if (collection1.size() == 1) {
                if (collection.size() == 1) {
                    throw CommandAdvancement.ERROR_NO_ACTION_PERFORMED.create(IChatBaseComponent.translatable(commandadvancement_action.getKey() + ".one.to.one.failure", Advancement.name((AdvancementHolder) collection1.iterator().next()), ((EntityPlayer) collection.iterator().next()).getDisplayName()));
                } else {
                    throw CommandAdvancement.ERROR_NO_ACTION_PERFORMED.create(IChatBaseComponent.translatable(commandadvancement_action.getKey() + ".one.to.many.failure", Advancement.name((AdvancementHolder) collection1.iterator().next()), collection.size()));
                }
            } else if (collection.size() == 1) {
                throw CommandAdvancement.ERROR_NO_ACTION_PERFORMED.create(IChatBaseComponent.translatable(commandadvancement_action.getKey() + ".many.to.one.failure", collection1.size(), ((EntityPlayer) collection.iterator().next()).getDisplayName()));
            } else {
                throw CommandAdvancement.ERROR_NO_ACTION_PERFORMED.create(IChatBaseComponent.translatable(commandadvancement_action.getKey() + ".many.to.many.failure", collection1.size(), collection.size()));
            }
        } else {
            if (collection1.size() == 1) {
                if (collection.size() == 1) {
                    commandlistenerwrapper.sendSuccess(() -> {
                        return IChatBaseComponent.translatable(commandadvancement_action.getKey() + ".one.to.one.success", Advancement.name((AdvancementHolder) collection1.iterator().next()), ((EntityPlayer) collection.iterator().next()).getDisplayName());
                    }, true);
                } else {
                    commandlistenerwrapper.sendSuccess(() -> {
                        return IChatBaseComponent.translatable(commandadvancement_action.getKey() + ".one.to.many.success", Advancement.name((AdvancementHolder) collection1.iterator().next()), collection.size());
                    }, true);
                }
            } else if (collection.size() == 1) {
                commandlistenerwrapper.sendSuccess(() -> {
                    return IChatBaseComponent.translatable(commandadvancement_action.getKey() + ".many.to.one.success", collection1.size(), ((EntityPlayer) collection.iterator().next()).getDisplayName());
                }, true);
            } else {
                commandlistenerwrapper.sendSuccess(() -> {
                    return IChatBaseComponent.translatable(commandadvancement_action.getKey() + ".many.to.many.success", collection1.size(), collection.size());
                }, true);
            }

            return i;
        }
    }

    private static int performCriterion(CommandListenerWrapper commandlistenerwrapper, Collection<EntityPlayer> collection, CommandAdvancement.Action commandadvancement_action, AdvancementHolder advancementholder, String s) throws CommandSyntaxException {
        int i = 0;
        Advancement advancement = advancementholder.value();

        if (!advancement.criteria().containsKey(s)) {
            throw CommandAdvancement.ERROR_CRITERION_NOT_FOUND.create(Advancement.name(advancementholder), s);
        } else {
            Iterator iterator = collection.iterator();

            while (iterator.hasNext()) {
                EntityPlayer entityplayer = (EntityPlayer) iterator.next();

                if (commandadvancement_action.performCriterion(entityplayer, advancementholder, s)) {
                    ++i;
                }
            }

            if (i == 0) {
                if (collection.size() == 1) {
                    throw CommandAdvancement.ERROR_NO_ACTION_PERFORMED.create(IChatBaseComponent.translatable(commandadvancement_action.getKey() + ".criterion.to.one.failure", s, Advancement.name(advancementholder), ((EntityPlayer) collection.iterator().next()).getDisplayName()));
                } else {
                    throw CommandAdvancement.ERROR_NO_ACTION_PERFORMED.create(IChatBaseComponent.translatable(commandadvancement_action.getKey() + ".criterion.to.many.failure", s, Advancement.name(advancementholder), collection.size()));
                }
            } else {
                if (collection.size() == 1) {
                    commandlistenerwrapper.sendSuccess(() -> {
                        return IChatBaseComponent.translatable(commandadvancement_action.getKey() + ".criterion.to.one.success", s, Advancement.name(advancementholder), ((EntityPlayer) collection.iterator().next()).getDisplayName());
                    }, true);
                } else {
                    commandlistenerwrapper.sendSuccess(() -> {
                        return IChatBaseComponent.translatable(commandadvancement_action.getKey() + ".criterion.to.many.success", s, Advancement.name(advancementholder), collection.size());
                    }, true);
                }

                return i;
            }
        }
    }

    private static List<AdvancementHolder> getAdvancements(CommandContext<CommandListenerWrapper> commandcontext, AdvancementHolder advancementholder, CommandAdvancement.Filter commandadvancement_filter) {
        AdvancementTree advancementtree = ((CommandListenerWrapper) commandcontext.getSource()).getServer().getAdvancements().tree();
        AdvancementNode advancementnode = advancementtree.get(advancementholder);

        if (advancementnode == null) {
            return List.of(advancementholder);
        } else {
            List<AdvancementHolder> list = new ArrayList();

            if (commandadvancement_filter.parents) {
                for (AdvancementNode advancementnode1 = advancementnode.parent(); advancementnode1 != null; advancementnode1 = advancementnode1.parent()) {
                    list.add(advancementnode1.holder());
                }
            }

            list.add(advancementholder);
            if (commandadvancement_filter.children) {
                addChildren(advancementnode, list);
            }

            return list;
        }
    }

    private static void addChildren(AdvancementNode advancementnode, List<AdvancementHolder> list) {
        Iterator iterator = advancementnode.children().iterator();

        while (iterator.hasNext()) {
            AdvancementNode advancementnode1 = (AdvancementNode) iterator.next();

            list.add(advancementnode1.holder());
            addChildren(advancementnode1, list);
        }

    }

    private static enum Action {

        GRANT("grant") {
            @Override
            protected boolean perform(EntityPlayer entityplayer, AdvancementHolder advancementholder) {
                AdvancementProgress advancementprogress = entityplayer.getAdvancements().getOrStartProgress(advancementholder);

                if (advancementprogress.isDone()) {
                    return false;
                } else {
                    Iterator iterator = advancementprogress.getRemainingCriteria().iterator();

                    while (iterator.hasNext()) {
                        String s = (String) iterator.next();

                        entityplayer.getAdvancements().award(advancementholder, s);
                    }

                    return true;
                }
            }

            @Override
            protected boolean performCriterion(EntityPlayer entityplayer, AdvancementHolder advancementholder, String s) {
                return entityplayer.getAdvancements().award(advancementholder, s);
            }
        },
        REVOKE("revoke") {
            @Override
            protected boolean perform(EntityPlayer entityplayer, AdvancementHolder advancementholder) {
                AdvancementProgress advancementprogress = entityplayer.getAdvancements().getOrStartProgress(advancementholder);

                if (!advancementprogress.hasProgress()) {
                    return false;
                } else {
                    Iterator iterator = advancementprogress.getCompletedCriteria().iterator();

                    while (iterator.hasNext()) {
                        String s = (String) iterator.next();

                        entityplayer.getAdvancements().revoke(advancementholder, s);
                    }

                    return true;
                }
            }

            @Override
            protected boolean performCriterion(EntityPlayer entityplayer, AdvancementHolder advancementholder, String s) {
                return entityplayer.getAdvancements().revoke(advancementholder, s);
            }
        };

        private final String key;

        Action(final String s) {
            this.key = "commands.advancement." + s;
        }

        public int perform(EntityPlayer entityplayer, Iterable<AdvancementHolder> iterable) {
            int i = 0;
            Iterator iterator = iterable.iterator();

            while (iterator.hasNext()) {
                AdvancementHolder advancementholder = (AdvancementHolder) iterator.next();

                if (this.perform(entityplayer, advancementholder)) {
                    ++i;
                }
            }

            return i;
        }

        protected abstract boolean perform(EntityPlayer entityplayer, AdvancementHolder advancementholder);

        protected abstract boolean performCriterion(EntityPlayer entityplayer, AdvancementHolder advancementholder, String s);

        protected String getKey() {
            return this.key;
        }
    }

    private static enum Filter {

        ONLY(false, false), THROUGH(true, true), FROM(false, true), UNTIL(true, false), EVERYTHING(true, true);

        final boolean parents;
        final boolean children;

        private Filter(final boolean flag, final boolean flag1) {
            this.parents = flag;
            this.children = flag1;
        }
    }
}
