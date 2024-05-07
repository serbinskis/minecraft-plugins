package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.npc.EntityVillager;
import net.minecraft.world.entity.projectile.EntityFireworks;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.EnumColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;

public class BehaviorCelebrate extends Behavior<EntityVillager> {

    @Nullable
    private Raid currentRaid;

    public BehaviorCelebrate(int i, int j) {
        super(ImmutableMap.of(), i, j);
    }

    protected boolean checkExtraStartConditions(WorldServer worldserver, EntityVillager entityvillager) {
        BlockPosition blockposition = entityvillager.blockPosition();

        this.currentRaid = worldserver.getRaidAt(blockposition);
        return this.currentRaid != null && this.currentRaid.isVictory() && BehaviorOutside.hasNoBlocksAbove(worldserver, entityvillager, blockposition);
    }

    protected boolean canStillUse(WorldServer worldserver, EntityVillager entityvillager, long i) {
        return this.currentRaid != null && !this.currentRaid.isStopped();
    }

    protected void stop(WorldServer worldserver, EntityVillager entityvillager, long i) {
        this.currentRaid = null;
        entityvillager.getBrain().updateActivityFromSchedule(worldserver.getDayTime(), worldserver.getGameTime());
    }

    protected void tick(WorldServer worldserver, EntityVillager entityvillager, long i) {
        RandomSource randomsource = entityvillager.getRandom();

        if (randomsource.nextInt(100) == 0) {
            entityvillager.playCelebrateSound();
        }

        if (randomsource.nextInt(200) == 0 && BehaviorOutside.hasNoBlocksAbove(worldserver, entityvillager, entityvillager.blockPosition())) {
            EnumColor enumcolor = (EnumColor) SystemUtils.getRandom((Object[]) EnumColor.values(), randomsource);
            int j = randomsource.nextInt(3);
            ItemStack itemstack = this.getFirework(enumcolor, j);
            EntityFireworks entityfireworks = new EntityFireworks(entityvillager.level(), entityvillager, entityvillager.getX(), entityvillager.getEyeY(), entityvillager.getZ(), itemstack);

            entityvillager.level().addFreshEntity(entityfireworks);
        }

    }

    private ItemStack getFirework(EnumColor enumcolor, int i) {
        ItemStack itemstack = new ItemStack(Items.FIREWORK_ROCKET);

        itemstack.set(DataComponents.FIREWORKS, new Fireworks((byte) i, List.of(new FireworkExplosion(FireworkExplosion.a.BURST, IntList.of(enumcolor.getFireworkColor()), IntList.of(), false, false))));
        return itemstack;
    }
}
