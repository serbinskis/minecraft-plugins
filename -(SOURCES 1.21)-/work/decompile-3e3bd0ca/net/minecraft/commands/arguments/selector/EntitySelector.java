package net.minecraft.commands.arguments.selector;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.advancements.critereon.CriterionConditionValue;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.arguments.ArgumentEntity;
import net.minecraft.network.chat.ChatComponentUtils;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.Vec3D;

public class EntitySelector {

    public static final int INFINITE = Integer.MAX_VALUE;
    public static final BiConsumer<Vec3D, List<? extends Entity>> ORDER_ARBITRARY = (vec3d, list) -> {
    };
    private static final EntityTypeTest<Entity, ?> ANY_TYPE = new EntityTypeTest<Entity, Entity>() {
        public Entity tryCast(Entity entity) {
            return entity;
        }

        @Override
        public Class<? extends Entity> getBaseClass() {
            return Entity.class;
        }
    };
    private final int maxResults;
    private final boolean includesEntities;
    private final boolean worldLimited;
    private final List<Predicate<Entity>> contextFreePredicates;
    private final CriterionConditionValue.DoubleRange range;
    private final Function<Vec3D, Vec3D> position;
    @Nullable
    private final AxisAlignedBB aabb;
    private final BiConsumer<Vec3D, List<? extends Entity>> order;
    private final boolean currentEntity;
    @Nullable
    private final String playerName;
    @Nullable
    private final UUID entityUUID;
    private final EntityTypeTest<Entity, ?> type;
    private final boolean usesSelector;

    public EntitySelector(int i, boolean flag, boolean flag1, List<Predicate<Entity>> list, CriterionConditionValue.DoubleRange criterionconditionvalue_doublerange, Function<Vec3D, Vec3D> function, @Nullable AxisAlignedBB axisalignedbb, BiConsumer<Vec3D, List<? extends Entity>> biconsumer, boolean flag2, @Nullable String s, @Nullable UUID uuid, @Nullable EntityTypes<?> entitytypes, boolean flag3) {
        this.maxResults = i;
        this.includesEntities = flag;
        this.worldLimited = flag1;
        this.contextFreePredicates = list;
        this.range = criterionconditionvalue_doublerange;
        this.position = function;
        this.aabb = axisalignedbb;
        this.order = biconsumer;
        this.currentEntity = flag2;
        this.playerName = s;
        this.entityUUID = uuid;
        this.type = (EntityTypeTest) (entitytypes == null ? EntitySelector.ANY_TYPE : entitytypes);
        this.usesSelector = flag3;
    }

    public int getMaxResults() {
        return this.maxResults;
    }

    public boolean includesEntities() {
        return this.includesEntities;
    }

    public boolean isSelfSelector() {
        return this.currentEntity;
    }

    public boolean isWorldLimited() {
        return this.worldLimited;
    }

    public boolean usesSelector() {
        return this.usesSelector;
    }

    private void checkPermissions(CommandListenerWrapper commandlistenerwrapper) throws CommandSyntaxException {
        if (this.usesSelector && !commandlistenerwrapper.hasPermission(2)) {
            throw ArgumentEntity.ERROR_SELECTORS_NOT_ALLOWED.create();
        }
    }

    public Entity findSingleEntity(CommandListenerWrapper commandlistenerwrapper) throws CommandSyntaxException {
        this.checkPermissions(commandlistenerwrapper);
        List<? extends Entity> list = this.findEntities(commandlistenerwrapper);

        if (list.isEmpty()) {
            throw ArgumentEntity.NO_ENTITIES_FOUND.create();
        } else if (list.size() > 1) {
            throw ArgumentEntity.ERROR_NOT_SINGLE_ENTITY.create();
        } else {
            return (Entity) list.get(0);
        }
    }

    public List<? extends Entity> findEntities(CommandListenerWrapper commandlistenerwrapper) throws CommandSyntaxException {
        this.checkPermissions(commandlistenerwrapper);
        if (!this.includesEntities) {
            return this.findPlayers(commandlistenerwrapper);
        } else if (this.playerName != null) {
            EntityPlayer entityplayer = commandlistenerwrapper.getServer().getPlayerList().getPlayerByName(this.playerName);

            return entityplayer == null ? List.of() : List.of(entityplayer);
        } else if (this.entityUUID != null) {
            Iterator iterator = commandlistenerwrapper.getServer().getAllLevels().iterator();

            while (iterator.hasNext()) {
                WorldServer worldserver = (WorldServer) iterator.next();
                Entity entity = worldserver.getEntity(this.entityUUID);

                if (entity != null) {
                    if (entity.getType().isEnabled(commandlistenerwrapper.enabledFeatures())) {
                        return List.of(entity);
                    }
                    break;
                }
            }

            return List.of();
        } else {
            Vec3D vec3d = (Vec3D) this.position.apply(commandlistenerwrapper.getPosition());
            AxisAlignedBB axisalignedbb = this.getAbsoluteAabb(vec3d);
            Predicate predicate;

            if (this.currentEntity) {
                predicate = this.getPredicate(vec3d, axisalignedbb, (FeatureFlagSet) null);
                return commandlistenerwrapper.getEntity() != null && predicate.test(commandlistenerwrapper.getEntity()) ? List.of(commandlistenerwrapper.getEntity()) : List.of();
            } else {
                predicate = this.getPredicate(vec3d, axisalignedbb, commandlistenerwrapper.enabledFeatures());
                List<Entity> list = new ObjectArrayList();

                if (this.isWorldLimited()) {
                    this.addEntities(list, commandlistenerwrapper.getLevel(), axisalignedbb, predicate);
                } else {
                    Iterator iterator1 = commandlistenerwrapper.getServer().getAllLevels().iterator();

                    while (iterator1.hasNext()) {
                        WorldServer worldserver1 = (WorldServer) iterator1.next();

                        this.addEntities(list, worldserver1, axisalignedbb, predicate);
                    }
                }

                return this.sortAndLimit(vec3d, list);
            }
        }
    }

