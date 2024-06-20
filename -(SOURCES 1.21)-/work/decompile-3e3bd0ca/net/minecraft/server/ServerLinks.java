package net.minecraft.server;

import com.mojang.datafixers.util.Either;
import io.netty.buffer.ByteBuf;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.function.IntFunction;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;

public record ServerLinks(List<ServerLinks.Entry> entries) {

    public static final ServerLinks EMPTY = new ServerLinks(List.of());
    public static final StreamCodec<ByteBuf, Either<ServerLinks.KnownLinkType, IChatBaseComponent>> TYPE_STREAM_CODEC = ByteBufCodecs.either(ServerLinks.KnownLinkType.STREAM_CODEC, ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC);
    public static final StreamCodec<ByteBuf, List<ServerLinks.UntrustedEntry>> UNTRUSTED_LINKS_STREAM_CODEC = ServerLinks.UntrustedEntry.STREAM_CODEC.apply(ByteBufCodecs.list());

    public boolean isEmpty() {
        return this.entries.isEmpty();
    }

    public Optional<ServerLinks.Entry> findKnownType(ServerLinks.KnownLinkType serverlinks_knownlinktype) {
        return this.entries.stream().filter((serverlinks_entry) -> {
            return (Boolean) serverlinks_entry.type.map((serverlinks_knownlinktype1) -> {
                return serverlinks_knownlinktype1 == serverlinks_knownlinktype;
            }, (ichatbasecomponent) -> {
                return false;
            });
        }).findFirst();
    }

    public List<ServerLinks.UntrustedEntry> untrust() {
        return this.entries.stream().map((serverlinks_entry) -> {
            return new ServerLinks.UntrustedEntry(serverlinks_entry.type, serverlinks_entry.link.toString());
        }).toList();
    }

    public static enum KnownLinkType {

        BUG_REPORT(0, "report_bug"), COMMUNITY_GUIDELINES(1, "community_guidelines"), SUPPORT(2, "support"), STATUS(3, "status"), FEEDBACK(4, "feedback"), COMMUNITY(5, "community"), WEBSITE(6, "website"), FORUMS(7, "forums"), NEWS(8, "news"), ANNOUNCEMENTS(9, "announcements");

        private static final IntFunction<ServerLinks.KnownLinkType> BY_ID = ByIdMap.continuous((serverlinks_knownlinktype) -> {
            return serverlinks_knownlinktype.id;
        }, values(), ByIdMap.a.ZERO);
        public static final StreamCodec<ByteBuf, ServerLinks.KnownLinkType> STREAM_CODEC = ByteBufCodecs.idMapper(ServerLinks.KnownLinkType.BY_ID, (serverlinks_knownlinktype) -> {
            return serverlinks_knownlinktype.id;
        });
        private final int id;
        private final String name;

        private KnownLinkType(final int i, final String s) {
            this.id = i;
            this.name = s;
        }

        private IChatBaseComponent displayName() {
            return IChatBaseComponent.translatable("known_server_link." + this.name);
        }

        public ServerLinks.Entry create(URI uri) {
            return ServerLinks.Entry.knownType(this, uri);
        }
    }

    public static record UntrustedEntry(Either<ServerLinks.KnownLinkType, IChatBaseComponent> type, String link) {

        public static final StreamCodec<ByteBuf, ServerLinks.UntrustedEntry> STREAM_CODEC = StreamCodec.composite(ServerLinks.TYPE_STREAM_CODEC, ServerLinks.UntrustedEntry::type, ByteBufCodecs.STRING_UTF8, ServerLinks.UntrustedEntry::link, ServerLinks.UntrustedEntry::new);
    }

    public static record Entry(Either<ServerLinks.KnownLinkType, IChatBaseComponent> type, URI link) {

        public static ServerLinks.Entry knownType(ServerLinks.KnownLinkType serverlinks_knownlinktype, URI uri) {
            return new ServerLinks.Entry(Either.left(serverlinks_knownlinktype), uri);
        }

        public static ServerLinks.Entry custom(IChatBaseComponent ichatbasecomponent, URI uri) {
            return new ServerLinks.Entry(Either.right(ichatbasecomponent), uri);
        }

        public IChatBaseComponent displayName() {
            return (IChatBaseComponent) this.type.map(ServerLinks.KnownLinkType::displayName, (ichatbasecomponent) -> {
                return ichatbasecomponent;
            });
        }
    }
}
