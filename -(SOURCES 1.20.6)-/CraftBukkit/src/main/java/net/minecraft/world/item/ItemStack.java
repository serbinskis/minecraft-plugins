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
import net.minecraft.sounds.SoundEffect;
import net.minecraft.stats.StatisticList;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.MathHelper;
import net.minecraft.util.NullOps;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Unit;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.InteractionResultWrapper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumItemSlot;
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
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.item.context.ItemActionContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentDurability;
import net.minecraft.world.item.enchantment.EnchantmentManager;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.IMaterial;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.pattern.ShapeDetectorBlock;
import net.minecraft.world.level.saveddata.maps.MapId;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.slf4j.Logger;

// CraftBukkit start
import java.util.Map;
import java.util.Objects;
import net.minecraft.core.EnumDirection;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.network.protocol.game.PacketPlayOutBlockChange;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.world.level.block.BlockBed;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BlockSapling;
import net.minecraft.world.level.block.BlockSign;
import net.minecraft.world.level.block.BlockTileEntity;
import net.minecraft.world.level.block.BlockWitherSkull;
import net.minecraft.world.level.block.SoundEffectType;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityJukeBox;
import net.minecraft.world.level.block.entity.TileEntitySign;
import net.minecraft.world.level.block.entity.TileEntitySkull;
import net.minecraft.world.level.gameevent.GameEvent;
import org.bukkit.Location;
import org.bukkit.TreeType;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.block.CapturedBlockState;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftLocation;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.world.StructureGrowEvent;
// CraftBukkit end

public final class ItemStack implements DataComponentHolder {

