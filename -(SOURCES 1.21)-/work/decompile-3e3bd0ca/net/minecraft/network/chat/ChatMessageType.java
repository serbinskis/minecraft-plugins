package net.minecraft.network.chat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.core.Holder;
import net.minecraft.core.IRegistry;
import net.minecraft.core.IRegistryCustom;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;

public record ChatMessageType(ChatDecoration chat, ChatDecoration narration) {

    public static final Codec<ChatMessageType> DIRECT_CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(ChatDecoration.CODEC.fieldOf("chat").forGetter(ChatMessageType::chat), ChatDecoration.CODEC.fieldOf("narration").forGetter(ChatMessageType::narration)).apply(instance, ChatMessageType::new);
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, ChatMessageType> DIRECT_STREAM_CODEC = StreamCodec.composite(ChatDecoration.STREAM_CODEC, ChatMessageType::chat, ChatDecoration.STREAM_CODEC, ChatMessageType::narration, ChatMessageType::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<ChatMessageType>> STREAM_CODEC = ByteBufCodecs.holder(Registries.CHAT_TYPE, ChatMessageType.DIRECT_STREAM_CODEC);
    public static final ChatDecoration DEFAULT_CHAT_DECORATION = ChatDecoration.withSender("chat.type.text");
    public static final ResourceKey<ChatMessageType> CHAT = create("chat");
    public static final ResourceKey<ChatMessageType> SAY_COMMAND = create("say_command");
    public static final ResourceKey<ChatMessageType> MSG_COMMAND_INCOMING = create("msg_command_incoming");
    public static final ResourceKey<ChatMessageType> MSG_COMMAND_OUTGOING = create("msg_command_outgoing");
    public static final ResourceKey<ChatMessageType> TEAM_MSG_COMMAND_INCOMING = create("team_msg_command_incoming");
    public static final ResourceKey<ChatMessageType> TEAM_MSG_COMMAND_OUTGOING = create("team_msg_command_outgoing");
    public static final ResourceKey<ChatMessageType> EMOTE_COMMAND = create("emote_command");

    private static ResourceKey<ChatMessageType> create(String s) {
        return ResourceKey.create(Registries.CHAT_TYPE, MinecraftKey.withDefaultNamespace(s));
    }

    public static void bootstrap(BootstrapContext<ChatMessageType> bootstrapcontext) {
        bootstrapcontext.register(ChatMessageType.CHAT, new ChatMessageType(ChatMessageType.DEFAULT_CHAT_DECORATION, ChatDecoration.withSender("chat.type.text.narrate")));
        bootstrapcontext.register(ChatMessageType.SAY_COMMAND, new ChatMessageType(ChatDecoration.withSender("chat.type.announcement"), ChatDecoration.withSender("chat.type.text.narrate")));
        bootstrapcontext.register(ChatMessageType.MSG_COMMAND_INCOMING, new ChatMessageType(ChatDecoration.incomingDirectMessage("commands.message.display.incoming"), ChatDecoration.withSender("chat.type.text.narrate")));
        bootstrapcontext.register(ChatMessageType.MSG_COMMAND_OUTGOING, new ChatMessageType(ChatDecoration.outgoingDirectMessage("commands.message.display.outgoing"), ChatDecoration.withSender("chat.type.text.narrate")));
        bootstrapcontext.register(ChatMessageType.TEAM_MSG_COMMAND_INCOMING, new ChatMessageType(ChatDecoration.teamMessage("chat.type.team.text"), ChatDecoration.withSender("chat.type.text.narrate")));
        bootstrapcontext.register(ChatMessageType.TEAM_MSG_COMMAND_OUTGOING, new ChatMessageType(ChatDecoration.teamMessage("chat.type.team.sent"), ChatDecoration.withSender("chat.type.text.narrate")));
        bootstrapcontext.register(ChatMessageType.EMOTE_COMMAND, new ChatMessageType(ChatDecoration.withSender("chat.type.emote"), ChatDecoration.withSender("chat.type.emote")));
    }

    public static ChatMessageType.a bind(ResourceKey<ChatMessageType> resourcekey, Entity entity) {
        return bind(resourcekey, entity.level().registryAccess(), entity.getDisplayName());
    }

    public static ChatMessageType.a bind(ResourceKey<ChatMessageType> resourcekey, CommandListenerWrapper commandlistenerwrapper) {
        return bind(resourcekey, commandlistenerwrapper.registryAccess(), commandlistenerwrapper.getDisplayName());
    }

    public static ChatMessageType.a bind(ResourceKey<ChatMessageType> resourcekey, IRegistryCustom iregistrycustom, IChatBaseComponent ichatbasecomponent) {
        IRegistry<ChatMessageType> iregistry = iregistrycustom.registryOrThrow(Registries.CHAT_TYPE);

        return new ChatMessageType.a(iregistry.getHolderOrThrow(resourcekey), ichatbasecomponent);
    }

    public static record a(Holder<ChatMessageType> chatType, IChatBaseComponent name, Optional<IChatBaseComponent> targetName) {

        public static final StreamCodec<RegistryFriendlyByteBuf, ChatMessageType.a> STREAM_CODEC = StreamCodec.composite(ChatMessageType.STREAM_CODEC, ChatMessageType.a::chatType, ComponentSerialization.TRUSTED_STREAM_CODEC, ChatMessageType.a::name, ComponentSerialization.TRUSTED_OPTIONAL_STREAM_CODEC, ChatMessageType.a::targetName, ChatMessageType.a::new);

        a(Holder<ChatMessageType> holder, IChatBaseComponent ichatbasecomponent) {
            this(holder, ichatbasecomponent, Optional.empty());
        }

        public IChatBaseComponent decorate(IChatBaseComponent ichatbasecomponent) {
            return ((ChatMessageType) this.chatType.value()).chat().decorate(ichatbasecomponent, this);
        }

        public IChatBaseComponent decorateNarration(IChatBaseComponent ichatbasecomponent) {
            return ((ChatMessageType) this.chatType.value()).narration().decorate(ichatbasecomponent, this);
        }

        public ChatMessageType.a withTargetName(IChatBaseComponent ichatbasecomponent) {
            return new ChatMessageType.a(this.chatType, this.name, Optional.of(ichatbasecomponent));
        }
    }
}
