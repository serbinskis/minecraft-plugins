package net.minecraft.world.entity.animal;

import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.core.BlockPosition;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.tags.TagsItem;
import net.minecraft.util.MathHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyDamageScaler;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAgeable;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumMobSpawn;
import net.minecraft.world.entity.GroupDataEntity;
import net.minecraft.world.entity.IShearable;
import net.minecraft.world.entity.ai.attributes.AttributeProvider;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.entity.ai.goal.PathfinderGoalBreed;
import net.minecraft.world.entity.ai.goal.PathfinderGoalEatTile;
import net.minecraft.world.entity.ai.goal.PathfinderGoalFloat;
import net.minecraft.world.entity.ai.goal.PathfinderGoalFollowParent;
import net.minecraft.world.entity.ai.goal.PathfinderGoalLookAtPlayer;
import net.minecraft.world.entity.ai.goal.PathfinderGoalPanic;
import net.minecraft.world.entity.ai.goal.PathfinderGoalRandomLookaround;
import net.minecraft.world.entity.ai.goal.PathfinderGoalRandomStrollLand;
import net.minecraft.world.entity.ai.goal.PathfinderGoalTempt;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.inventory.Container;
import net.minecraft.world.inventory.Containers;
import net.minecraft.world.inventory.InventoryCrafting;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.EnumColor;
import net.minecraft.world.item.ItemDye;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeCrafting;
import net.minecraft.world.item.crafting.Recipes;
import net.minecraft.world.level.IMaterial;
import net.minecraft.world.level.World;
import net.minecraft.world.level.WorldAccess;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;

// CraftBukkit start
import net.minecraft.world.inventory.InventoryCraftResult;
import net.minecraft.world.item.Item;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.SheepRegrowWoolEvent;
import org.bukkit.inventory.InventoryView;
// CraftBukkit end

public class EntitySheep extends EntityAnimal implements IShearable {

    private static final int EAT_ANIMATION_TICKS = 40;
    private static final DataWatcherObject<Byte> DATA_WOOL_ID = DataWatcher.defineId(EntitySheep.class, DataWatcherRegistry.BYTE);
    private static final Map<EnumColor, IMaterial> ITEM_BY_DYE = (Map) SystemUtils.make(Maps.newEnumMap(EnumColor.class), (enummap) -> {
        enummap.put(EnumColor.WHITE, Blocks.WHITE_WOOL);
        enummap.put(EnumColor.ORANGE, Blocks.ORANGE_WOOL);
        enummap.put(EnumColor.MAGENTA, Blocks.MAGENTA_WOOL);
        enummap.put(EnumColor.LIGHT_BLUE, Blocks.LIGHT_BLUE_WOOL);
        enummap.put(EnumColor.YELLOW, Blocks.YELLOW_WOOL);
        enummap.put(EnumColor.LIME, Blocks.LIME_WOOL);
        enummap.put(EnumColor.PINK, Blocks.PINK_WOOL);
        enummap.put(EnumColor.GRAY, Blocks.GRAY_WOOL);
        enummap.put(EnumColor.LIGHT_GRAY, Blocks.LIGHT_GRAY_WOOL);
        enummap.put(EnumColor.CYAN, Blocks.CYAN_WOOL);
        enummap.put(EnumColor.PURPLE, Blocks.PURPLE_WOOL);
        enummap.put(EnumColor.BLUE, Blocks.BLUE_WOOL);
        enummap.put(EnumColor.BROWN, Blocks.BROWN_WOOL);
        enummap.put(EnumColor.GREEN, Blocks.GREEN_WOOL);
        enummap.put(EnumColor.RED, Blocks.RED_WOOL);
        enummap.put(EnumColor.BLACK, Blocks.BLACK_WOOL);
    });
    private static final Map<EnumColor, float[]> COLORARRAY_BY_COLOR = Maps.newEnumMap((Map) Arrays.stream(EnumColor.values()).collect(Collectors.toMap((enumcolor) -> {
        return enumcolor;
    }, EntitySheep::createSheepColor)));
    private int eatAnimationTick;
    private PathfinderGoalEatTile eatBlockGoal;

    private static float[] createSheepColor(EnumColor enumcolor) {
        if (enumcolor == EnumColor.WHITE) {
            return new float[]{0.9019608F, 0.9019608F, 0.9019608F};
        } else {
            float[] afloat = enumcolor.getTextureDiffuseColors();
            float f = 0.75F;

            return new float[]{afloat[0] * 0.75F, afloat[1] * 0.75F, afloat[2] * 0.75F};
        }
    }

