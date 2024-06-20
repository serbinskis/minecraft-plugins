package net.minecraft.world.level.block.entity.vault;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.util.MathHelper;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;

public class VaultServerData {

    static final String TAG_NAME = "server_data";
    static Codec<VaultServerData> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(UUIDUtil.CODEC_LINKED_SET.lenientOptionalFieldOf("rewarded_players", Set.of()).forGetter((vaultserverdata) -> {
            return vaultserverdata.rewardedPlayers;
        }), Codec.LONG.lenientOptionalFieldOf("state_updating_resumes_at", 0L).forGetter((vaultserverdata) -> {
            return vaultserverdata.stateUpdatingResumesAt;
        }), ItemStack.CODEC.listOf().lenientOptionalFieldOf("items_to_eject", List.of()).forGetter((vaultserverdata) -> {
            return vaultserverdata.itemsToEject;
        }), Codec.INT.lenientOptionalFieldOf("total_ejections_needed", 0).forGetter((vaultserverdata) -> {
            return vaultserverdata.totalEjectionsNeeded;
        })).apply(instance, VaultServerData::new);
    });
    private static final int MAX_REWARD_PLAYERS = 128;
    private final Set<UUID> rewardedPlayers = new ObjectLinkedOpenHashSet();
    private long stateUpdatingResumesAt;
    private final List<ItemStack> itemsToEject = new ObjectArrayList();
    private long lastInsertFailTimestamp;
    private int totalEjectionsNeeded;
    boolean isDirty;

    VaultServerData(Set<UUID> set, long i, List<ItemStack> list, int j) {
        this.rewardedPlayers.addAll(set);
        this.stateUpdatingResumesAt = i;
        this.itemsToEject.addAll(list);
        this.totalEjectionsNeeded = j;
    }

    VaultServerData() {}

    void setLastInsertFailTimestamp(long i) {
        this.lastInsertFailTimestamp = i;
    }

    long getLastInsertFailTimestamp() {
        return this.lastInsertFailTimestamp;
    }

    Set<UUID> getRewardedPlayers() {
        return this.rewardedPlayers;
    }

    boolean hasRewardedPlayer(EntityHuman entityhuman) {
        return this.rewardedPlayers.contains(entityhuman.getUUID());
    }

    @VisibleForTesting
    public void addToRewardedPlayers(EntityHuman entityhuman) {
        this.rewardedPlayers.add(entityhuman.getUUID());
        if (this.rewardedPlayers.size() > 128) {
            Iterator<UUID> iterator = this.rewardedPlayers.iterator();

            if (iterator.hasNext()) {
                iterator.next();
                iterator.remove();
            }
        }

        this.markChanged();
    }

    long stateUpdatingResumesAt() {
        return this.stateUpdatingResumesAt;
    }

    void pauseStateUpdatingUntil(long i) {
        this.stateUpdatingResumesAt = i;
        this.markChanged();
    }

    List<ItemStack> getItemsToEject() {
        return this.itemsToEject;
    }

    void markEjectionFinished() {
        this.totalEjectionsNeeded = 0;
        this.markChanged();
    }

    void setItemsToEject(List<ItemStack> list) {
        this.itemsToEject.clear();
        this.itemsToEject.addAll(list);
        this.totalEjectionsNeeded = this.itemsToEject.size();
        this.markChanged();
    }

    ItemStack getNextItemToEject() {
        return this.itemsToEject.isEmpty() ? ItemStack.EMPTY : (ItemStack) Objects.requireNonNullElse((ItemStack) this.itemsToEject.get(this.itemsToEject.size() - 1), ItemStack.EMPTY);
    }

    ItemStack popNextItemToEject() {
        if (this.itemsToEject.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            this.markChanged();
            return (ItemStack) Objects.requireNonNullElse((ItemStack) this.itemsToEject.remove(this.itemsToEject.size() - 1), ItemStack.EMPTY);
        }
    }

    void set(VaultServerData vaultserverdata) {
        this.stateUpdatingResumesAt = vaultserverdata.stateUpdatingResumesAt();
        this.itemsToEject.clear();
        this.itemsToEject.addAll(vaultserverdata.itemsToEject);
        this.rewardedPlayers.clear();
        this.rewardedPlayers.addAll(vaultserverdata.rewardedPlayers);
    }

    private void markChanged() {
        this.isDirty = true;
    }

    public float ejectionProgress() {
        return this.totalEjectionsNeeded == 1 ? 1.0F : 1.0F - MathHelper.inverseLerp((float) this.getItemsToEject().size(), 1.0F, (float) this.totalEjectionsNeeded);
    }
}
