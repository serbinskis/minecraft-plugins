package net.minecraft.world.item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.TagsItem;
import net.minecraft.util.Unit;
import net.minecraft.world.EnumHand;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.projectile.EntityArrow;
import net.minecraft.world.entity.projectile.IProjectile;
import net.minecraft.world.item.enchantment.EnchantmentManager;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.World;

public abstract class ItemProjectileWeapon extends Item {

    public static final Predicate<ItemStack> ARROW_ONLY = (itemstack) -> {
        return itemstack.is(TagsItem.ARROWS);
    };
    public static final Predicate<ItemStack> ARROW_OR_FIREWORK = ItemProjectileWeapon.ARROW_ONLY.or((itemstack) -> {
        return itemstack.is(Items.FIREWORK_ROCKET);
    });

    public ItemProjectileWeapon(Item.Info item_info) {
        super(item_info);
    }

    public Predicate<ItemStack> getSupportedHeldProjectiles() {
        return this.getAllSupportedProjectiles();
    }

    public abstract Predicate<ItemStack> getAllSupportedProjectiles();

    public static ItemStack getHeldProjectile(EntityLiving entityliving, Predicate<ItemStack> predicate) {
        return predicate.test(entityliving.getItemInHand(EnumHand.OFF_HAND)) ? entityliving.getItemInHand(EnumHand.OFF_HAND) : (predicate.test(entityliving.getItemInHand(EnumHand.MAIN_HAND)) ? entityliving.getItemInHand(EnumHand.MAIN_HAND) : ItemStack.EMPTY);
    }

    @Override
    public int getEnchantmentValue() {
        return 1;
    }

    public abstract int getDefaultProjectileRange();

    protected void shoot(World world, EntityLiving entityliving, EnumHand enumhand, ItemStack itemstack, List<ItemStack> list, float f, float f1, boolean flag, @Nullable EntityLiving entityliving1) {
        float f2 = 10.0F;
        float f3 = list.size() == 1 ? 0.0F : 20.0F / (float) (list.size() - 1);
        float f4 = (float) ((list.size() - 1) % 2) * f3 / 2.0F;
        float f5 = 1.0F;

        for (int i = 0; i < list.size(); ++i) {
            ItemStack itemstack1 = (ItemStack) list.get(i);

            if (!itemstack1.isEmpty()) {
                float f6 = f4 + f5 * (float) ((i + 1) / 2) * f3;

                f5 = -f5;
                itemstack.hurtAndBreak(this.getDurabilityUse(itemstack1), entityliving, EntityLiving.getSlotForHand(enumhand));
                IProjectile iprojectile = this.createProjectile(world, entityliving, itemstack, itemstack1, flag);

                this.shootProjectile(entityliving, iprojectile, i, f, f1, f6, entityliving1);
                world.addFreshEntity(iprojectile);
            }
        }

    }

    protected int getDurabilityUse(ItemStack itemstack) {
        return 1;
    }

    protected abstract void shootProjectile(EntityLiving entityliving, IProjectile iprojectile, int i, float f, float f1, float f2, @Nullable EntityLiving entityliving1);

    protected IProjectile createProjectile(World world, EntityLiving entityliving, ItemStack itemstack, ItemStack itemstack1, boolean flag) {
        Item item = itemstack1.getItem();
        ItemArrow itemarrow;

        if (item instanceof ItemArrow itemarrow1) {
            itemarrow = itemarrow1;
        } else {
            itemarrow = (ItemArrow) Items.ARROW;
        }

        ItemArrow itemarrow2 = itemarrow;
        EntityArrow entityarrow = itemarrow2.createArrow(world, itemstack1, entityliving);

        if (flag) {
            entityarrow.setCritArrow(true);
        }

        int i = EnchantmentManager.getItemEnchantmentLevel(Enchantments.POWER, itemstack);

        if (i > 0) {
            entityarrow.setBaseDamage(entityarrow.getBaseDamage() + (double) i * 0.5D + 0.5D);
        }

        int j = EnchantmentManager.getItemEnchantmentLevel(Enchantments.PUNCH, itemstack);

        if (j > 0) {
            entityarrow.setKnockback(j);
        }

        if (EnchantmentManager.getItemEnchantmentLevel(Enchantments.FLAME, itemstack) > 0) {
            entityarrow.igniteForSeconds(100);
        }

        int k = EnchantmentManager.getItemEnchantmentLevel(Enchantments.PIERCING, itemstack);

        if (k > 0) {
            entityarrow.setPierceLevel((byte) k);
        }

        return entityarrow;
    }

    protected static boolean hasInfiniteArrows(ItemStack itemstack, ItemStack itemstack1, boolean flag) {
        return flag || itemstack1.is(Items.ARROW) && EnchantmentManager.getItemEnchantmentLevel(Enchantments.INFINITY, itemstack) > 0;
    }

    protected static List<ItemStack> draw(ItemStack itemstack, ItemStack itemstack1, EntityLiving entityliving) {
        if (itemstack1.isEmpty()) {
            return List.of();
        } else {
            int i = EnchantmentManager.getItemEnchantmentLevel(Enchantments.MULTISHOT, itemstack);
            int j = i == 0 ? 1 : 3;
            List<ItemStack> list = new ArrayList(j);
            ItemStack itemstack2 = itemstack1.copy();

            for (int k = 0; k < j; ++k) {
                list.add(useAmmo(itemstack, k == 0 ? itemstack1 : itemstack2, entityliving, k > 0));
            }

            return list;
        }
    }

    protected static ItemStack useAmmo(ItemStack itemstack, ItemStack itemstack1, EntityLiving entityliving, boolean flag) {
        boolean flag1 = !flag && !hasInfiniteArrows(itemstack, itemstack1, entityliving.hasInfiniteMaterials());
        ItemStack itemstack2;

        if (!flag1) {
            itemstack2 = itemstack1.copyWithCount(1);
            itemstack2.set(DataComponents.INTANGIBLE_PROJECTILE, Unit.INSTANCE);
            return itemstack2;
        } else {
            itemstack2 = itemstack1.split(1);
            if (itemstack1.isEmpty() && entityliving instanceof EntityHuman) {
                EntityHuman entityhuman = (EntityHuman) entityliving;

                entityhuman.getInventory().removeItem(itemstack1);
            }

            return itemstack2;
        }
    }
}
