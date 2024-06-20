package net.minecraft.world.level.storage.loot.functions;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.ChatComponentUtils;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.util.INamable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameter;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.slf4j.Logger;

public class LootItemFunctionSetName extends LootItemFunctionConditional {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final MapCodec<LootItemFunctionSetName> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return commonFields(instance).and(instance.group(ComponentSerialization.CODEC.optionalFieldOf("name").forGetter((lootitemfunctionsetname) -> {
            return lootitemfunctionsetname.name;
        }), LootTableInfo.EntityTarget.CODEC.optionalFieldOf("entity").forGetter((lootitemfunctionsetname) -> {
            return lootitemfunctionsetname.resolutionContext;
        }), LootItemFunctionSetName.a.CODEC.optionalFieldOf("target", LootItemFunctionSetName.a.CUSTOM_NAME).forGetter((lootitemfunctionsetname) -> {
            return lootitemfunctionsetname.target;
        }))).apply(instance, LootItemFunctionSetName::new);
    });
    private final Optional<IChatBaseComponent> name;
    private final Optional<LootTableInfo.EntityTarget> resolutionContext;
    private final LootItemFunctionSetName.a target;

    private LootItemFunctionSetName(List<LootItemCondition> list, Optional<IChatBaseComponent> optional, Optional<LootTableInfo.EntityTarget> optional1, LootItemFunctionSetName.a lootitemfunctionsetname_a) {
        super(list);
        this.name = optional;
        this.resolutionContext = optional1;
        this.target = lootitemfunctionsetname_a;
    }

    @Override
    public LootItemFunctionType<LootItemFunctionSetName> getType() {
        return LootItemFunctions.SET_NAME;
    }

    @Override
    public Set<LootContextParameter<?>> getReferencedContextParams() {
        return (Set) this.resolutionContext.map((loottableinfo_entitytarget) -> {
            return Set.of(loottableinfo_entitytarget.getParam());
        }).orElse(Set.of());
    }

    public static UnaryOperator<IChatBaseComponent> createResolver(LootTableInfo loottableinfo, @Nullable LootTableInfo.EntityTarget loottableinfo_entitytarget) {
        if (loottableinfo_entitytarget != null) {
            Entity entity = (Entity) loottableinfo.getParamOrNull(loottableinfo_entitytarget.getParam());

            if (entity != null) {
                CommandListenerWrapper commandlistenerwrapper = entity.createCommandSourceStack().withPermission(2);

                return (ichatbasecomponent) -> {
                    try {
                        return ChatComponentUtils.updateForEntity(commandlistenerwrapper, ichatbasecomponent, entity, 0);
                    } catch (CommandSyntaxException commandsyntaxexception) {
                        LootItemFunctionSetName.LOGGER.warn("Failed to resolve text component", commandsyntaxexception);
                        return ichatbasecomponent;
                    }
                };
            }
        }

        return (ichatbasecomponent) -> {
            return ichatbasecomponent;
        };
    }

    @Override
    public ItemStack run(ItemStack itemstack, LootTableInfo loottableinfo) {
        this.name.ifPresent((ichatbasecomponent) -> {
            itemstack.set(this.target.component(), (IChatBaseComponent) createResolver(loottableinfo, (LootTableInfo.EntityTarget) this.resolutionContext.orElse((Object) null)).apply(ichatbasecomponent));
        });
        return itemstack;
    }

    public static LootItemFunctionConditional.a<?> setName(IChatBaseComponent ichatbasecomponent, LootItemFunctionSetName.a lootitemfunctionsetname_a) {
        return simpleBuilder((list) -> {
            return new LootItemFunctionSetName(list, Optional.of(ichatbasecomponent), Optional.empty(), lootitemfunctionsetname_a);
        });
    }

    public static LootItemFunctionConditional.a<?> setName(IChatBaseComponent ichatbasecomponent, LootItemFunctionSetName.a lootitemfunctionsetname_a, LootTableInfo.EntityTarget loottableinfo_entitytarget) {
        return simpleBuilder((list) -> {
            return new LootItemFunctionSetName(list, Optional.of(ichatbasecomponent), Optional.of(loottableinfo_entitytarget), lootitemfunctionsetname_a);
        });
    }

    public static enum a implements INamable {

        CUSTOM_NAME("custom_name"), ITEM_NAME("item_name");

        public static final Codec<LootItemFunctionSetName.a> CODEC = INamable.fromEnum(LootItemFunctionSetName.a::values);
        private final String name;

        private a(final String s) {
            this.name = s;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public DataComponentType<IChatBaseComponent> component() {
            DataComponentType datacomponenttype;

            switch (this.ordinal()) {
                case 0:
                    datacomponenttype = DataComponents.CUSTOM_NAME;
                    break;
                case 1:
                    datacomponenttype = DataComponents.ITEM_NAME;
                    break;
                default:
                    throw new MatchException((String) null, (Throwable) null);
            }

            return datacomponenttype;
        }
    }
}
