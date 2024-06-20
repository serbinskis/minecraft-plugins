package net.minecraft.gametest.framework;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import io.netty.channel.ChannelHandler;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.LongStream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.protocol.EnumProtocolDirection;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.commands.FillBiomeCommand;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.tags.TagsBlock;
import net.minecraft.world.EnumHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.ItemActionContext;
import net.minecraft.world.level.EnumGamemode;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BlockButtonAbstract;
import net.minecraft.world.level.block.BlockLever;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EnumBlockMirror;
import net.minecraft.world.level.block.EnumBlockRotation;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityContainer;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.IBlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.levelgen.HeightMap;
import net.minecraft.world.level.levelgen.structure.StructureBoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.DefinedStructure;
import net.minecraft.world.level.pathfinder.PathEntity;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.Vec3D;

public class GameTestHarnessHelper {

    private final GameTestHarnessInfo testInfo;
    private boolean finalCheckAdded;

    public GameTestHarnessHelper(GameTestHarnessInfo gametestharnessinfo) {
        this.testInfo = gametestharnessinfo;
    }

    public WorldServer getLevel() {
        return this.testInfo.getLevel();
    }

    public IBlockData getBlockState(BlockPosition blockposition) {
        return this.getLevel().getBlockState(this.absolutePos(blockposition));
    }

    public <T extends TileEntity> T getBlockEntity(BlockPosition blockposition) {
        TileEntity tileentity = this.getLevel().getBlockEntity(this.absolutePos(blockposition));

        if (tileentity == null) {
            throw new GameTestHarnessAssertionPosition("Missing block entity", this.absolutePos(blockposition), blockposition, this.testInfo.getTick());
        } else {
            return tileentity;
        }
    }

    public void killAllEntities() {
        this.killAllEntitiesOfClass(Entity.class);
    }

    public void killAllEntitiesOfClass(Class oclass) {
        AxisAlignedBB axisalignedbb = this.getBounds();
        List<Entity> list = this.getLevel().getEntitiesOfClass(oclass, axisalignedbb.inflate(1.0D), (entity) -> {
            return !(entity instanceof EntityHuman);
        });

        list.forEach(Entity::kill);
    }

    public EntityItem spawnItem(Item item, Vec3D vec3d) {
        WorldServer worldserver = this.getLevel();
        Vec3D vec3d1 = this.absoluteVec(vec3d);
        EntityItem entityitem = new EntityItem(worldserver, vec3d1.x, vec3d1.y, vec3d1.z, new ItemStack(item, 1));

        entityitem.setDeltaMovement(0.0D, 0.0D, 0.0D);
        worldserver.addFreshEntity(entityitem);
        return entityitem;
    }

    public EntityItem spawnItem(Item item, float f, float f1, float f2) {
        return this.spawnItem(item, new Vec3D((double) f, (double) f1, (double) f2));
    }

    public EntityItem spawnItem(Item item, BlockPosition blockposition) {
        return this.spawnItem(item, (float) blockposition.getX(), (float) blockposition.getY(), (float) blockposition.getZ());
    }

    public <E extends Entity> E spawn(EntityTypes<E> entitytypes, BlockPosition blockposition) {
        return this.spawn(entitytypes, Vec3D.atBottomCenterOf(blockposition));
    }

    public <E extends Entity> E spawn(EntityTypes<E> entitytypes, Vec3D vec3d) {
        WorldServer worldserver = this.getLevel();
        E e0 = entitytypes.create(worldserver);

        if (e0 == null) {
            throw new NullPointerException("Failed to create entity " + String.valueOf(entitytypes.builtInRegistryHolder().key().location()));
        } else {
            if (e0 instanceof EntityInsentient) {
                EntityInsentient entityinsentient = (EntityInsentient) e0;

                entityinsentient.setPersistenceRequired();
            }

            Vec3D vec3d1 = this.absoluteVec(vec3d);

            e0.moveTo(vec3d1.x, vec3d1.y, vec3d1.z, e0.getYRot(), e0.getXRot());
            worldserver.addFreshEntity(e0);
            return e0;
        }
    }

    public <E extends Entity> E findOneEntity(EntityTypes<E> entitytypes) {
        return this.findClosestEntity(entitytypes, 0, 0, 0, 2.147483647E9D);
    }

    public <E extends Entity> E findClosestEntity(EntityTypes<E> entitytypes, int i, int j, int k, double d0) {
        List<E> list = this.findEntities(entitytypes, i, j, k, d0);

        if (list.isEmpty()) {
            throw new GameTestHarnessAssertion("Expected " + entitytypes.toShortString() + " to exist around " + i + "," + j + "," + k);
        } else if (list.size() > 1) {
            throw new GameTestHarnessAssertion("Expected only one " + entitytypes.toShortString() + " to exist around " + i + "," + j + "," + k + ", but found " + list.size());
        } else {
            Vec3D vec3d = this.absoluteVec(new Vec3D((double) i, (double) j, (double) k));

            list.sort((entity, entity1) -> {
                double d1 = entity.position().distanceTo(vec3d);
                double d2 = entity1.position().distanceTo(vec3d);

                return Double.compare(d1, d2);
            });
            return (Entity) list.get(0);
        }
    }

    public <E extends Entity> List<E> findEntities(EntityTypes<E> entitytypes, int i, int j, int k, double d0) {
        return this.findEntities(entitytypes, Vec3D.atBottomCenterOf(new BlockPosition(i, j, k)), d0);
    }

