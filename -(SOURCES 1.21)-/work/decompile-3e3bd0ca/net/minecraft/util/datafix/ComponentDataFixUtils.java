package net.minecraft.util.datafix;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.Optional;
import net.minecraft.util.ChatDeserializer;

public class ComponentDataFixUtils {

    private static final String EMPTY_CONTENTS = createTextComponentJson("");

    public ComponentDataFixUtils() {}

    public static <T> Dynamic<T> createPlainTextComponent(DynamicOps<T> dynamicops, String s) {
        String s1 = createTextComponentJson(s);

        return new Dynamic(dynamicops, dynamicops.createString(s1));
    }

    public static <T> Dynamic<T> createEmptyComponent(DynamicOps<T> dynamicops) {
        return new Dynamic(dynamicops, dynamicops.createString(ComponentDataFixUtils.EMPTY_CONTENTS));
    }

    private static String createTextComponentJson(String s) {
        JsonObject jsonobject = new JsonObject();

        jsonobject.addProperty("text", s);
        return ChatDeserializer.toStableString(jsonobject);
    }

    public static <T> Dynamic<T> createTranslatableComponent(DynamicOps<T> dynamicops, String s) {
        JsonObject jsonobject = new JsonObject();

        jsonobject.addProperty("translate", s);
        return new Dynamic(dynamicops, dynamicops.createString(ChatDeserializer.toStableString(jsonobject)));
    }

    public static <T> Dynamic<T> wrapLiteralStringAsComponent(Dynamic<T> dynamic) {
        return (Dynamic) DataFixUtils.orElse(dynamic.asString().map((s) -> {
            return createPlainTextComponent(dynamic.getOps(), s);
        }).result(), dynamic);
    }

    public static Dynamic<?> rewriteFromLenient(Dynamic<?> dynamic) {
        Optional<String> optional = dynamic.asString().result();

        if (optional.isEmpty()) {
            return dynamic;
        } else {
            String s = (String) optional.get();

            if (!s.isEmpty() && !s.equals("null")) {
                char c0 = s.charAt(0);
                char c1 = s.charAt(s.length() - 1);

                if (c0 == '"' && c1 == '"' || c0 == '{' && c1 == '}' || c0 == '[' && c1 == ']') {
                    try {
                        JsonElement jsonelement = JsonParser.parseString(s);

                        if (jsonelement.isJsonPrimitive()) {
                            return createPlainTextComponent(dynamic.getOps(), jsonelement.getAsString());
                        }

                        return dynamic.createString(ChatDeserializer.toStableString(jsonelement));
                    } catch (JsonParseException jsonparseexception) {
                        ;
                    }
                }

                return createPlainTextComponent(dynamic.getOps(), s);
            } else {
                return createEmptyComponent(dynamic.getOps());
            }
        }
    }

    public static Optional<String> extractTranslationString(String s) {
        try {
            JsonElement jsonelement = JsonParser.parseString(s);

            if (jsonelement.isJsonObject()) {
                JsonObject jsonobject = jsonelement.getAsJsonObject();
                JsonElement jsonelement1 = jsonobject.get("translate");

                if (jsonelement1 != null && jsonelement1.isJsonPrimitive()) {
                    return Optional.of(jsonelement1.getAsString());
                }
            }
        } catch (JsonParseException jsonparseexception) {
            ;
        }

        return Optional.empty();
    }
}
