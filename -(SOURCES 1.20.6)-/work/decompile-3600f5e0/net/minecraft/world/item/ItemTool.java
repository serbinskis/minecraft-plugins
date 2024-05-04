package net.minecraft.world.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.block.Block;

public class ItemTool extends ItemToolMaterial {

    protected ItemTool(ToolMaterial toolmaterial, TagKey<Block> tagkey, Item.Info item_info) {
        super(toolmaterial, item_info.component(DataComponents.TOOL, toolmaterial.createToolProperties(tagkey)));
    }

    public static ItemAttributeModifiers createAttributes(ToolMaterial toolmaterial, float f, float f1) {
        return ItemAttributeModifiers.builder().add(GenericAttributes.ATTACK_DAMAGE, new AttributeModifier(ItemTool.BASE_ATTACK_DAMAGE_UUID, "Tool modifier", (double) (f + toolmaterial.getAttackDamageBonus()), AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND).add(GenericAttributes.ATTACK_SPEED, new AttributeModifier(ItemTool.BASE_ATTACK_SPEED_UUID, "Tool modifier", (double) f1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND).build();
    }

    @Override
    public boolean hurtEnemy(ItemStack itemstack, EntityLiving entityliving, EntityLiving entityliving1) {
        itemstack.hurtAndBreak(2, entityliving1, EnumItemSlot.MAINHAND);
        return true;
    }
}
