package net.minecraft.world.item;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.SystemUtils;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Unit;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.InteractionResultWrapper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.food.FoodInfo;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.context.ItemActionContext;
import net.minecraft.world.level.IMaterial;
import net.minecraft.world.level.RayTrace;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.WorldMap;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.Vec3D;
import org.slf4j.Logger;

public class Item implements FeatureElement, IMaterial {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Map<Block, Item> BY_BLOCK = Maps.newHashMap();
    public static final UUID BASE_ATTACK_DAMAGE_UUID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
    public static final UUID BASE_ATTACK_SPEED_UUID = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3");
    public static final int DEFAULT_MAX_STACK_SIZE = 64;
    public static final int ABSOLUTE_MAX_STACK_SIZE = 99;
    public static final int MAX_BAR_WIDTH = 13;
    private final Holder.c<Item> builtInRegistryHolder;
    private final DataComponentMap components;
    @Nullable
    private final Item craftingRemainingItem;
    @Nullable
    private String descriptionId;
    private final FeatureFlagSet requiredFeatures;

    public static int getId(Item item) {
        return item == null ? 0 : BuiltInRegistries.ITEM.getId(item);
    }

    public static Item byId(int i) {
        return (Item) BuiltInRegistries.ITEM.byId(i);
    }

    /** @deprecated */
    @Deprecated
    public static Item byBlock(Block block) {
        return (Item) Item.BY_BLOCK.getOrDefault(block, Items.AIR);
    }