    public static float[] getColorArray(EnumColor enumcolor) {
        return (float[]) EntitySheep.COLORARRAY_BY_COLOR.get(enumcolor);
    }

    public EntitySheep(EntityTypes<? extends EntitySheep> entitytypes, World world) {
        super(entitytypes, world);
    }

    @Override
    protected void registerGoals() {
        this.eatBlockGoal = new PathfinderGoalEatTile(this);
        this.goalSelector.addGoal(0, new PathfinderGoalFloat(this));
        this.goalSelector.addGoal(1, new PathfinderGoalPanic(this, 1.25D));
        this.goalSelector.addGoal(2, new PathfinderGoalBreed(this, 1.0D));
        this.goalSelector.addGoal(3, new PathfinderGoalTempt(this, 1.1D, (itemstack) -> {
            return itemstack.is(TagsItem.SHEEP_FOOD);
        }, false));
        this.goalSelector.addGoal(4, new PathfinderGoalFollowParent(this, 1.1D));
        this.goalSelector.addGoal(5, this.eatBlockGoal);
        this.goalSelector.addGoal(6, new PathfinderGoalRandomStrollLand(this, 1.0D));
        this.goalSelector.addGoal(7, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 6.0F));
        this.goalSelector.addGoal(8, new PathfinderGoalRandomLookaround(this));
    }

    @Override
    public boolean isFood(ItemStack itemstack) {
        return itemstack.is(TagsItem.SHEEP_FOOD);
    }

    @Override
    protected void customServerAiStep() {
        this.eatAnimationTick = this.eatBlockGoal.getEatAnimationTick();
        super.customServerAiStep();
    }

    @Override
    public void aiStep() {
        if (this.level().isClientSide) {
            this.eatAnimationTick = Math.max(0, this.eatAnimationTick - 1);
        }

        super.aiStep();
    }

    public static AttributeProvider.Builder createAttributes() {
        return EntityInsentient.createMobAttributes().add(GenericAttributes.MAX_HEALTH, 8.0D).add(GenericAttributes.MOVEMENT_SPEED, 0.23000000417232513D);
    }

    @Override
    protected void defineSynchedData(DataWatcher.a datawatcher_a) {
        super.defineSynchedData(datawatcher_a);
        datawatcher_a.define(EntitySheep.DATA_WOOL_ID, (byte) 0);
    }

    @Override
    public ResourceKey<LootTable> getDefaultLootTable() {
        if (this.isSheared()) {
            return this.getType().getDefaultLootTable();
        } else {
            ResourceKey resourcekey;

            switch (this.getColor()) {
                case WHITE:
                    resourcekey = LootTables.SHEEP_WHITE;
                    break;
                case ORANGE:
                    resourcekey = LootTables.SHEEP_ORANGE;
                    break;
                case MAGENTA:
                    resourcekey = LootTables.SHEEP_MAGENTA;
                    break;
                case LIGHT_BLUE:
                    resourcekey = LootTables.SHEEP_LIGHT_BLUE;
                    break;
                case YELLOW:
                    resourcekey = LootTables.SHEEP_YELLOW;
                    break;
                case LIME:
                    resourcekey = LootTables.SHEEP_LIME;
                    break;
                case PINK:
                    resourcekey = LootTables.SHEEP_PINK;
                    break;
                case GRAY:
                    resourcekey = LootTables.SHEEP_GRAY;
                    break;
                case LIGHT_GRAY:
                    resourcekey = LootTables.SHEEP_LIGHT_GRAY;
                    break;
                case CYAN:
                    resourcekey = LootTables.SHEEP_CYAN;
                    break;
                case PURPLE:
                    resourcekey = LootTables.SHEEP_PURPLE;
                    break;
                case BLUE:
                    resourcekey = LootTables.SHEEP_BLUE;
                    break;
                case BROWN:
                    resourcekey = LootTables.SHEEP_BROWN;
                    break;
                case GREEN:
                    resourcekey = LootTables.SHEEP_GREEN;
                    break;
                case RED:
                    resourcekey = LootTables.SHEEP_RED;
                    break;
                case BLACK:
                    resourcekey = LootTables.SHEEP_BLACK;
                    break;
                default:
                    throw new MatchException((String) null, (Throwable) null);
            }

            return resourcekey;
        }
    }

    @Override
    public void handleEntityEvent(byte b0) {
        if (b0 == 10) {
            this.eatAnimationTick = 40;
        } else {
            super.handleEntityEvent(b0);
        }

    }

    public float getHeadEatPositionScale(float f) {
        return this.eatAnimationTick <= 0 ? 0.0F : (this.eatAnimationTick >= 4 && this.eatAnimationTick <= 36 ? 1.0F : (this.eatAnimationTick < 4 ? ((float) this.eatAnimationTick - f) / 4.0F : -((float) (this.eatAnimationTick - 40) - f) / 4.0F));
    }

    public float getHeadEatAngleScale(float f) {
        if (this.eatAnimationTick > 4 && this.eatAnimationTick <= 36) {
            float f1 = ((float) (this.eatAnimationTick - 4) - f) / 32.0F;

            return 0.62831855F + 0.21991149F * MathHelper.sin(f1 * 28.7F);
        } else {
            return this.eatAnimationTick > 0 ? 0.62831855F : this.getXRot() * 0.017453292F;
        }
    }

    @Override
    public EnumInteractionResult mobInteract(EntityHuman entityhuman, EnumHand enumhand) {
        ItemStack itemstack = entityhuman.getItemInHand(enumhand);

        if (itemstack.is(Items.SHEARS)) {
            if (!this.level().isClientSide && this.readyForShearing()) {
                // CraftBukkit start
                if (!CraftEventFactory.handlePlayerShearEntityEvent(entityhuman, this, itemstack, enumhand)) {
                    return EnumInteractionResult.PASS;
                }
                // CraftBukkit end
                this.shear(SoundCategory.PLAYERS);
                this.gameEvent(GameEvent.SHEAR, entityhuman);
                itemstack.hurtAndBreak(1, entityhuman, getSlotForHand(enumhand));
                return EnumInteractionResult.SUCCESS;
            } else {
                return EnumInteractionResult.CONSUME;
            }
        } else {
            return super.mobInteract(entityhuman, enumhand);
        }
    }

    @Override
    public void shear(SoundCategory soundcategory) {
        this.level().playSound((EntityHuman) null, (Entity) this, SoundEffects.SHEEP_SHEAR, soundcategory, 1.0F, 1.0F);
        this.setSheared(true);
        int i = 1 + this.random.nextInt(3);

        for (int j = 0; j < i; ++j) {
            this.forceDrops = true; // CraftBukkit
            EntityItem entityitem = this.spawnAtLocation((IMaterial) EntitySheep.ITEM_BY_DYE.get(this.getColor()), 1);
            this.forceDrops = false; // CraftBukkit

            if (entityitem != null) {
                entityitem.setDeltaMovement(entityitem.getDeltaMovement().add((double) ((this.random.nextFloat() - this.random.nextFloat()) * 0.1F), (double) (this.random.nextFloat() * 0.05F), (double) ((this.random.nextFloat() - this.random.nextFloat()) * 0.1F)));
            }
        }

    }

    @Override
    public boolean readyForShearing() {
        return this.isAlive() && !this.isSheared() && !this.isBaby();
    }

    @Override
    public void addAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.addAdditionalSaveData(nbttagcompound);
        nbttagcompound.putBoolean("Sheared", this.isSheared());
        nbttagcompound.putByte("Color", (byte) this.getColor().getId());
    }

    @Override
    public void readAdditionalSaveData(NBTTagCompound nbttagcompound) {
        super.readAdditionalSaveData(nbttagcompound);
        this.setSheared(nbttagcompound.getBoolean("Sheared"));
        this.setColor(EnumColor.byId(nbttagcompound.getByte("Color")));
    }

    @Override
    protected SoundEffect getAmbientSound() {
        return SoundEffects.SHEEP_AMBIENT;
    }

    @Override
    protected SoundEffect getHurtSound(DamageSource damagesource) {
        return SoundEffects.SHEEP_HURT;
    }

    @Override
    protected SoundEffect getDeathSound() {
        return SoundEffects.SHEEP_DEATH;
    }

    @Override
    protected void playStepSound(BlockPosition blockposition, IBlockData iblockdata) {
        this.playSound(SoundEffects.SHEEP_STEP, 0.15F, 1.0F);
    }

    public EnumColor getColor() {
        return EnumColor.byId((Byte) this.entityData.get(EntitySheep.DATA_WOOL_ID) & 15);
    }

    public void setColor(EnumColor enumcolor) {
        byte b0 = (Byte) this.entityData.get(EntitySheep.DATA_WOOL_ID);

        this.entityData.set(EntitySheep.DATA_WOOL_ID, (byte) (b0 & 240 | enumcolor.getId() & 15));
    }

    public boolean isSheared() {
        return ((Byte) this.entityData.get(EntitySheep.DATA_WOOL_ID) & 16) != 0;
    }

    public void setSheared(boolean flag) {
        byte b0 = (Byte) this.entityData.get(EntitySheep.DATA_WOOL_ID);

        if (flag) {
            this.entityData.set(EntitySheep.DATA_WOOL_ID, (byte) (b0 | 16));
        } else {
            this.entityData.set(EntitySheep.DATA_WOOL_ID, (byte) (b0 & -17));
        }

    }

    public static EnumColor getRandomSheepColor(RandomSource randomsource) {
        int i = randomsource.nextInt(100);

        return i < 5 ? EnumColor.BLACK : (i < 10 ? EnumColor.GRAY : (i < 15 ? EnumColor.LIGHT_GRAY : (i < 18 ? EnumColor.BROWN : (randomsource.nextInt(500) == 0 ? EnumColor.PINK : EnumColor.WHITE))));
    }

    @Nullable
    @Override
    public EntitySheep getBreedOffspring(WorldServer worldserver, EntityAgeable entityageable) {
        EntitySheep entitysheep = (EntitySheep) EntityTypes.SHEEP.create(worldserver);

        if (entitysheep != null) {
            entitysheep.setColor(this.getOffspringColor(this, (EntitySheep) entityageable));
        }

        return entitysheep;
    }

    @Override
    public void ate() {
        // CraftBukkit start
        SheepRegrowWoolEvent event = new SheepRegrowWoolEvent((org.bukkit.entity.Sheep) this.getBukkitEntity());
        this.level().getCraftServer().getPluginManager().callEvent(event);

        if (event.isCancelled()) return;
        // CraftBukkit end
        super.ate();
        this.setSheared(false);
        if (this.isBaby()) {
            this.ageUp(60);
        }

    }

    @Nullable
    @Override
    public GroupDataEntity finalizeSpawn(WorldAccess worldaccess, DifficultyDamageScaler difficultydamagescaler, EnumMobSpawn enummobspawn, @Nullable GroupDataEntity groupdataentity) {
        this.setColor(getRandomSheepColor(worldaccess.getRandom()));
        return super.finalizeSpawn(worldaccess, difficultydamagescaler, enummobspawn, groupdataentity);
    }

    private EnumColor getOffspringColor(EntityAnimal entityanimal, EntityAnimal entityanimal1) {
        EnumColor enumcolor = ((EntitySheep) entityanimal).getColor();
        EnumColor enumcolor1 = ((EntitySheep) entityanimal1).getColor();
        InventoryCrafting inventorycrafting = makeContainer(enumcolor, enumcolor1);
        Optional<Item> optional = this.level().getRecipeManager().getRecipeFor(Recipes.CRAFTING, inventorycrafting, this.level()).map((recipeholder) -> { // CraftBukkit - decompile error
            return ((RecipeCrafting) recipeholder.value()).assemble(inventorycrafting, this.level().registryAccess());
        }).map(ItemStack::getItem);

        Objects.requireNonNull(ItemDye.class);
        optional = optional.filter(ItemDye.class::isInstance);
        Objects.requireNonNull(ItemDye.class);
        return (EnumColor) optional.map(ItemDye.class::cast).map(ItemDye::getDyeColor).orElseGet(() -> {
            return this.level().random.nextBoolean() ? enumcolor : enumcolor1;
        });
    }

    private static InventoryCrafting makeContainer(EnumColor enumcolor, EnumColor enumcolor1) {
        TransientCraftingContainer transientcraftingcontainer = new TransientCraftingContainer(new Container((Containers) null, -1) {
            @Override
            public ItemStack quickMoveStack(EntityHuman entityhuman, int i) {
                return ItemStack.EMPTY;
            }

            @Override
            public boolean stillValid(EntityHuman entityhuman) {
                return false;
            }

            // CraftBukkit start
            @Override
            public InventoryView getBukkitView() {
                return null; // TODO: O.O
            }
            // CraftBukkit end
        }, 2, 1);

        transientcraftingcontainer.setItem(0, new ItemStack(ItemDye.byColor(enumcolor)));
        transientcraftingcontainer.setItem(1, new ItemStack(ItemDye.byColor(enumcolor1)));
        transientcraftingcontainer.resultInventory = new InventoryCraftResult(); // CraftBukkit - add result slot for event
        return transientcraftingcontainer;
    }
}
