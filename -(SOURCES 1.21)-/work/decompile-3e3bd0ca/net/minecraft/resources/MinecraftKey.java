package net.minecraft.resources;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.ByteBuf;
import java.lang.reflect.Type;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;
import net.minecraft.ResourceKeyInvalidException;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ChatDeserializer;

public final class MinecraftKey implements Comparable<MinecraftKey> {

    public static final Codec<MinecraftKey> CODEC = Codec.STRING.comapFlatMap(MinecraftKey::read, MinecraftKey::toString).stable();
    public static final StreamCodec<ByteBuf, MinecraftKey> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(MinecraftKey::parse, MinecraftKey::toString);
    public static final SimpleCommandExceptionType ERROR_INVALID = new SimpleCommandExceptionType(IChatBaseComponent.translatable("argument.id.invalid"));
    public static final char NAMESPACE_SEPARATOR = ':';
    public static final String DEFAULT_NAMESPACE = "minecraft";
    public static final String REALMS_NAMESPACE = "realms";
    private final String namespace;
    private final String path;

    private MinecraftKey(String s, String s1) {
        assert isValidNamespace(s);

        assert isValidPath(s1);

        this.namespace = s;
        this.path = s1;
    }

    private static MinecraftKey createUntrusted(String s, String s1) {
        return new MinecraftKey(assertValidNamespace(s, s1), assertValidPath(s, s1));
    }

    public static MinecraftKey fromNamespaceAndPath(String s, String s1) {
        return createUntrusted(s, s1);
    }

    public static MinecraftKey parse(String s) {
        return bySeparator(s, ':');
    }

    public static MinecraftKey withDefaultNamespace(String s) {
        return new MinecraftKey("minecraft", assertValidPath("minecraft", s));
    }

    @Nullable
    public static MinecraftKey tryParse(String s) {
        return tryBySeparator(s, ':');
    }

    @Nullable
    public static MinecraftKey tryBuild(String s, String s1) {
        return isValidNamespace(s) && isValidPath(s1) ? new MinecraftKey(s, s1) : null;
    }

    public static MinecraftKey bySeparator(String s, char c0) {
        int i = s.indexOf(c0);

        if (i >= 0) {
            String s1 = s.substring(i + 1);

            if (i != 0) {
                String s2 = s.substring(0, i);

                return createUntrusted(s2, s1);
            } else {
                return withDefaultNamespace(s1);
            }
        } else {
            return withDefaultNamespace(s);
        }
    }

    @Nullable
    public static MinecraftKey tryBySeparator(String s, char c0) {
        int i = s.indexOf(c0);

        if (i >= 0) {
            String s1 = s.substring(i + 1);

            if (!isValidPath(s1)) {
                return null;
            } else if (i != 0) {
                String s2 = s.substring(0, i);

                return isValidNamespace(s2) ? new MinecraftKey(s2, s1) : null;
            } else {
                return new MinecraftKey("minecraft", s1);
            }
        } else {
            return isValidPath(s) ? new MinecraftKey("minecraft", s) : null;
        }
    }

    public static DataResult<MinecraftKey> read(String s) {
        try {
            return DataResult.success(parse(s));
        } catch (ResourceKeyInvalidException resourcekeyinvalidexception) {
            return DataResult.error(() -> {
                return "Not a valid resource location: " + s + " " + resourcekeyinvalidexception.getMessage();
            });
        }
    }

