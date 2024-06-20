package net.minecraft.network.chat;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.commands.arguments.SignedArgument;

public record SignableCommand<S>(List<SignableCommand.a<S>> arguments) {

    public static <S> boolean hasSignableArguments(ParseResults<S> parseresults) {
        return !of(parseresults).arguments().isEmpty();
    }

    public static <S> SignableCommand<S> of(ParseResults<S> parseresults) {
        String s = parseresults.getReader().getString();
        CommandContextBuilder<S> commandcontextbuilder = parseresults.getContext();
        CommandContextBuilder<S> commandcontextbuilder1 = commandcontextbuilder;

        List list;
        CommandContextBuilder commandcontextbuilder2;

        for (list = collectArguments(s, commandcontextbuilder); (commandcontextbuilder2 = commandcontextbuilder1.getChild()) != null && commandcontextbuilder2.getRootNode() != commandcontextbuilder.getRootNode(); commandcontextbuilder1 = commandcontextbuilder2) {
            list.addAll(collectArguments(s, commandcontextbuilder2));
        }

        return new SignableCommand<>(list);
    }

    private static <S> List<SignableCommand.a<S>> collectArguments(String s, CommandContextBuilder<S> commandcontextbuilder) {
        List<SignableCommand.a<S>> list = new ArrayList();
        Iterator iterator = commandcontextbuilder.getNodes().iterator();

        while (iterator.hasNext()) {
            ParsedCommandNode<S> parsedcommandnode = (ParsedCommandNode) iterator.next();
            CommandNode commandnode = parsedcommandnode.getNode();

            if (commandnode instanceof ArgumentCommandNode<S, ?> argumentcommandnode) {
                if (argumentcommandnode.getType() instanceof SignedArgument) {
                    ParsedArgument<S, ?> parsedargument = (ParsedArgument) commandcontextbuilder.getArguments().get(argumentcommandnode.getName());

                    if (parsedargument != null) {
                        String s1 = parsedargument.getRange().get(s);

                        list.add(new SignableCommand.a<>(argumentcommandnode, s1));
                    }
                }
            }
        }

        return list;
    }

    @Nullable
    public SignableCommand.a<S> getArgument(String s) {
        Iterator iterator = this.arguments.iterator();

        SignableCommand.a signablecommand_a;

        do {
            if (!iterator.hasNext()) {
                return null;
            }

            signablecommand_a = (SignableCommand.a) iterator.next();
        } while (!s.equals(signablecommand_a.name()));

        return signablecommand_a;
    }

    public static record a<S>(ArgumentCommandNode<S, ?> node, String value) {

        public String name() {
            return this.node.getName();
        }
    }
}
