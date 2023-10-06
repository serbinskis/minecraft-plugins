package net.minecraft.world.level.storage.loot.functions;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.network.chat.ChatComponentUtils;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameter;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.slf4j.Logger;

public class LootItemFunctionSetName extends LootItemFunctionConditional {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Codec<LootItemFunctionSetName> CODEC = RecordCodecBuilder.create((instance) -> {
        return commonFields(instance).and(instance.group(ExtraCodecs.strictOptionalField(ExtraCodecs.COMPONENT, "name").forGetter((lootitemfunctionsetname) -> {
            return lootitemfunctionsetname.name;
        }), ExtraCodecs.strictOptionalField(LootTableInfo.EntityTarget.CODEC, "entity").forGetter((lootitemfunctionsetname) -> {
            return lootitemfunctionsetname.resolutionContext;
        }))).apply(instance, LootItemFunctionSetName::new);
    });
    private final Optional<IChatBaseComponent> name;
    private final Optional<LootTableInfo.EntityTarget> resolutionContext;

    private LootItemFunctionSetName(List<LootItemCondition> list, Optional<IChatBaseComponent> optional, Optional<LootTableInfo.EntityTarget> optional1) {
        super(list);
        this.name = optional;
        this.resolutionContext = optional1;
    }

    @Override
    public LootItemFunctionType getType() {
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
            itemstack.setHoverName((IChatBaseComponent) createResolver(loottableinfo, (LootTableInfo.EntityTarget) this.resolutionContext.orElse((Object) null)).apply(ichatbasecomponent));
        });
        return itemstack;
    }

    public static LootItemFunctionConditional.a<?> setName(IChatBaseComponent ichatbasecomponent) {
        return simpleBuilder((list) -> {
            return new LootItemFunctionSetName(list, Optional.of(ichatbasecomponent), Optional.empty());
        });
    }

    public static LootItemFunctionConditional.a<?> setName(IChatBaseComponent ichatbasecomponent, LootTableInfo.EntityTarget loottableinfo_entitytarget) {
        return simpleBuilder((list) -> {
            return new LootItemFunctionSetName(list, Optional.of(ichatbasecomponent), Optional.of(loottableinfo_entitytarget));
        });
    }
}