    public static final Codec<Holder<Item>> ITEM_NON_AIR_CODEC = BuiltInRegistries.ITEM.holderByNameCodec().validate((holder) -> {
        return holder.is((Holder) Items.AIR.builtInRegistryHolder()) ? DataResult.error(() -> {
            return "Item must not be minecraft:air";
        }) : DataResult.success(holder);
    });
    public static final Codec<ItemStack> CODEC = Codec.lazyInitialized(() -> {
        return RecordCodecBuilder.<ItemStack>create((instance) -> { // CraftBukkit - decompile error
            return instance.group(ItemStack.ITEM_NON_AIR_CODEC.fieldOf("id").forGetter(ItemStack::getItemHolder), ExtraCodecs.POSITIVE_INT.fieldOf("count").orElse(1).forGetter(ItemStack::getCount), DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter((itemstack) -> {
                return itemstack.components.asPatch();
            })).apply(instance, ItemStack::new);
        });
    });
    public static final Codec<ItemStack> SINGLE_ITEM_CODEC = Codec.lazyInitialized(() -> {
        return RecordCodecBuilder.<ItemStack>create((instance) -> { // CraftBukkit - decompile error
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
                Holder<Item> holder = (Holder) ITEM_STREAM_CODEC.decode(registryfriendlybytebuf); // CraftBukkit - decompile error
                DataComponentPatch datacomponentpatch = (DataComponentPatch) DataComponentPatch.STREAM_CODEC.decode(registryfriendlybytebuf);

                // CraftBukkit start
                ItemStack itemstack = new ItemStack(holder, i, datacomponentpatch);
                if (!datacomponentpatch.isEmpty()) {
                    CraftItemStack.setItemMeta(itemstack, CraftItemStack.getItemMeta(itemstack));
                }
                return itemstack;
                // CraftBukkit end
            }
        }

        public void encode(RegistryFriendlyByteBuf registryfriendlybytebuf, ItemStack itemstack) {
            if (itemstack.isEmpty() || itemstack.getItem() == null) { // CraftBukkit - NPE fix itemstack.getItem()
                registryfriendlybytebuf.writeVarInt(0);
            } else {
                registryfriendlybytebuf.writeVarInt(itemstack.getCount());
                ITEM_STREAM_CODEC.encode(registryfriendlybytebuf, itemstack.getItemHolder()); // CraftBukkit - decompile error
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
        }) : (itemstack.getCount() > itemstack.getMaxStackSize() ? DataResult.<ItemStack>error(() -> { // CraftBukkit - decompile error
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
        return datacomponentmap.has(DataComponents.MAX_DAMAGE) && (Integer) datacomponentmap.getOrDefault(DataComponents.MAX_STACK_SIZE, 1) > 1 ? DataResult.error(() -> {
            return "Item cannot be both damageable and stackable";
        }) : DataResult.success(Unit.INSTANCE);
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
            // CraftBukkit start - handle all block place event logic here
            DataComponentPatch oldData = this.getComponentsPatch();
            int oldCount = this.getCount();
            WorldServer world = (WorldServer) itemactioncontext.getLevel();

            if (!(item instanceof ItemBucket || item instanceof SolidBucketItem)) { // if not bucket
                world.captureBlockStates = true;
                // special case bonemeal
                if (item == Items.BONE_MEAL) {
                    world.captureTreeGeneration = true;
                }
            }
            EnumInteractionResult enuminteractionresult;
            try {
                enuminteractionresult = item.useOn(itemactioncontext);
            } finally {
                world.captureBlockStates = false;
            }
            DataComponentPatch newData = this.getComponentsPatch();
            int newCount = this.getCount();
            this.setCount(oldCount);
            this.restorePatch(oldData);
            if (enuminteractionresult.consumesAction() && world.captureTreeGeneration && world.capturedBlockStates.size() > 0) {
                world.captureTreeGeneration = false;
                Location location = CraftLocation.toBukkit(blockposition, world.getWorld());
                TreeType treeType = BlockSapling.treeType;
                BlockSapling.treeType = null;
                List<CraftBlockState> blocks = new java.util.ArrayList<>(world.capturedBlockStates.values());
                world.capturedBlockStates.clear();
                StructureGrowEvent structureEvent = null;
                if (treeType != null) {
                    boolean isBonemeal = getItem() == Items.BONE_MEAL;
                    structureEvent = new StructureGrowEvent(location, treeType, isBonemeal, (Player) entityhuman.getBukkitEntity(), (List< BlockState>) (List<? extends BlockState>) blocks);
                    org.bukkit.Bukkit.getPluginManager().callEvent(structureEvent);
                }

                BlockFertilizeEvent fertilizeEvent = new BlockFertilizeEvent(CraftBlock.at(world, blockposition), (Player) entityhuman.getBukkitEntity(), (List< BlockState>) (List<? extends BlockState>) blocks);
                fertilizeEvent.setCancelled(structureEvent != null && structureEvent.isCancelled());
                org.bukkit.Bukkit.getPluginManager().callEvent(fertilizeEvent);

                if (!fertilizeEvent.isCancelled()) {
                    // Change the stack to its new contents if it hasn't been tampered with.
                    if (this.getCount() == oldCount && Objects.equals(this.components.asPatch(), oldData)) {
                        this.restorePatch(newData);
                        this.setCount(newCount);
                    }
                    for (CraftBlockState blockstate : blocks) {
                        // SPIGOT-7572 - Move fix for SPIGOT-7248 to CapturedBlockState, to allow bees in bee nest
                        CapturedBlockState.setBlockState(blockstate);
                    }
                    entityhuman.awardStat(StatisticList.ITEM_USED.get(item)); // SPIGOT-7236 - award stat
                }

                ItemSign.openSign = null; // SPIGOT-6758 - Reset on early return
                return enuminteractionresult;
            }
            world.captureTreeGeneration = false;

            if (entityhuman != null && enuminteractionresult.indicateItemUse()) {
                EnumHand enumhand = itemactioncontext.getHand();
                org.bukkit.event.block.BlockPlaceEvent placeEvent = null;
                List<BlockState> blocks = new java.util.ArrayList<>(world.capturedBlockStates.values());
                world.capturedBlockStates.clear();
                if (blocks.size() > 1) {
                    placeEvent = org.bukkit.craftbukkit.event.CraftEventFactory.callBlockMultiPlaceEvent(world, entityhuman, enumhand, blocks, blockposition.getX(), blockposition.getY(), blockposition.getZ());
                } else if (blocks.size() == 1) {
                    placeEvent = org.bukkit.craftbukkit.event.CraftEventFactory.callBlockPlaceEvent(world, entityhuman, enumhand, blocks.get(0), blockposition.getX(), blockposition.getY(), blockposition.getZ());
                }

                if (placeEvent != null && (placeEvent.isCancelled() || !placeEvent.canBuild())) {
                    enuminteractionresult = EnumInteractionResult.FAIL; // cancel placement
                    // PAIL: Remove this when MC-99075 fixed
                    placeEvent.getPlayer().updateInventory();
                    // revert back all captured blocks
                    world.preventPoiUpdated = true; // CraftBukkit - SPIGOT-5710
                    for (BlockState blockstate : blocks) {
                        blockstate.update(true, false);
                    }
                    world.preventPoiUpdated = false;

                    // Brute force all possible updates
                    BlockPosition placedPos = ((CraftBlock) placeEvent.getBlock()).getPosition();
                    for (EnumDirection dir : EnumDirection.values()) {
                        ((EntityPlayer) entityhuman).connection.send(new PacketPlayOutBlockChange(world, placedPos.relative(dir)));
                    }
                    ItemSign.openSign = null; // SPIGOT-6758 - Reset on early return
                } else {
                    // Change the stack to its new contents if it hasn't been tampered with.
                    if (this.getCount() == oldCount && Objects.equals(this.components.asPatch(), oldData)) {
                        this.restorePatch(newData);
                        this.setCount(newCount);
                    }

                    for (Map.Entry<BlockPosition, TileEntity> e : world.capturedTileEntities.entrySet()) {
                        world.setBlockEntity(e.getValue());
                    }

                    for (BlockState blockstate : blocks) {
                        int updateFlag = ((CraftBlockState) blockstate).getFlag();
                        IBlockData oldBlock = ((CraftBlockState) blockstate).getHandle();
                        BlockPosition newblockposition = ((CraftBlockState) blockstate).getPosition();
                        IBlockData block = world.getBlockState(newblockposition);

                        if (!(block.getBlock() instanceof BlockTileEntity)) { // Containers get placed automatically
                            block.onPlace(world, newblockposition, oldBlock, true);
                        }

                        world.notifyAndUpdatePhysics(newblockposition, null, oldBlock, block, world.getBlockState(newblockposition), updateFlag, 512); // send null chunk as chunk.k() returns false by this point
                    }

                    // Special case juke boxes as they update their tile entity. Copied from ItemRecord.
                    // PAIL: checkme on updates.
                    if (this.item instanceof ItemRecord) {
                        TileEntity tileentity = world.getBlockEntity(blockposition);

                        if (tileentity instanceof TileEntityJukeBox) {
                            TileEntityJukeBox tileentityjukebox = (TileEntityJukeBox) tileentity;

                            // There can only be one
                            ItemStack record = this.copy();
                            if (!record.isEmpty()) {
                                record.setCount(1);
                            }

                            tileentityjukebox.setTheItem(record);
                            world.gameEvent(GameEvent.BLOCK_CHANGE, blockposition, GameEvent.a.of(entityhuman, world.getBlockState(blockposition)));
                        }

                        this.shrink(1);
                        entityhuman.awardStat(StatisticList.PLAY_RECORD);
                    }

                    if (this.item == Items.WITHER_SKELETON_SKULL) { // Special case skulls to allow wither spawns to be cancelled
                        BlockPosition bp = blockposition;
                        if (!world.getBlockState(blockposition).canBeReplaced()) {
                            if (!world.getBlockState(blockposition).isSolid()) {
                                bp = null;
                            } else {
                                bp = bp.relative(itemactioncontext.getClickedFace());
                            }
                        }
                        if (bp != null) {
                            TileEntity te = world.getBlockEntity(bp);
                            if (te instanceof TileEntitySkull) {
                                BlockWitherSkull.checkSpawn(world, bp, (TileEntitySkull) te);
                            }
                        }
                    }

                    // SPIGOT-4678
                    if (this.item instanceof ItemSign && ItemSign.openSign != null) {
                        try {
                            if (world.getBlockEntity(ItemSign.openSign) instanceof TileEntitySign tileentitysign) {
                                if (world.getBlockState(ItemSign.openSign).getBlock() instanceof BlockSign blocksign) {
                                    blocksign.openTextEdit(entityhuman, tileentitysign, true, org.bukkit.event.player.PlayerSignOpenEvent.Cause.PLACE); // Craftbukkit
                                }
                            }
                        } finally {
                            ItemSign.openSign = null;
                        }
                    }

                    // SPIGOT-7315: Moved from BlockBed#setPlacedBy
                    if (placeEvent != null && this.item instanceof ItemBed) {
                        BlockPosition position = ((CraftBlock) placeEvent.getBlock()).getPosition();
                        IBlockData blockData =  world.getBlockState(position);

                        if (blockData.getBlock() instanceof BlockBed) {
                            world.blockUpdated(position, Blocks.AIR);
                            blockData.updateNeighbourShapes(world, position, 3);
                        }
                    }

                    // SPIGOT-1288 - play sound stripped from ItemBlock
                    if (this.item instanceof ItemBlock) {
                        SoundEffectType soundeffecttype = ((ItemBlock) this.item).getBlock().defaultBlockState().getSoundType(); // TODO: not strictly correct, however currently only affects decorated pots
                        world.playSound(entityhuman, blockposition, soundeffecttype.getPlaceSound(), SoundCategory.BLOCKS, (soundeffecttype.getVolume() + 1.0F) / 2.0F, soundeffecttype.getPitch() * 0.8F);
                    }

                    entityhuman.awardStat(StatisticList.ITEM_USED.get(item));
                }
            }
            world.capturedTileEntities.clear();
            world.capturedBlockStates.clear();
            // CraftBukkit end

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

    public void hurtAndBreak(int i, RandomSource randomsource, @Nullable EntityPlayer entityplayer, Runnable runnable) {
        if (this.isDamageableItem()) {
            int j;

            if (i > 0) {
                j = EnchantmentManager.getItemEnchantmentLevel(Enchantments.UNBREAKING, this);
                int k = 0;

                for (int l = 0; j > 0 && l < i; ++l) {
                    if (EnchantmentDurability.shouldIgnoreDurabilityDrop(this, j, randomsource)) {
                        ++k;
                    }
                }

                i -= k;
                // CraftBukkit start
                if (entityplayer != null) {
                    PlayerItemDamageEvent event = new PlayerItemDamageEvent(entityplayer.getBukkitEntity(), CraftItemStack.asCraftMirror(this), i);
                    event.getPlayer().getServer().getPluginManager().callEvent(event);

                    if (i != event.getDamage() || event.isCancelled()) {
                        event.getPlayer().updateInventory();
                    }
                    if (event.isCancelled()) {
                        return;
                    }

                    i = event.getDamage();
                }
                // CraftBukkit end
                if (i <= 0) {
                    return;
                }
            }

            if (entityplayer != null && i != 0) {
                CriterionTriggers.ITEM_DURABILITY_CHANGED.trigger(entityplayer, this, this.getDamageValue() + i);
            }

            j = this.getDamageValue() + i;
            this.setDamageValue(j);
            if (j >= this.getMaxDamage()) {
                runnable.run();
            }

        }
    }

    public void hurtAndBreak(int i, EntityLiving entityliving, EnumItemSlot enumitemslot) {
        if (!entityliving.level().isClientSide) {
            if (entityliving instanceof EntityHuman) {
                EntityHuman entityhuman = (EntityHuman) entityliving;

                if (entityhuman.hasInfiniteMaterials()) {
                    return;
                }
            }

            RandomSource randomsource = entityliving.getRandom();
            EntityPlayer entityplayer;

            if (entityliving instanceof EntityPlayer) {
                EntityPlayer entityplayer1 = (EntityPlayer) entityliving;

                entityplayer = entityplayer1;
            } else {
                entityplayer = null;
            }

            this.hurtAndBreak(i, randomsource, entityplayer, () -> {
                entityliving.broadcastBreakEvent(enumitemslot);
                Item item = this.getItem();
                // CraftBukkit start - Check for item breaking
                if (this.count == 1 && entityliving instanceof EntityHuman) {
                    org.bukkit.craftbukkit.event.CraftEventFactory.callPlayerItemBreakEvent((EntityHuman) entityliving, this);
                }
                // CraftBukkit end

                this.shrink(1);
                if (entityliving instanceof EntityHuman) {
                    ((EntityHuman) entityliving).awardStat(StatisticList.ITEM_BROKEN.get(item));
                }

                this.setDamageValue(0);
            });
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

    public void hurtEnemy(EntityLiving entityliving, EntityHuman entityhuman) {
        Item item = this.getItem();
        ItemEnchantments itemenchantments = this.getEnchantments();

        if (item.hurtEnemy(this, entityliving, entityhuman)) {
            entityhuman.awardStat(StatisticList.ITEM_USED.get(item));
            EnchantmentManager.doPostItemStackHurtEffects(entityhuman, entityliving, itemenchantments);
        }

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

    public ItemStack transmuteCopy(IMaterial imaterial, int i) {
        return this.isEmpty() ? ItemStack.EMPTY : this.transmuteCopyIgnoreEmpty(imaterial, i);
    }

    public ItemStack transmuteCopyIgnoreEmpty(IMaterial imaterial, int i) {
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

    public int getUseDuration() {
        return this.getItem().getUseDuration(this);
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

    // CraftBukkit start
    public void restorePatch(DataComponentPatch datacomponentpatch) {
        this.components.restorePatch(datacomponentpatch);
    }
    // CraftBukkit end

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
        T t0 = (T) this.get(datacomponenttype); // CraftBukkit - decompile error

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
            EnumItemSlot[] aenumitemslot = EnumItemSlot.values();
            int i = aenumitemslot.length;

            for (int j = 0; j < i; ++j) {
                EnumItemSlot enumitemslot = aenumitemslot[j];
                MutableBoolean mutableboolean = new MutableBoolean(true);

                this.forEachModifier(enumitemslot, (holder, attributemodifier) -> {
                    if (mutableboolean.isTrue()) {
                        consumer.accept(CommonComponents.EMPTY);
                        consumer.accept(IChatBaseComponent.translatable("item.modifiers." + enumitemslot.getName()).withStyle(EnumChatFormat.GRAY));
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
            if (attributemodifier.id() == Item.BASE_ATTACK_DAMAGE_UUID) {
                d0 += entityhuman.getAttributeBaseValue(GenericAttributes.ATTACK_DAMAGE);
                d0 += (double) EnchantmentManager.getDamageBonus(this, (EntityTypes) null);
                flag = true;
            } else if (attributemodifier.id() == Item.BASE_ATTACK_SPEED_UUID) {
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
            consumer.accept(IChatBaseComponent.translatable("attribute.modifier.plus." + attributemodifier.operation().id(), ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(d1), IChatBaseComponent.translatable(((AttributeBase) holder.value()).getDescriptionId())).withStyle(EnumChatFormat.BLUE));
        } else if (d0 < 0.0D) {
            consumer.accept(IChatBaseComponent.translatable("attribute.modifier.take." + attributemodifier.operation().id(), ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(-d1), IChatBaseComponent.translatable(((AttributeBase) holder.value()).getDescriptionId())).withStyle(EnumChatFormat.RED));
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

    public void enchant(Enchantment enchantment, int i) {
        EnchantmentManager.updateEnchantments(this, (itemenchantments_a) -> {
            itemenchantments_a.upgrade(enchantment, i);
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

    public void forEachModifier(EnumItemSlot enumitemslot, BiConsumer<Holder<AttributeBase>, AttributeModifier> biconsumer) {
        ItemAttributeModifiers itemattributemodifiers = (ItemAttributeModifiers) this.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);

        if (!itemattributemodifiers.modifiers().isEmpty()) {
            itemattributemodifiers.forEach(enumitemslot, biconsumer);
        } else {
            this.getItem().getDefaultAttributeModifiers().forEach(enumitemslot, biconsumer);
        }

    }

    // CraftBukkit start
    @Deprecated
    public void setItem(Item item) {
        this.item = item;
    }
    // CraftBukkit end

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
        if ((entityliving == null || !entityliving.hasInfiniteMaterials()) && this != ItemStack.EMPTY) { // CraftBukkit
            this.shrink(i);
        }

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
