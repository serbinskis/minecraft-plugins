package net.minecraft.world.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.alchemy.PotionUtil;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.SuspiciousEffectHolder;

public class ItemSuspiciousStew extends Item {

    public static final String EFFECTS_TAG = "effects";
    public static final int DEFAULT_DURATION = 160;

    public ItemSuspiciousStew(Item.Info item_info) {
        super(item_info);
    }

    public static void saveMobEffects(ItemStack itemstack, List<SuspiciousEffectHolder.a> list) {
        NBTTagCompound nbttagcompound = itemstack.getOrCreateTag();

        SuspiciousEffectHolder.a.LIST_CODEC.encodeStart(DynamicOpsNBT.INSTANCE, list).result().ifPresent((nbtbase) -> {
            nbttagcompound.put("effects", nbtbase);
        });
    }

    public static void appendMobEffects(ItemStack itemstack, List<SuspiciousEffectHolder.a> list) {
        NBTTagCompound nbttagcompound = itemstack.getOrCreateTag();
        List<SuspiciousEffectHolder.a> list1 = new ArrayList();

        Objects.requireNonNull(list1);
        listPotionEffects(itemstack, list1::add);
        list1.addAll(list);
        SuspiciousEffectHolder.a.LIST_CODEC.encodeStart(DynamicOpsNBT.INSTANCE, list1).result().ifPresent((nbtbase) -> {
            nbttagcompound.put("effects", nbtbase);
        });
    }

    private static void listPotionEffects(ItemStack itemstack, Consumer<SuspiciousEffectHolder.a> consumer) {
        NBTTagCompound nbttagcompound = itemstack.getTag();

        if (nbttagcompound != null && nbttagcompound.contains("effects", 9)) {
            SuspiciousEffectHolder.a.LIST_CODEC.parse(DynamicOpsNBT.INSTANCE, nbttagcompound.getList("effects", 10)).result().ifPresent((list) -> {
                list.forEach(consumer);
            });
        }

    }

    @Override
    public void appendHoverText(ItemStack itemstack, @Nullable World world, List<IChatBaseComponent> list, TooltipFlag tooltipflag) {
        super.appendHoverText(itemstack, world, list, tooltipflag);
        if (tooltipflag.isCreative()) {
            List<MobEffect> list1 = new ArrayList();

            listPotionEffects(itemstack, (suspiciouseffectholder_a) -> {
                list1.add(suspiciouseffectholder_a.createEffectInstance());
            });
            PotionUtil.addPotionTooltip((List) list1, list, 1.0F);
        }

    }

    @Override
    public ItemStack finishUsingItem(ItemStack itemstack, World world, EntityLiving entityliving) {
        ItemStack itemstack1 = super.finishUsingItem(itemstack, world, entityliving);

        listPotionEffects(itemstack1, (suspiciouseffectholder_a) -> {
            entityliving.addEffect(suspiciouseffectholder_a.createEffectInstance());
        });
        return entityliving instanceof EntityHuman && ((EntityHuman) entityliving).getAbilities().instabuild ? itemstack1 : new ItemStack(Items.BOWL);
    }
}
