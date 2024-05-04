package net.minecraft.world.entity.animal.sniffer;

import com.mojang.serialization.Dynamic;
import io.netty.buffer.ByteBuf;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleParamBlock;
import net.minecraft.core.particles.Particles;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.game.PacketDebug;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.tags.TagsBlock;
import net.minecraft.tags.TagsItem;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAgeable;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityPose;
import net.minecraft.world.entity.EntitySize;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.BehaviorController;
import net.minecraft.world.entity.ai.attributes.AttributeProvider;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.animal.EntityAnimal;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.EnumRenderType;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathEntity;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameterSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameters;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.Vec3D;

public class Sniffer extends EntityAnimal {

    private static final int DIGGING_PARTICLES_DELAY_TICKS = 1700;
    private static final int DIGGING_PARTICLES_DURATION_TICKS = 6000;
    private static final int DIGGING_PARTICLES_AMOUNT = 30;
    private static final int DIGGING_DROP_SEED_OFFSET_TICKS = 120;
    private static final int SNIFFER_BABY_AGE_TICKS = 48000;
    private static final float DIGGING_BB_HEIGHT_OFFSET = 0.4F;
    private static final EntitySize DIGGING_DIMENSIONS = EntitySize.scalable(EntityTypes.SNIFFER.getWidth(), EntityTypes.SNIFFER.getHeight() - 0.4F).withEyeHeight(0.81F);
    private static final DataWatcherObject<Sniffer.State> DATA_STATE = DataWatcher.defineId(Sniffer.class, DataWatcherRegistry.SNIFFER_STATE);
    private static final DataWatcherObject<Integer> DATA_DROP_SEED_AT_TICK = DataWatcher.defineId(Sniffer.class, DataWatcherRegistry.INT);
    public final AnimationState feelingHappyAnimationState = new AnimationState();
    public final AnimationState scentingAnimationState = new AnimationState();
    public final AnimationState sniffingAnimationState = new AnimationState();
    public final AnimationState diggingAnimationState = new AnimationState();
    public final AnimationState risingAnimationState = new AnimationState();

    public static AttributeProvider.Builder createAttributes() {
        return EntityInsentient.createMobAttributes().add(GenericAttributes.MOVEMENT_SPEED, 0.10000000149011612D).add(GenericAttributes.MAX_HEALTH, 14.0D);
    }

    public Sniffer(EntityTypes<? extends EntityAnimal> entitytypes, World world) {
        super(entitytypes, world);
        this.getNavigation().setCanFloat(true);
        this.setPathfindingMalus(PathType.WATER, -1.0F);
        this.setPathfindingMalus(PathType.DANGER_POWDER_SNOW, -1.0F);
        this.setPathfindingMalus(PathType.DAMAGE_CAUTIOUS, -1.0F);
    }

    @Override
    protected void defineSynchedData(DataWatcher.a datawatcher_a) {
        super.defineSynchedData(datawatcher_a);
        datawatcher_a.define(Sniffer.DATA_STATE, Sniffer.State.IDLING);
        datawatcher_a.define(Sniffer.DATA_DROP_SEED_AT_TICK, 0);
    }

    @Override
    public void onPathfindingStart() {
        super.onPathfindingStart();
        if (this.isOnFire() || this.isInWater()) {
            this.setPathfindingMalus(PathType.WATER, 0.0F);
        }

    }

    @Override
    public void onPathfindingDone() {
        this.setPathfindingMalus(PathType.WATER, -1.0F);
    }

    @Override
    public EntitySize getDefaultDimensions(EntityPose entitypose) {
        return this.getState() == Sniffer.State.DIGGING ? Sniffer.DIGGING_DIMENSIONS.scale(this.getAgeScale()) : super.getDefaultDimensions(entitypose);
    }

    public boolean isSearching() {
        return this.getState() == Sniffer.State.SEARCHING;
    }

    public boolean isTempted() {
        return (Boolean) this.brain.getMemory(MemoryModuleType.IS_TEMPTED).orElse(false);
    }

    public boolean canSniff() {
        return !this.isTempted() && !this.isPanicking() && !this.isInWater() && !this.isInLove() && this.onGround() && !this.isPassenger() && !this.isLeashed();
    }

    public boolean canPlayDiggingSound() {
        return this.getState() == Sniffer.State.DIGGING || this.getState() == Sniffer.State.SEARCHING;
    }

    private BlockPosition getHeadBlock() {
        Vec3D vec3d = this.getHeadPosition();

        return BlockPosition.containing(vec3d.x(), this.getY() + 0.20000000298023224D, vec3d.z());
    }

    private Vec3D getHeadPosition() {
        return this.position().add(this.getForward().scale(2.25D));
    }

