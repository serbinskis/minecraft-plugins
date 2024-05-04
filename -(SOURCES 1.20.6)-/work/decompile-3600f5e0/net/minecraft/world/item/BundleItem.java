package net.minecraft.world.item;

import java.util.List;
import java.util.Optional;
import net.minecraft.EnumChatFormat;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.stats.StatisticList;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumHand;
import net.minecraft.world.InteractionResultWrapper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.BundleTooltip;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.level.World;
import org.apache.commons.lang3.math.Fraction;

public class BundleItem extends Item {

    private static final int BAR_COLOR = MathHelper.color(0.4F, 0.4F, 1.0F);
    private static final int TOOLTIP_MAX_WEIGHT = 64;

    public BundleItem(Item.Info item_info) {
        super(item_info);
    }

    public static float getFullnessDisplay(ItemStack itemstack) {
        BundleContents bundlecontents = (BundleContents) itemstack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);

        return bundlecontents.weight().floatValue();
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack itemstack, Slot slot, ClickAction clickaction, EntityHuman entityhuman) {
        if (clickaction != ClickAction.SECONDARY) {
            return false;
        } else {
            BundleContents bundlecontents = (BundleContents) itemstack.get(DataComponents.BUNDLE_CONTENTS);

            if (bundlecontents == null) {
                return false;
            } else {
                ItemStack itemstack1 = slot.getItem();
                BundleContents.a bundlecontents_a = new BundleContents.a(bundlecontents);

                if (itemstack1.isEmpty()) {
                    this.playRemoveOneSound(entityhuman);
                    ItemStack itemstack2 = bundlecontents_a.removeOne();

                    if (itemstack2 != null) {
                        ItemStack itemstack3 = slot.safeInsert(itemstack2);

                        bundlecontents_a.tryInsert(itemstack3);
                    }
                } else if (itemstack1.getItem().canFitInsideContainerItems()) {
                    int i = bundlecontents_a.tryTransfer(slot, entityhuman);

                    if (i > 0) {
                        this.playInsertSound(entityhuman);
                    }
                }

                itemstack.set(DataComponents.BUNDLE_CONTENTS, bundlecontents_a.toImmutable());
                return true;
            }
        }
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack itemstack, ItemStack itemstack1, Slot slot, ClickAction clickaction, EntityHuman entityhuman, SlotAccess slotaccess) {
        if (clickaction == ClickAction.SECONDARY && slot.allowModification(entityhuman)) {
            BundleContents bundlecontents = (BundleContents) itemstack.get(DataComponents.BUNDLE_CONTENTS);

            if (bundlecontents == null) {
                return false;
            } else {
                BundleContents.a bundlecontents_a = new BundleContents.a(bundlecontents);

                if (itemstack1.isEmpty()) {
                    ItemStack itemstack2 = bundlecontents_a.removeOne();

                    if (itemstack2 != null) {
                        this.playRemoveOneSound(entityhuman);
                        slotaccess.set(itemstack2);
                    }
                } else {
                    int i = bundlecontents_a.tryInsert(itemstack1);

                    if (i > 0) {
                        this.playInsertSound(entityhuman);
                    }
                }

                itemstack.set(DataComponents.BUNDLE_CONTENTS, bundlecontents_a.toImmutable());
                return true;
            }
        } else {
            return false;
        }
    }

    @Override
    public InteractionResultWrapper<ItemStack> use(World world, EntityHuman entityhuman, EnumHand enumhand) {
        ItemStack itemstack = entityhuman.getItemInHand(enumhand);

        if (dropContents(itemstack, entityhuman)) {
            this.playDropContentsSound(entityhuman);
            entityhuman.awardStat(StatisticList.ITEM_USED.get(this));
            return InteractionResultWrapper.sidedSuccess(itemstack, world.isClientSide());
        } else {
            return InteractionResultWrapper.fail(itemstack);
        }
    }

    @Override
    public boolean isBarVisible(ItemStack itemstack) {
        BundleContents bundlecontents = (BundleContents) itemstack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);

        return bundlecontents.weight().compareTo(Fraction.ZERO) > 0;
    }

    @Override
    public int getBarWidth(ItemStack itemstack) {
        BundleContents bundlecontents = (BundleContents) itemstack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);

        return Math.min(1 + MathHelper.mulAndTruncate(bundlecontents.weight(), 12), 13);
    }

    @Override
    public int getBarColor(ItemStack itemstack) {
        return BundleItem.BAR_COLOR;
    }

    private static boolean dropContents(ItemStack itemstack, EntityHuman entityhuman) {
        BundleContents bundlecontents = (BundleContents) itemstack.get(DataComponents.BUNDLE_CONTENTS);

        if (bundlecontents != null && !bundlecontents.isEmpty()) {
            itemstack.set(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
            if (entityhuman instanceof EntityPlayer) {
                bundlecontents.itemsCopy().forEach((itemstack1) -> {
                    entityhuman.drop(itemstack1, true);
                });
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack itemstack) {
        return !itemstack.has(DataComponents.HIDE_TOOLTIP) && !itemstack.has(DataComponents.HIDE_ADDITIONAL_TOOLTIP) ? Optional.ofNullable((BundleContents) itemstack.get(DataComponents.BUNDLE_CONTENTS)).map(BundleTooltip::new) : Optional.empty();
    }

    @Override
    public void appendHoverText(ItemStack itemstack, Item.b item_b, List<IChatBaseComponent> list, TooltipFlag tooltipflag) {
        BundleContents bundlecontents = (BundleContents) itemstack.get(DataComponents.BUNDLE_CONTENTS);

        if (bundlecontents != null) {
            int i = MathHelper.mulAndTruncate(bundlecontents.weight(), 64);

            list.add(IChatBaseComponent.translatable("item.minecraft.bundle.fullness", i, 64).withStyle(EnumChatFormat.GRAY));
        }

    }

    @Override
    public void onDestroyed(EntityItem entityitem) {
        BundleContents bundlecontents = (BundleContents) entityitem.getItem().get(DataComponents.BUNDLE_CONTENTS);

        if (bundlecontents != null) {
            entityitem.getItem().set(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
            ItemLiquidUtil.onContainerDestroyed(entityitem, bundlecontents.itemsCopy());
        }
    }

    private void playRemoveOneSound(Entity entity) {
        entity.playSound(SoundEffects.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }

    private void playInsertSound(Entity entity) {
        entity.playSound(SoundEffects.BUNDLE_INSERT, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }

    private void playDropContentsSound(Entity entity) {
        entity.playSound(SoundEffects.BUNDLE_DROP_CONTENTS, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }
}
