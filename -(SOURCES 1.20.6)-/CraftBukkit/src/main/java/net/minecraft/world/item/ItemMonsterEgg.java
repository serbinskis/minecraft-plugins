package net.minecraft.world.item;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.server.level.WorldServer;
import net.minecraft.stats.StatisticList;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.InteractionResultWrapper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAgeable;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumMobSpawn;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.ItemActionContext;
import net.minecraft.world.level.RayTrace;
import net.minecraft.world.level.Spawner;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.BlockFluids;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.MovingObjectPosition;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.Vec3D;

public class ItemMonsterEgg extends Item {

    private static final Map<EntityTypes<? extends EntityInsentient>, ItemMonsterEgg> BY_ID = Maps.newIdentityHashMap();
    private static final MapCodec<EntityTypes<?>> ENTITY_TYPE_FIELD_CODEC = BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("id");
    private final int backgroundColor;
    private final int highlightColor;
    private final EntityTypes<?> defaultType;

    public ItemMonsterEgg(EntityTypes<? extends EntityInsentient> entitytypes, int i, int j, Item.Info item_info) {
        super(item_info);
        this.defaultType = entitytypes;
        this.backgroundColor = i;
        this.highlightColor = j;
        ItemMonsterEgg.BY_ID.put(entitytypes, this);
    }

    @Override
    public EnumInteractionResult useOn(ItemActionContext itemactioncontext) {
        World world = itemactioncontext.getLevel();

        if (!(world instanceof WorldServer)) {
            return EnumInteractionResult.SUCCESS;
        } else {
            ItemStack itemstack = itemactioncontext.getItemInHand();
            BlockPosition blockposition = itemactioncontext.getClickedPos();
            EnumDirection enumdirection = itemactioncontext.getClickedFace();
            IBlockData iblockdata = world.getBlockState(blockposition);
            TileEntity tileentity = world.getBlockEntity(blockposition);
            EntityTypes entitytypes;

            if (tileentity instanceof Spawner) {
                Spawner spawner = (Spawner) tileentity;

                entitytypes = this.getType(itemstack);
                spawner.setEntityId(entitytypes, world.getRandom());
                world.sendBlockUpdated(blockposition, iblockdata, iblockdata, 3);
                world.gameEvent((Entity) itemactioncontext.getPlayer(), (Holder) GameEvent.BLOCK_CHANGE, blockposition);
                itemstack.shrink(1);
                return EnumInteractionResult.CONSUME;
            } else {
                BlockPosition blockposition1;

                if (iblockdata.getCollisionShape(world, blockposition).isEmpty()) {
                    blockposition1 = blockposition;
                } else {
                    blockposition1 = blockposition.relative(enumdirection);
                }

                entitytypes = this.getType(itemstack);
                if (entitytypes.spawn((WorldServer) world, itemstack, itemactioncontext.getPlayer(), blockposition1, EnumMobSpawn.SPAWN_EGG, true, !Objects.equals(blockposition, blockposition1) && enumdirection == EnumDirection.UP) != null) {
                    itemstack.shrink(1);
                    world.gameEvent((Entity) itemactioncontext.getPlayer(), (Holder) GameEvent.ENTITY_PLACE, blockposition);
                }

                return EnumInteractionResult.CONSUME;
            }
        }
    }