    public Sniffer.State getState() {
        return (Sniffer.State) this.entityData.get(Sniffer.DATA_STATE);
    }

    private Sniffer setState(Sniffer.State sniffer_state) {
        this.entityData.set(Sniffer.DATA_STATE, sniffer_state);
        return this;
    }

    @Override
    public void onSyncedDataUpdated(DataWatcherObject<?> datawatcherobject) {
        if (Sniffer.DATA_STATE.equals(datawatcherobject)) {
            Sniffer.State sniffer_state = this.getState();

            this.resetAnimations();
            switch (sniffer_state.ordinal()) {
                case 1:
                    this.feelingHappyAnimationState.startIfStopped(this.tickCount);
                    break;
                case 2:
                    this.scentingAnimationState.startIfStopped(this.tickCount);
                    break;
                case 3:
                    this.sniffingAnimationState.startIfStopped(this.tickCount);
                case 4:
                default:
                    break;
                case 5:
                    this.diggingAnimationState.startIfStopped(this.tickCount);
                    break;
                case 6:
                    this.risingAnimationState.startIfStopped(this.tickCount);
            }

            this.refreshDimensions();
        }

        super.onSyncedDataUpdated(datawatcherobject);
    }

    private void resetAnimations() {
        this.diggingAnimationState.stop();
        this.sniffingAnimationState.stop();
        this.risingAnimationState.stop();
        this.feelingHappyAnimationState.stop();
        this.scentingAnimationState.stop();
    }

    public Sniffer transitionTo(Sniffer.State sniffer_state) {
        switch (sniffer_state.ordinal()) {
            case 0:
                this.setState(Sniffer.State.IDLING);
                break;
            case 1:
                this.playSound(SoundEffects.SNIFFER_HAPPY, 1.0F, 1.0F);
                this.setState(Sniffer.State.FEELING_HAPPY);
                break;
            case 2:
                this.setState(Sniffer.State.SCENTING).onScentingStart();
                break;
            case 3:
                this.playSound(SoundEffects.SNIFFER_SNIFFING, 1.0F, 1.0F);
                this.setState(Sniffer.State.SNIFFING);
                break;
            case 4:
                this.setState(Sniffer.State.SEARCHING);
                break;
            case 5:
                this.setState(Sniffer.State.DIGGING).onDiggingStart();
                break;
            case 6:
                this.playSound(SoundEffects.SNIFFER_DIGGING_STOP, 1.0F, 1.0F);
                this.setState(Sniffer.State.RISING);
        }

        return this;
    }

    private Sniffer onScentingStart() {
        this.playSound(SoundEffects.SNIFFER_SCENTING, 1.0F, this.isBaby() ? 1.3F : 1.0F);
        return this;
    }

    private Sniffer onDiggingStart() {
        this.entityData.set(Sniffer.DATA_DROP_SEED_AT_TICK, this.tickCount + 120);
        this.level().broadcastEntityEvent(this, (byte) 63);
        return this;
    }

    public Sniffer onDiggingComplete(boolean flag) {
        if (flag) {
            this.storeExploredPosition(this.getOnPos());
        }

        return this;
    }

    public Optional<BlockPosition> calculateDigPosition() {
        return IntStream.range(0, 5).mapToObj((i) -> {
            return LandRandomPos.getPos(this, 10 + 2 * i, 3);
        }).filter(Objects::nonNull).map(BlockPosition::containing).filter((blockposition) -> {
            return this.level().getWorldBorder().isWithinBounds(blockposition);
        }).map(BlockPosition::below).filter(this::canDig).findFirst();
    }

    public boolean canDig() {
        return !this.isPanicking() && !this.isTempted() && !this.isBaby() && !this.isInWater() && this.onGround() && !this.isPassenger() && this.canDig(this.getHeadBlock().below());
    }

    private boolean canDig(BlockPosition blockposition) {
        return this.level().getBlockState(blockposition).is(TagsBlock.SNIFFER_DIGGABLE_BLOCK) && this.getExploredPositions().noneMatch((globalpos) -> {
            return GlobalPos.of(this.level().dimension(), blockposition).equals(globalpos);
        }) && (Boolean) Optional.ofNullable(this.getNavigation().createPath(blockposition, 1)).map(PathEntity::canReach).orElse(false);
    }

