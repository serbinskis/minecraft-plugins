package net.minecraft.advancements;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.EnumChatFormat;
import net.minecraft.advancements.critereon.LootDeserializationContext;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.chat.ChatComponentUtils;
import net.minecraft.network.chat.ChatHoverable;
import net.minecraft.network.chat.ChatModifier;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.util.ChatDeserializer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.IMaterial;

public record Advancement(Optional<MinecraftKey> parent, Optional<AdvancementDisplay> display, AdvancementRewards rewards, Map<String, Criterion<?>> criteria, AdvancementRequirements requirements, boolean sendsTelemetryEvent, Optional<IChatBaseComponent> name) {

    public Advancement(Optional<MinecraftKey> optional, Optional<AdvancementDisplay> optional1, AdvancementRewards advancementrewards, Map<String, Criterion<?>> map, AdvancementRequirements advancementrequirements, boolean flag) {
        this(optional, optional1, advancementrewards, Map.copyOf(map), advancementrequirements, flag, optional1.map(Advancement::decorateName));
    }

    private static IChatBaseComponent decorateName(AdvancementDisplay advancementdisplay) {
        IChatBaseComponent ichatbasecomponent = advancementdisplay.getTitle();
        EnumChatFormat enumchatformat = advancementdisplay.getFrame().getChatColor();
        IChatMutableComponent ichatmutablecomponent = ChatComponentUtils.mergeStyles(ichatbasecomponent.copy(), ChatModifier.EMPTY.withColor(enumchatformat)).append("\n").append(advancementdisplay.getDescription());
        IChatMutableComponent ichatmutablecomponent1 = ichatbasecomponent.copy().withStyle((chatmodifier) -> {
            return chatmodifier.withHoverEvent(new ChatHoverable(ChatHoverable.EnumHoverAction.SHOW_TEXT, ichatmutablecomponent));
        });

        return ChatComponentUtils.wrapInSquareBrackets(ichatmutablecomponent1).withStyle(enumchatformat);
    }

    public static IChatBaseComponent name(AdvancementHolder advancementholder) {
        return (IChatBaseComponent) advancementholder.value().name().orElseGet(() -> {
            return IChatBaseComponent.literal(advancementholder.id().toString());
        });
    }

    public JsonObject serializeToJson() {
        JsonObject jsonobject = new JsonObject();

        this.parent.ifPresent((minecraftkey) -> {
            jsonobject.addProperty("parent", minecraftkey.toString());
        });
        this.display.ifPresent((advancementdisplay) -> {
            jsonobject.add("display", advancementdisplay.serializeToJson());
        });
        jsonobject.add("rewards", this.rewards.serializeToJson());
        JsonObject jsonobject1 = new JsonObject();
        Iterator iterator = this.criteria.entrySet().iterator();

        while (iterator.hasNext()) {
            Entry<String, Criterion<?>> entry = (Entry) iterator.next();

            jsonobject1.add((String) entry.getKey(), ((Criterion) entry.getValue()).serializeToJson());
        }

        jsonobject.add("criteria", jsonobject1);
        jsonobject.add("requirements", this.requirements.toJson());
        jsonobject.addProperty("sends_telemetry_event", this.sendsTelemetryEvent);
        return jsonobject;
    }

    public static Advancement fromJson(JsonObject jsonobject, LootDeserializationContext lootdeserializationcontext) {
        Optional<MinecraftKey> optional = jsonobject.has("parent") ? Optional.of(new MinecraftKey(ChatDeserializer.getAsString(jsonobject, "parent"))) : Optional.empty();
        Optional<AdvancementDisplay> optional1 = jsonobject.has("display") ? Optional.of(AdvancementDisplay.fromJson(ChatDeserializer.getAsJsonObject(jsonobject, "display"))) : Optional.empty();
        AdvancementRewards advancementrewards = jsonobject.has("rewards") ? AdvancementRewards.deserialize(ChatDeserializer.getAsJsonObject(jsonobject, "rewards")) : AdvancementRewards.EMPTY;
        Map<String, Criterion<?>> map = Criterion.criteriaFromJson(ChatDeserializer.getAsJsonObject(jsonobject, "criteria"), lootdeserializationcontext);

        if (map.isEmpty()) {
            throw new JsonSyntaxException("Advancement criteria cannot be empty");
        } else {
            JsonArray jsonarray = ChatDeserializer.getAsJsonArray(jsonobject, "requirements", new JsonArray());
            AdvancementRequirements advancementrequirements;

            if (jsonarray.isEmpty()) {
                advancementrequirements = AdvancementRequirements.allOf(map.keySet());
            } else {
                advancementrequirements = AdvancementRequirements.fromJson(jsonarray, map.keySet());
            }

            boolean flag = ChatDeserializer.getAsBoolean(jsonobject, "sends_telemetry_event", false);

            return new Advancement(optional, optional1, advancementrewards, map, advancementrequirements, flag);
        }
    }

