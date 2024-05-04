package net.minecraft.world.item;

import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.IPosition;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.stats.StatisticList;
import net.minecraft.util.RandomSource;
import net.minecraft.world.EnumHand;
import net.minecraft.world.InteractionResultWrapper;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.projectile.IProjectile;
import net.minecraft.world.entity.projectile.windcharge.WindCharge;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.BlockDispenser;
import net.minecraft.world.phys.Vec3D;

public class WindChargeItem extends Item implements ProjectileItem {

    private static final int COOLDOWN = 10;

    public WindChargeItem(Item.Info item_info) {
        super(item_info);
    }

    @Override
    public InteractionResultWrapper<ItemStack> use(World world, EntityHuman entityhuman, EnumHand enumhand) {
        if (!world.isClientSide()) {
            Vec3D vec3d = entityhuman.getEyePosition().add(entityhuman.getForward().scale(0.800000011920929D));

            if (!world.getBlockState(BlockPosition.containing(vec3d)).canBeReplaced()) {
                vec3d = entityhuman.getEyePosition().add(entityhuman.getForward().scale(0.05000000074505806D));
            }

            WindCharge windcharge = new WindCharge(entityhuman, world, vec3d.x(), vec3d.y(), vec3d.z());

            windcharge.shootFromRotation(entityhuman, entityhuman.getXRot(), entityhuman.getYRot(), 0.0F, 1.5F, 1.0F);
            world.addFreshEntity(windcharge);
        }

        world.playSound((EntityHuman) null, entityhuman.getX(), entityhuman.getY(), entityhuman.getZ(), SoundEffects.WIND_CHARGE_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
        ItemStack itemstack = entityhuman.getItemInHand(enumhand);

        entityhuman.getCooldowns().addCooldown(this, 10);
        entityhuman.awardStat(StatisticList.ITEM_USED.get(this));
        itemstack.consume(1, entityhuman);
        return InteractionResultWrapper.sidedSuccess(itemstack, world.isClientSide());
    }

    @Override
    public IProjectile asProjectile(World world, IPosition iposition, ItemStack itemstack, EnumDirection enumdirection) {
        RandomSource randomsource = world.getRandom();
        double d0 = randomsource.triangle((double) enumdirection.getStepX(), 0.11485000000000001D);
        double d1 = randomsource.triangle((double) enumdirection.getStepY(), 0.11485000000000001D);
        double d2 = randomsource.triangle((double) enumdirection.getStepZ(), 0.11485000000000001D);

        return new WindCharge(world, iposition.x(), iposition.y(), iposition.z(), d0, d1, d2);
    }

    @Override
    public void shoot(IProjectile iprojectile, double d0, double d1, double d2, float f, float f1) {}

    @Override
    public ProjectileItem.a createDispenseConfig() {
        return ProjectileItem.a.builder().positionFunction((sourceblock, enumdirection) -> {
            return BlockDispenser.getDispensePosition(sourceblock, 1.0D, Vec3D.ZERO);
        }).uncertainty(6.6666665F).power(1.0F).build();
    }
}
