package net.minecraft.world.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.EnumChatFormat;
import net.minecraft.advancements.critereon.CriterionConditionBlock;
import net.minecraft.core.HolderSet;
import net.minecraft.core.IRegistryCustom;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.pattern.ShapeDetectorBlock;

public class AdventureModePredicate {

    private static final Codec<AdventureModePredicate> SIMPLE_CODEC = CriterionConditionBlock.CODEC.flatComapMap((criterionconditionblock) -> {
        return new AdventureModePredicate(List.of(criterionconditionblock), true);
    }, (adventuremodepredicate) -> {
        return DataResult.error(() -> {
            return "Cannot encode";
        });
    });
    private static final Codec<AdventureModePredicate> FULL_CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(ExtraCodecs.nonEmptyList(CriterionConditionBlock.CODEC.listOf()).fieldOf("predicates").forGetter((adventuremodepredicate) -> {
            return adventuremodepredicate.predicates;
        }), Codec.BOOL.optionalFieldOf("show_in_tooltip", true).forGetter(AdventureModePredicate::showInTooltip)).apply(instance, AdventureModePredicate::new);
    });
    public static final Codec<AdventureModePredicate> CODEC = Codec.withAlternative(AdventureModePredicate.FULL_CODEC, AdventureModePredicate.SIMPLE_CODEC);
    public static final StreamCodec<RegistryFriendlyByteBuf, AdventureModePredicate> STREAM_CODEC = StreamCodec.composite(CriterionConditionBlock.STREAM_CODEC.apply(ByteBufCodecs.list()), (adventuremodepredicate) -> {
        return adventuremodepredicate.predicates;
    }, ByteBufCodecs.BOOL, AdventureModePredicate::showInTooltip, AdventureModePredicate::new);
    public static final IChatBaseComponent CAN_BREAK_HEADER = IChatBaseComponent.translatable("item.canBreak").withStyle(EnumChatFormat.GRAY);
    public static final IChatBaseComponent CAN_PLACE_HEADER = IChatBaseComponent.translatable("item.canPlace").withStyle(EnumChatFormat.GRAY);
    private static final IChatBaseComponent UNKNOWN_USE = IChatBaseComponent.translatable("item.canUse.unknown").withStyle(EnumChatFormat.GRAY);
    private final List<CriterionConditionBlock> predicates;
    private final boolean showInTooltip;
    private final List<IChatBaseComponent> tooltip;
    @Nullable
    private ShapeDetectorBlock lastCheckedBlock;
    private boolean lastResult;
    private boolean checksBlockEntity;

    private AdventureModePredicate(List<CriterionConditionBlock> list, boolean flag, List<IChatBaseComponent> list1) {
        this.predicates = list;
        this.showInTooltip = flag;
        this.tooltip = list1;
    }

    public AdventureModePredicate(List<CriterionConditionBlock> list, boolean flag) {
        this.predicates = list;
        this.showInTooltip = flag;
        this.tooltip = computeTooltip(list);
    }

    private static boolean areSameBlocks(ShapeDetectorBlock shapedetectorblock, @Nullable ShapeDetectorBlock shapedetectorblock1, boolean flag) {
        if (shapedetectorblock1 != null && shapedetectorblock.getState() == shapedetectorblock1.getState()) {
            if (!flag) {
                return true;
            } else if (shapedetectorblock.getEntity() == null && shapedetectorblock1.getEntity() == null) {
                return true;
            } else if (shapedetectorblock.getEntity() != null && shapedetectorblock1.getEntity() != null) {
                IRegistryCustom iregistrycustom = shapedetectorblock.getLevel().registryAccess();

                return Objects.equals(shapedetectorblock.getEntity().saveWithId(iregistrycustom), shapedetectorblock1.getEntity().saveWithId(iregistrycustom));
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean test(ShapeDetectorBlock shapedetectorblock) {
        if (areSameBlocks(shapedetectorblock, this.lastCheckedBlock, this.checksBlockEntity)) {
            return this.lastResult;
        } else {
            this.lastCheckedBlock = shapedetectorblock;
            this.checksBlockEntity = false;
            Iterator iterator = this.predicates.iterator();

            CriterionConditionBlock criterionconditionblock;

            do {
                if (!iterator.hasNext()) {
                    this.lastResult = false;
                    return false;
                }

                criterionconditionblock = (CriterionConditionBlock) iterator.next();
            } while (!criterionconditionblock.matches(shapedetectorblock));

            this.checksBlockEntity |= criterionconditionblock.requiresNbt();
            this.lastResult = true;
            return true;
        }
    }

    public void addToTooltip(Consumer<IChatBaseComponent> consumer) {
        this.tooltip.forEach(consumer);
    }

    public AdventureModePredicate withTooltip(boolean flag) {
        return new AdventureModePredicate(this.predicates, flag, this.tooltip);
    }

    private static List<IChatBaseComponent> computeTooltip(List<CriterionConditionBlock> list) {
        Iterator iterator = list.iterator();

        CriterionConditionBlock criterionconditionblock;

        do {
            if (!iterator.hasNext()) {
                return list.stream().flatMap((criterionconditionblock1) -> {
                    return ((HolderSet) criterionconditionblock1.blocks().orElseThrow()).stream();
                }).distinct().map((holder) -> {
                    return ((Block) holder.value()).getName().withStyle(EnumChatFormat.DARK_GRAY);
                }).toList();
            }

            criterionconditionblock = (CriterionConditionBlock) iterator.next();
        } while (!criterionconditionblock.blocks().isEmpty());

        return List.of(AdventureModePredicate.UNKNOWN_USE);
    }

    public boolean showInTooltip() {
        return this.showInTooltip;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else if (!(object instanceof AdventureModePredicate)) {
            return false;
        } else {
            AdventureModePredicate adventuremodepredicate = (AdventureModePredicate) object;

            return this.predicates.equals(adventuremodepredicate.predicates) && this.showInTooltip == adventuremodepredicate.showInTooltip;
        }
    }

    public int hashCode() {
        return this.predicates.hashCode() * 31 + (this.showInTooltip ? 1 : 0);
    }

    public String toString() {
        String s = String.valueOf(this.predicates);

        return "AdventureModePredicate{predicates=" + s + ", showInTooltip=" + this.showInTooltip + "}";
    }
}
