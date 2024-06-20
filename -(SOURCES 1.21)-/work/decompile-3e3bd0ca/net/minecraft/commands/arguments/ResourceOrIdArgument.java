package net.minecraft.commands.arguments;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.serialization.Codec;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.MojangsonParser;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class ResourceOrIdArgument<T> implements ArgumentType<Holder<T>> {

    private static final Collection<String> EXAMPLES = List.of("foo", "foo:bar", "012", "{}", "true");
    public static final DynamicCommandExceptionType ERROR_FAILED_TO_PARSE = new DynamicCommandExceptionType((object) -> {
        return IChatBaseComponent.translatableEscape("argument.resource_or_id.failed_to_parse", object);
    });
    private static final SimpleCommandExceptionType ERROR_INVALID = new SimpleCommandExceptionType(IChatBaseComponent.translatable("argument.resource_or_id.invalid"));
    private final HolderLookup.a registryLookup;
    private final boolean hasRegistry;
    private final Codec<Holder<T>> codec;

    protected ResourceOrIdArgument(CommandBuildContext commandbuildcontext, ResourceKey<IRegistry<T>> resourcekey, Codec<Holder<T>> codec) {
        this.registryLookup = commandbuildcontext;
        this.hasRegistry = commandbuildcontext.lookup(resourcekey).isPresent();
        this.codec = codec;
    }

    public static ResourceOrIdArgument.c lootTable(CommandBuildContext commandbuildcontext) {
        return new ResourceOrIdArgument.c(commandbuildcontext);
    }

    public static Holder<LootTable> getLootTable(CommandContext<CommandListenerWrapper> commandcontext, String s) throws CommandSyntaxException {
        return getResource(commandcontext, s);
    }

    public static ResourceOrIdArgument.a lootModifier(CommandBuildContext commandbuildcontext) {
        return new ResourceOrIdArgument.a(commandbuildcontext);
    }

    public static Holder<LootItemFunction> getLootModifier(CommandContext<CommandListenerWrapper> commandcontext, String s) {
        return getResource(commandcontext, s);
    }

    public static ResourceOrIdArgument.b lootPredicate(CommandBuildContext commandbuildcontext) {
        return new ResourceOrIdArgument.b(commandbuildcontext);
    }

    public static Holder<LootItemCondition> getLootPredicate(CommandContext<CommandListenerWrapper> commandcontext, String s) {
        return getResource(commandcontext, s);
    }

    private static <T> Holder<T> getResource(CommandContext<CommandListenerWrapper> commandcontext, String s) {
        return (Holder) commandcontext.getArgument(s, Holder.class);
    }

    @Nullable
    public Holder<T> parse(StringReader stringreader) throws CommandSyntaxException {
        NBTBase nbtbase = parseInlineOrId(stringreader);

        if (!this.hasRegistry) {
            return null;
        } else {
            RegistryOps<NBTBase> registryops = this.registryLookup.createSerializationContext(DynamicOpsNBT.INSTANCE);

            return (Holder) this.codec.parse(registryops, nbtbase).getOrThrow((s) -> {
                return ResourceOrIdArgument.ERROR_FAILED_TO_PARSE.createWithContext(stringreader, s);
            });
        }
    }

    @VisibleForTesting
    static NBTBase parseInlineOrId(StringReader stringreader) throws CommandSyntaxException {
        int i = stringreader.getCursor();
        NBTBase nbtbase = (new MojangsonParser(stringreader)).readValue();

        if (hasConsumedWholeArg(stringreader)) {
            return nbtbase;
        } else {
            stringreader.setCursor(i);
            MinecraftKey minecraftkey = MinecraftKey.read(stringreader);

            if (hasConsumedWholeArg(stringreader)) {
                return NBTTagString.valueOf(minecraftkey.toString());
            } else {
                stringreader.setCursor(i);
                throw ResourceOrIdArgument.ERROR_INVALID.createWithContext(stringreader);
            }
        }
    }

    private static boolean hasConsumedWholeArg(StringReader stringreader) {
        return !stringreader.canRead() || stringreader.peek() == ' ';
    }

    public Collection<String> getExamples() {
        return ResourceOrIdArgument.EXAMPLES;
    }

    public static class c extends ResourceOrIdArgument<LootTable> {

        protected c(CommandBuildContext commandbuildcontext) {
            super(commandbuildcontext, Registries.LOOT_TABLE, LootTable.CODEC);
        }
    }

    public static class a extends ResourceOrIdArgument<LootItemFunction> {

        protected a(CommandBuildContext commandbuildcontext) {
            super(commandbuildcontext, Registries.ITEM_MODIFIER, LootItemFunctions.CODEC);
        }
    }

    public static class b extends ResourceOrIdArgument<LootItemCondition> {

        protected b(CommandBuildContext commandbuildcontext) {
            super(commandbuildcontext, Registries.PREDICATE, LootItemCondition.CODEC);
        }
    }
}
