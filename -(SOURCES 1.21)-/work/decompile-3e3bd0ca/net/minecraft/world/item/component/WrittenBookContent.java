package net.minecraft.world.item.component;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.ChatComponentUtils;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.network.Filterable;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.player.EntityHuman;

public record WrittenBookContent(Filterable<String> title, String author, int generation, List<Filterable<IChatBaseComponent>> pages, boolean resolved) implements BookContent<IChatBaseComponent, WrittenBookContent> {

    public static final WrittenBookContent EMPTY = new WrittenBookContent(Filterable.passThrough(""), "", 0, List.of(), true);
    public static final int PAGE_LENGTH = 32767;
    public static final int TITLE_LENGTH = 16;
    public static final int TITLE_MAX_LENGTH = 32;
    public static final int MAX_GENERATION = 3;
    public static final int MAX_CRAFTABLE_GENERATION = 2;
    public static final Codec<IChatBaseComponent> CONTENT_CODEC = ComponentSerialization.flatCodec(32767);
    public static final Codec<List<Filterable<IChatBaseComponent>>> PAGES_CODEC = pagesCodec(WrittenBookContent.CONTENT_CODEC);
    public static final Codec<WrittenBookContent> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(Filterable.codec(Codec.string(0, 32)).fieldOf("title").forGetter(WrittenBookContent::title), Codec.STRING.fieldOf("author").forGetter(WrittenBookContent::author), ExtraCodecs.intRange(0, 3).optionalFieldOf("generation", 0).forGetter(WrittenBookContent::generation), WrittenBookContent.PAGES_CODEC.optionalFieldOf("pages", List.of()).forGetter(WrittenBookContent::pages), Codec.BOOL.optionalFieldOf("resolved", false).forGetter(WrittenBookContent::resolved)).apply(instance, WrittenBookContent::new);
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, WrittenBookContent> STREAM_CODEC = StreamCodec.composite(Filterable.streamCodec(ByteBufCodecs.stringUtf8(32)), WrittenBookContent::title, ByteBufCodecs.STRING_UTF8, WrittenBookContent::author, ByteBufCodecs.VAR_INT, WrittenBookContent::generation, Filterable.streamCodec(ComponentSerialization.STREAM_CODEC).apply(ByteBufCodecs.list()), WrittenBookContent::pages, ByteBufCodecs.BOOL, WrittenBookContent::resolved, WrittenBookContent::new);

    public WrittenBookContent(Filterable<String> filterable, String s, int i, List<Filterable<IChatBaseComponent>> list, boolean flag) {
        if (i >= 0 && i <= 3) {
            this.title = filterable;
            this.author = s;
            this.generation = i;
            this.pages = list;
            this.resolved = flag;
        } else {
            throw new IllegalArgumentException("Generation was " + i + ", but must be between 0 and 3");
        }
    }

    private static Codec<Filterable<IChatBaseComponent>> pageCodec(Codec<IChatBaseComponent> codec) {
        return Filterable.codec(codec);
    }

    public static Codec<List<Filterable<IChatBaseComponent>>> pagesCodec(Codec<IChatBaseComponent> codec) {
        return pageCodec(codec).listOf();
    }

    @Nullable
    public WrittenBookContent tryCraftCopy() {
        return this.generation >= 2 ? null : new WrittenBookContent(this.title, this.author, this.generation + 1, this.pages, this.resolved);
    }

    @Nullable
    public WrittenBookContent resolve(CommandListenerWrapper commandlistenerwrapper, @Nullable EntityHuman entityhuman) {
        if (this.resolved) {
            return null;
        } else {
            Builder<Filterable<IChatBaseComponent>> builder = ImmutableList.builderWithExpectedSize(this.pages.size());
            Iterator iterator = this.pages.iterator();

            while (iterator.hasNext()) {
                Filterable<IChatBaseComponent> filterable = (Filterable) iterator.next();
                Optional<Filterable<IChatBaseComponent>> optional = resolvePage(commandlistenerwrapper, entityhuman, filterable);

                if (optional.isEmpty()) {
                    return null;
                }

                builder.add((Filterable) optional.get());
            }

            return new WrittenBookContent(this.title, this.author, this.generation, builder.build(), true);
        }
    }

    public WrittenBookContent markResolved() {
        return new WrittenBookContent(this.title, this.author, this.generation, this.pages, true);
    }

    private static Optional<Filterable<IChatBaseComponent>> resolvePage(CommandListenerWrapper commandlistenerwrapper, @Nullable EntityHuman entityhuman, Filterable<IChatBaseComponent> filterable) {
        return filterable.resolve((ichatbasecomponent) -> {
            try {
                IChatMutableComponent ichatmutablecomponent = ChatComponentUtils.updateForEntity(commandlistenerwrapper, ichatbasecomponent, entityhuman, 0);

                return isPageTooLarge(ichatmutablecomponent, commandlistenerwrapper.registryAccess()) ? Optional.empty() : Optional.of(ichatmutablecomponent);
            } catch (Exception exception) {
                return Optional.of(ichatbasecomponent);
            }
        });
    }

    private static boolean isPageTooLarge(IChatBaseComponent ichatbasecomponent, HolderLookup.a holderlookup_a) {
        return IChatBaseComponent.ChatSerializer.toJson(ichatbasecomponent, holderlookup_a).length() > 32767;
    }

    public List<IChatBaseComponent> getPages(boolean flag) {
        return Lists.transform(this.pages, (filterable) -> {
            return (IChatBaseComponent) filterable.get(flag);
        });
    }

    @Override
    public WrittenBookContent withReplacedPages(List<Filterable<IChatBaseComponent>> list) {
        return new WrittenBookContent(this.title, this.author, this.generation, list, false);
    }
}
