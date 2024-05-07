package net.minecraft.commands.arguments;

import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.CommandSigningContext;
import net.minecraft.commands.arguments.selector.ArgumentParserSelector;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.chat.ChatDecorator;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.network.FilteredText;

public class ArgumentChat implements SignedArgument<ArgumentChat.a> {

    private static final Collection<String> EXAMPLES = Arrays.asList("Hello world!", "foo", "@e", "Hello @p :)");
    static final Dynamic2CommandExceptionType TOO_LONG = new Dynamic2CommandExceptionType((object, object1) -> {
        return IChatBaseComponent.translatableEscape("argument.message.too_long", object, object1);
    });

    public ArgumentChat() {}

    public static ArgumentChat message() {
        return new ArgumentChat();
    }

    public static IChatBaseComponent getMessage(CommandContext<CommandListenerWrapper> commandcontext, String s) throws CommandSyntaxException {
        ArgumentChat.a argumentchat_a = (ArgumentChat.a) commandcontext.getArgument(s, ArgumentChat.a.class);

        return argumentchat_a.resolveComponent((CommandListenerWrapper) commandcontext.getSource());
    }

    public static void resolveChatMessage(CommandContext<CommandListenerWrapper> commandcontext, String s, Consumer<PlayerChatMessage> consumer) throws CommandSyntaxException {
        ArgumentChat.a argumentchat_a = (ArgumentChat.a) commandcontext.getArgument(s, ArgumentChat.a.class);
        CommandListenerWrapper commandlistenerwrapper = (CommandListenerWrapper) commandcontext.getSource();
        IChatBaseComponent ichatbasecomponent = argumentchat_a.resolveComponent(commandlistenerwrapper);
        CommandSigningContext commandsigningcontext = commandlistenerwrapper.getSigningContext();
        PlayerChatMessage playerchatmessage = commandsigningcontext.getArgument(s);

        if (playerchatmessage != null) {
            resolveSignedMessage(consumer, commandlistenerwrapper, playerchatmessage.withUnsignedContent(ichatbasecomponent));
        } else {
            resolveDisguisedMessage(consumer, commandlistenerwrapper, PlayerChatMessage.system(argumentchat_a.text).withUnsignedContent(ichatbasecomponent));
        }

    }

    private static void resolveSignedMessage(Consumer<PlayerChatMessage> consumer, CommandListenerWrapper commandlistenerwrapper, PlayerChatMessage playerchatmessage) {
        MinecraftServer minecraftserver = commandlistenerwrapper.getServer();
        CompletableFuture<FilteredText> completablefuture = filterPlainText(commandlistenerwrapper, playerchatmessage);
        IChatBaseComponent ichatbasecomponent = minecraftserver.getChatDecorator().decorate(commandlistenerwrapper.getPlayer(), playerchatmessage.decoratedContent());

        commandlistenerwrapper.getChatMessageChainer().append(completablefuture, (filteredtext) -> {
            PlayerChatMessage playerchatmessage1 = playerchatmessage.withUnsignedContent(ichatbasecomponent).filter(filteredtext.mask());

            consumer.accept(playerchatmessage1);
        });
    }

    private static void resolveDisguisedMessage(Consumer<PlayerChatMessage> consumer, CommandListenerWrapper commandlistenerwrapper, PlayerChatMessage playerchatmessage) {
        ChatDecorator chatdecorator = commandlistenerwrapper.getServer().getChatDecorator();
        IChatBaseComponent ichatbasecomponent = chatdecorator.decorate(commandlistenerwrapper.getPlayer(), playerchatmessage.decoratedContent());

        consumer.accept(playerchatmessage.withUnsignedContent(ichatbasecomponent));
    }

    private static CompletableFuture<FilteredText> filterPlainText(CommandListenerWrapper commandlistenerwrapper, PlayerChatMessage playerchatmessage) {
        EntityPlayer entityplayer = commandlistenerwrapper.getPlayer();

        return entityplayer != null && playerchatmessage.hasSignatureFrom(entityplayer.getUUID()) ? entityplayer.getTextFilter().processStreamMessage(playerchatmessage.signedContent()) : CompletableFuture.completedFuture(FilteredText.passThrough(playerchatmessage.signedContent()));
    }

