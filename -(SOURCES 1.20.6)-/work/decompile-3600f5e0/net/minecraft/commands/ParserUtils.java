package net.minecraft.commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.mojang.brigadier.StringReader;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.lang.reflect.Field;
import net.minecraft.CharPredicate;
import net.minecraft.SystemUtils;
import net.minecraft.core.HolderLookup;

public class ParserUtils {

    private static final Field JSON_READER_POS = (Field) SystemUtils.make(() -> {
        try {
            Field field = JsonReader.class.getDeclaredField("pos");

            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException nosuchfieldexception) {
            throw new IllegalStateException("Couldn't get field 'pos' for JsonReader", nosuchfieldexception);
        }
    });
    private static final Field JSON_READER_LINESTART = (Field) SystemUtils.make(() -> {
        try {
            Field field = JsonReader.class.getDeclaredField("lineStart");

            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException nosuchfieldexception) {
            throw new IllegalStateException("Couldn't get field 'lineStart' for JsonReader", nosuchfieldexception);
        }
    });

    public ParserUtils() {}

    private static int getPos(JsonReader jsonreader) {
        try {
            return ParserUtils.JSON_READER_POS.getInt(jsonreader) - ParserUtils.JSON_READER_LINESTART.getInt(jsonreader);
        } catch (IllegalAccessException illegalaccessexception) {
            throw new IllegalStateException("Couldn't read position of JsonReader", illegalaccessexception);
        }
    }

    public static <T> T parseJson(HolderLookup.a holderlookup_a, StringReader stringreader, Codec<T> codec) {
        JsonReader jsonreader = new JsonReader(new java.io.StringReader(stringreader.getRemaining()));

        jsonreader.setLenient(false);

        Object object;

        try {
            JsonElement jsonelement = Streams.parse(jsonreader);

            object = codec.parse(holderlookup_a.createSerializationContext(JsonOps.INSTANCE), jsonelement).getOrThrow(JsonParseException::new);
        } catch (StackOverflowError stackoverflowerror) {
            throw new JsonParseException(stackoverflowerror);
        } finally {
            stringreader.setCursor(stringreader.getCursor() + getPos(jsonreader));
        }

        return object;
    }

    public static String readWhile(StringReader stringreader, CharPredicate charpredicate) {
        int i = stringreader.getCursor();

        while (stringreader.canRead() && charpredicate.test(stringreader.peek())) {
            stringreader.skip();
        }

        return stringreader.getString().substring(i, stringreader.getCursor());
    }
}