    private void dropSeed() {
        if (!this.level().isClientSide() && (Integer) this.entityData.get(Sniffer.DATA_DROP_SEED_AT_TICK) == this.tickCount) {
            WorldServer worldserver = (WorldServer) this.level();
            LootTable loottable = worldserver.getServer().reloadableRegistries().getLootTable(LootTables.SNIFFER_DIGGING);
            LootParams lootparams = (new LootParams.a(worldserver)).withParameter(LootContextParameters.ORIGIN, this.getHeadPosition()).withParameter(LootContextParameters.THIS_ENTITY, this).create(LootContextParameterSets.GIFT);
            List<ItemStack> list = loottable.getRandomItems(lootparams);
            BlockPosition blockposition = this.getHeadBlock();
            Iterator iterator = list.iterator();

            while (iterator.hasNext()) {
                ItemStack itemstack = (ItemStack) iterator.next();
                EntityItem entityitem = new EntityItem(worldserver, (double) blockposition.getX(), (double) blockposition.getY(), (double) blockposition.getZ(), itemstack);

                // CraftBukkit start - handle EntityDropItemEvent
                org.bukkit.event.entity.EntityDropItemEvent event = new org.bukkit.event.entity.EntityDropItemEvent(this.getBukkitEntity(), (org.bukkit.entity.Item) entityitem.getBukkitEntity());
                org.bukkit.Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled()) {
                    continue;
                }
                // CraftBukkit end
                entityitem.setDefaultPickUpDelay();
                worldserver.addFreshEntity(entityitem);
            }

            this.playSound(SoundEffects.SNIFFER_DROP_SEED, 1.0F, 1.0F);
        }
    }

    private Sniffer emitDiggingParticles(AnimationState animationstate) {
        boolean flag = animationstate.getAccumulatedTime() > 1700L && animationstate.getAccumulatedTime() < 6000L;

        if (flag) {
            BlockPosition blockposition = this.getHeadBlock();
            IBlockData iblockdata = this.level().getBlockState(blockposition.below());

            if (iblockdata.getRenderShape() != EnumRenderType.INVISIBLE) {
                for (int i = 0; i < 30; ++i) {
                    Vec3D vec3d = Vec3D.atCenterOf(blockposition).add(0.0D, -0.6499999761581421D, 0.0D);

                    this.level().addParticle(new ParticleParamBlock(Particles.BLOCK, iblockdata), vec3d.x, vec3d.y, vec3d.z, 0.0D, 0.0D, 0.0D);
                }

                if (this.tickCount % 10 == 0) {
                    this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), iblockdata.getSoundType().getHitSound(), this.getSoundSource(), 0.5F, 0.5F, false);
                }
            }
        }

        if (this.tickCount % 10 == 0) {
            this.level().gameEvent((Holder) GameEvent.ENTITY_ACTION, this.getHeadBlock(), GameEvent.a.of((Entity) this));
        }

        return this;
    }

    public Sniffer storeExploredPosition(BlockPosition blockposition) {
        List<GlobalPos> list = (List) this.getExploredPositions().limit(20L).collect(Collectors.toList());

        list.add(0, GlobalPos.of(this.level().dimension(), blockposition));
        this.getBrain().setMemory(MemoryModuleType.SNIFFER_EXPLORED_POSITIONS, list); // CraftBukkit - decompile error
        return this;
    }

    public Stream<GlobalPos> getExploredPositions() {
        return this.getBrain().getMemory(MemoryModuleType.SNIFFER_EXPLORED_POSITIONS).stream().flatMap(Collection::stream);
    }

    @Override
    protected void jumpFromGround() {
        super.jumpFromGround();
        double d0 = this.moveControl.getSpeedModifier();

        if (d0 > 0.0D) {
            double d1 = this.getDeltaMovement().horizontalDistanceSqr();

            if (d1 < 0.01D) {
                this.moveRelative(0.1F, new Vec3D(0.0D, 0.0D, 1.0D));
            }
        }

    }

    @Override
    public void spawnChildFromBreeding(WorldServer worldserver, EntityAnimal entityanimal) {
        ItemStack itemstack = new ItemStack(Items.SNIFFER_EGG);
        EntityItem entityitem = new EntityItem(worldserver, this.position().x(), this.position().y(), this.position().z(), itemstack);

        entityitem.setDefaultPickUpDelay();
        this.finalizeSpawnChildFromBreeding(worldserver, entityanimal, (EntityAgeable) null);
        this.playSound(SoundEffects.SNIFFER_EGG_PLOP, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 0.5F);
        worldserver.addFreshEntity(entityitem);
    }

    @Override
    public void die(DamageSource damagesource) {
        this.transitionTo(Sniffer.State.IDLING);
        super.die(damagesource);
    }

    @Override
    public void tick() {
        switch (this.getState().ordinal()) {
            case 4:
                this.playSearchingSound();
                break;
            case 5:
                this.emitDiggingParticles(this.diggingAnimationState).dropSeed();
        }

        super.tick();
    }

    @Override
    public EnumInteractionResult mobInteract(EntityHuman entityhuman, EnumHand enumhand) {
        ItemStack itemstack = entityhuman.getItemInHand(enumhand);
        boolean flag = this.isFood(itemstack);
        EnumInteractionResult enuminteractionresult = super.mobInteract(entityhuman, enumhand);

        if (enuminteractionresult.consumesAction() && flag) {
            this.level().playSound((EntityHuman) null, (Entity) this, this.getEatingSound(itemstack), SoundCategory.NEUTRAL, 1.0F, MathHelper.randomBetween(this.level().random, 0.8F, 1.2F));
        }

        return enuminteractionresult;
    }

    private void playSearchingSound() {
        if (this.level().isClientSide() && this.tickCount % 20 == 0) {
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEffects.SNIFFER_SEARCHING, this.getSoundSource(), 1.0F, 1.0F, false);
        }

    }

    @Override
    protected void playStepSound(BlockPosition blockposition, IBlockData iblockdata) {
        this.playSound(SoundEffects.SNIFFER_STEP, 0.15F, 1.0F);
    }

    @Override
    public SoundEffect getEatingSound(ItemStack itemstack) {
        return SoundEffects.SNIFFER_EAT;
    }

    @Override
    protected SoundEffect getAmbientSound() {
        return Set.of(Sniffer.State.DIGGING, Sniffer.State.SEARCHING).contains(this.getState()) ? null : SoundEffects.SNIFFER_IDLE;
    }

    @Override
    protected SoundEffect getHurtSound(DamageSource damagesource) {
        return SoundEffects.SNIFFER_HURT;
    }

    @Override
    protected SoundEffect getDeathSound() {
        return SoundEffects.SNIFFER_DEATH;
    }

    @Override
    public int getMaxHeadYRot() {
        return 50;
    }

    @Override
    public void setBaby(boolean flag) {
        this.setAge(flag ? -48000 : 0);
    }

    @Override
    public EntityAgeable getBreedOffspring(WorldServer worldserver, EntityAgeable entityageable) {
        return (EntityAgeable) EntityTypes.SNIFFER.create(worldserver);
    }

    @Override
    public boolean canMate(EntityAnimal entityanimal) {
        if (!(entityanimal instanceof Sniffer sniffer)) {
            return false;
        } else {
            Set<Sniffer.State> set = Set.of(Sniffer.State.IDLING, Sniffer.State.SCENTING, Sniffer.State.FEELING_HAPPY);

            return set.contains(this.getState()) && set.contains(sniffer.getState()) && super.canMate(entityanimal);
        }
    }

    @Override
    public AxisAlignedBB getBoundingBoxForCulling() {
        return super.getBoundingBoxForCulling().inflate(0.6000000238418579D);
    }

    @Override
    public boolean isFood(ItemStack itemstack) {
        return itemstack.is(TagsItem.SNIFFER_FOOD);
    }

    @Override
    protected BehaviorController<?> makeBrain(Dynamic<?> dynamic) {
        return SnifferAi.makeBrain(this.brainProvider().makeBrain(dynamic));
    }

    @Override
    public BehaviorController<Sniffer> getBrain() {
        return (BehaviorController<Sniffer>) super.getBrain(); // CraftBukkit - decompile error
    }

    @Override
    protected BehaviorController.b<Sniffer> brainProvider() {
        return BehaviorController.provider(SnifferAi.MEMORY_TYPES, SnifferAi.SENSOR_TYPES);
    }

    @Override
    protected void customServerAiStep() {
        this.level().getProfiler().push("snifferBrain");
        this.getBrain().tick((WorldServer) this.level(), this);
        this.level().getProfiler().popPush("snifferActivityUpdate");
        SnifferAi.updateActivity(this);
        this.level().getProfiler().pop();
        super.customServerAiStep();
    }

    @Override
    protected void sendDebugPackets() {
        super.sendDebugPackets();
        PacketDebug.sendEntityBrain(this);
    }

    public static enum State {

        IDLING(0), FEELING_HAPPY(1), SCENTING(2), SNIFFING(3), SEARCHING(4), DIGGING(5), RISING(6);

        public static final IntFunction<Sniffer.State> BY_ID = ByIdMap.continuous(Sniffer.State::id, values(), ByIdMap.a.ZERO);
        public static final StreamCodec<ByteBuf, Sniffer.State> STREAM_CODEC = ByteBufCodecs.idMapper(Sniffer.State.BY_ID, Sniffer.State::id);
        private final int id;

        private State(final int i) {
            this.id = i;
        }

        public int id() {
            return this.id;
        }
    }
}
