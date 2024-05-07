package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Iterator;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.arguments.ArgumentChatComponent;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.EnumMobSpawn;
import net.minecraft.world.entity.GroupDataEntity;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.raid.EntityRaider;
import net.minecraft.world.entity.raid.PersistentRaid;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.phys.Vec3D;

public class RaidCommand {

    public RaidCommand() {}

    public static void register(CommandDispatcher<CommandListenerWrapper> commanddispatcher, CommandBuildContext commandbuildcontext) {
        commanddispatcher.register((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) ((LiteralArgumentBuilder) net.minecraft.commands.CommandDispatcher.literal("raid").requires((commandlistenerwrapper) -> {
            return commandlistenerwrapper.hasPermission(3);
        })).then(net.minecraft.commands.CommandDispatcher.literal("start").then(net.minecraft.commands.CommandDispatcher.argument("omenlvl", IntegerArgumentType.integer(0)).executes((commandcontext) -> {
            return start((CommandListenerWrapper) commandcontext.getSource(), IntegerArgumentType.getInteger(commandcontext, "omenlvl"));
        })))).then(net.minecraft.commands.CommandDispatcher.literal("stop").executes((commandcontext) -> {
            return stop((CommandListenerWrapper) commandcontext.getSource());
        }))).then(net.minecraft.commands.CommandDispatcher.literal("check").executes((commandcontext) -> {
            return check((CommandListenerWrapper) commandcontext.getSource());
        }))).then(net.minecraft.commands.CommandDispatcher.literal("sound").then(net.minecraft.commands.CommandDispatcher.argument("type", ArgumentChatComponent.textComponent(commandbuildcontext)).executes((commandcontext) -> {
            return playSound((CommandListenerWrapper) commandcontext.getSource(), ArgumentChatComponent.getComponent(commandcontext, "type"));
        })))).then(net.minecraft.commands.CommandDispatcher.literal("spawnleader").executes((commandcontext) -> {
            return spawnLeader((CommandListenerWrapper) commandcontext.getSource());
        }))).then(net.minecraft.commands.CommandDispatcher.literal("setomen").then(net.minecraft.commands.CommandDispatcher.argument("level", IntegerArgumentType.integer(0)).executes((commandcontext) -> {
            return setRaidOmenLevel((CommandListenerWrapper) commandcontext.getSource(), IntegerArgumentType.getInteger(commandcontext, "level"));
        })))).then(net.minecraft.commands.CommandDispatcher.literal("glow").executes((commandcontext) -> {
            return glow((CommandListenerWrapper) commandcontext.getSource());
        })));
    }

    private static int glow(CommandListenerWrapper commandlistenerwrapper) throws CommandSyntaxException {
        Raid raid = getRaid(commandlistenerwrapper.getPlayerOrException());

        if (raid != null) {
            Set<EntityRaider> set = raid.getAllRaiders();
            Iterator iterator = set.iterator();

            while (iterator.hasNext()) {
                EntityRaider entityraider = (EntityRaider) iterator.next();

                entityraider.addEffect(new MobEffect(MobEffects.GLOWING, 1000, 1));
            }
        }

        return 1;
    }

    private static int setRaidOmenLevel(CommandListenerWrapper commandlistenerwrapper, int i) throws CommandSyntaxException {
        Raid raid = getRaid(commandlistenerwrapper.getPlayerOrException());

        if (raid != null) {
            int j = raid.getMaxRaidOmenLevel();

            if (i > j) {
                commandlistenerwrapper.sendFailure(IChatBaseComponent.literal("Sorry, the max raid omen level you can set is " + j));
            } else {
                int k = raid.getRaidOmenLevel();

                raid.setRaidOmenLevel(i);
                commandlistenerwrapper.sendSuccess(() -> {
                    return IChatBaseComponent.literal("Changed village's raid omen level from " + k + " to " + i);
                }, false);
            }
        } else {
            commandlistenerwrapper.sendFailure(IChatBaseComponent.literal("No raid found here"));
        }

        return 1;
    }

    private static int spawnLeader(CommandListenerWrapper commandlistenerwrapper) {
        commandlistenerwrapper.sendSuccess(() -> {
            return IChatBaseComponent.literal("Spawned a raid captain");
        }, false);
        EntityRaider entityraider = (EntityRaider) EntityTypes.PILLAGER.create(commandlistenerwrapper.getLevel());

        if (entityraider == null) {
            commandlistenerwrapper.sendFailure(IChatBaseComponent.literal("Pillager failed to spawn"));
            return 0;
        } else {
            entityraider.setPatrolLeader(true);
            entityraider.setItemSlot(EnumItemSlot.HEAD, Raid.getLeaderBannerInstance(commandlistenerwrapper.registryAccess().lookupOrThrow(Registries.BANNER_PATTERN)));
            entityraider.setPos(commandlistenerwrapper.getPosition().x, commandlistenerwrapper.getPosition().y, commandlistenerwrapper.getPosition().z);
            entityraider.finalizeSpawn(commandlistenerwrapper.getLevel(), commandlistenerwrapper.getLevel().getCurrentDifficultyAt(BlockPosition.containing(commandlistenerwrapper.getPosition())), EnumMobSpawn.COMMAND, (GroupDataEntity) null);
            commandlistenerwrapper.getLevel().addFreshEntityWithPassengers(entityraider);
            return 1;
        }
    }

    private static int playSound(CommandListenerWrapper commandlistenerwrapper, @Nullable IChatBaseComponent ichatbasecomponent) {
        if (ichatbasecomponent != null && ichatbasecomponent.getString().equals("local")) {
            WorldServer worldserver = commandlistenerwrapper.getLevel();
            Vec3D vec3d = commandlistenerwrapper.getPosition().add(5.0D, 0.0D, 0.0D);

            worldserver.playSeededSound((EntityHuman) null, vec3d.x, vec3d.y, vec3d.z, (Holder) SoundEffects.RAID_HORN, SoundCategory.NEUTRAL, 2.0F, 1.0F, worldserver.random.nextLong());
        }

        return 1;
    }

    private static int start(CommandListenerWrapper commandlistenerwrapper, int i) throws CommandSyntaxException {
        EntityPlayer entityplayer = commandlistenerwrapper.getPlayerOrException();
        BlockPosition blockposition = entityplayer.blockPosition();

        if (entityplayer.serverLevel().isRaided(blockposition)) {
            commandlistenerwrapper.sendFailure(IChatBaseComponent.literal("Raid already started close by"));
            return -1;
        } else {
            PersistentRaid persistentraid = entityplayer.serverLevel().getRaids();
            Raid raid = persistentraid.createOrExtendRaid(entityplayer, entityplayer.blockPosition());

            if (raid != null) {
                raid.setRaidOmenLevel(i);
                persistentraid.setDirty();
                commandlistenerwrapper.sendSuccess(() -> {
                    return IChatBaseComponent.literal("Created a raid in your local village");
                }, false);
            } else {
                commandlistenerwrapper.sendFailure(IChatBaseComponent.literal("Failed to create a raid in your local village"));
            }

            return 1;
        }
    }

    private static int stop(CommandListenerWrapper commandlistenerwrapper) throws CommandSyntaxException {
        EntityPlayer entityplayer = commandlistenerwrapper.getPlayerOrException();
        BlockPosition blockposition = entityplayer.blockPosition();
        Raid raid = entityplayer.serverLevel().getRaidAt(blockposition);

        if (raid != null) {
            raid.stop();
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.literal("Stopped raid");
            }, false);
            return 1;
        } else {
            commandlistenerwrapper.sendFailure(IChatBaseComponent.literal("No raid here"));
            return -1;
        }
    }

    private static int check(CommandListenerWrapper commandlistenerwrapper) throws CommandSyntaxException {
        Raid raid = getRaid(commandlistenerwrapper.getPlayerOrException());

        if (raid != null) {
            StringBuilder stringbuilder = new StringBuilder();

            stringbuilder.append("Found a started raid! ");
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.literal(stringbuilder.toString());
            }, false);
            StringBuilder stringbuilder1 = new StringBuilder();

            stringbuilder1.append("Num groups spawned: ");
            stringbuilder1.append(raid.getGroupsSpawned());
            stringbuilder1.append(" Raid omen level: ");
            stringbuilder1.append(raid.getRaidOmenLevel());
            stringbuilder1.append(" Num mobs: ");
            stringbuilder1.append(raid.getTotalRaidersAlive());
            stringbuilder1.append(" Raid health: ");
            stringbuilder1.append(raid.getHealthOfLivingRaiders());
            stringbuilder1.append(" / ");
            stringbuilder1.append(raid.getTotalHealth());
            commandlistenerwrapper.sendSuccess(() -> {
                return IChatBaseComponent.literal(stringbuilder1.toString());
            }, false);
            return 1;
        } else {
            commandlistenerwrapper.sendFailure(IChatBaseComponent.literal("Found no started raids"));
            return 0;
        }
    }

    @Nullable
    private static Raid getRaid(EntityPlayer entityplayer) {
        return entityplayer.serverLevel().getRaidAt(entityplayer.blockPosition());
    }
}