    public String getPath() {
        return this.path;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public MinecraftKey withPath(String s) {
        return new MinecraftKey(this.namespace, assertValidPath(this.namespace, s));
    }

    public MinecraftKey withPath(UnaryOperator<String> unaryoperator) {
        return this.withPath((String) unaryoperator.apply(this.path));
    }

    public MinecraftKey withPrefix(String s) {
        return this.withPath(s + this.path);
    }

    public MinecraftKey withSuffix(String s) {
        return this.withPath(this.path + s);
    }

    public String toString() {
        return this.namespace + ":" + this.path;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else if (!(object instanceof MinecraftKey)) {
            return false;
        } else {
            MinecraftKey minecraftkey = (MinecraftKey) object;

            return this.namespace.equals(minecraftkey.namespace) && this.path.equals(minecraftkey.path);
        }
    }

    public int hashCode() {
        return 31 * this.namespace.hashCode() + this.path.hashCode();
    }

    public int compareTo(MinecraftKey minecraftkey) {
        int i = this.path.compareTo(minecraftkey.path);

        if (i == 0) {
            i = this.namespace.compareTo(minecraftkey.namespace);
        }

        return i;
    }

    public String toDebugFileName() {
        return this.toString().replace('/', '_').replace(':', '_');
    }

    public String toLanguageKey() {
        return this.namespace + "." + this.path;
    }

    public String toShortLanguageKey() {
        return this.namespace.equals("minecraft") ? this.path : this.toLanguageKey();
    }

    public String toLanguageKey(String s) {
        return s + "." + this.toLanguageKey();
    }

    public String toLanguageKey(String s, String s1) {
        return s + "." + this.toLanguageKey() + "." + s1;
    }

    private static String readGreedy(StringReader stringreader) {
        int i = stringreader.getCursor();

        while (stringreader.canRead() && isAllowedInResourceLocation(stringreader.peek())) {
            stringreader.skip();
        }

        return stringreader.getString().substring(i, stringreader.getCursor());
    }

    public static MinecraftKey read(StringReader stringreader) throws CommandSyntaxException {
        int i = stringreader.getCursor();
        String s = readGreedy(stringreader);

        try {
            return parse(s);
        } catch (ResourceKeyInvalidException resourcekeyinvalidexception) {
            stringreader.setCursor(i);
            throw MinecraftKey.ERROR_INVALID.createWithContext(stringreader);
        }
    }

    public static MinecraftKey readNonEmpty(StringReader stringreader) throws CommandSyntaxException {
        int i = stringreader.getCursor();
        String s = readGreedy(stringreader);

        if (s.isEmpty()) {
            throw MinecraftKey.ERROR_INVALID.createWithContext(stringreader);
        } else {
            try {
                return parse(s);
            } catch (ResourceKeyInvalidException resourcekeyinvalidexception) {
                stringreader.setCursor(i);
                throw MinecraftKey.ERROR_INVALID.createWithContext(stringreader);
            }
        }
    }

    public static boolean isAllowedInResourceLocation(char c0) {
        return c0 >= '0' && c0 <= '9' || c0 >= 'a' && c0 <= 'z' || c0 == '_' || c0 == ':' || c0 == '/' || c0 == '.' || c0 == '-';
    }

    public static boolean isValidPath(String s) {
        for (int i = 0; i < s.length(); ++i) {
            if (!validPathChar(s.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    public static boolean isValidNamespace(String s) {
        for (int i = 0; i < s.length(); ++i) {
            if (!validNamespaceChar(s.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    private static String assertValidNamespace(String s, String s1) {
        if (!isValidNamespace(s)) {
            throw new ResourceKeyInvalidException("Non [a-z0-9_.-] character in namespace of location: " + s + ":" + s1);
        } else {
            return s;
        }
    }

    public static boolean validPathChar(char c0) {
        return c0 == '_' || c0 == '-' || c0 >= 'a' && c0 <= 'z' || c0 >= '0' && c0 <= '9' || c0 == '/' || c0 == '.';
    }

    private static boolean validNamespaceChar(char c0) {
        return c0 == '_' || c0 == '-' || c0 >= 'a' && c0 <= 'z' || c0 >= '0' && c0 <= '9' || c0 == '.';
    }

    private static String assertValidPath(String s, String s1) {
        if (!isValidPath(s1)) {
            throw new ResourceKeyInvalidException("Non [a-z0-9/._-] character in path of location: " + s + ":" + s1);
        } else {
            return s1;
        }
    }

    public static class a implements JsonDeserializer<MinecraftKey>, JsonSerializer<MinecraftKey> {

        public a() {}

        public MinecraftKey deserialize(JsonElement jsonelement, Type type, JsonDeserializationContext jsondeserializationcontext) throws JsonParseException {
            return MinecraftKey.parse(ChatDeserializer.convertToString(jsonelement, "location"));
        }

        public JsonElement serialize(MinecraftKey minecraftkey, Type type, JsonSerializationContext jsonserializationcontext) {
            return new JsonPrimitive(minecraftkey.toString());
        }
    }
}
