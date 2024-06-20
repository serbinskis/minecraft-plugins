package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.ContainerComponentManipulator;
import net.minecraft.world.level.storage.loot.ContainerComponentManipulators;
import net.minecraft.world.level.storage.loot.LootCollector;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class ModifyContainerContents extends LootItemFunctionConditional {

    public static final MapCodec<ModifyContainerContents> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return commonFields(instance).and(instance.group(ContainerComponentManipulators.CODEC.fieldOf("component").forGetter((modifycontainercontents) -> {
            return modifycontainercontents.component;
        }), LootItemFunctions.ROOT_CODEC.fieldOf("modifier").forGetter((modifycontainercontents) -> {
            return modifycontainercontents.modifier;
        }))).apply(instance, ModifyContainerContents::new);
    });
    private final ContainerComponentManipulator<?> component;
    private final LootItemFunction modifier;

    private ModifyContainerContents(List<LootItemCondition> list, ContainerComponentManipulator<?> containercomponentmanipulator, LootItemFunction lootitemfunction) {
        super(list);
        this.component = containercomponentmanipulator;
        this.modifier = lootitemfunction;
    }

    @Override
    public LootItemFunctionType<ModifyContainerContents> getType() {
        return LootItemFunctions.MODIFY_CONTENTS;
    }

    @Override
    public ItemStack run(ItemStack itemstack, LootTableInfo loottableinfo) {
        if (itemstack.isEmpty()) {
            return itemstack;
        } else {
            this.component.modifyItems(itemstack, (itemstack1) -> {
                return (ItemStack) this.modifier.apply(itemstack1, loottableinfo);
            });
            return itemstack;
        }
    }

    @Override
    public void validate(LootCollector lootcollector) {
        super.validate(lootcollector);
        this.modifier.validate(lootcollector.forChild(".modifier"));
    }
}