    public <E extends Entity> List<E> findEntities(EntityTypes<E> entitytypes, Vec3D vec3d, double d0) {
        WorldServer worldserver = this.getLevel();
        Vec3D vec3d1 = this.absoluteVec(vec3d);
        AxisAlignedBB axisalignedbb = this.testInfo.getStructureBounds();
        AxisAlignedBB axisalignedbb1 = new AxisAlignedBB(vec3d1.add(-d0, -d0, -d0), vec3d1.add(d0, d0, d0));

        return worldserver.getEntities((EntityTypeTest) entitytypes, axisalignedbb, (entity) -> {
            return entity.getBoundingBox().intersects(axisalignedbb1) && entity.isAlive();
        });
    }

    public <E extends Entity> E spawn(EntityTypes<E> entitytypes, int i, int j, int k) {
        return this.spawn(entitytypes, new BlockPosition(i, j, k));
    }

    public <E extends Entity> E spawn(EntityTypes<E> entitytypes, float f, float f1, float f2) {
        return this.spawn(entitytypes, new Vec3D((double) f, (double) f1, (double) f2));
    }

    public <E extends EntityInsentient> E spawnWithNoFreeWill(EntityTypes<E> entitytypes, BlockPosition blockposition) {
        E e0 = (EntityInsentient) this.spawn(entitytypes, blockposition);

        e0.removeFreeWill();
        return e0;
    }

    public <E extends EntityInsentient> E spawnWithNoFreeWill(EntityTypes<E> entitytypes, int i, int j, int k) {
        return this.spawnWithNoFreeWill(entitytypes, new BlockPosition(i, j, k));
    }

    public <E extends EntityInsentient> E spawnWithNoFreeWill(EntityTypes<E> entitytypes, Vec3D vec3d) {
        E e0 = (EntityInsentient) this.spawn(entitytypes, vec3d);

        e0.removeFreeWill();
        return e0;
    }

    public <E extends EntityInsentient> E spawnWithNoFreeWill(EntityTypes<E> entitytypes, float f, float f1, float f2) {
        return this.spawnWithNoFreeWill(entitytypes, new Vec3D((double) f, (double) f1, (double) f2));
    }

    public void moveTo(EntityInsentient entityinsentient, float f, float f1, float f2) {
        Vec3D vec3d = this.absoluteVec(new Vec3D((double) f, (double) f1, (double) f2));

        entityinsentient.moveTo(vec3d.x, vec3d.y, vec3d.z, entityinsentient.getYRot(), entityinsentient.getXRot());
    }

    public GameTestHarnessSequence walkTo(EntityInsentient entityinsentient, BlockPosition blockposition, float f) {
        return this.startSequence().thenExecuteAfter(2, () -> {
            PathEntity pathentity = entityinsentient.getNavigation().createPath(this.absolutePos(blockposition), 0);

            entityinsentient.getNavigation().moveTo(pathentity, (double) f);
        });
    }

    public void pressButton(int i, int j, int k) {
        this.pressButton(new BlockPosition(i, j, k));
    }

    public void pressButton(BlockPosition blockposition) {
        this.assertBlockState(blockposition, (iblockdata) -> {
            return iblockdata.is(TagsBlock.BUTTONS);
        }, () -> {
            return "Expected button";
        });
        BlockPosition blockposition1 = this.absolutePos(blockposition);
        IBlockData iblockdata = this.getLevel().getBlockState(blockposition1);
        BlockButtonAbstract blockbuttonabstract = (BlockButtonAbstract) iblockdata.getBlock();

        blockbuttonabstract.press(iblockdata, this.getLevel(), blockposition1, (EntityHuman) null);
    }

    public void useBlock(BlockPosition blockposition) {
        this.useBlock(blockposition, this.makeMockPlayer(EnumGamemode.CREATIVE));
    }

    public void useBlock(BlockPosition blockposition, EntityHuman entityhuman) {
        BlockPosition blockposition1 = this.absolutePos(blockposition);

        this.useBlock(blockposition, entityhuman, new MovingObjectPositionBlock(Vec3D.atCenterOf(blockposition1), EnumDirection.NORTH, blockposition1, true));
    }

    public void useBlock(BlockPosition blockposition, EntityHuman entityhuman, MovingObjectPositionBlock movingobjectpositionblock) {
        BlockPosition blockposition1 = this.absolutePos(blockposition);
        IBlockData iblockdata = this.getLevel().getBlockState(blockposition1);
        EnumHand enumhand = EnumHand.MAIN_HAND;
        ItemInteractionResult iteminteractionresult = iblockdata.useItemOn(entityhuman.getItemInHand(enumhand), this.getLevel(), entityhuman, enumhand, movingobjectpositionblock);

        if (!iteminteractionresult.consumesAction()) {
            if (iteminteractionresult != ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION || !iblockdata.useWithoutItem(this.getLevel(), entityhuman, movingobjectpositionblock).consumesAction()) {
                ItemActionContext itemactioncontext = new ItemActionContext(entityhuman, enumhand, movingobjectpositionblock);

                entityhuman.getItemInHand(enumhand).useOn(itemactioncontext);
            }
        }
    }

    public EntityLiving makeAboutToDrown(EntityLiving entityliving) {
        entityliving.setAirSupply(0);
        entityliving.setHealth(0.25F);
        return entityliving;
    }

    public EntityLiving withLowHealth(EntityLiving entityliving) {
        entityliving.setHealth(0.25F);
        return entityliving;
    }

    public EntityHuman makeMockPlayer(final EnumGamemode enumgamemode) {
        return new EntityHuman(this, this.getLevel(), BlockPosition.ZERO, 0.0F, new GameProfile(UUID.randomUUID(), "test-mock-player")) {
            @Override
            public boolean isSpectator() {
                return enumgamemode == EnumGamemode.SPECTATOR;
            }

            @Override
            public boolean isCreative() {
                return enumgamemode.isCreative();
            }

            @Override
            public boolean isLocalPlayer() {
                return true;
            }
        };
    }

