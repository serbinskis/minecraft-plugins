package net.minecraft.world.item;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DataResult.Error;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.EnumChatFormat;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.ChatComponentUtils;
import net.minecraft.network.chat.ChatHoverable;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.stats.StatisticList;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.MathHelper;
import net.minecraft.util.NullOps;
import net.minecraft.util.Unit;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.InteractionResultWrapper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.ai.attributes.AttributeBase;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.entity.decoration.EntityItemFrame;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.item.context.ItemActionContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentManager;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.IMaterial;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.pattern.ShapeDetectorBlock;
import net.minecraft.world.level.saveddata.maps.MapId;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.slf4j.Logger;

public final class ItemStack implements DataComponentHolder {

    public static final Codec<Holder<Item>> ITEM_NON_AIR_CODEC = BuiltInRegistries.ITEM.holderByNameCodec().validate((holder) -> {
        return holder.is((Holder) Items.AIR.builtInRegistryHolder()) ? DataResult.error(() -> {
            return "Item must not be minecraft:air";
        }) : DataResult.success(holder);
    });
    public static final Codec<ItemStack> CODEC = Codec.lazyInitialized(() -> {
        return RecordCodecBuilder.create((instance) -> {
            return instance.group(ItemStack.ITEM_NON_AIR_CODEC.fieldOf("id").forGetter(ItemStack::getItemHolder), ExtraCodecs.intRange(1, 99).fieldOf("count").orElse(1).forGetter(ItemStack::getCount), DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter((itemstack) -> {
                return itemstack.components.asPatch();
            })).apply(instance, ItemStack::new);
        });
    });
    public static final Codec<ItemStack> SINGLE_ITEM_CODEC = Codec.lazyInitialized(() -> {
        return RecordCodecBuilder.create((instance) -> {
            return instance.group(ItemStack.ITEM_NON_AIR_CODEC.fieldOf("id").forGetter(ItemStack::getItemHolder), DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter((itemstack) -> {
                return itemstack.components.asPatch();
            })).apply(instance, (holder, datacomponentpatch) -> {
                return new ItemStack(holder, 1, datacomponentpatch);
            });
        });
    });
    public static final Codec<ItemStack> STRICT_CODEC = ItemStack.CODEC.validate(ItemStack::validateStrict);
    public static final Codec<ItemStack> STRICT_SINGLE_ITEM_CODEC = ItemStack.SINGLE_ITEM_CODEC.validate(ItemStack::validateStrict);
    public static final Codec<ItemStack> OPTIONAL_CODEC = ExtraCodecs.optionalEmptyMap(ItemStack.CODEC).xmap((optional) -> {
        return (ItemStack) optional.orElse(ItemStack.EMPTY);
    }, (itemstack) -> {
        return itemstack.isEmpty() ? Optional.empty() : Optional.of(itemstack);
    });
    public static final Codec<ItemStack> SIMPLE_ITEM_CODEC = ItemStack.ITEM_NON_AIR_CODEC.xmap(ItemStack::new, ItemStack::getItemHolder);
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemStack> OPTIONAL_STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, ItemStack>() {
        private static final StreamCodec<RegistryFriendlyByteBuf, Holder<Item>> ITEM_STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.ITEM);

        public ItemStack decode(RegistryFriendlyByteBuf registryfriendlybytebuf) {
            int i = registryfriendlybytebuf.readVarInt();

            if (i <= 0) {
                return ItemStack.EMPTY;
            } else {
                Holder<Item> holder = (Holder) null.ITEM_STREAM_CODEC.decode(registryfriendlybytebuf);
                DataComponentPatch datacomponentpatch = (DataComponentPatch) DataComponentPatch.STREAM_CODEC.decode(registryfriendlybytebuf);

                return new ItemStack(holder, i, datacomponentpatch);
            }
        }