    public ArgumentChat.a parse(StringReader stringreader) throws CommandSyntaxException {
        return ArgumentChat.a.parseText(stringreader, true);
    }

    public Collection<String> getExamples() {
        return ArgumentChat.EXAMPLES;
    }

    public static record a(String text, ArgumentChat.b[] parts) {

        IChatBaseComponent resolveComponent(CommandListenerWrapper commandlistenerwrapper) throws CommandSyntaxException {
            return this.toComponent(commandlistenerwrapper, commandlistenerwrapper.hasPermission(2));
        }

        public IChatBaseComponent toComponent(CommandListenerWrapper commandlistenerwrapper, boolean flag) throws CommandSyntaxException {
            if (this.parts.length != 0 && flag) {
                IChatMutableComponent ichatmutablecomponent = IChatBaseComponent.literal(this.text.substring(0, this.parts[0].start()));
                int i = this.parts[0].start();
                ArgumentChat.b[] aargumentchat_b = this.parts;
                int j = aargumentchat_b.length;

                for (int k = 0; k < j; ++k) {
                    ArgumentChat.b argumentchat_b = aargumentchat_b[k];
                    IChatBaseComponent ichatbasecomponent = argumentchat_b.toComponent(commandlistenerwrapper);

                    if (i < argumentchat_b.start()) {
                        ichatmutablecomponent.append(this.text.substring(i, argumentchat_b.start()));
                    }

                    ichatmutablecomponent.append(ichatbasecomponent);
                    i = argumentchat_b.end();
                }

                if (i < this.text.length()) {
                    ichatmutablecomponent.append(this.text.substring(i));
                }

                return ichatmutablecomponent;
            } else {
                return IChatBaseComponent.literal(this.text);
            }
        }

        public static ArgumentChat.a parseText(StringReader stringreader, boolean flag) throws CommandSyntaxException {
            if (stringreader.getRemainingLength() > 256) {
                throw ArgumentChat.TOO_LONG.create(stringreader.getRemainingLength(), 256);
            } else {
                String s = stringreader.getRemaining();

                if (!flag) {
                    stringreader.setCursor(stringreader.getTotalLength());
                    return new ArgumentChat.a(s, new ArgumentChat.b[0]);
                } else {
                    List<ArgumentChat.b> list = Lists.newArrayList();
                    int i = stringreader.getCursor();

                    while (stringreader.canRead()) {
                        if (stringreader.peek() == '@') {
                            int j = stringreader.getCursor();

                            EntitySelector entityselector;

                            try {
                                ArgumentParserSelector argumentparserselector = new ArgumentParserSelector(stringreader);

                                entityselector = argumentparserselector.parse();
                            } catch (CommandSyntaxException commandsyntaxexception) {
                                if (commandsyntaxexception.getType() != ArgumentParserSelector.ERROR_MISSING_SELECTOR_TYPE && commandsyntaxexception.getType() != ArgumentParserSelector.ERROR_UNKNOWN_SELECTOR_TYPE) {
                                    throw commandsyntaxexception;
                                }

                                stringreader.setCursor(j + 1);
                                continue;
                            }

                            list.add(new ArgumentChat.b(j - i, stringreader.getCursor() - i, entityselector));
                        } else {
                            stringreader.skip();
                        }
                    }

                    return new ArgumentChat.a(s, (ArgumentChat.b[]) list.toArray(new ArgumentChat.b[0]));
                }
            }
        }
    }

    public static record b(int start, int end, EntitySelector selector) {

        public IChatBaseComponent toComponent(CommandListenerWrapper commandlistenerwrapper) throws CommandSyntaxException {
            return EntitySelector.joinNames(this.selector.findEntities(commandlistenerwrapper));
        }
    }
}