    /** @deprecated */
    @Deprecated(forRemoval = true)
    public EntityPlayer makeMockServerPlayerInLevel() {
        CommonListenerCookie commonlistenercookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "test-mock-player"), false);
        EntityPlayer entityplayer = new EntityPlayer(this, this.getLevel().getServer(), this.getLevel(), commonlistenercookie.gameProfile(), commonlistenercookie.clientInformation()) {
            @Override
            public boolean isSpectator() {
                return false;
            }

            @Override
            public boolean isCreative() {
                return true;
            }
        };
        NetworkManager networkmanager = new NetworkManager(EnumProtocolDirection.SERVERBOUND);

        new EmbeddedChannel(new ChannelHandler[]{networkmanager});
        this.getLevel().getServer().getPlayerList().placeNewPlayer(networkmanager, entityplayer, commonlistenercookie);
        return entityplayer;
    }

    public void pullLever(int i, int j, int k) {
        this.pullLever(new BlockPosition(i, j, k));
    }

    public void pullLever(BlockPosition blockposition) {
        this.assertBlockPresent(Blocks.LEVER, blockposition);
        BlockPosition blockposition1 = this.absolutePos(blockposition);
        IBlockData iblockdata = this.getLevel().getBlockState(blockposition1);
        BlockLever blocklever = (BlockLever) iblockdata.getBlock();

        blocklever.pull(iblockdata, this.getLevel(), blockposition1, (EntityHuman) null);
    }

    public void pulseRedstone(BlockPosition blockposition, long i) {
        this.setBlock(blockposition, Blocks.REDSTONE_BLOCK);
        this.runAfterDelay(i, () -> {
            this.setBlock(blockposition, Blocks.AIR);
        });
    }

    public void destroyBlock(BlockPosition blockposition) {
        this.getLevel().destroyBlock(this.absolutePos(blockposition), false, (Entity) null);
    }

    public void setBlock(int i, int j, int k, Block block) {
        this.setBlock(new BlockPosition(i, j, k), block);
    }

    public void setBlock(int i, int j, int k, IBlockData iblockdata) {
        this.setBlock(new BlockPosition(i, j, k), iblockdata);
    }

    public void setBlock(BlockPosition blockposition, Block block) {
        this.setBlock(blockposition, block.defaultBlockState());
    }

    public void setBlock(BlockPosition blockposition, IBlockData iblockdata) {
        this.getLevel().setBlock(this.absolutePos(blockposition), iblockdata, 3);
    }

    public void setNight() {
        this.setDayTime(13000);
    }

    public void setDayTime(int i) {
        this.getLevel().setDayTime((long) i);
    }

    public void assertBlockPresent(Block block, int i, int j, int k) {
        this.assertBlockPresent(block, new BlockPosition(i, j, k));
    }

    public void assertBlockPresent(Block block, BlockPosition blockposition) {
        IBlockData iblockdata = this.getBlockState(blockposition);
        Predicate predicate = (block1) -> {
            return iblockdata.is(block);
        };
        String s = block.getName().getString();

        this.assertBlock(blockposition, predicate, "Expected " + s + ", got " + iblockdata.getBlock().getName().getString());
    }

    public void assertBlockNotPresent(Block block, int i, int j, int k) {
        this.assertBlockNotPresent(block, new BlockPosition(i, j, k));
    }

    public void assertBlockNotPresent(Block block, BlockPosition blockposition) {
        this.assertBlock(blockposition, (block1) -> {
            return !this.getBlockState(blockposition).is(block);
        }, "Did not expect " + block.getName().getString());
    }

    public void succeedWhenBlockPresent(Block block, int i, int j, int k) {
        this.succeedWhenBlockPresent(block, new BlockPosition(i, j, k));
    }

    public void succeedWhenBlockPresent(Block block, BlockPosition blockposition) {
        this.succeedWhen(() -> {
            this.assertBlockPresent(block, blockposition);
        });
    }

    public void assertBlock(BlockPosition blockposition, Predicate<Block> predicate, String s) {
        this.assertBlock(blockposition, predicate, () -> {
            return s;
        });
    }

    public void assertBlock(BlockPosition blockposition, Predicate<Block> predicate, Supplier<String> supplier) {
        this.assertBlockState(blockposition, (iblockdata) -> {
            return predicate.test(iblockdata.getBlock());
        }, supplier);
    }

    public <T extends Comparable<T>> void assertBlockProperty(BlockPosition blockposition, IBlockState<T> iblockstate, T t0) {
        IBlockData iblockdata = this.getBlockState(blockposition);
        boolean flag = iblockdata.hasProperty(iblockstate);

        if (!flag || !iblockdata.getValue(iblockstate).equals(t0)) {
            String s = flag ? "was " + String.valueOf(iblockdata.getValue(iblockstate)) : "property " + iblockstate.getName() + " is missing";
            String s1 = String.format(Locale.ROOT, "Expected property %s to be %s, %s", iblockstate.getName(), t0, s);

            throw new GameTestHarnessAssertionPosition(s1, this.absolutePos(blockposition), blockposition, this.testInfo.getTick());
        }
    }

    public <T extends Comparable<T>> void assertBlockProperty(BlockPosition blockposition, IBlockState<T> iblockstate, Predicate<T> predicate, String s) {
        this.assertBlockState(blockposition, (iblockdata) -> {
            if (!iblockdata.hasProperty(iblockstate)) {
                return false;
            } else {
                T t0 = iblockdata.getValue(iblockstate);

                return predicate.test(t0);
            }
        }, () -> {
            return s;
        });
    }

    public void assertBlockState(BlockPosition blockposition, Predicate<IBlockData> predicate, Supplier<String> supplier) {
        IBlockData iblockdata = this.getBlockState(blockposition);

        if (!predicate.test(iblockdata)) {
            throw new GameTestHarnessAssertionPosition((String) supplier.get(), this.absolutePos(blockposition), blockposition, this.testInfo.getTick());
        }
    }

    public <T extends TileEntity> void assertBlockEntityData(BlockPosition blockposition, Predicate<T> predicate, Supplier<String> supplier) {
        T t0 = this.getBlockEntity(blockposition);

        if (!predicate.test(t0)) {
            throw new GameTestHarnessAssertionPosition((String) supplier.get(), this.absolutePos(blockposition), blockposition, this.testInfo.getTick());
        }
    }

    public void assertRedstoneSignal(BlockPosition blockposition, EnumDirection enumdirection, IntPredicate intpredicate, Supplier<String> supplier) {
        BlockPosition blockposition1 = this.absolutePos(blockposition);
        WorldServer worldserver = this.getLevel();
        IBlockData iblockdata = worldserver.getBlockState(blockposition1);
        int i = iblockdata.getSignal(worldserver, blockposition1, enumdirection);

        if (!intpredicate.test(i)) {
            throw new GameTestHarnessAssertionPosition((String) supplier.get(), blockposition1, blockposition, this.testInfo.getTick());
        }
    }

    public void assertEntityPresent(EntityTypes<?> entitytypes) {
        List<? extends Entity> list = this.getLevel().getEntities((EntityTypeTest) entitytypes, this.getBounds(), Entity::isAlive);

        if (list.isEmpty()) {
            throw new GameTestHarnessAssertion("Expected " + entitytypes.toShortString() + " to exist");
        }
    }

    public void assertEntityPresent(EntityTypes<?> entitytypes, int i, int j, int k) {
        this.assertEntityPresent(entitytypes, new BlockPosition(i, j, k));
    }

    public void assertEntityPresent(EntityTypes<?> entitytypes, BlockPosition blockposition) {
        BlockPosition blockposition1 = this.absolutePos(blockposition);
        List<? extends Entity> list = this.getLevel().getEntities((EntityTypeTest) entitytypes, new AxisAlignedBB(blockposition1), Entity::isAlive);

        if (list.isEmpty()) {
            throw new GameTestHarnessAssertionPosition("Expected " + entitytypes.toShortString(), blockposition1, blockposition, this.testInfo.getTick());
        }
    }

    public void assertEntityPresent(EntityTypes<?> entitytypes, Vec3D vec3d, Vec3D vec3d1) {
        List<? extends Entity> list = this.getLevel().getEntities((EntityTypeTest) entitytypes, new AxisAlignedBB(vec3d, vec3d1), Entity::isAlive);

        if (list.isEmpty()) {
            throw new GameTestHarnessAssertionPosition("Expected " + entitytypes.toShortString() + " between ", BlockPosition.containing(vec3d), BlockPosition.containing(vec3d1), this.testInfo.getTick());
        }
    }

    public void assertEntitiesPresent(EntityTypes<?> entitytypes, int i) {
        List<? extends Entity> list = this.getLevel().getEntities((EntityTypeTest) entitytypes, this.getBounds(), Entity::isAlive);

        if (list.size() != i) {
            throw new GameTestHarnessAssertion("Expected " + i + " of type " + entitytypes.toShortString() + " to exist, found " + list.size());
        }
    }

    public void assertEntitiesPresent(EntityTypes<?> entitytypes, BlockPosition blockposition, int i, double d0) {
        BlockPosition blockposition1 = this.absolutePos(blockposition);
        List<? extends Entity> list = this.getEntities(entitytypes, blockposition, d0);

        if (list.size() != i) {
            throw new GameTestHarnessAssertionPosition("Expected " + i + " entities of type " + entitytypes.toShortString() + ", actual number of entities found=" + list.size(), blockposition1, blockposition, this.testInfo.getTick());
        }
    }

    public void assertEntityPresent(EntityTypes<?> entitytypes, BlockPosition blockposition, double d0) {
        List<? extends Entity> list = this.getEntities(entitytypes, blockposition, d0);

        if (list.isEmpty()) {
            BlockPosition blockposition1 = this.absolutePos(blockposition);

            throw new GameTestHarnessAssertionPosition("Expected " + entitytypes.toShortString(), blockposition1, blockposition, this.testInfo.getTick());
        }
    }

    public <T extends Entity> List<T> getEntities(EntityTypes<T> entitytypes, BlockPosition blockposition, double d0) {
        BlockPosition blockposition1 = this.absolutePos(blockposition);

        return this.getLevel().getEntities((EntityTypeTest) entitytypes, (new AxisAlignedBB(blockposition1)).inflate(d0), Entity::isAlive);
    }

    public <T extends Entity> List<T> getEntities(EntityTypes<T> entitytypes) {
        return this.getLevel().getEntities((EntityTypeTest) entitytypes, this.getBounds(), Entity::isAlive);
    }

    public void assertEntityInstancePresent(Entity entity, int i, int j, int k) {
        this.assertEntityInstancePresent(entity, new BlockPosition(i, j, k));
    }

    public void assertEntityInstancePresent(Entity entity, BlockPosition blockposition) {
        BlockPosition blockposition1 = this.absolutePos(blockposition);
        List<? extends Entity> list = this.getLevel().getEntities((EntityTypeTest) entity.getType(), new AxisAlignedBB(blockposition1), Entity::isAlive);

        list.stream().filter((entity1) -> {
            return entity1 == entity;
        }).findFirst().orElseThrow(() -> {
            return new GameTestHarnessAssertionPosition("Expected " + entity.getType().toShortString(), blockposition1, blockposition, this.testInfo.getTick());
        });
    }

    public void assertItemEntityCountIs(Item item, BlockPosition blockposition, double d0, int i) {
        BlockPosition blockposition1 = this.absolutePos(blockposition);
        List<EntityItem> list = this.getLevel().getEntities((EntityTypeTest) EntityTypes.ITEM, (new AxisAlignedBB(blockposition1)).inflate(d0), Entity::isAlive);
        int j = 0;
        Iterator iterator = list.iterator();

        while (iterator.hasNext()) {
            EntityItem entityitem = (EntityItem) iterator.next();
            ItemStack itemstack = entityitem.getItem();

            if (itemstack.is(item)) {
                j += itemstack.getCount();
            }
        }

        if (j != i) {
            throw new GameTestHarnessAssertionPosition("Expected " + i + " " + item.getDescription().getString() + " items to exist (found " + j + ")", blockposition1, blockposition, this.testInfo.getTick());
        }
    }

    public void assertItemEntityPresent(Item item, BlockPosition blockposition, double d0) {
        BlockPosition blockposition1 = this.absolutePos(blockposition);
        List<? extends Entity> list = this.getLevel().getEntities((EntityTypeTest) EntityTypes.ITEM, (new AxisAlignedBB(blockposition1)).inflate(d0), Entity::isAlive);
        Iterator iterator = list.iterator();

        EntityItem entityitem;

        do {
            if (!iterator.hasNext()) {
                throw new GameTestHarnessAssertionPosition("Expected " + item.getDescription().getString() + " item", blockposition1, blockposition, this.testInfo.getTick());
            }

            Entity entity = (Entity) iterator.next();

            entityitem = (EntityItem) entity;
        } while (!entityitem.getItem().getItem().equals(item));

    }

    public void assertItemEntityNotPresent(Item item, BlockPosition blockposition, double d0) {
        BlockPosition blockposition1 = this.absolutePos(blockposition);
        List<? extends Entity> list = this.getLevel().getEntities((EntityTypeTest) EntityTypes.ITEM, (new AxisAlignedBB(blockposition1)).inflate(d0), Entity::isAlive);
        Iterator iterator = list.iterator();

        EntityItem entityitem;

        do {
            if (!iterator.hasNext()) {
                return;
            }

            Entity entity = (Entity) iterator.next();

            entityitem = (EntityItem) entity;
        } while (!entityitem.getItem().getItem().equals(item));

        throw new GameTestHarnessAssertionPosition("Did not expect " + item.getDescription().getString() + " item", blockposition1, blockposition, this.testInfo.getTick());
    }

    public void assertItemEntityPresent(Item item) {
        List<? extends Entity> list = this.getLevel().getEntities((EntityTypeTest) EntityTypes.ITEM, this.getBounds(), Entity::isAlive);
        Iterator iterator = list.iterator();

        EntityItem entityitem;

        do {
            if (!iterator.hasNext()) {
                throw new GameTestHarnessAssertion("Expected " + item.getDescription().getString() + " item");
            }

            Entity entity = (Entity) iterator.next();

            entityitem = (EntityItem) entity;
        } while (!entityitem.getItem().getItem().equals(item));

    }

    public void assertItemEntityNotPresent(Item item) {
        List<? extends Entity> list = this.getLevel().getEntities((EntityTypeTest) EntityTypes.ITEM, this.getBounds(), Entity::isAlive);
        Iterator iterator = list.iterator();

        EntityItem entityitem;

        do {
            if (!iterator.hasNext()) {
                return;
            }

            Entity entity = (Entity) iterator.next();

            entityitem = (EntityItem) entity;
        } while (!entityitem.getItem().getItem().equals(item));

        throw new GameTestHarnessAssertion("Did not expect " + item.getDescription().getString() + " item");
    }

    public void assertEntityNotPresent(EntityTypes<?> entitytypes) {
        List<? extends Entity> list = this.getLevel().getEntities((EntityTypeTest) entitytypes, this.getBounds(), Entity::isAlive);

        if (!list.isEmpty()) {
            throw new GameTestHarnessAssertion("Did not expect " + entitytypes.toShortString() + " to exist");
        }
    }

    public void assertEntityNotPresent(EntityTypes<?> entitytypes, int i, int j, int k) {
        this.assertEntityNotPresent(entitytypes, new BlockPosition(i, j, k));
    }

    public void assertEntityNotPresent(EntityTypes<?> entitytypes, BlockPosition blockposition) {
        BlockPosition blockposition1 = this.absolutePos(blockposition);
        List<? extends Entity> list = this.getLevel().getEntities((EntityTypeTest) entitytypes, new AxisAlignedBB(blockposition1), Entity::isAlive);

        if (!list.isEmpty()) {
            throw new GameTestHarnessAssertionPosition("Did not expect " + entitytypes.toShortString(), blockposition1, blockposition, this.testInfo.getTick());
        }
    }

    public void assertEntityNotPresent(EntityTypes<?> entitytypes, Vec3D vec3d, Vec3D vec3d1) {
        List<? extends Entity> list = this.getLevel().getEntities((EntityTypeTest) entitytypes, new AxisAlignedBB(vec3d, vec3d1), Entity::isAlive);

        if (!list.isEmpty()) {
            throw new GameTestHarnessAssertionPosition("Did not expect " + entitytypes.toShortString() + " between ", BlockPosition.containing(vec3d), BlockPosition.containing(vec3d1), this.testInfo.getTick());
        }
    }

    public void assertEntityTouching(EntityTypes<?> entitytypes, double d0, double d1, double d2) {
        Vec3D vec3d = new Vec3D(d0, d1, d2);
        Vec3D vec3d1 = this.absoluteVec(vec3d);
        Predicate<? super Entity> predicate = (entity) -> {
            return entity.getBoundingBox().intersects(vec3d1, vec3d1);
        };
        List<? extends Entity> list = this.getLevel().getEntities((EntityTypeTest) entitytypes, this.getBounds(), predicate);

        if (list.isEmpty()) {
            String s = entitytypes.toShortString();

            throw new GameTestHarnessAssertion("Expected " + s + " to touch " + String.valueOf(vec3d1) + " (relative " + String.valueOf(vec3d) + ")");
        }
    }

    public void assertEntityNotTouching(EntityTypes<?> entitytypes, double d0, double d1, double d2) {
        Vec3D vec3d = new Vec3D(d0, d1, d2);
        Vec3D vec3d1 = this.absoluteVec(vec3d);
        Predicate<? super Entity> predicate = (entity) -> {
            return !entity.getBoundingBox().intersects(vec3d1, vec3d1);
        };
        List<? extends Entity> list = this.getLevel().getEntities((EntityTypeTest) entitytypes, this.getBounds(), predicate);

        if (list.isEmpty()) {
            String s = entitytypes.toShortString();

            throw new GameTestHarnessAssertion("Did not expect " + s + " to touch " + String.valueOf(vec3d1) + " (relative " + String.valueOf(vec3d) + ")");
        }
    }

    public <E extends Entity, T> void assertEntityData(BlockPosition blockposition, EntityTypes<E> entitytypes, Function<? super E, T> function, @Nullable T t0) {
        BlockPosition blockposition1 = this.absolutePos(blockposition);
        List<E> list = this.getLevel().getEntities((EntityTypeTest) entitytypes, new AxisAlignedBB(blockposition1), Entity::isAlive);

        if (list.isEmpty()) {
            throw new GameTestHarnessAssertionPosition("Expected " + entitytypes.toShortString(), blockposition1, blockposition, this.testInfo.getTick());
        } else {
            Iterator iterator = list.iterator();

            while (iterator.hasNext()) {
                E e0 = (Entity) iterator.next();
                T t1 = function.apply(e0);
                String s;

                if (t1 == null) {
                    if (t0 != null) {
                        s = String.valueOf(t0);
                        throw new GameTestHarnessAssertion("Expected entity data to be: " + s + ", but was: " + String.valueOf(t1));
                    }
                } else if (!t1.equals(t0)) {
                    s = String.valueOf(t0);
                    throw new GameTestHarnessAssertion("Expected entity data to be: " + s + ", but was: " + String.valueOf(t1));
                }
            }

        }
    }

    public <E extends EntityLiving> void assertEntityIsHolding(BlockPosition blockposition, EntityTypes<E> entitytypes, Item item) {
        BlockPosition blockposition1 = this.absolutePos(blockposition);
        List<E> list = this.getLevel().getEntities((EntityTypeTest) entitytypes, new AxisAlignedBB(blockposition1), Entity::isAlive);

        if (list.isEmpty()) {
            throw new GameTestHarnessAssertionPosition("Expected entity of type: " + String.valueOf(entitytypes), blockposition1, blockposition, this.getTick());
        } else {
            Iterator iterator = list.iterator();

            EntityLiving entityliving;

            do {
                if (!iterator.hasNext()) {
                    throw new GameTestHarnessAssertionPosition("Entity should be holding: " + String.valueOf(item), blockposition1, blockposition, this.getTick());
                }

                entityliving = (EntityLiving) iterator.next();
            } while (!entityliving.isHolding(item));

        }
    }

    public <E extends Entity & InventoryCarrier> void assertEntityInventoryContains(BlockPosition blockposition, EntityTypes<E> entitytypes, Item item) {
        BlockPosition blockposition1 = this.absolutePos(blockposition);
        List<E> list = this.getLevel().getEntities((EntityTypeTest) entitytypes, new AxisAlignedBB(blockposition1), (object) -> {
            return ((Entity) object).isAlive();
        });

        if (list.isEmpty()) {
            throw new GameTestHarnessAssertionPosition("Expected " + entitytypes.toShortString() + " to exist", blockposition1, blockposition, this.getTick());
        } else {
            Iterator iterator = list.iterator();

            Entity entity;

            do {
                if (!iterator.hasNext()) {
                    throw new GameTestHarnessAssertionPosition("Entity inventory should contain: " + String.valueOf(item), blockposition1, blockposition, this.getTick());
                }

                entity = (Entity) iterator.next();
            } while (!((InventoryCarrier) entity).getInventory().hasAnyMatching((itemstack) -> {
                return itemstack.is(item);
            }));

        }
    }

    public void assertContainerEmpty(BlockPosition blockposition) {
        BlockPosition blockposition1 = this.absolutePos(blockposition);
        TileEntity tileentity = this.getLevel().getBlockEntity(blockposition1);

        if (tileentity instanceof TileEntityContainer && !((TileEntityContainer) tileentity).isEmpty()) {
            throw new GameTestHarnessAssertion("Container should be empty");
        }
    }

    public void assertContainerContains(BlockPosition blockposition, Item item) {
        BlockPosition blockposition1 = this.absolutePos(blockposition);
        TileEntity tileentity = this.getLevel().getBlockEntity(blockposition1);

        if (!(tileentity instanceof TileEntityContainer)) {
            String s = String.valueOf(blockposition);

            throw new GameTestHarnessAssertion("Expected a container at " + s + ", found " + String.valueOf(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(tileentity.getType())));
        } else if (((TileEntityContainer) tileentity).countItem(item) != 1) {
            throw new GameTestHarnessAssertion("Container should contain: " + String.valueOf(item));
        }
    }

    public void assertSameBlockStates(StructureBoundingBox structureboundingbox, BlockPosition blockposition) {
        BlockPosition.betweenClosedStream(structureboundingbox).forEach((blockposition1) -> {
            BlockPosition blockposition2 = blockposition.offset(blockposition1.getX() - structureboundingbox.minX(), blockposition1.getY() - structureboundingbox.minY(), blockposition1.getZ() - structureboundingbox.minZ());

            this.assertSameBlockState(blockposition1, blockposition2);
        });
    }

    public void assertSameBlockState(BlockPosition blockposition, BlockPosition blockposition1) {
        IBlockData iblockdata = this.getBlockState(blockposition);
        IBlockData iblockdata1 = this.getBlockState(blockposition1);

        if (iblockdata != iblockdata1) {
            this.fail("Incorrect state. Expected " + String.valueOf(iblockdata1) + ", got " + String.valueOf(iblockdata), blockposition);
        }

    }

    public void assertAtTickTimeContainerContains(long i, BlockPosition blockposition, Item item) {
        this.runAtTickTime(i, () -> {
            this.assertContainerContains(blockposition, item);
        });
    }

    public void assertAtTickTimeContainerEmpty(long i, BlockPosition blockposition) {
        this.runAtTickTime(i, () -> {
            this.assertContainerEmpty(blockposition);
        });
    }

    public <E extends Entity, T> void succeedWhenEntityData(BlockPosition blockposition, EntityTypes<E> entitytypes, Function<E, T> function, T t0) {
        this.succeedWhen(() -> {
            this.assertEntityData(blockposition, entitytypes, function, t0);
        });
    }

    public void assertEntityPosition(Entity entity, AxisAlignedBB axisalignedbb, String s) {
        if (!axisalignedbb.contains(this.relativeVec(entity.position()))) {
            this.fail(s);
        }

    }

    public <E extends Entity> void assertEntityProperty(E e0, Predicate<E> predicate, String s) {
        if (!predicate.test(e0)) {
            String s1 = String.valueOf(e0);

            throw new GameTestHarnessAssertion("Entity " + s1 + " failed " + s + " test");
        }
    }

    public <E extends Entity, T> void assertEntityProperty(E e0, Function<E, T> function, String s, T t0) {
        T t1 = function.apply(e0);

        if (!t1.equals(t0)) {
            String s1 = String.valueOf(e0);

            throw new GameTestHarnessAssertion("Entity " + s1 + " value " + s + "=" + String.valueOf(t1) + " is not equal to expected " + String.valueOf(t0));
        }
    }

    public void assertLivingEntityHasMobEffect(EntityLiving entityliving, Holder<MobEffectList> holder, int i) {
        MobEffect mobeffect = entityliving.getEffect(holder);

        if (mobeffect == null || mobeffect.getAmplifier() != i) {
            int j = i + 1;
            String s = String.valueOf(entityliving);

            throw new GameTestHarnessAssertion("Entity " + s + " failed has " + ((MobEffectList) holder.value()).getDescriptionId() + " x " + j + " test");
        }
    }

    public void succeedWhenEntityPresent(EntityTypes<?> entitytypes, int i, int j, int k) {
        this.succeedWhenEntityPresent(entitytypes, new BlockPosition(i, j, k));
    }

    public void succeedWhenEntityPresent(EntityTypes<?> entitytypes, BlockPosition blockposition) {
        this.succeedWhen(() -> {
            this.assertEntityPresent(entitytypes, blockposition);
        });
    }

    public void succeedWhenEntityNotPresent(EntityTypes<?> entitytypes, int i, int j, int k) {
        this.succeedWhenEntityNotPresent(entitytypes, new BlockPosition(i, j, k));
    }

    public void succeedWhenEntityNotPresent(EntityTypes<?> entitytypes, BlockPosition blockposition) {
        this.succeedWhen(() -> {
            this.assertEntityNotPresent(entitytypes, blockposition);
        });
    }

    public void succeed() {
        this.testInfo.succeed();
    }

    private void ensureSingleFinalCheck() {
        if (this.finalCheckAdded) {
            throw new IllegalStateException("This test already has final clause");
        } else {
            this.finalCheckAdded = true;
        }
    }

    public void succeedIf(Runnable runnable) {
        this.ensureSingleFinalCheck();
        this.testInfo.createSequence().thenWaitUntil(0L, runnable).thenSucceed();
    }

    public void succeedWhen(Runnable runnable) {
        this.ensureSingleFinalCheck();
        this.testInfo.createSequence().thenWaitUntil(runnable).thenSucceed();
    }

    public void succeedOnTickWhen(int i, Runnable runnable) {
        this.ensureSingleFinalCheck();
        this.testInfo.createSequence().thenWaitUntil((long) i, runnable).thenSucceed();
    }

    public void runAtTickTime(long i, Runnable runnable) {
        this.testInfo.setRunAtTickTime(i, runnable);
    }

    public void runAfterDelay(long i, Runnable runnable) {
        this.runAtTickTime(this.testInfo.getTick() + i, runnable);
    }

    public void randomTick(BlockPosition blockposition) {
        BlockPosition blockposition1 = this.absolutePos(blockposition);
        WorldServer worldserver = this.getLevel();

        worldserver.getBlockState(blockposition1).randomTick(worldserver, blockposition1, worldserver.random);
    }

    public void tickPrecipitation(BlockPosition blockposition) {
        BlockPosition blockposition1 = this.absolutePos(blockposition);
        WorldServer worldserver = this.getLevel();

        worldserver.tickPrecipitation(blockposition1);
    }

    public void tickPrecipitation() {
        AxisAlignedBB axisalignedbb = this.getRelativeBounds();
        int i = (int) Math.floor(axisalignedbb.maxX);
        int j = (int) Math.floor(axisalignedbb.maxZ);
        int k = (int) Math.floor(axisalignedbb.maxY);

        for (int l = (int) Math.floor(axisalignedbb.minX); l < i; ++l) {
            for (int i1 = (int) Math.floor(axisalignedbb.minZ); i1 < j; ++i1) {
                this.tickPrecipitation(new BlockPosition(l, k, i1));
            }
        }

    }

    public int getHeight(HeightMap.Type heightmap_type, int i, int j) {
        BlockPosition blockposition = this.absolutePos(new BlockPosition(i, 0, j));

        return this.relativePos(this.getLevel().getHeightmapPos(heightmap_type, blockposition)).getY();
    }

    public void fail(String s, BlockPosition blockposition) {
        throw new GameTestHarnessAssertionPosition(s, this.absolutePos(blockposition), blockposition, this.getTick());
    }

    public void fail(String s, Entity entity) {
        throw new GameTestHarnessAssertionPosition(s, entity.blockPosition(), this.relativePos(entity.blockPosition()), this.getTick());
    }

    public void fail(String s) {
        throw new GameTestHarnessAssertion(s);
    }

    public void failIf(Runnable runnable) {
        this.testInfo.createSequence().thenWaitUntil(runnable).thenFail(() -> {
            return new GameTestHarnessAssertion("Fail conditions met");
        });
    }

    public void failIfEver(Runnable runnable) {
        LongStream.range(this.testInfo.getTick(), (long) this.testInfo.getTimeoutTicks()).forEach((i) -> {
            GameTestHarnessInfo gametestharnessinfo = this.testInfo;

            Objects.requireNonNull(runnable);
            gametestharnessinfo.setRunAtTickTime(i, runnable::run);
        });
    }

    public GameTestHarnessSequence startSequence() {
        return this.testInfo.createSequence();
    }

    public BlockPosition absolutePos(BlockPosition blockposition) {
        BlockPosition blockposition1 = this.testInfo.getStructureBlockPos();
        BlockPosition blockposition2 = blockposition1.offset(blockposition);

        return DefinedStructure.transform(blockposition2, EnumBlockMirror.NONE, this.testInfo.getRotation(), blockposition1);
    }

    public BlockPosition relativePos(BlockPosition blockposition) {
        BlockPosition blockposition1 = this.testInfo.getStructureBlockPos();
        EnumBlockRotation enumblockrotation = this.testInfo.getRotation().getRotated(EnumBlockRotation.CLOCKWISE_180);
        BlockPosition blockposition2 = DefinedStructure.transform(blockposition, EnumBlockMirror.NONE, enumblockrotation, blockposition1);

        return blockposition2.subtract(blockposition1);
    }

    public Vec3D absoluteVec(Vec3D vec3d) {
        Vec3D vec3d1 = Vec3D.atLowerCornerOf(this.testInfo.getStructureBlockPos());

        return DefinedStructure.transform(vec3d1.add(vec3d), EnumBlockMirror.NONE, this.testInfo.getRotation(), this.testInfo.getStructureBlockPos());
    }

    public Vec3D relativeVec(Vec3D vec3d) {
        Vec3D vec3d1 = Vec3D.atLowerCornerOf(this.testInfo.getStructureBlockPos());

        return DefinedStructure.transform(vec3d.subtract(vec3d1), EnumBlockMirror.NONE, this.testInfo.getRotation(), this.testInfo.getStructureBlockPos());
    }

    public EnumBlockRotation getTestRotation() {
        return this.testInfo.getRotation();
    }

    public void assertTrue(boolean flag, String s) {
        if (!flag) {
            throw new GameTestHarnessAssertion(s);
        }
    }

    public <N> void assertValueEqual(N n0, N n1, String s) {
        if (!n0.equals(n1)) {
            throw new GameTestHarnessAssertion("Expected " + s + " to be " + String.valueOf(n1) + ", but was " + String.valueOf(n0));
        }
    }

    public void assertFalse(boolean flag, String s) {
        if (flag) {
            throw new GameTestHarnessAssertion(s);
        }
    }

    public long getTick() {
        return this.testInfo.getTick();
    }

    public AxisAlignedBB getBounds() {
        return this.testInfo.getStructureBounds();
    }

    private AxisAlignedBB getRelativeBounds() {
        AxisAlignedBB axisalignedbb = this.testInfo.getStructureBounds();

        return axisalignedbb.move(BlockPosition.ZERO.subtract(this.absolutePos(BlockPosition.ZERO)));
    }

    public void forEveryBlockInStructure(Consumer<BlockPosition> consumer) {
        AxisAlignedBB axisalignedbb = this.getRelativeBounds().contract(1.0D, 1.0D, 1.0D);

        BlockPosition.MutableBlockPosition.betweenClosedStream(axisalignedbb).forEach(consumer);
    }

    public void onEachTick(Runnable runnable) {
        LongStream.range(this.testInfo.getTick(), (long) this.testInfo.getTimeoutTicks()).forEach((i) -> {
            GameTestHarnessInfo gametestharnessinfo = this.testInfo;

            Objects.requireNonNull(runnable);
            gametestharnessinfo.setRunAtTickTime(i, runnable::run);
        });
    }

    public void placeAt(EntityHuman entityhuman, ItemStack itemstack, BlockPosition blockposition, EnumDirection enumdirection) {
        BlockPosition blockposition1 = this.absolutePos(blockposition.relative(enumdirection));
        MovingObjectPositionBlock movingobjectpositionblock = new MovingObjectPositionBlock(Vec3D.atCenterOf(blockposition1), enumdirection, blockposition1, false);
        ItemActionContext itemactioncontext = new ItemActionContext(entityhuman, EnumHand.MAIN_HAND, movingobjectpositionblock);

        itemstack.useOn(itemactioncontext);
    }

    public void setBiome(ResourceKey<BiomeBase> resourcekey) {
        AxisAlignedBB axisalignedbb = this.getBounds();
        BlockPosition blockposition = BlockPosition.containing(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ);
        BlockPosition blockposition1 = BlockPosition.containing(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ);
        Either<Integer, CommandSyntaxException> either = FillBiomeCommand.fill(this.getLevel(), blockposition, blockposition1, this.getLevel().registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(resourcekey));

        if (either.right().isPresent()) {
            this.fail("Failed to set biome for test");
        }

    }
}
