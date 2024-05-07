package net.minecraft.world.item;

import java.util.List;
import java.util.Objects;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.stats.StatisticList;
import net.minecraft.world.EnumHand;
import net.minecraft.world.InteractionResultWrapper;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.World;

public class OminousBottleItem extends Item {

    private static final int DRINK_DURATION = 32;
    public static final int EFFECT_DURATION = 120000;
    public static final int MIN_AMPLIFIER = 0;
    public static final int MAX_AMPLIFIER = 4;

    public OminousBottleItem(Item.Info item_info) {
        super(item_info);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack itemstack, World world, EntityLiving entityliving) {
        if (entityliving instanceof EntityPlayer entityplayer) {
            CriterionTriggers.CONSUME_ITEM.trigger(entityplayer, itemstack);
            entityplayer.awardStat(StatisticList.ITEM_USED.get(this));
        }

        itemstack.consume(1, entityliving);
        if (!world.isClientSide) {
            world.playSound((EntityHuman) null, entityliving.blockPosition(), SoundEffects.OMINOUS_BOTTLE_DISPOSE, entityliving.getSoundSource(), 1.0F, 1.0F);
            Integer integer = (Integer) itemstack.getOrDefault(DataComponents.OMINOUS_BOTTLE_AMPLIFIER, 0);

            entityliving.removeEffect(MobEffects.BAD_OMEN);
            entityliving.addEffect(new MobEffect(MobEffects.BAD_OMEN, 120000, integer, false, false, true));
        }

        return itemstack;
    }

    @Override
    public int getUseDuration(ItemStack itemstack) {
        return 32;
    }

    @Override
    public EnumAnimation getUseAnimation(ItemStack itemstack) {
        return EnumAnimation.DRINK;
    }

    @Override
    public InteractionResultWrapper<ItemStack> use(World world, EntityHuman entityhuman, EnumHand enumhand) {
        return ItemLiquidUtil.startUsingInstantly(world, entityhuman, enumhand);
    }

    @Override
    public void appendHoverText(ItemStack itemstack, Item.b item_b, List<IChatBaseComponent> list, TooltipFlag tooltipflag) {
        super.appendHoverText(itemstack, item_b, list, tooltipflag);
        Integer integer = (Integer) itemstack.getOrDefault(DataComponents.OMINOUS_BOTTLE_AMPLIFIER, 0);
        List<MobEffect> list1 = List.of(new MobEffect(MobEffects.BAD_OMEN, 120000, integer, false, false, true));

        Objects.requireNonNull(list);
        PotionContents.addPotionTooltip(list1, list::add, 1.0F, item_b.tickRate());
    }
}
