package net.minecraft.commands.arguments.item;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.resources.MinecraftKey;

public class ArgumentTag implements ArgumentType<ArgumentTag.a> {

    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "#foo");
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_TAG = new DynamicCommandExceptionType((object) -> {
        return IChatBaseComponent.translatableEscape("arguments.function.tag.unknown", object);
    });
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_FUNCTION = new DynamicCommandExceptionType((object) -> {
        return IChatBaseComponent.translatableEscape("arguments.function.unknown", object);
    });

    public ArgumentTag() {}

    public static ArgumentTag functions() {
        return new ArgumentTag();
    }

    public ArgumentTag.a parse(StringReader stringreader) throws CommandSyntaxException {
        final MinecraftKey minecraftkey;

        if (stringreader.canRead() && stringreader.peek() == '#') {
            stringreader.skip();
            minecraftkey = MinecraftKey.read(stringreader);
            return new ArgumentTag.a(this) {
                @Override
                public Collection<CommandFunction<CommandListenerWrapper>> create(CommandContext<CommandListenerWrapper> commandcontext) throws CommandSyntaxException {
                    return ArgumentTag.getFunctionTag(commandcontext, minecraftkey);
                }

                @Override
                public Pair<MinecraftKey, Either<CommandFunction<CommandListenerWrapper>, Collection<CommandFunction<CommandListenerWrapper>>>> unwrap(CommandContext<CommandListenerWrapper> commandcontext) throws CommandSyntaxException {
                    return Pair.of(minecraftkey, Either.right(ArgumentTag.getFunctionTag(commandcontext, minecraftkey)));
                }

                @Override
                public Pair<MinecraftKey, Collection<CommandFunction<CommandListenerWrapper>>> unwrapToCollection(CommandContext<CommandListenerWrapper> commandcontext) throws CommandSyntaxException {
                    return Pair.of(minecraftkey, ArgumentTag.getFunctionTag(commandcontext, minecraftkey));
                }
            };
        } else {
            minecraftkey = MinecraftKey.read(stringreader);
            return new ArgumentTag.a(this) {
                @Override
                public Collection<CommandFunction<CommandListenerWrapper>> create(CommandContext<CommandListenerWrapper> commandcontext) throws CommandSyntaxException {
                    return Collections.singleton(ArgumentTag.getFunction(commandcontext, minecraftkey));
                }

                @Override
                public Pair<MinecraftKey, Either<CommandFunction<CommandListenerWrapper>, Collection<CommandFunction<CommandListenerWrapper>>>> unwrap(CommandContext<CommandListenerWrapper> commandcontext) throws CommandSyntaxException {
                    return Pair.of(minecraftkey, Either.left(ArgumentTag.getFunction(commandcontext, minecraftkey)));
                }

                @Override
                public Pair<MinecraftKey, Collection<CommandFunction<CommandListenerWrapper>>> unwrapToCollection(CommandContext<CommandListenerWrapper> commandcontext) throws CommandSyntaxException {
                    return Pair.of(minecraftkey, Collections.singleton(ArgumentTag.getFunction(commandcontext, minecraftkey)));
                }
            };
        }
    }

    static CommandFunction<CommandListenerWrapper> getFunction(CommandContext<CommandListenerWrapper> commandcontext, MinecraftKey minecraftkey) throws CommandSyntaxException {
        return (CommandFunction) ((CommandListenerWrapper) commandcontext.getSource()).getServer().getFunctions().get(minecraftkey).orElseThrow(() -> {
            return ArgumentTag.ERROR_UNKNOWN_FUNCTION.create(minecraftkey.toString());
        });
    }

    static Collection<CommandFunction<CommandListenerWrapper>> getFunctionTag(CommandContext<CommandListenerWrapper> commandcontext, MinecraftKey minecraftkey) throws CommandSyntaxException {
        Collection<CommandFunction<CommandListenerWrapper>> collection = ((CommandListenerWrapper) commandcontext.getSource()).getServer().getFunctions().getTag(minecraftkey);

        if (collection == null) {
            throw ArgumentTag.ERROR_UNKNOWN_TAG.create(minecraftkey.toString());
        } else {
            return collection;
        }
    }

    public static Collection<CommandFunction<CommandListenerWrapper>> getFunctions(CommandContext<CommandListenerWrapper> commandcontext, String s) throws CommandSyntaxException {
        return ((ArgumentTag.a) commandcontext.getArgument(s, ArgumentTag.a.class)).create(commandcontext);
    }

    public static Pair<MinecraftKey, Either<CommandFunction<CommandListenerWrapper>, Collection<CommandFunction<CommandListenerWrapper>>>> getFunctionOrTag(CommandContext<CommandListenerWrapper> commandcontext, String s) throws CommandSyntaxException {
        return ((ArgumentTag.a) commandcontext.getArgument(s, ArgumentTag.a.class)).unwrap(commandcontext);
    }

    public static Pair<MinecraftKey, Collection<CommandFunction<CommandListenerWrapper>>> getFunctionCollection(CommandContext<CommandListenerWrapper> commandcontext, String s) throws CommandSyntaxException {
        return ((ArgumentTag.a) commandcontext.getArgument(s, ArgumentTag.a.class)).unwrapToCollection(commandcontext);
    }

    public Collection<String> getExamples() {
        return ArgumentTag.EXAMPLES;
    }

    public interface a {

        Collection<CommandFunction<CommandListenerWrapper>> create(CommandContext<CommandListenerWrapper> commandcontext) throws CommandSyntaxException;

        Pair<MinecraftKey, Either<CommandFunction<CommandListenerWrapper>, Collection<CommandFunction<CommandListenerWrapper>>>> unwrap(CommandContext<CommandListenerWrapper> commandcontext) throws CommandSyntaxException;

        Pair<MinecraftKey, Collection<CommandFunction<CommandListenerWrapper>>> unwrapToCollection(CommandContext<CommandListenerWrapper> commandcontext) throws CommandSyntaxException;
    }
}