    public Item(Item.Info item_info) {
        this.builtInRegistryHolder = BuiltInRegistries.ITEM.createIntrusiveHolder(this);
        this.components = item_info.buildAndValidateComponents();
        this.craftingRemainingItem = item_info.craftingRemainingItem;
        this.requiredFeatures = item_info.requiredFeatures;
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            String s = this.getClass().getSimpleName();

            if (!s.endsWith("Item")) {
                Item.LOGGER.error("Item classes should end with Item and {} doesn't.", s);
            }
        }

    }

    /** @deprecated */
    @Deprecated
    public Holder.c<Item> builtInRegistryHolder() {
        return this.builtInRegistryHolder;
    }

    public DataComponentMap components() {
        return this.components;
    }

    public int getDefaultMaxStackSize() {
        return (Integer) this.components.getOrDefault(DataComponents.MAX_STACK_SIZE, 1);
    }

    public void onUseTick(World world, EntityLiving entityliving, ItemStack itemstack, int i) {}

    public void onDestroyed(EntityItem entityitem) {}

    public void verifyComponentsAfterLoad(ItemStack itemstack) {}

    public boolean canAttackBlock(IBlockData iblockdata, World world, BlockPosition blockposition, EntityHuman entityhuman) {
        return true;
    }

    @Override
    public Item asItem() {
        return this;
    }

    public EnumInteractionResult useOn(ItemActionContext itemactioncontext) {
        return EnumInteractionResult.PASS;
    }

    public float getDestroySpeed(ItemStack itemstack, IBlockData iblockdata) {
        Tool tool = (Tool) itemstack.get(DataComponents.TOOL);

        return tool != null ? tool.getMiningSpeed(iblockdata) : 1.0F;
    }

    public InteractionResultWrapper<ItemStack> use(World world, EntityHuman entityhuman, EnumHand enumhand) {
        ItemStack itemstack = entityhuman.getItemInHand(enumhand);
        FoodInfo foodinfo = (FoodInfo) itemstack.get(DataComponents.FOOD);

        if (foodinfo != null) {
            if (entityhuman.canEat(foodinfo.canAlwaysEat())) {
                entityhuman.startUsingItem(enumhand);
                return InteractionResultWrapper.consume(itemstack);
            } else {
                return InteractionResultWrapper.fail(itemstack);
            }
        } else {
            return InteractionResultWrapper.pass(entityhuman.getItemInHand(enumhand));
        }
    }

    public ItemStack finishUsingItem(ItemStack itemstack, World world, EntityLiving entityliving) {
        return itemstack.has(DataComponents.FOOD) ? entityliving.eat(world, itemstack) : itemstack;
    }

    public boolean isBarVisible(ItemStack itemstack) {
        return itemstack.isDamaged();
    }

    public int getBarWidth(ItemStack itemstack) {
        return MathHelper.clamp(Math.round(13.0F - (float) itemstack.getDamageValue() * 13.0F / (float) itemstack.getMaxDamage()), 0, 13);
    }

    public int getBarColor(ItemStack itemstack) {
        int i = itemstack.getMaxDamage();
        float f = Math.max(0.0F, ((float) i - (float) itemstack.getDamageValue()) / (float) i);

        return MathHelper.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
    }

    public boolean overrideStackedOnOther(ItemStack itemstack, Slot slot, ClickAction clickaction, EntityHuman entityhuman) {
        return false;
    }

    public boolean overrideOtherStackedOnMe(ItemStack itemstack, ItemStack itemstack1, Slot slot, ClickAction clickaction, EntityHuman entityhuman, SlotAccess slotaccess) {
        return false;
    }

    public float getAttackDamageBonus(EntityHuman entityhuman, float f) {
        return 0.0F;
    }

    public boolean hurtEnemy(ItemStack itemstack, EntityLiving entityliving, EntityLiving entityliving1) {
        return false;
    }

    public boolean mineBlock(ItemStack itemstack, World world, IBlockData iblockdata, BlockPosition blockposition, EntityLiving entityliving) {
        Tool tool = (Tool) itemstack.get(DataComponents.TOOL);

        if (tool == null) {
            return false;
        } else {
            if (!world.isClientSide && iblockdata.getDestroySpeed(world, blockposition) != 0.0F && tool.damagePerBlock() > 0) {
                itemstack.hurtAndBreak(tool.damagePerBlock(), entityliving, EnumItemSlot.MAINHAND);
            }

            return true;
        }
    }

    public boolean isCorrectToolForDrops(ItemStack itemstack, IBlockData iblockdata) {
        Tool tool = (Tool) itemstack.get(DataComponents.TOOL);

        return tool != null && tool.isCorrectForDrops(iblockdata);
    }

    public EnumInteractionResult interactLivingEntity(ItemStack itemstack, EntityHuman entityhuman, EntityLiving entityliving, EnumHand enumhand) {
        return EnumInteractionResult.PASS;
    }

    public IChatBaseComponent getDescription() {
        return IChatBaseComponent.translatable(this.getDescriptionId());
    }

    public String toString() {
        return BuiltInRegistries.ITEM.getKey(this).getPath();
    }

    protected String getOrCreateDescriptionId() {
        if (this.descriptionId == null) {
            this.descriptionId = SystemUtils.makeDescriptionId("item", BuiltInRegistries.ITEM.getKey(this));
        }

        return this.descriptionId;
    }

    public String getDescriptionId() {
        return this.getOrCreateDescriptionId();
    }

    public String getDescriptionId(ItemStack itemstack) {
        return this.getDescriptionId();
    }

    @Nullable
    public final Item getCraftingRemainingItem() {
        return this.craftingRemainingItem;
    }

    public boolean hasCraftingRemainingItem() {
        return this.craftingRemainingItem != null;
    }

    public void inventoryTick(ItemStack itemstack, World world, Entity entity, int i, boolean flag) {}

    public void onCraftedBy(ItemStack itemstack, World world, EntityHuman entityhuman) {
        this.onCraftedPostProcess(itemstack, world);
    }

    public void onCraftedPostProcess(ItemStack itemstack, World world) {}

    public boolean isComplex() {
        return false;
    }

    public EnumAnimation getUseAnimation(ItemStack itemstack) {
        return itemstack.has(DataComponents.FOOD) ? EnumAnimation.EAT : EnumAnimation.NONE;
    }

    public int getUseDuration(ItemStack itemstack) {
        FoodInfo foodinfo = (FoodInfo) itemstack.get(DataComponents.FOOD);

        return foodinfo != null ? foodinfo.eatDurationTicks() : 0;
    }

    public void releaseUsing(ItemStack itemstack, World world, EntityLiving entityliving, int i) {}

    public void appendHoverText(ItemStack itemstack, Item.b item_b, List<IChatBaseComponent> list, TooltipFlag tooltipflag) {}

    public Optional<TooltipComponent> getTooltipImage(ItemStack itemstack) {
        return Optional.empty();
    }

    public IChatBaseComponent getName(ItemStack itemstack) {
        return IChatBaseComponent.translatable(this.getDescriptionId(itemstack));
    }

    public boolean isFoil(ItemStack itemstack) {
        return itemstack.isEnchanted();
    }

    public boolean isEnchantable(ItemStack itemstack) {
        return itemstack.getMaxStackSize() == 1 && itemstack.has(DataComponents.MAX_DAMAGE);
    }

    protected static MovingObjectPositionBlock getPlayerPOVHitResult(World world, EntityHuman entityhuman, RayTrace.FluidCollisionOption raytrace_fluidcollisionoption) {
        Vec3D vec3d = entityhuman.getEyePosition();
        Vec3D vec3d1 = vec3d.add(entityhuman.calculateViewVector(entityhuman.getXRot(), entityhuman.getYRot()).scale(entityhuman.blockInteractionRange()));

        return world.clip(new RayTrace(vec3d, vec3d1, RayTrace.BlockCollisionOption.OUTLINE, raytrace_fluidcollisionoption, entityhuman));
    }

    public int getEnchantmentValue() {
        return 0;
    }

    public boolean isValidRepairItem(ItemStack itemstack, ItemStack itemstack1) {
        return false;
    }

    /** @deprecated */
    @Deprecated
    public ItemAttributeModifiers getDefaultAttributeModifiers() {
        return ItemAttributeModifiers.EMPTY;
    }

    public boolean useOnRelease(ItemStack itemstack) {
        return false;
    }

    public ItemStack getDefaultInstance() {
        return new ItemStack(this);
    }

    public SoundEffect getDrinkingSound() {
        return SoundEffects.GENERIC_DRINK;
    }

    public SoundEffect getEatingSound() {
        return SoundEffects.GENERIC_EAT;
    }

    public SoundEffect getBreakingSound() {
        return SoundEffects.ITEM_BREAK;
    }

    public boolean canFitInsideContainerItems() {
        return true;
    }

    @Override
    public FeatureFlagSet requiredFeatures() {
        return this.requiredFeatures;
    }

    public static class Info {

        private static final Interner<DataComponentMap> COMPONENT_INTERNER = Interners.newStrongInterner();
        @Nullable
        private DataComponentMap.a components;
        @Nullable
        Item craftingRemainingItem;
        FeatureFlagSet requiredFeatures;

        public Info() {
            this.requiredFeatures = FeatureFlags.VANILLA_SET;
        }

        public Item.Info food(FoodInfo foodinfo) {
            return this.component(DataComponents.FOOD, foodinfo);
        }

        public Item.Info stacksTo(int i) {
            return this.component(DataComponents.MAX_STACK_SIZE, i);
        }

        public Item.Info durability(int i) {
            this.component(DataComponents.MAX_DAMAGE, i);
            this.component(DataComponents.MAX_STACK_SIZE, 1);
            this.component(DataComponents.DAMAGE, 0);
            return this;
        }

        public Item.Info craftRemainder(Item item) {
            this.craftingRemainingItem = item;
            return this;
        }

        public Item.Info rarity(EnumItemRarity enumitemrarity) {
            return this.component(DataComponents.RARITY, enumitemrarity);
        }

        public Item.Info fireResistant() {
            return this.component(DataComponents.FIRE_RESISTANT, Unit.INSTANCE);
        }

        public Item.Info requiredFeatures(FeatureFlag... afeatureflag) {
            this.requiredFeatures = FeatureFlags.REGISTRY.subset(afeatureflag);
            return this;
        }

        public <T> Item.Info component(DataComponentType<T> datacomponenttype, T t0) {
            if (this.components == null) {
                this.components = DataComponentMap.builder().addAll(DataComponents.COMMON_ITEM_COMPONENTS);
            }

            this.components.set(datacomponenttype, t0);
            return this;
        }

        public Item.Info attributes(ItemAttributeModifiers itemattributemodifiers) {
            return this.component(DataComponents.ATTRIBUTE_MODIFIERS, itemattributemodifiers);
        }

        DataComponentMap buildAndValidateComponents() {
            DataComponentMap datacomponentmap = this.buildComponents();

            if (datacomponentmap.has(DataComponents.DAMAGE) && (Integer) datacomponentmap.getOrDefault(DataComponents.MAX_STACK_SIZE, 1) > 1) {
                throw new IllegalStateException("Item cannot have both durability and be stackable");
            } else {
                return datacomponentmap;
            }
        }

        private DataComponentMap buildComponents() {
            return this.components == null ? DataComponents.COMMON_ITEM_COMPONENTS : (DataComponentMap) Item.Info.COMPONENT_INTERNER.intern(this.components.build());
        }
    }

    public interface b {

        Item.b EMPTY = new Item.b() {
            @Nullable
            @Override
            public HolderLookup.a registries() {
                return null;
            }

            @Override
            public float tickRate() {
                return 20.0F;
            }

            @Nullable
            @Override
            public WorldMap mapData(MapId mapid) {
                return null;
            }
        };

        @Nullable
        HolderLookup.a registries();

        float tickRate();

        @Nullable
        WorldMap mapData(MapId mapid);

        static Item.b of(@Nullable final World world) {
            return world == null ? Item.b.EMPTY : new Item.b() {
                @Override
                public HolderLookup.a registries() {
                    return world.registryAccess();
                }

                @Override
                public float tickRate() {
                    return world.tickRateManager().tickrate();
                }

                @Override
                public WorldMap mapData(MapId mapid) {
                    return world.getMapData(mapid);
                }
            };
        }

        static Item.b of(final HolderLookup.a holderlookup_a) {
            return new Item.b() {
                @Override
                public HolderLookup.a registries() {
                    return holderlookup_a;
                }

                @Override
                public float tickRate() {
                    return 20.0F;
                }

                @Nullable
                @Override
                public WorldMap mapData(MapId mapid) {
                    return null;
                }
            };
        }
    }
}