    private void addEntities(List<Entity> list, WorldServer worldserver, @Nullable AxisAlignedBB axisalignedbb, Predicate<Entity> predicate) {
        int i = this.getResultLimit();

        if (list.size() < i) {
            if (axisalignedbb != null) {
                worldserver.getEntities(this.type, axisalignedbb, predicate, list, i);
            } else {
                worldserver.getEntities(this.type, predicate, list, i);
            }

        }
    }

    private int getResultLimit() {
        return this.order == EntitySelector.ORDER_ARBITRARY ? this.maxResults : Integer.MAX_VALUE;
    }

    public EntityPlayer findSinglePlayer(CommandListenerWrapper commandlistenerwrapper) throws CommandSyntaxException {
        this.checkPermissions(commandlistenerwrapper);
        List<EntityPlayer> list = this.findPlayers(commandlistenerwrapper);

        if (list.size() != 1) {
            throw ArgumentEntity.NO_PLAYERS_FOUND.create();
        } else {
            return (EntityPlayer) list.get(0);
        }
    }

    public List<EntityPlayer> findPlayers(CommandListenerWrapper commandlistenerwrapper) throws CommandSyntaxException {
        this.checkPermissions(commandlistenerwrapper);
        EntityPlayer entityplayer;

        if (this.playerName != null) {
            entityplayer = commandlistenerwrapper.getServer().getPlayerList().getPlayerByName(this.playerName);
            return entityplayer == null ? List.of() : List.of(entityplayer);
        } else if (this.entityUUID != null) {
            entityplayer = commandlistenerwrapper.getServer().getPlayerList().getPlayer(this.entityUUID);
            return entityplayer == null ? List.of() : List.of(entityplayer);
        } else {
            Vec3D vec3d = (Vec3D) this.position.apply(commandlistenerwrapper.getPosition());
            AxisAlignedBB axisalignedbb = this.getAbsoluteAabb(vec3d);
            Predicate<Entity> predicate = this.getPredicate(vec3d, axisalignedbb, (FeatureFlagSet) null);

            if (this.currentEntity) {
                Entity entity = commandlistenerwrapper.getEntity();

                if (entity instanceof EntityPlayer) {
                    EntityPlayer entityplayer1 = (EntityPlayer) entity;

                    if (predicate.test(entityplayer1)) {
                        return List.of(entityplayer1);
                    }
                }

                return List.of();
            } else {
                int i = this.getResultLimit();
                Object object;

                if (this.isWorldLimited()) {
                    object = commandlistenerwrapper.getLevel().getPlayers(predicate, i);
                } else {
                    object = new ObjectArrayList();
                    Iterator iterator = commandlistenerwrapper.getServer().getPlayerList().getPlayers().iterator();

                    while (iterator.hasNext()) {
                        EntityPlayer entityplayer2 = (EntityPlayer) iterator.next();

                        if (predicate.test(entityplayer2)) {
                            ((List) object).add(entityplayer2);
                            if (((List) object).size() >= i) {
                                return (List) object;
                            }
                        }
                    }
                }

                return this.sortAndLimit(vec3d, (List) object);
            }
        }
    }

    @Nullable
    private AxisAlignedBB getAbsoluteAabb(Vec3D vec3d) {
        return this.aabb != null ? this.aabb.move(vec3d) : null;
    }

    private Predicate<Entity> getPredicate(Vec3D vec3d, @Nullable AxisAlignedBB axisalignedbb, @Nullable FeatureFlagSet featureflagset) {
        boolean flag = featureflagset != null;
        boolean flag1 = axisalignedbb != null;
        boolean flag2 = !this.range.isAny();
        int i = (flag ? 1 : 0) + (flag1 ? 1 : 0) + (flag2 ? 1 : 0);
        Object object;

        if (i == 0) {
            object = this.contextFreePredicates;
        } else {
            List<Predicate<Entity>> list = new ObjectArrayList(this.contextFreePredicates.size() + i);

            list.addAll(this.contextFreePredicates);
            if (flag) {
                list.add((entity) -> {
                    return entity.getType().isEnabled(featureflagset);
                });
            }

            if (flag1) {
                list.add((entity) -> {
                    return axisalignedbb.intersects(entity.getBoundingBox());
                });
            }

            if (flag2) {
                list.add((entity) -> {
                    return this.range.matchesSqr(entity.distanceToSqr(vec3d));
                });
            }

            object = list;
        }

        return SystemUtils.allOf((List) object);
    }

    private <T extends Entity> List<T> sortAndLimit(Vec3D vec3d, List<T> list) {
        if (list.size() > 1) {
            this.order.accept(vec3d, list);
        }

        return list.subList(0, Math.min(this.maxResults, list.size()));
    }

    public static IChatBaseComponent joinNames(List<? extends Entity> list) {
        return ChatComponentUtils.formatList(list, Entity::getDisplayName);
    }
}
