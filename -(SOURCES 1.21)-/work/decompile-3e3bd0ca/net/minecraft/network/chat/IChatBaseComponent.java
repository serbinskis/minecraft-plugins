package net.minecraft.network.chat;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.stream.JsonReader;
import com.mojang.brigadier.Message;
import com.mojang.serialization.JsonOps;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.contents.DataSource;
import net.minecraft.network.chat.contents.KeybindContents;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.network.chat.contents.NbtContents;
import net.minecraft.network.chat.contents.ScoreContents;
import net.minecraft.network.chat.contents.SelectorContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.util.FormattedString;
import net.minecraft.world.level.ChunkCoordIntPair;

public interface IChatBaseComponent extends Message, IChatFormatted {

    ChatModifier getStyle();

    ComponentContents getContents();

    @Override
    default String getString() {
        return IChatFormatted.super.getString();
    }

    default String getString(int i) {
        StringBuilder stringbuilder = new StringBuilder();

        this.visit((s) -> {
            int j = i - stringbuilder.length();

            if (j <= 0) {
                return IChatBaseComponent.STOP_ITERATION;
            } else {
                stringbuilder.append(s.length() <= j ? s : s.substring(0, j));
                return Optional.empty();
            }
        });
        return stringbuilder.toString();
    }

    List<IChatBaseComponent> getSiblings();

    @Nullable
    default String tryCollapseToString() {
        ComponentContents componentcontents = this.getContents();

        if (componentcontents instanceof LiteralContents literalcontents) {
            if (this.getSiblings().isEmpty() && this.getStyle().isEmpty()) {
                return literalcontents.text();
            }
        }

        return null;
    }

    default IChatMutableComponent plainCopy() {
        return IChatMutableComponent.create(this.getContents());
    }

    default IChatMutableComponent copy() {
        return new IChatMutableComponent(this.getContents(), new ArrayList(this.getSiblings()), this.getStyle());
    }

    FormattedString getVisualOrderText();

    @Override
    default <T> Optional<T> visit(IChatFormatted.b<T> ichatformatted_b, ChatModifier chatmodifier) {
        ChatModifier chatmodifier1 = this.getStyle().applyTo(chatmodifier);
        Optional<T> optional = this.getContents().visit(ichatformatted_b, chatmodifier1);

        if (optional.isPresent()) {
            return optional;
        } else {
            Iterator iterator = this.getSiblings().iterator();

            Optional optional1;

            do {
                if (!iterator.hasNext()) {
                    return Optional.empty();
                }

                IChatBaseComponent ichatbasecomponent = (IChatBaseComponent) iterator.next();

                optional1 = ichatbasecomponent.visit(ichatformatted_b, chatmodifier1);
            } while (!optional1.isPresent());

            return optional1;
        }
    }

    @Override
    default <T> Optional<T> visit(IChatFormatted.a<T> ichatformatted_a) {
        Optional<T> optional = this.getContents().visit(ichatformatted_a);

        if (optional.isPresent()) {
            return optional;
        } else {
            Iterator iterator = this.getSiblings().iterator();

            Optional optional1;

            do {
                if (!iterator.hasNext()) {
                    return Optional.empty();
                }

                IChatBaseComponent ichatbasecomponent = (IChatBaseComponent) iterator.next();

                optional1 = ichatbasecomponent.visit(ichatformatted_a);
            } while (!optional1.isPresent());

            return optional1;
        }
    }

    default List<IChatBaseComponent> toFlatList() {
        return this.toFlatList(ChatModifier.EMPTY);
    }

    default List<IChatBaseComponent> toFlatList(ChatModifier chatmodifier) {
        List<IChatBaseComponent> list = Lists.newArrayList();

        this.visit((chatmodifier1, s) -> {
            if (!s.isEmpty()) {
                list.add(literal(s).withStyle(chatmodifier1));
            }

            return Optional.empty();
        }, chatmodifier);
        return list;
    }

    default boolean contains(IChatBaseComponent ichatbasecomponent) {
        if (this.equals(ichatbasecomponent)) {
            return true;
        } else {
            List<IChatBaseComponent> list = this.toFlatList();
            List<IChatBaseComponent> list1 = ichatbasecomponent.toFlatList(this.getStyle());

            return Collections.indexOfSubList(list, list1) != -1;
        }
    }

    static IChatBaseComponent nullToEmpty(@Nullable String s) {
        return (IChatBaseComponent) (s != null ? literal(s) : CommonComponents.EMPTY);
    }

    static IChatMutableComponent literal(String s) {
        return IChatMutableComponent.create(LiteralContents.create(s));
    }

    static IChatMutableComponent translatable(String s) {
        return IChatMutableComponent.create(new TranslatableContents(s, (String) null, TranslatableContents.NO_ARGS));
    }

    static IChatMutableComponent translatable(String s, Object... aobject) {
        return IChatMutableComponent.create(new TranslatableContents(s, (String) null, aobject));
    }

