package net.minecraft.world.entity.decoration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketListenerPlayOut;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.tags.PaintingVariantTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.IMaterial;
import net.minecraft.world.level.World;
import net.minecraft.world.phys.Vec3D;

public class EntityPainting extends EntityHanging implements VariantHolder<Holder<PaintingVariant>> {

    private static final DataWatcherObject<Holder<PaintingVariant>> DATA_PAINTING_VARIANT_ID = DataWatcher.defineId(EntityPainting.class, DataWatcherRegistry.PAINTING_VARIANT);
    private static final ResourceKey<PaintingVariant> DEFAULT_VARIANT = PaintingVariants.KEBAB;
    public static final MapCodec<Holder<PaintingVariant>> VARIANT_MAP_CODEC = BuiltInRegistries.PAINTING_VARIANT.holderByNameCodec().fieldOf("variant");
    public static final Codec<Holder<PaintingVariant>> VARIANT_CODEC = EntityPainting.VARIANT_MAP_CODEC.codec();

    private static Holder<PaintingVariant> getDefaultVariant() {
        return BuiltInRegistries.PAINTING_VARIANT.getHolderOrThrow(EntityPainting.DEFAULT_VARIANT);
    }

    public EntityPainting(EntityTypes<? extends EntityPainting> entitytypes, World world) {
        super(entitytypes, world);
    }

    @Override
    protected void defineSynchedData(DataWatcher.a datawatcher_a) {
        datawatcher_a.define(EntityPainting.DATA_PAINTING_VARIANT_ID, getDefaultVariant());
    }

    @Override
    public void onSyncedDataUpdated(DataWatcherObject<?> datawatcherobject) {
        if (EntityPainting.DATA_PAINTING_VARIANT_ID.equals(datawatcherobject)) {
            this.recalculateBoundingBox();
        }

    }

    public void setVariant(Holder<PaintingVariant> holder) {
        this.entityData.set(EntityPainting.DATA_PAINTING_VARIANT_ID, holder);
    }

    @Override
    public Holder<PaintingVariant> getVariant() {
        return (Holder) this.entityData.get(EntityPainting.DATA_PAINTING_VARIANT_ID);
    }

    public static Optional<EntityPainting> create(World world, BlockPosition blockposition, EnumDirection enumdirection) {
        EntityPainting entitypainting = new EntityPainting(world, blockposition);
        List<Holder<PaintingVariant>> list = new ArrayList();
        Iterable iterable = BuiltInRegistries.PAINTING_VARIANT.getTagOrEmpty(PaintingVariantTags.PLACEABLE);

        Objects.requireNonNull(list);
        iterable.forEach(list::add);
        if (list.isEmpty()) {
            return Optional.empty();
        } else {
            entitypainting.setDirection(enumdirection);
            list.removeIf((holder) -> {
                entitypainting.setVariant(holder);
                return !entitypainting.survives();
            });
            if (list.isEmpty()) {
                return Optional.empty();
            } else {
                int i = list.stream().mapToInt(EntityPainting::variantArea).max().orElse(0);

                list.removeIf((holder) -> {
                    return variantArea(holder) < i;
                });
                Optional<Holder<PaintingVariant>> optional = SystemUtils.getRandomSafe(list, entitypainting.random);

                if (optional.isEmpty()) {
                    return Optional.empty();
                } else {
                    entitypainting.setVariant((Holder) optional.get());
                    entitypainting.setDirection(enumdirection);
                    return Optional.of(entitypainting);
                }
            }
        }
    }

    private static int variantArea(Holder<PaintingVariant> holder) {
        return ((PaintingVariant) holder.value()).getWidth() * ((PaintingVariant) holder.value()).getHeight();
    }

    private EntityPainting(World world, BlockPosition blockposition) {
        super(EntityTypes.PAINTING, world, blockposition);
    }

    public EntityPainting(World world, BlockPosition blockposition, EnumDirection enumdirection, Holder<PaintingVariant> holder) {
        this(world, blockposition);
        this.setVariant(holder);
        this.setDirection(enumdirection);
    }

    @Override
    public void addAdditionalSaveData(NBTTagCompound nbttagcompound) {
        storeVariant(nbttagcompound, this.getVariant());
        nbttagcompound.putByte("facing", (byte) this.direction.get2DDataValue());
        super.addAdditionalSaveData(nbttagcompound);
    }

    @Override
    public void readAdditionalSaveData(NBTTagCompound nbttagcompound) {
        Holder<PaintingVariant> holder = (Holder) EntityPainting.VARIANT_CODEC.parse(DynamicOpsNBT.INSTANCE, nbttagcompound).result().orElseGet(EntityPainting::getDefaultVariant);

        this.setVariant(holder);
        this.direction = EnumDirection.from2DDataValue(nbttagcompound.getByte("facing"));
        super.readAdditionalSaveData(nbttagcompound);
        this.setDirection(this.direction);
    }

    public static void storeVariant(NBTTagCompound nbttagcompound, Holder<PaintingVariant> holder) {
        EntityPainting.VARIANT_CODEC.encodeStart(DynamicOpsNBT.INSTANCE, holder).ifSuccess((nbtbase) -> {
            nbttagcompound.merge((NBTTagCompound) nbtbase);
        });
    }

    @Override
    public int getWidth() {
        return ((PaintingVariant) this.getVariant().value()).getWidth();
    }

    @Override
    public int getHeight() {
        return ((PaintingVariant) this.getVariant().value()).getHeight();
    }

    @Override
    public void dropItem(@Nullable Entity entity) {
        if (this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            this.playSound(SoundEffects.PAINTING_BREAK, 1.0F, 1.0F);
            if (entity instanceof EntityHuman) {
                EntityHuman entityhuman = (EntityHuman) entity;

                if (entityhuman.hasInfiniteMaterials()) {
                    return;
                }
            }

            this.spawnAtLocation((IMaterial) Items.PAINTING);
        }
    }

    @Override
    public void playPlacementSound() {
        this.playSound(SoundEffects.PAINTING_PLACE, 1.0F, 1.0F);
    }

    @Override
    public void moveTo(double d0, double d1, double d2, float f, float f1) {
        this.setPos(d0, d1, d2);
    }

    @Override
    public void lerpTo(double d0, double d1, double d2, float f, float f1, int i) {
        this.setPos(d0, d1, d2);
    }

    @Override
    public Vec3D trackingPosition() {
        return Vec3D.atLowerCornerOf(this.pos);
    }

    @Override
    public Packet<PacketListenerPlayOut> getAddEntityPacket() {
        return new PacketPlayOutSpawnEntity(this, this.direction.get3DDataValue(), this.getPos());
    }

    @Override
    public void recreateFromPacket(PacketPlayOutSpawnEntity packetplayoutspawnentity) {
        super.recreateFromPacket(packetplayoutspawnentity);
        this.setDirection(EnumDirection.from3DDataValue(packetplayoutspawnentity.getData()));
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(Items.PAINTING);
    }
}
