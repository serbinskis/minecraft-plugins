package net.minecraft.advancements;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.EnumChatFormat;
import net.minecraft.advancements.critereon.CriterionValidator;
import net.minecraft.core.HolderGetter;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.ChatComponentUtils;
import net.minecraft.network.chat.ChatHoverable;
import net.minecraft.network.chat.ChatModifier;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.IMaterial;

public record Advancement(Optional<MinecraftKey> parent, Optional<AdvancementDisplay> display, AdvancementRewards rewards, Map<String, Criterion<?>> criteria, AdvancementRequirements requirements, boolean sendsTelemetryEvent, Optional<IChatBaseComponent> name) {

    private static final Codec<Map<String, Criterion<?>>> CRITERIA_CODEC = Codec.unboundedMap(Codec.STRING, Criterion.CODEC).validate((map) -> {
        return map.isEmpty() ? DataResult.error(() -> {
            return "Advancement criteria cannot be empty";
        }) : DataResult.success(map);
    });
    public static final Codec<Advancement> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(MinecraftKey.CODEC.optionalFieldOf("parent").forGetter(Advancement::parent), AdvancementDisplay.CODEC.optionalFieldOf("display").forGetter(Advancement::display), AdvancementRewards.CODEC.optionalFieldOf("rewards", AdvancementRewards.EMPTY).forGetter(Advancement::rewards), Advancement.CRITERIA_CODEC.fieldOf("criteria").forGetter(Advancement::criteria), AdvancementRequirements.CODEC.optionalFieldOf("requirements").forGetter((advancement) -> {
            return Optional.of(advancement.requirements());
        }), Codec.BOOL.optionalFieldOf("sends_telemetry_event", false).forGetter(Advancement::sendsTelemetryEvent)).apply(instance, (optional, optional1, advancementrewards, map, optional2, obool) -> {
            AdvancementRequirements advancementrequirements = (AdvancementRequirements) optional2.orElseGet(() -> {
                return AdvancementRequirements.allOf(map.keySet());
            });

            return new Advancement(optional, optional1, advancementrewards, map, advancementrequirements, obool);
        });
    }).validate(Advancement::validate);
    public static final StreamCodec<RegistryFriendlyByteBuf, Advancement> STREAM_CODEC = StreamCodec.ofMember(Advancement::write, Advancement::read);

    public Advancement(Optional<MinecraftKey> optional, Optional<AdvancementDisplay> optional1, AdvancementRewards advancementrewards, Map<String, Criterion<?>> map, AdvancementRequirements advancementrequirements, boolean flag) {
        this(optional, optional1, advancementrewards, Map.copyOf(map), advancementrequirements, flag, optional1.map(Advancement::decorateName));
    }

    private static DataResult<Advancement> validate(Advancement advancement) {
        return advancement.requirements().validate(advancement.criteria().keySet()).map((advancementrequirements) -> {
            return advancement;
        });
    }

    private static IChatBaseComponent decorateName(AdvancementDisplay advancementdisplay) {
        IChatBaseComponent ichatbasecomponent = advancementdisplay.getTitle();
        EnumChatFormat enumchatformat = advancementdisplay.getType().getChatColor();
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

    private void write(RegistryFriendlyByteBuf registryfriendlybytebuf) {
        registryfriendlybytebuf.writeOptional(this.parent, PacketDataSerializer::writeResourceLocation);
        AdvancementDisplay.STREAM_CODEC.apply(ByteBufCodecs::optional).encode(registryfriendlybytebuf, this.display);
        this.requirements.write(registryfriendlybytebuf);
        registryfriendlybytebuf.writeBoolean(this.sendsTelemetryEvent);
    }

    private static Advancement read(RegistryFriendlyByteBuf registryfriendlybytebuf) {
        return new Advancement(registryfriendlybytebuf.readOptional(PacketDataSerializer::readResourceLocation), (Optional) AdvancementDisplay.STREAM_CODEC.apply(ByteBufCodecs::optional).decode(registryfriendlybytebuf), AdvancementRewards.EMPTY, Map.of(), new AdvancementRequirements(registryfriendlybytebuf), registryfriendlybytebuf.readBoolean());
    }

    public boolean isRoot() {
        return this.parent.isEmpty();
    }

    public void validate(ProblemReporter problemreporter, HolderGetter.a holdergetter_a) {
        this.criteria.forEach((s, criterion) -> {
            CriterionValidator criterionvalidator = new CriterionValidator(problemreporter.forChild(s), holdergetter_a);

            criterion.triggerInstance().validate(criterionvalidator);
        });
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
            return this.display(new AdvancementDisplay(itemstack, ichatbasecomponent, ichatbasecomponent1, Optional.ofNullable(minecraftkey), advancementframetype, flag, flag1, flag2));
        }

        public Advancement.SerializedAdvancement display(IMaterial imaterial, IChatBaseComponent ichatbasecomponent, IChatBaseComponent ichatbasecomponent1, @Nullable MinecraftKey minecraftkey, AdvancementFrameType advancementframetype, boolean flag, boolean flag1, boolean flag2) {
            return this.display(new AdvancementDisplay(new ItemStack(imaterial.asItem()), ichatbasecomponent, ichatbasecomponent1, Optional.ofNullable(minecraftkey), advancementframetype, flag, flag1, flag2));
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
