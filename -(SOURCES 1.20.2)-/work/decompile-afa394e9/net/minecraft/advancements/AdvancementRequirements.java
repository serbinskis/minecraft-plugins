package net.minecraft.advancements;

import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonSyntaxException;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.util.ChatDeserializer;

public record AdvancementRequirements(String[][] requirements) {

    public static final AdvancementRequirements EMPTY = new AdvancementRequirements(new String[0][]);

    public AdvancementRequirements(PacketDataSerializer packetdataserializer) {
        this(read(packetdataserializer));
    }

    private static String[][] read(PacketDataSerializer packetdataserializer) {
        String[][] astring = new String[packetdataserializer.readVarInt()][];

        for (int i = 0; i < astring.length; ++i) {
            astring[i] = new String[packetdataserializer.readVarInt()];

            for (int j = 0; j < astring[i].length; ++j) {
                astring[i][j] = packetdataserializer.readUtf();
            }
        }

        return astring;
    }

    public void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeVarInt(this.requirements.length);
        String[][] astring = this.requirements;
        int i = astring.length;

        for (int j = 0; j < i; ++j) {
            String[] astring1 = astring[j];

            packetdataserializer.writeVarInt(astring1.length);
            String[] astring2 = astring1;
            int k = astring1.length;

            for (int l = 0; l < k; ++l) {
                String s = astring2[l];

                packetdataserializer.writeUtf(s);
            }
        }

    }

    public static AdvancementRequirements allOf(Collection<String> collection) {
        return new AdvancementRequirements((String[][]) collection.stream().map((s) -> {
            return new String[]{s};
        }).toArray((i) -> {
            return new String[i][];
        }));
    }

    public static AdvancementRequirements anyOf(Collection<String> collection) {
        return new AdvancementRequirements(new String[][]{(String[]) collection.toArray((i) -> {
                    return new String[i];
                })});
    }

    public int size() {
        return this.requirements.length;
    }

    public boolean test(Predicate<String> predicate) {
        if (this.requirements.length == 0) {
            return false;
        } else {
            String[][] astring = this.requirements;
            int i = astring.length;

            for (int j = 0; j < i; ++j) {
                String[] astring1 = astring[j];

                if (!anyMatch(astring1, predicate)) {
                    return false;
                }
            }

            return true;
        }
    }

    public int count(Predicate<String> predicate) {
        int i = 0;
        String[][] astring = this.requirements;
        int j = astring.length;

        for (int k = 0; k < j; ++k) {
            String[] astring1 = astring[k];

            if (anyMatch(astring1, predicate)) {
                ++i;
            }
        }

        return i;
    }

    private static boolean anyMatch(String[] astring, Predicate<String> predicate) {
        String[] astring1 = astring;
        int i = astring.length;

        for (int j = 0; j < i; ++j) {
            String s = astring1[j];

            if (predicate.test(s)) {
                return true;
            }
        }

        return false;
    }

    public static AdvancementRequirements fromJson(JsonArray jsonarray, Set<String> set) {
        String[][] astring = new String[jsonarray.size()][];
        Set<String> set1 = new ObjectOpenHashSet();

        for (int i = 0; i < jsonarray.size(); ++i) {
            JsonArray jsonarray1 = ChatDeserializer.convertToJsonArray(jsonarray.get(i), "requirements[" + i + "]");

            if (jsonarray1.isEmpty() && set.isEmpty()) {
                throw new JsonSyntaxException("Requirement entry cannot be empty");
            }

            astring[i] = new String[jsonarray1.size()];

            for (int j = 0; j < jsonarray1.size(); ++j) {
                String s = ChatDeserializer.convertToString(jsonarray1.get(j), "requirements[" + i + "][" + j + "]");

                astring[i][j] = s;
                set1.add(s);
            }
        }

        if (!set.equals(set1)) {
            Set<String> set2 = Sets.difference(set, set1);
            Set<String> set3 = Sets.difference(set1, set);

            throw new JsonSyntaxException("Advancement completion requirements did not exactly match specified criteria. Missing: " + set2 + ". Unknown: " + set3);
        } else {
            return new AdvancementRequirements(astring);
        }
    }

    public JsonArray toJson() {
        JsonArray jsonarray = new JsonArray();
        String[][] astring = this.requirements;
        int i = astring.length;

        for (int j = 0; j < i; ++j) {
            String[] astring1 = astring[j];
            JsonArray jsonarray1 = new JsonArray();
            Stream stream = Arrays.stream(astring1);

            Objects.requireNonNull(jsonarray1);
            stream.forEach(jsonarray1::add);
            jsonarray.add(jsonarray1);
        }

        return jsonarray;
    }

    public boolean isEmpty() {
        return this.requirements.length == 0;
    }

    public String toString() {
        return Arrays.deepToString(this.requirements);
    }

    public Set<String> names() {
        Set<String> set = new ObjectOpenHashSet();
        String[][] astring = this.requirements;
        int i = astring.length;

        for (int j = 0; j < i; ++j) {
            String[] astring1 = astring[j];

            Collections.addAll(set, astring1);
        }

        return set;
    }

    public interface a {

        AdvancementRequirements.a AND = AdvancementRequirements::allOf;
        AdvancementRequirements.a OR = AdvancementRequirements::anyOf;

        AdvancementRequirements create(Collection<String> collection);
    }
}