    @Override
    public InteractionResultWrapper<ItemStack> use(World world, EntityHuman entityhuman, EnumHand enumhand) {
        ItemStack itemstack = entityhuman.getItemInHand(enumhand);
        MovingObjectPositionBlock movingobjectpositionblock = getPlayerPOVHitResult(world, entityhuman, RayTrace.FluidCollisionOption.SOURCE_ONLY);

        if (movingobjectpositionblock.getType() != MovingObjectPosition.EnumMovingObjectType.BLOCK) {
            return InteractionResultWrapper.pass(itemstack);
        } else if (!(world instanceof WorldServer)) {
            return InteractionResultWrapper.success(itemstack);
        } else {
            BlockPosition blockposition = movingobjectpositionblock.getBlockPos();

            if (!(world.getBlockState(blockposition).getBlock() instanceof BlockFluids)) {
                return InteractionResultWrapper.pass(itemstack);
            } else if (world.mayInteract(entityhuman, blockposition) && entityhuman.mayUseItemAt(blockposition, movingobjectpositionblock.getDirection(), itemstack)) {
                EntityTypes<?> entitytypes = this.getType(itemstack);
                Entity entity = entitytypes.spawn((WorldServer) world, itemstack, entityhuman, blockposition, EnumMobSpawn.SPAWN_EGG, false, false);

                if (entity == null) {
                    return InteractionResultWrapper.pass(itemstack);
                } else {
                    itemstack.consume(1, entityhuman);
                    entityhuman.awardStat(StatisticList.ITEM_USED.get(this));
                    world.gameEvent((Entity) entityhuman, (Holder) GameEvent.ENTITY_PLACE, entity.position());
                    return InteractionResultWrapper.consume(itemstack);
                }
            } else {
                return InteractionResultWrapper.fail(itemstack);
            }
        }
    }

    public boolean spawnsEntity(ItemStack itemstack, EntityTypes<?> entitytypes) {
        return Objects.equals(this.getType(itemstack), entitytypes);
    }

    public int getColor(int i) {
        return i == 0 ? this.backgroundColor : this.highlightColor;
    }

    @Nullable
    public static ItemMonsterEgg byId(@Nullable EntityTypes<?> entitytypes) {
        return (ItemMonsterEgg) ItemMonsterEgg.BY_ID.get(entitytypes);
    }

    public static Iterable<ItemMonsterEgg> eggs() {
        return Iterables.unmodifiableIterable(ItemMonsterEgg.BY_ID.values());
    }

    public EntityTypes<?> getType(ItemStack itemstack) {
        CustomData customdata = (CustomData) itemstack.getOrDefault(DataComponents.ENTITY_DATA, CustomData.EMPTY);

        return !customdata.isEmpty() ? (EntityTypes) customdata.read(ItemMonsterEgg.ENTITY_TYPE_FIELD_CODEC).result().orElse(this.defaultType) : this.defaultType;
    }

    @Override
    public FeatureFlagSet requiredFeatures() {
        return this.defaultType.requiredFeatures();
    }

    public Optional<EntityInsentient> spawnOffspringFromSpawnEgg(EntityHuman entityhuman, EntityInsentient entityinsentient, EntityTypes<? extends EntityInsentient> entitytypes, WorldServer worldserver, Vec3D vec3d, ItemStack itemstack) {
        if (!this.spawnsEntity(itemstack, entitytypes)) {
            return Optional.empty();
        } else {
            Object object;

            if (entityinsentient instanceof EntityAgeable) {
                object = ((EntityAgeable) entityinsentient).getBreedOffspring(worldserver, (EntityAgeable) entityinsentient);
            } else {
                object = (EntityInsentient) entitytypes.create(worldserver);
            }

            if (object == null) {
                return Optional.empty();
            } else {
                ((EntityInsentient) object).setBaby(true);
                if (!((EntityInsentient) object).isBaby()) {
                    return Optional.empty();
                } else {
                    ((EntityInsentient) object).moveTo(vec3d.x(), vec3d.y(), vec3d.z(), 0.0F, 0.0F);
                    worldserver.addFreshEntityWithPassengers((Entity) object, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.SPAWNER_EGG); // CraftBukkit
                    ((EntityInsentient) object).setCustomName((IChatBaseComponent) itemstack.get(DataComponents.CUSTOM_NAME));
                    itemstack.consume(1, entityhuman);
                    return Optional.of((EntityInsentient) object); // CraftBukkit - decompile error
                }
            }
        }
    }
}
