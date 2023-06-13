package net.minecraft.world.food;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;

// CraftBukkit start
import net.minecraft.network.protocol.game.PacketPlayOutUpdateHealth;
import net.minecraft.server.level.EntityPlayer;
// CraftBukkit end

public class FoodMetaData {

    public int foodLevel = 20;
    public float saturationLevel = 5.0F;
    public float exhaustionLevel;
    private int tickTimer;
    // CraftBukkit start
    private EntityHuman entityhuman;
    public int saturatedRegenRate = 10;
    public int unsaturatedRegenRate = 80;
    public int starvationRate = 80;
    // CraftBukkit end
    private int lastFoodLevel = 20;

    public FoodMetaData() { throw new AssertionError("Whoopsie, we missed the bukkit."); } // CraftBukkit start - throw an error

    // CraftBukkit start - added EntityHuman constructor
    public FoodMetaData(EntityHuman entityhuman) {
        org.apache.commons.lang.Validate.notNull(entityhuman);
        this.entityhuman = entityhuman;
    }
    // CraftBukkit end

    public void eat(int i, float f) {
        this.foodLevel = Math.min(i + this.foodLevel, 20);
        this.saturationLevel = Math.min(this.saturationLevel + (float) i * f * 2.0F, (float) this.foodLevel);
    }

    public void eat(Item item, ItemStack itemstack) {
        if (item.isEdible()) {
            FoodInfo foodinfo = item.getFoodProperties();
            // CraftBukkit start
            int oldFoodLevel = foodLevel;

            org.bukkit.event.entity.FoodLevelChangeEvent event = org.bukkit.craftbukkit.event.CraftEventFactory.callFoodLevelChangeEvent(entityhuman, foodinfo.getNutrition() + oldFoodLevel, itemstack);

            if (!event.isCancelled()) {
                this.eat(event.getFoodLevel() - oldFoodLevel, foodinfo.getSaturationModifier());
            }

            ((EntityPlayer) entityhuman).getBukkitEntity().sendHealthUpdate();
            // CraftBukkit end
        }

    }

    public void tick(EntityHuman entityhuman) {
        EnumDifficulty enumdifficulty = entityhuman.level().getDifficulty();

        this.lastFoodLevel = this.foodLevel;
        if (this.exhaustionLevel > 4.0F) {
            this.exhaustionLevel -= 4.0F;
            if (this.saturationLevel > 0.0F) {
                this.saturationLevel = Math.max(this.saturationLevel - 1.0F, 0.0F);
            } else if (enumdifficulty != EnumDifficulty.PEACEFUL) {
                // CraftBukkit start
                org.bukkit.event.entity.FoodLevelChangeEvent event = org.bukkit.craftbukkit.event.CraftEventFactory.callFoodLevelChangeEvent(entityhuman, Math.max(this.foodLevel - 1, 0));

                if (!event.isCancelled()) {
                    this.foodLevel = event.getFoodLevel();
                }

                ((EntityPlayer) entityhuman).connection.send(new PacketPlayOutUpdateHealth(((EntityPlayer) entityhuman).getBukkitEntity().getScaledHealth(), this.foodLevel, this.saturationLevel));
                // CraftBukkit end
            }
        }

        boolean flag = entityhuman.level().getGameRules().getBoolean(GameRules.RULE_NATURAL_REGENERATION);

        if (flag && this.saturationLevel > 0.0F && entityhuman.isHurt() && this.foodLevel >= 20) {
            ++this.tickTimer;
            if (this.tickTimer >= this.saturatedRegenRate) { // CraftBukkit
                float f = Math.min(this.saturationLevel, 6.0F);

                entityhuman.heal(f / 6.0F, org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason.SATIATED); // CraftBukkit - added RegainReason
                // this.addExhaustion(f); CraftBukkit - EntityExhaustionEvent
                entityhuman.causeFoodExhaustion(f, org.bukkit.event.entity.EntityExhaustionEvent.ExhaustionReason.REGEN); // CraftBukkit - EntityExhaustionEvent
                this.tickTimer = 0;
            }
        } else if (flag && this.foodLevel >= 18 && entityhuman.isHurt()) {
            ++this.tickTimer;
            if (this.tickTimer >= this.unsaturatedRegenRate) { // CraftBukkit - add regen rate manipulation
                entityhuman.heal(1.0F, org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason.SATIATED); // CraftBukkit - added RegainReason
                // this.a(6.0F); CraftBukkit - EntityExhaustionEvent
                entityhuman.causeFoodExhaustion(6.0f, org.bukkit.event.entity.EntityExhaustionEvent.ExhaustionReason.REGEN); // CraftBukkit - EntityExhaustionEvent
                this.tickTimer = 0;
            }
        } else if (this.foodLevel <= 0) {
            ++this.tickTimer;
            if (this.tickTimer >= this.starvationRate) { // CraftBukkit - add regen rate manipulation
                if (entityhuman.getHealth() > 10.0F || enumdifficulty == EnumDifficulty.HARD || entityhuman.getHealth() > 1.0F && enumdifficulty == EnumDifficulty.NORMAL) {
                    entityhuman.hurt(entityhuman.damageSources().starve(), 1.0F);
                }

                this.tickTimer = 0;
            }
        } else {
            this.tickTimer = 0;
        }

    }

    public void readAdditionalSaveData(NBTTagCompound nbttagcompound) {
        if (nbttagcompound.contains("foodLevel", 99)) {
            this.foodLevel = nbttagcompound.getInt("foodLevel");
            this.tickTimer = nbttagcompound.getInt("foodTickTimer");
            this.saturationLevel = nbttagcompound.getFloat("foodSaturationLevel");
            this.exhaustionLevel = nbttagcompound.getFloat("foodExhaustionLevel");
        }

    }

    public void addAdditionalSaveData(NBTTagCompound nbttagcompound) {
        nbttagcompound.putInt("foodLevel", this.foodLevel);
        nbttagcompound.putInt("foodTickTimer", this.tickTimer);
        nbttagcompound.putFloat("foodSaturationLevel", this.saturationLevel);
        nbttagcompound.putFloat("foodExhaustionLevel", this.exhaustionLevel);
    }

    public int getFoodLevel() {
        return this.foodLevel;
    }

    public int getLastFoodLevel() {
        return this.lastFoodLevel;
    }

    public boolean needsFood() {
        return this.foodLevel < 20;
    }

    public void addExhaustion(float f) {
        this.exhaustionLevel = Math.min(this.exhaustionLevel + f, 40.0F);
    }

    public float getExhaustionLevel() {
        return this.exhaustionLevel;
    }

    public float getSaturationLevel() {
        return this.saturationLevel;
    }

    public void setFoodLevel(int i) {
        this.foodLevel = i;
    }

    public void setSaturation(float f) {
        this.saturationLevel = f;
    }

    public void setExhaustion(float f) {
        this.exhaustionLevel = f;
    }
}
