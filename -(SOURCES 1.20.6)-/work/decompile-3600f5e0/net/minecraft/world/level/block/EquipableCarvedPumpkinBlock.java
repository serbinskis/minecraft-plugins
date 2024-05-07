package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.level.block.state.BlockBase;

public class EquipableCarvedPumpkinBlock extends BlockPumpkinCarved implements Equipable {

    public static final MapCodec<EquipableCarvedPumpkinBlock> CODEC = simpleCodec(EquipableCarvedPumpkinBlock::new);

    @Override
    public MapCodec<EquipableCarvedPumpkinBlock> codec() {
        return EquipableCarvedPumpkinBlock.CODEC;
    }

    protected EquipableCarvedPumpkinBlock(BlockBase.Info blockbase_info) {
        super(blockbase_info);
    }

    @Override
    public EnumItemSlot getEquipmentSlot() {
        return EnumItemSlot.HEAD;
    }
}