    static IChatMutableComponent translatableEscape(String s, Object... aobject) {
        for (int i = 0; i < aobject.length; ++i) {
            Object object = aobject[i];

            if (!TranslatableContents.isAllowedPrimitiveArgument(object) && !(object instanceof IChatBaseComponent)) {
                aobject[i] = String.valueOf(object);
            }
        }

        return translatable(s, aobject);
    }

    static IChatMutableComponent translatableWithFallback(String s, @Nullable String s1) {
        return IChatMutableComponent.create(new TranslatableContents(s, s1, TranslatableContents.NO_ARGS));
    }

    static IChatMutableComponent translatableWithFallback(String s, @Nullable String s1, Object... aobject) {
        return IChatMutableComponent.create(new TranslatableContents(s, s1, aobject));
    }

    static IChatMutableComponent empty() {
        return IChatMutableComponent.create(LiteralContents.EMPTY);
    }

    static IChatMutableComponent keybind(String s) {
        return IChatMutableComponent.create(new KeybindContents(s));
    }

    static IChatMutableComponent nbt(String s, boolean flag, Optional<IChatBaseComponent> optional, DataSource datasource) {
        return IChatMutableComponent.create(new NbtContents(s, flag, optional, datasource));
    }

    static IChatMutableComponent score(String s, String s1) {
        return IChatMutableComponent.create(new ScoreContents(s, s1));
    }

    static IChatMutableComponent selector(String s, Optional<IChatBaseComponent> optional) {
        return IChatMutableComponent.create(new SelectorContents(s, optional));
    }

    static IChatBaseComponent translationArg(Date date) {
        return literal(date.toString());
    }

    static IChatBaseComponent translationArg(Message message) {
        Object object;

        if (message instanceof IChatBaseComponent ichatbasecomponent) {
            object = ichatbasecomponent;
        } else {
            object = literal(message.getString());
        }

        return (IChatBaseComponent) object;
    }

    static IChatBaseComponent translationArg(UUID uuid) {
        return literal(uuid.toString());
    }

    static IChatBaseComponent translationArg(MinecraftKey minecraftkey) {
        return literal(minecraftkey.toString());
    }

    static IChatBaseComponent translationArg(ChunkCoordIntPair chunkcoordintpair) {
        return literal(chunkcoordintpair.toString());
    }

    static IChatBaseComponent translationArg(URI uri) {
        return literal(uri.toString());
    }

    public static class b implements JsonDeserializer<IChatMutableComponent>, JsonSerializer<IChatBaseComponent> {

        private final HolderLookup.a registries;

        public b(HolderLookup.a holderlookup_a) {
            this.registries = holderlookup_a;
        }

        public IChatMutableComponent deserialize(JsonElement jsonelement, Type type, JsonDeserializationContext jsondeserializationcontext) throws JsonParseException {
            return IChatBaseComponent.ChatSerializer.deserialize(jsonelement, this.registries);
        }

        public JsonElement serialize(IChatBaseComponent ichatbasecomponent, Type type, JsonSerializationContext jsonserializationcontext) {
            return IChatBaseComponent.ChatSerializer.serialize(ichatbasecomponent, this.registries);
        }
    }

    public static class ChatSerializer {

        private static final Gson GSON = (new GsonBuilder()).disableHtmlEscaping().create();

        private ChatSerializer() {}

        static IChatMutableComponent deserialize(JsonElement jsonelement, HolderLookup.a holderlookup_a) {
            return (IChatMutableComponent) ComponentSerialization.CODEC.parse(holderlookup_a.createSerializationContext(JsonOps.INSTANCE), jsonelement).getOrThrow(JsonParseException::new);
        }

        static JsonElement serialize(IChatBaseComponent ichatbasecomponent, HolderLookup.a holderlookup_a) {
            return (JsonElement) ComponentSerialization.CODEC.encodeStart(holderlookup_a.createSerializationContext(JsonOps.INSTANCE), ichatbasecomponent).getOrThrow(JsonParseException::new);
        }

        public static String toJson(IChatBaseComponent ichatbasecomponent, HolderLookup.a holderlookup_a) {
            return IChatBaseComponent.ChatSerializer.GSON.toJson(serialize(ichatbasecomponent, holderlookup_a));
        }

        @Nullable
        public static IChatMutableComponent fromJson(String s, HolderLookup.a holderlookup_a) {
            JsonElement jsonelement = JsonParser.parseString(s);

            return jsonelement == null ? null : deserialize(jsonelement, holderlookup_a);
        }

        @Nullable
        public static IChatMutableComponent fromJson(@Nullable JsonElement jsonelement, HolderLookup.a holderlookup_a) {
            return jsonelement == null ? null : deserialize(jsonelement, holderlookup_a);
        }

        @Nullable
        public static IChatMutableComponent fromJsonLenient(String s, HolderLookup.a holderlookup_a) {
            JsonReader jsonreader = new JsonReader(new StringReader(s));

            jsonreader.setLenient(true);
            JsonElement jsonelement = JsonParser.parseReader(jsonreader);

            return jsonelement == null ? null : deserialize(jsonelement, holderlookup_a);
        }
    }
}
