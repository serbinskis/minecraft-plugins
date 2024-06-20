package net.minecraft.world.item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.WorldServer;
import net.minecraft.tags.TagsItem;
import net.minecraft.util.Unit;
import net.minecraft.world.EnumHand;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.projectile.EntityArrow;
import net.minecraft.world.entity.projectile.IProjectile;
import net.minecraft.world.item.enchantment.EnchantmentManager;
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

    protected void shoot(WorldServer worldserver, EntityLiving entityliving, EnumHand enumhand, ItemStack itemstack, List<ItemStack> list, float f, float f1, boolean flag, @Nullable EntityLiving entityliving1) {
        float f2 = EnchantmentManager.processProjectileSpread(worldserver, itemstack, entityliving, 0.0F);
        float f3 = list.size() == 1 ? 0.0F : 2.0F * f2 / (float) (list.size() - 1);
        float f4 = (float) ((list.size() - 1) % 2) * f3 / 2.0F;
        float f5 = 1.0F;

        for (int i = 0; i < list.size(); ++i) {
            ItemStack itemstack1 = (ItemStack) list.get(i);

            if (!itemstack1.isEmpty()) {
                float f6 = f4 + f5 * (float) ((i + 1) / 2) * f3;

                f5 = -f5;
                IProjectile iprojectile = this.createProjectile(worldserver, entityliving, itemstack, itemstack1, flag);

                this.shootProjectile(entityliving, iprojectile, i, f, f1, f6, entityliving1);
                worldserver.addFreshEntity(iprojectile);
                itemstack.hurtAndBreak(this.getDurabilityUse(itemstack1), entityliving, EntityLiving.getSlotForHand(enumhand));
                if (itemstack.isEmpty()) {
                    break;
                }
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
        EntityArrow entityarrow = itemarrow2.createArrow(world, itemstack1, entityliving, itemstack);

        if (flag) {
            entityarrow.setCritArrow(true);
        }

        return entityarrow;
    }

    protected static List<ItemStack> draw(ItemStack itemstack, ItemStack itemstack1, EntityLiving entityliving) {
        if (itemstack1.isEmpty()) {
            return List.of();
        } else {
            World world = entityliving.level();
            int i;

            if (world instanceof WorldServer) {
                WorldServer worldserver = (WorldServer) world;

                i = EnchantmentManager.processProjectileCount(worldserver, itemstack, entityliving, 1);
            } else {
                i = 1;
            }

            int j = i;
            List<ItemStack> list = new ArrayList(j);
            ItemStack itemstack2 = itemstack1.copy();

            for (int k = 0; k < j; ++k) {
                ItemStack itemstack3 = useAmmo(itemstack, k == 0 ? itemstack1 : itemstack2, entityliving, k > 0);

                if (!itemstack3.isEmpty()) {
                    list.add(itemstack3);
                }
            }

            return list;
        }
    }

    protected static ItemStack useAmmo(ItemStack itemstack, ItemStack itemstack1, EntityLiving entityliving, boolean flag) {
        int i;
        label28:
        {
            if (!flag && !entityliving.hasInfiniteMaterials()) {
                World world = entityliving.level();

                if (world instanceof WorldServer) {
                    WorldServer worldserver = (WorldServer) world;

                    i = EnchantmentManager.processAmmoUse(worldserver, itemstack, itemstack1, 1);
                    break label28;
                }
            }

            i = 0;
        }

        int j = i;

        if (j > itemstack1.getCount()) {
            return ItemStack.EMPTY;
        } else {
            ItemStack itemstack2;

            if (j == 0) {
                itemstack2 = itemstack1.copyWithCount(1);
                itemstack2.set(DataComponents.INTANGIBLE_PROJECTILE, Unit.INSTANCE);
                return itemstack2;
            } else {
                itemstack2 = itemstack1.split(j);
                if (itemstack1.isEmpty() && entityliving instanceof EntityHuman) {
                    EntityHuman entityhuman = (EntityHuman) entityliving;

                    entityhuman.getInventory().removeItem(itemstack1);
                }

                return itemstack2;
            }
        }
    }
}
