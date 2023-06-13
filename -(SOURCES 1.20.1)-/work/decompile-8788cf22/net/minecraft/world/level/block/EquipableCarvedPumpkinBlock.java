package net.minecraft.world.level.block;

import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.level.block.state.BlockBase;

public class EquipableCarvedPumpkinBlock extends BlockPumpkinCarved implements Equipable {

    protected EquipableCarvedPumpkinBlock(BlockBase.Info blockbase_info) {
        super(blockbase_info);
    }

    @Override
    public EnumItemSlot getEquipmentSlot() {
        return EnumItemSlot.HEAD;
    }
}