    public void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeOptional(this.parent, PacketDataSerializer::writeResourceLocation);
        packetdataserializer.writeOptional(this.display, (packetdataserializer1, advancementdisplay) -> {
            advancementdisplay.serializeToNetwork(packetdataserializer1);
        });
        this.requirements.write(packetdataserializer);
        packetdataserializer.writeBoolean(this.sendsTelemetryEvent);
    }

    public static Advancement read(PacketDataSerializer packetdataserializer) {
        return new Advancement(packetdataserializer.readOptional(PacketDataSerializer::readResourceLocation), packetdataserializer.readOptional(AdvancementDisplay::fromNetwork), AdvancementRewards.EMPTY, Map.of(), new AdvancementRequirements(packetdataserializer), packetdataserializer.readBoolean());
    }

    public boolean isRoot() {
        return this.parent.isEmpty();
    }

    public static class SerializedAdvancement {

        private Optional<MinecraftKey> parent = Optional.empty();
        private Optional<AdvancementDisplay> display = Optional.empty();
        private AdvancementRewards rewards;
        private final Builder<String, Criterion<?>> criteria;
        private Optional<AdvancementRequirements> requirements;
        private AdvancementRequirements.a requirementsStrategy;
        private boolean sendsTelemetryEvent;

        public SerializedAdvancement() {
            this.rewards = AdvancementRewards.EMPTY;
            this.criteria = ImmutableMap.builder();
            this.requirements = Optional.empty();
            this.requirementsStrategy = AdvancementRequirements.a.AND;
        }

        public static Advancement.SerializedAdvancement advancement() {
            return (new Advancement.SerializedAdvancement()).sendsTelemetryEvent();
        }

        public static Advancement.SerializedAdvancement recipeAdvancement() {
            return new Advancement.SerializedAdvancement();
        }

        public Advancement.SerializedAdvancement parent(AdvancementHolder advancementholder) {
            this.parent = Optional.of(advancementholder.id());
            return this;
        }

        /** @deprecated */
        @Deprecated(forRemoval = true)
        public Advancement.SerializedAdvancement parent(MinecraftKey minecraftkey) {
            this.parent = Optional.of(minecraftkey);
            return this;
        }

        public Advancement.SerializedAdvancement display(ItemStack itemstack, IChatBaseComponent ichatbasecomponent, IChatBaseComponent ichatbasecomponent1, @Nullable MinecraftKey minecraftkey, AdvancementFrameType advancementframetype, boolean flag, boolean flag1, boolean flag2) {
            return this.display(new AdvancementDisplay(itemstack, ichatbasecomponent, ichatbasecomponent1, minecraftkey, advancementframetype, flag, flag1, flag2));
        }

        public Advancement.SerializedAdvancement display(IMaterial imaterial, IChatBaseComponent ichatbasecomponent, IChatBaseComponent ichatbasecomponent1, @Nullable MinecraftKey minecraftkey, AdvancementFrameType advancementframetype, boolean flag, boolean flag1, boolean flag2) {
            return this.display(new AdvancementDisplay(new ItemStack(imaterial.asItem()), ichatbasecomponent, ichatbasecomponent1, minecraftkey, advancementframetype, flag, flag1, flag2));
        }

        public Advancement.SerializedAdvancement display(AdvancementDisplay advancementdisplay) {
            this.display = Optional.of(advancementdisplay);
            return this;
        }

        public Advancement.SerializedAdvancement rewards(AdvancementRewards.a advancementrewards_a) {
            return this.rewards(advancementrewards_a.build());
        }

        public Advancement.SerializedAdvancement rewards(AdvancementRewards advancementrewards) {
            this.rewards = advancementrewards;
            return this;
        }

        public Advancement.SerializedAdvancement addCriterion(String s, Criterion<?> criterion) {
            this.criteria.put(s, criterion);
            return this;
        }

        public Advancement.SerializedAdvancement requirements(AdvancementRequirements.a advancementrequirements_a) {
            this.requirementsStrategy = advancementrequirements_a;
            return this;
        }

        public Advancement.SerializedAdvancement requirements(AdvancementRequirements advancementrequirements) {
            this.requirements = Optional.of(advancementrequirements);
            return this;
        }

        public Advancement.SerializedAdvancement sendsTelemetryEvent() {
            this.sendsTelemetryEvent = true;
            return this;
        }

        public AdvancementHolder build(MinecraftKey minecraftkey) {
            Map<String, Criterion<?>> map = this.criteria.buildOrThrow();
            AdvancementRequirements advancementrequirements = (AdvancementRequirements) this.requirements.orElseGet(() -> {
                return this.requirementsStrategy.create(map.keySet());
            });

            return new AdvancementHolder(minecraftkey, new Advancement(this.parent, this.display, this.rewards, map, advancementrequirements, this.sendsTelemetryEvent));
        }

        public AdvancementHolder save(Consumer<AdvancementHolder> consumer, String s) {
            AdvancementHolder advancementholder = this.build(new MinecraftKey(s));

            consumer.accept(advancementholder);
            return advancementholder;
        }
    }
}