        public void encode(RegistryFriendlyByteBuf registryfriendlybytebuf, ItemStack itemstack) {
            if (itemstack.isEmpty()) {
                registryfriendlybytebuf.writeVarInt(0);
            } else {
                registryfriendlybytebuf.writeVarInt(itemstack.getCount());
                null.ITEM_STREAM_CODEC.encode(registryfriendlybytebuf, itemstack.getItemHolder());
                DataComponentPatch.STREAM_CODEC.encode(registryfriendlybytebuf, itemstack.components.asPatch());
            }
        }
    };
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemStack> STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, ItemStack>() {
        public ItemStack decode(RegistryFriendlyByteBuf registryfriendlybytebuf) {
            ItemStack itemstack = (ItemStack) ItemStack.OPTIONAL_STREAM_CODEC.decode(registryfriendlybytebuf);

            if (itemstack.isEmpty()) {
                throw new DecoderException("Empty ItemStack not allowed");
            } else {
                return itemstack;
            }
        }

        public void encode(RegistryFriendlyByteBuf registryfriendlybytebuf, ItemStack itemstack) {
            if (itemstack.isEmpty()) {
                throw new EncoderException("Empty ItemStack not allowed");
            } else {
                ItemStack.OPTIONAL_STREAM_CODEC.encode(registryfriendlybytebuf, itemstack);
            }
        }
    };
    public static final StreamCodec<RegistryFriendlyByteBuf, List<ItemStack>> OPTIONAL_LIST_STREAM_CODEC = ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.collection(NonNullList::createWithCapacity));
    public static final StreamCodec<RegistryFriendlyByteBuf, List<ItemStack>> LIST_STREAM_CODEC = ItemStack.STREAM_CODEC.apply(ByteBufCodecs.collection(NonNullList::createWithCapacity));
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final ItemStack EMPTY = new ItemStack((Void) null);
    private static final IChatBaseComponent DISABLED_ITEM_TOOLTIP = IChatBaseComponent.translatable("item.disabled").withStyle(EnumChatFormat.RED);
    private int count;
    private int popTime;
    /** @deprecated */
    @Deprecated
    @Nullable
    private Item item;
    final PatchedDataComponentMap components;
    @Nullable
    private Entity entityRepresentation;

    private static DataResult<ItemStack> validateStrict(ItemStack itemstack) {
        DataResult<Unit> dataresult = validateComponents(itemstack.getComponents());

        return dataresult.isError() ? dataresult.map((unit) -> {
            return itemstack;
        }) : (itemstack.getCount() > itemstack.getMaxStackSize() ? DataResult.error(() -> {
            int i = itemstack.getCount();

            return "Item stack with stack size of " + i + " was larger than maximum: " + itemstack.getMaxStackSize();
        }) : DataResult.success(itemstack));
    }

    public static StreamCodec<RegistryFriendlyByteBuf, ItemStack> validatedStreamCodec(final StreamCodec<RegistryFriendlyByteBuf, ItemStack> streamcodec) {
        return new StreamCodec<RegistryFriendlyByteBuf, ItemStack>() {
            public ItemStack decode(RegistryFriendlyByteBuf registryfriendlybytebuf) {
                ItemStack itemstack = (ItemStack) streamcodec.decode(registryfriendlybytebuf);

                if (!itemstack.isEmpty()) {
                    RegistryOps<Unit> registryops = registryfriendlybytebuf.registryAccess().createSerializationContext(NullOps.INSTANCE);

                    ItemStack.CODEC.encodeStart(registryops, itemstack).getOrThrow(DecoderException::new);
                }

                return itemstack;
            }

            public void encode(RegistryFriendlyByteBuf registryfriendlybytebuf, ItemStack itemstack) {
                streamcodec.encode(registryfriendlybytebuf, itemstack);
            }
        };
    }

    public Optional<TooltipComponent> getTooltipImage() {
        return this.getItem().getTooltipImage(this);
    }

    @Override
    public DataComponentMap getComponents() {
        return (DataComponentMap) (!this.isEmpty() ? this.components : DataComponentMap.EMPTY);
    }

    public DataComponentMap getPrototype() {
        return !this.isEmpty() ? this.getItem().components() : DataComponentMap.EMPTY;
    }

    public DataComponentPatch getComponentsPatch() {
        return !this.isEmpty() ? this.components.asPatch() : DataComponentPatch.EMPTY;
    }

    public ItemStack(IMaterial imaterial) {
        this(imaterial, 1);
    }

    public ItemStack(Holder<Item> holder) {
        this((IMaterial) holder.value(), 1);
    }

    public ItemStack(Holder<Item> holder, int i, DataComponentPatch datacomponentpatch) {
        this((IMaterial) holder.value(), i, PatchedDataComponentMap.fromPatch(((Item) holder.value()).components(), datacomponentpatch));
    }

    public ItemStack(Holder<Item> holder, int i) {
        this((IMaterial) holder.value(), i);
    }

    public ItemStack(IMaterial imaterial, int i) {
        this(imaterial, i, new PatchedDataComponentMap(imaterial.asItem().components()));
    }

    private ItemStack(IMaterial imaterial, int i, PatchedDataComponentMap patcheddatacomponentmap) {
        this.item = imaterial.asItem();
        this.count = i;
        this.components = patcheddatacomponentmap;
        this.getItem().verifyComponentsAfterLoad(this);
    }

    private ItemStack(@Nullable Void ovoid) {
        this.item = null;
        this.components = new PatchedDataComponentMap(DataComponentMap.EMPTY);
    }

    public static DataResult<Unit> validateComponents(DataComponentMap datacomponentmap) {
        if (datacomponentmap.has(DataComponents.MAX_DAMAGE) && (Integer) datacomponentmap.getOrDefault(DataComponents.MAX_STACK_SIZE, 1) > 1) {
            return DataResult.error(() -> {
                return "Item cannot be both damageable and stackable";
            });
        } else {
            ItemContainerContents itemcontainercontents = (ItemContainerContents) datacomponentmap.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
            Iterator iterator = itemcontainercontents.nonEmptyItems().iterator();

            int i;
            int j;

            do {
                if (!iterator.hasNext()) {
                    return DataResult.success(Unit.INSTANCE);
                }

                ItemStack itemstack = (ItemStack) iterator.next();

                i = itemstack.getCount();
                j = itemstack.getMaxStackSize();
            } while (i <= j);

            return DataResult.error(() -> {
                return "Item stack with count of " + i + " was larger than maximum: " + j;
            });
        }
    }

    public static Optional<ItemStack> parse(HolderLookup.a holderlookup_a, NBTBase nbtbase) {
        return ItemStack.CODEC.parse(holderlookup_a.createSerializationContext(DynamicOpsNBT.INSTANCE), nbtbase).resultOrPartial((s) -> {
            ItemStack.LOGGER.error("Tried to load invalid item: '{}'", s);
        });
    }

    public static ItemStack parseOptional(HolderLookup.a holderlookup_a, NBTTagCompound nbttagcompound) {
        return nbttagcompound.isEmpty() ? ItemStack.EMPTY : (ItemStack) parse(holderlookup_a, nbttagcompound).orElse(ItemStack.EMPTY);
    }

    public boolean isEmpty() {
        return this == ItemStack.EMPTY || this.item == Items.AIR || this.count <= 0;
    }

    public boolean isItemEnabled(FeatureFlagSet featureflagset) {
        return this.isEmpty() || this.getItem().isEnabled(featureflagset);
    }

    public ItemStack split(int i) {
        int j = Math.min(i, this.getCount());
        ItemStack itemstack = this.copyWithCount(j);

        this.shrink(j);
        return itemstack;
    }

    public ItemStack copyAndClear() {
        if (this.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            ItemStack itemstack = this.copy();

            this.setCount(0);
            return itemstack;
        }
    }

    public Item getItem() {
        return this.isEmpty() ? Items.AIR : this.item;
    }

    public Holder<Item> getItemHolder() {
        return this.getItem().builtInRegistryHolder();
    }

    public boolean is(TagKey<Item> tagkey) {
        return this.getItem().builtInRegistryHolder().is(tagkey);
    }

    public boolean is(Item item) {
        return this.getItem() == item;
    }

    public boolean is(Predicate<Holder<Item>> predicate) {
        return predicate.test(this.getItem().builtInRegistryHolder());
    }

    public boolean is(Holder<Item> holder) {
        return this.getItem().builtInRegistryHolder() == holder;
    }

    public boolean is(HolderSet<Item> holderset) {
        return holderset.contains(this.getItemHolder());
    }

    public Stream<TagKey<Item>> getTags() {
        return this.getItem().builtInRegistryHolder().tags();
    }

    public EnumInteractionResult useOn(ItemActionContext itemactioncontext) {
        EntityHuman entityhuman = itemactioncontext.getPlayer();
        BlockPosition blockposition = itemactioncontext.getClickedPos();

        if (entityhuman != null && !entityhuman.getAbilities().mayBuild && !this.canPlaceOnBlockInAdventureMode(new ShapeDetectorBlock(itemactioncontext.getLevel(), blockposition, false))) {
            return EnumInteractionResult.PASS;
        } else {
            Item item = this.getItem();
            EnumInteractionResult enuminteractionresult = item.useOn(itemactioncontext);

            if (entityhuman != null && enuminteractionresult.indicateItemUse()) {
                entityhuman.awardStat(StatisticList.ITEM_USED.get(item));
            }

            return enuminteractionresult;
        }
    }

    public float getDestroySpeed(IBlockData iblockdata) {
        return this.getItem().getDestroySpeed(this, iblockdata);
    }

    public InteractionResultWrapper<ItemStack> use(World world, EntityHuman entityhuman, EnumHand enumhand) {
        return this.getItem().use(world, entityhuman, enumhand);
    }

    public ItemStack finishUsingItem(World world, EntityLiving entityliving) {
        return this.getItem().finishUsingItem(this, world, entityliving);
    }

    public NBTBase save(HolderLookup.a holderlookup_a, NBTBase nbtbase) {
        if (this.isEmpty()) {
            throw new IllegalStateException("Cannot encode empty ItemStack");
        } else {
            return (NBTBase) ItemStack.CODEC.encode(this, holderlookup_a.createSerializationContext(DynamicOpsNBT.INSTANCE), nbtbase).getOrThrow();
        }
    }

    public NBTBase save(HolderLookup.a holderlookup_a) {
        if (this.isEmpty()) {
            throw new IllegalStateException("Cannot encode empty ItemStack");
        } else {
            return (NBTBase) ItemStack.CODEC.encodeStart(holderlookup_a.createSerializationContext(DynamicOpsNBT.INSTANCE), this).getOrThrow();
        }
    }

    public NBTBase saveOptional(HolderLookup.a holderlookup_a) {
        return (NBTBase) (this.isEmpty() ? new NBTTagCompound() : this.save(holderlookup_a, new NBTTagCompound()));
    }

    public int getMaxStackSize() {
        return (Integer) this.getOrDefault(DataComponents.MAX_STACK_SIZE, 1);
    }

    public boolean isStackable() {
        return this.getMaxStackSize() > 1 && (!this.isDamageableItem() || !this.isDamaged());
    }

    public boolean isDamageableItem() {
        return this.has(DataComponents.MAX_DAMAGE) && !this.has(DataComponents.UNBREAKABLE) && this.has(DataComponents.DAMAGE);
    }

    public boolean isDamaged() {
        return this.isDamageableItem() && this.getDamageValue() > 0;
    }

    public int getDamageValue() {
        return MathHelper.clamp((Integer) this.getOrDefault(DataComponents.DAMAGE, 0), 0, this.getMaxDamage());
    }

    public void setDamageValue(int i) {
        this.set(DataComponents.DAMAGE, MathHelper.clamp(i, 0, this.getMaxDamage()));
    }

    public int getMaxDamage() {
        return (Integer) this.getOrDefault(DataComponents.MAX_DAMAGE, 0);
    }

    public void hurtAndBreak(int i, WorldServer worldserver, @Nullable EntityPlayer entityplayer, Consumer<Item> consumer) {
        if (this.isDamageableItem()) {
            if (entityplayer == null || !entityplayer.hasInfiniteMaterials()) {
                if (i > 0) {
                    i = EnchantmentManager.processDurabilityChange(worldserver, this, i);
                    if (i <= 0) {
                        return;
                    }
                }

                if (entityplayer != null && i != 0) {
                    CriterionTriggers.ITEM_DURABILITY_CHANGED.trigger(entityplayer, this, this.getDamageValue() + i);
                }

                int j = this.getDamageValue() + i;

                this.setDamageValue(j);
                if (j >= this.getMaxDamage()) {
                    Item item = this.getItem();

                    this.shrink(1);
                    consumer.accept(item);
                }

            }
        }
    }

    public void hurtAndBreak(int i, EntityLiving entityliving, EnumItemSlot enumitemslot) {
        World world = entityliving.level();

        if (world instanceof WorldServer worldserver) {
            EntityPlayer entityplayer;

            if (entityliving instanceof EntityPlayer entityplayer1) {
                entityplayer = entityplayer1;
            } else {
                entityplayer = null;
            }

            this.hurtAndBreak(i, worldserver, entityplayer, (item) -> {
                entityliving.onEquippedItemBroken(item, enumitemslot);
            });
        }

    }

    public ItemStack hurtAndConvertOnBreak(int i, IMaterial imaterial, EntityLiving entityliving, EnumItemSlot enumitemslot) {
        this.hurtAndBreak(i, entityliving, enumitemslot);
        if (this.isEmpty()) {
            ItemStack itemstack = this.transmuteCopyIgnoreEmpty(imaterial, 1);

            if (itemstack.isDamageableItem()) {
                itemstack.setDamageValue(0);
            }

            return itemstack;
        } else {
            return this;
        }
    }

    public boolean isBarVisible() {
        return this.getItem().isBarVisible(this);
    }

    public int getBarWidth() {
        return this.getItem().getBarWidth(this);
    }

    public int getBarColor() {
        return this.getItem().getBarColor(this);
    }

    public boolean overrideStackedOnOther(Slot slot, ClickAction clickaction, EntityHuman entityhuman) {
        return this.getItem().overrideStackedOnOther(this, slot, clickaction, entityhuman);
    }

    public boolean overrideOtherStackedOnMe(ItemStack itemstack, Slot slot, ClickAction clickaction, EntityHuman entityhuman, SlotAccess slotaccess) {
        return this.getItem().overrideOtherStackedOnMe(this, itemstack, slot, clickaction, entityhuman, slotaccess);
    }

    public boolean hurtEnemy(EntityLiving entityliving, EntityHuman entityhuman) {
        Item item = this.getItem();

        if (item.hurtEnemy(this, entityliving, entityhuman)) {
            entityhuman.awardStat(StatisticList.ITEM_USED.get(item));
            return true;
        } else {
            return false;
        }
    }

    public void postHurtEnemy(EntityLiving entityliving, EntityHuman entityhuman) {
        this.getItem().postHurtEnemy(this, entityliving, entityhuman);
    }

    public void mineBlock(World world, IBlockData iblockdata, BlockPosition blockposition, EntityHuman entityhuman) {
        Item item = this.getItem();

        if (item.mineBlock(this, world, iblockdata, blockposition, entityhuman)) {
            entityhuman.awardStat(StatisticList.ITEM_USED.get(item));
        }

    }

    public boolean isCorrectToolForDrops(IBlockData iblockdata) {
        return this.getItem().isCorrectToolForDrops(this, iblockdata);
    }

    public EnumInteractionResult interactLivingEntity(EntityHuman entityhuman, EntityLiving entityliving, EnumHand enumhand) {
        return this.getItem().interactLivingEntity(this, entityhuman, entityliving, enumhand);
    }

    public ItemStack copy() {
        if (this.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            ItemStack itemstack = new ItemStack(this.getItem(), this.count, this.components.copy());

            itemstack.setPopTime(this.getPopTime());
            return itemstack;
        }
    }

    public ItemStack copyWithCount(int i) {
        if (this.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            ItemStack itemstack = this.copy();

            itemstack.setCount(i);
            return itemstack;
        }
    }

    public ItemStack transmuteCopy(IMaterial imaterial) {
        return this.transmuteCopy(imaterial, this.getCount());
    }

    public ItemStack transmuteCopy(IMaterial imaterial, int i) {
        return this.isEmpty() ? ItemStack.EMPTY : this.transmuteCopyIgnoreEmpty(imaterial, i);
    }

    private ItemStack transmuteCopyIgnoreEmpty(IMaterial imaterial, int i) {
        return new ItemStack(imaterial.asItem().builtInRegistryHolder(), i, this.components.asPatch());
    }

    public static boolean matches(ItemStack itemstack, ItemStack itemstack1) {
        return itemstack == itemstack1 ? true : (itemstack.getCount() != itemstack1.getCount() ? false : isSameItemSameComponents(itemstack, itemstack1));
    }

    /** @deprecated */
    @Deprecated
    public static boolean listMatches(List<ItemStack> list, List<ItemStack> list1) {
        if (list.size() != list1.size()) {
            return false;
        } else {
            for (int i = 0; i < list.size(); ++i) {
                if (!matches((ItemStack) list.get(i), (ItemStack) list1.get(i))) {
                    return false;
                }
            }

            return true;
        }
    }

    public static boolean isSameItem(ItemStack itemstack, ItemStack itemstack1) {
        return itemstack.is(itemstack1.getItem());
    }

    public static boolean isSameItemSameComponents(ItemStack itemstack, ItemStack itemstack1) {
        return !itemstack.is(itemstack1.getItem()) ? false : (itemstack.isEmpty() && itemstack1.isEmpty() ? true : Objects.equals(itemstack.components, itemstack1.components));
    }

    public static MapCodec<ItemStack> lenientOptionalFieldOf(String s) {
        return ItemStack.CODEC.lenientOptionalFieldOf(s).xmap((optional) -> {
            return (ItemStack) optional.orElse(ItemStack.EMPTY);
        }, (itemstack) -> {
            return itemstack.isEmpty() ? Optional.empty() : Optional.of(itemstack);
        });
    }

    public static int hashItemAndComponents(@Nullable ItemStack itemstack) {
        if (itemstack != null) {
            int i = 31 + itemstack.getItem().hashCode();

            return 31 * i + itemstack.getComponents().hashCode();
        } else {
            return 0;
        }
    }

    /** @deprecated */
    @Deprecated
    public static int hashStackList(List<ItemStack> list) {
        int i = 0;

        ItemStack itemstack;

        for (Iterator iterator = list.iterator(); iterator.hasNext(); i = i * 31 + hashItemAndComponents(itemstack)) {
            itemstack = (ItemStack) iterator.next();
        }

        return i;
    }

    public String getDescriptionId() {
        return this.getItem().getDescriptionId(this);
    }

    public String toString() {
        int i = this.getCount();

        return "" + i + " " + String.valueOf(this.getItem());
    }

    public void inventoryTick(World world, Entity entity, int i, boolean flag) {
        if (this.popTime > 0) {
            --this.popTime;
        }

        if (this.getItem() != null) {
            this.getItem().inventoryTick(this, world, entity, i, flag);
        }

    }

    public void onCraftedBy(World world, EntityHuman entityhuman, int i) {
        entityhuman.awardStat(StatisticList.ITEM_CRAFTED.get(this.getItem()), i);
        this.getItem().onCraftedBy(this, world, entityhuman);
    }

    public void onCraftedBySystem(World world) {
        this.getItem().onCraftedPostProcess(this, world);
    }

    public int getUseDuration(EntityLiving entityliving) {
        return this.getItem().getUseDuration(this, entityliving);
    }

    public EnumAnimation getUseAnimation() {
        return this.getItem().getUseAnimation(this);
    }

    public void releaseUsing(World world, EntityLiving entityliving, int i) {
        this.getItem().releaseUsing(this, world, entityliving, i);
    }

    public boolean useOnRelease() {
        return this.getItem().useOnRelease(this);
    }

    @Nullable
    public <T> T set(DataComponentType<? super T> datacomponenttype, @Nullable T t0) {
        return this.components.set(datacomponenttype, t0);
    }

    @Nullable
    public <T, U> T update(DataComponentType<T> datacomponenttype, T t0, U u0, BiFunction<T, U, T> bifunction) {
        return this.set(datacomponenttype, bifunction.apply(this.getOrDefault(datacomponenttype, t0), u0));
    }

    @Nullable
    public <T> T update(DataComponentType<T> datacomponenttype, T t0, UnaryOperator<T> unaryoperator) {
        T t1 = this.getOrDefault(datacomponenttype, t0);

        return this.set(datacomponenttype, unaryoperator.apply(t1));
    }

    @Nullable
    public <T> T remove(DataComponentType<? extends T> datacomponenttype) {
        return this.components.remove(datacomponenttype);
    }

    public void applyComponentsAndValidate(DataComponentPatch datacomponentpatch) {
        DataComponentPatch datacomponentpatch1 = this.components.asPatch();

        this.components.applyPatch(datacomponentpatch);
        Optional<Error<ItemStack>> optional = validateStrict(this).error();

        if (optional.isPresent()) {
            ItemStack.LOGGER.error("Failed to apply component patch '{}' to item: '{}'", datacomponentpatch, ((Error) optional.get()).message());
            this.components.restorePatch(datacomponentpatch1);
        } else {
            this.getItem().verifyComponentsAfterLoad(this);
        }
    }

    public void applyComponents(DataComponentPatch datacomponentpatch) {
        this.components.applyPatch(datacomponentpatch);
        this.getItem().verifyComponentsAfterLoad(this);
    }

    public void applyComponents(DataComponentMap datacomponentmap) {
        this.components.setAll(datacomponentmap);
        this.getItem().verifyComponentsAfterLoad(this);
    }

    public IChatBaseComponent getHoverName() {
        IChatBaseComponent ichatbasecomponent = (IChatBaseComponent) this.get(DataComponents.CUSTOM_NAME);

        if (ichatbasecomponent != null) {
            return ichatbasecomponent;
        } else {
            IChatBaseComponent ichatbasecomponent1 = (IChatBaseComponent) this.get(DataComponents.ITEM_NAME);

            return ichatbasecomponent1 != null ? ichatbasecomponent1 : this.getItem().getName(this);
        }
    }

    private <T extends TooltipProvider> void addToTooltip(DataComponentType<T> datacomponenttype, Item.b item_b, Consumer<IChatBaseComponent> consumer, TooltipFlag tooltipflag) {
        T t0 = (TooltipProvider) this.get(datacomponenttype);

        if (t0 != null) {
            t0.addToTooltip(item_b, consumer, tooltipflag);
        }

    }

    public List<IChatBaseComponent> getTooltipLines(Item.b item_b, @Nullable EntityHuman entityhuman, TooltipFlag tooltipflag) {
        if (!tooltipflag.isCreative() && this.has(DataComponents.HIDE_TOOLTIP)) {
            return List.of();
        } else {
            List<IChatBaseComponent> list = Lists.newArrayList();
            IChatMutableComponent ichatmutablecomponent = IChatBaseComponent.empty().append(this.getHoverName()).withStyle(this.getRarity().color());

            if (this.has(DataComponents.CUSTOM_NAME)) {
                ichatmutablecomponent.withStyle(EnumChatFormat.ITALIC);
            }

            list.add(ichatmutablecomponent);
            if (!tooltipflag.isAdvanced() && !this.has(DataComponents.CUSTOM_NAME) && this.is(Items.FILLED_MAP)) {
                MapId mapid = (MapId) this.get(DataComponents.MAP_ID);

                if (mapid != null) {
                    list.add(ItemWorldMap.getTooltipForId(mapid));
                }
            }

            Objects.requireNonNull(list);
            Consumer<IChatBaseComponent> consumer = list::add;

            if (!this.has(DataComponents.HIDE_ADDITIONAL_TOOLTIP)) {
                this.getItem().appendHoverText(this, item_b, list, tooltipflag);
            }

            this.addToTooltip(DataComponents.JUKEBOX_PLAYABLE, item_b, consumer, tooltipflag);
            this.addToTooltip(DataComponents.TRIM, item_b, consumer, tooltipflag);
            this.addToTooltip(DataComponents.STORED_ENCHANTMENTS, item_b, consumer, tooltipflag);
            this.addToTooltip(DataComponents.ENCHANTMENTS, item_b, consumer, tooltipflag);
            this.addToTooltip(DataComponents.DYED_COLOR, item_b, consumer, tooltipflag);
            this.addToTooltip(DataComponents.LORE, item_b, consumer, tooltipflag);
            this.addAttributeTooltips(consumer, entityhuman);
            this.addToTooltip(DataComponents.UNBREAKABLE, item_b, consumer, tooltipflag);
            AdventureModePredicate adventuremodepredicate = (AdventureModePredicate) this.get(DataComponents.CAN_BREAK);

            if (adventuremodepredicate != null && adventuremodepredicate.showInTooltip()) {
                consumer.accept(CommonComponents.EMPTY);
                consumer.accept(AdventureModePredicate.CAN_BREAK_HEADER);
                adventuremodepredicate.addToTooltip(consumer);
            }

            AdventureModePredicate adventuremodepredicate1 = (AdventureModePredicate) this.get(DataComponents.CAN_PLACE_ON);

            if (adventuremodepredicate1 != null && adventuremodepredicate1.showInTooltip()) {
                consumer.accept(CommonComponents.EMPTY);
                consumer.accept(AdventureModePredicate.CAN_PLACE_HEADER);
                adventuremodepredicate1.addToTooltip(consumer);
            }

            if (tooltipflag.isAdvanced()) {
                if (this.isDamaged()) {
                    list.add(IChatBaseComponent.translatable("item.durability", this.getMaxDamage() - this.getDamageValue(), this.getMaxDamage()));
                }

                list.add(IChatBaseComponent.literal(BuiltInRegistries.ITEM.getKey(this.getItem()).toString()).withStyle(EnumChatFormat.DARK_GRAY));
                int i = this.components.size();

                if (i > 0) {
                    list.add(IChatBaseComponent.translatable("item.components", i).withStyle(EnumChatFormat.DARK_GRAY));
                }
            }

            if (entityhuman != null && !this.getItem().isEnabled(entityhuman.level().enabledFeatures())) {
                list.add(ItemStack.DISABLED_ITEM_TOOLTIP);
            }

            return list;
        }
    }

    private void addAttributeTooltips(Consumer<IChatBaseComponent> consumer, @Nullable EntityHuman entityhuman) {
        ItemAttributeModifiers itemattributemodifiers = (ItemAttributeModifiers) this.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);

        if (itemattributemodifiers.showInTooltip()) {
            EquipmentSlotGroup[] aequipmentslotgroup = EquipmentSlotGroup.values();
            int i = aequipmentslotgroup.length;

            for (int j = 0; j < i; ++j) {
                EquipmentSlotGroup equipmentslotgroup = aequipmentslotgroup[j];
                MutableBoolean mutableboolean = new MutableBoolean(true);

                this.forEachModifier(equipmentslotgroup, (holder, attributemodifier) -> {
                    if (mutableboolean.isTrue()) {
                        consumer.accept(CommonComponents.EMPTY);
                        consumer.accept(IChatBaseComponent.translatable("item.modifiers." + equipmentslotgroup.getSerializedName()).withStyle(EnumChatFormat.GRAY));
                        mutableboolean.setFalse();
                    }

                    this.addModifierTooltip(consumer, entityhuman, holder, attributemodifier);
                });
            }

        }
    }

    private void addModifierTooltip(Consumer<IChatBaseComponent> consumer, @Nullable EntityHuman entityhuman, Holder<AttributeBase> holder, AttributeModifier attributemodifier) {
        double d0 = attributemodifier.amount();
        boolean flag = false;

        if (entityhuman != null) {
            if (attributemodifier.is(Item.BASE_ATTACK_DAMAGE_ID)) {
                d0 += entityhuman.getAttributeBaseValue(GenericAttributes.ATTACK_DAMAGE);
                flag = true;
            } else if (attributemodifier.is(Item.BASE_ATTACK_SPEED_ID)) {
                d0 += entityhuman.getAttributeBaseValue(GenericAttributes.ATTACK_SPEED);
                flag = true;
            }
        }

        double d1;

        if (attributemodifier.operation() != AttributeModifier.Operation.ADD_MULTIPLIED_BASE && attributemodifier.operation() != AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
            if (holder.is(GenericAttributes.KNOCKBACK_RESISTANCE)) {
                d1 = d0 * 10.0D;
            } else {
                d1 = d0;
            }
        } else {
            d1 = d0 * 100.0D;
        }

        if (flag) {
            consumer.accept(CommonComponents.space().append((IChatBaseComponent) IChatBaseComponent.translatable("attribute.modifier.equals." + attributemodifier.operation().id(), ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(d1), IChatBaseComponent.translatable(((AttributeBase) holder.value()).getDescriptionId()))).withStyle(EnumChatFormat.DARK_GREEN));
        } else if (d0 > 0.0D) {
            consumer.accept(IChatBaseComponent.translatable("attribute.modifier.plus." + attributemodifier.operation().id(), ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(d1), IChatBaseComponent.translatable(((AttributeBase) holder.value()).getDescriptionId())).withStyle(((AttributeBase) holder.value()).getStyle(true)));
        } else if (d0 < 0.0D) {
            consumer.accept(IChatBaseComponent.translatable("attribute.modifier.take." + attributemodifier.operation().id(), ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(-d1), IChatBaseComponent.translatable(((AttributeBase) holder.value()).getDescriptionId())).withStyle(((AttributeBase) holder.value()).getStyle(false)));
        }

    }

    public boolean hasFoil() {
        Boolean obool = (Boolean) this.get(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);

        return obool != null ? obool : this.getItem().isFoil(this);
    }

    public EnumItemRarity getRarity() {
        EnumItemRarity enumitemrarity = (EnumItemRarity) this.getOrDefault(DataComponents.RARITY, EnumItemRarity.COMMON);

        if (!this.isEnchanted()) {
            return enumitemrarity;
        } else {
            EnumItemRarity enumitemrarity1;

            switch (enumitemrarity) {
                case COMMON:
                case UNCOMMON:
                    enumitemrarity1 = EnumItemRarity.RARE;
                    break;
                case RARE:
                    enumitemrarity1 = EnumItemRarity.EPIC;
                    break;
                default:
                    enumitemrarity1 = enumitemrarity;
            }

            return enumitemrarity1;
        }
    }

    public boolean isEnchantable() {
        if (!this.getItem().isEnchantable(this)) {
            return false;
        } else {
            ItemEnchantments itemenchantments = (ItemEnchantments) this.get(DataComponents.ENCHANTMENTS);

            return itemenchantments != null && itemenchantments.isEmpty();
        }
    }

    public void enchant(Holder<Enchantment> holder, int i) {
        EnchantmentManager.updateEnchantments(this, (itemenchantments_a) -> {
            itemenchantments_a.upgrade(holder, i);
        });
    }

    public boolean isEnchanted() {
        return !((ItemEnchantments) this.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY)).isEmpty();
    }

    public ItemEnchantments getEnchantments() {
        return (ItemEnchantments) this.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
    }

    public boolean isFramed() {
        return this.entityRepresentation instanceof EntityItemFrame;
    }

    public void setEntityRepresentation(@Nullable Entity entity) {
        if (!this.isEmpty()) {
            this.entityRepresentation = entity;
        }

    }

    @Nullable
    public EntityItemFrame getFrame() {
        return this.entityRepresentation instanceof EntityItemFrame ? (EntityItemFrame) this.getEntityRepresentation() : null;
    }

    @Nullable
    public Entity getEntityRepresentation() {
        return !this.isEmpty() ? this.entityRepresentation : null;
    }

    public void forEachModifier(EquipmentSlotGroup equipmentslotgroup, BiConsumer<Holder<AttributeBase>, AttributeModifier> biconsumer) {
        ItemAttributeModifiers itemattributemodifiers = (ItemAttributeModifiers) this.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);

        if (!itemattributemodifiers.modifiers().isEmpty()) {
            itemattributemodifiers.forEach(equipmentslotgroup, biconsumer);
        } else {
            this.getItem().getDefaultAttributeModifiers().forEach(equipmentslotgroup, biconsumer);
        }

        EnchantmentManager.forEachModifier(this, equipmentslotgroup, biconsumer);
    }

    public void forEachModifier(EnumItemSlot enumitemslot, BiConsumer<Holder<AttributeBase>, AttributeModifier> biconsumer) {
        ItemAttributeModifiers itemattributemodifiers = (ItemAttributeModifiers) this.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);

        if (!itemattributemodifiers.modifiers().isEmpty()) {
            itemattributemodifiers.forEach(enumitemslot, biconsumer);
        } else {
            this.getItem().getDefaultAttributeModifiers().forEach(enumitemslot, biconsumer);
        }

        EnchantmentManager.forEachModifier(this, enumitemslot, biconsumer);
    }

    public IChatBaseComponent getDisplayName() {
        IChatMutableComponent ichatmutablecomponent = IChatBaseComponent.empty().append(this.getHoverName());

        if (this.has(DataComponents.CUSTOM_NAME)) {
            ichatmutablecomponent.withStyle(EnumChatFormat.ITALIC);
        }

        IChatMutableComponent ichatmutablecomponent1 = ChatComponentUtils.wrapInSquareBrackets(ichatmutablecomponent);

        if (!this.isEmpty()) {
            ichatmutablecomponent1.withStyle(this.getRarity().color()).withStyle((chatmodifier) -> {
                return chatmodifier.withHoverEvent(new ChatHoverable(ChatHoverable.EnumHoverAction.SHOW_ITEM, new ChatHoverable.c(this)));
            });
        }

        return ichatmutablecomponent1;
    }

    public boolean canPlaceOnBlockInAdventureMode(ShapeDetectorBlock shapedetectorblock) {
        AdventureModePredicate adventuremodepredicate = (AdventureModePredicate) this.get(DataComponents.CAN_PLACE_ON);

        return adventuremodepredicate != null && adventuremodepredicate.test(shapedetectorblock);
    }

    public boolean canBreakBlockInAdventureMode(ShapeDetectorBlock shapedetectorblock) {
        AdventureModePredicate adventuremodepredicate = (AdventureModePredicate) this.get(DataComponents.CAN_BREAK);

        return adventuremodepredicate != null && adventuremodepredicate.test(shapedetectorblock);
    }

    public int getPopTime() {
        return this.popTime;
    }

    public void setPopTime(int i) {
        this.popTime = i;
    }

    public int getCount() {
        return this.isEmpty() ? 0 : this.count;
    }

    public void setCount(int i) {
        this.count = i;
    }

    public void limitSize(int i) {
        if (!this.isEmpty() && this.getCount() > i) {
            this.setCount(i);
        }

    }

    public void grow(int i) {
        this.setCount(this.getCount() + i);
    }

    public void shrink(int i) {
        this.grow(-i);
    }

    public void consume(int i, @Nullable EntityLiving entityliving) {
        if (entityliving == null || !entityliving.hasInfiniteMaterials()) {
            this.shrink(i);
        }

    }

    public ItemStack consumeAndReturn(int i, @Nullable EntityLiving entityliving) {
        ItemStack itemstack = this.copyWithCount(i);

        this.consume(i, entityliving);
        return itemstack;
    }

    public void onUseTick(World world, EntityLiving entityliving, int i) {
        this.getItem().onUseTick(world, entityliving, this, i);
    }

    public void onDestroyed(EntityItem entityitem) {
        this.getItem().onDestroyed(entityitem);
    }

    public SoundEffect getDrinkingSound() {
        return this.getItem().getDrinkingSound();
    }

    public SoundEffect getEatingSound() {
        return this.getItem().getEatingSound();
    }

    public SoundEffect getBreakingSound() {
        return this.getItem().getBreakingSound();
    }

    public boolean canBeHurtBy(DamageSource damagesource) {
        return !this.has(DataComponents.FIRE_RESISTANT) || !damagesource.is(DamageTypeTags.IS_FIRE);
    }
}
