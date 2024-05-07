package net.minecraft.world.level.block.entity.vault;

import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.dispenser.DispenseBehaviorItem;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.util.INamable;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3D;

public enum VaultState implements INamable {

    INACTIVE("inactive", VaultState.a.HALF_LIT) {
        @Override
        protected void onEnter(WorldServer worldserver, BlockPosition blockposition, VaultConfig vaultconfig, VaultSharedData vaultshareddata, boolean flag) {
            vaultshareddata.setDisplayItem(ItemStack.EMPTY);
            worldserver.levelEvent(3016, blockposition, flag ? 1 : 0);
        }
    },
    ACTIVE("active", VaultState.a.LIT) {
        @Override
        protected void onEnter(WorldServer worldserver, BlockPosition blockposition, VaultConfig vaultconfig, VaultSharedData vaultshareddata, boolean flag) {
            if (!vaultshareddata.hasDisplayItem()) {
                VaultBlockEntity.b.cycleDisplayItemFromLootTable(worldserver, this, vaultconfig, vaultshareddata, blockposition);
            }

            worldserver.levelEvent(3015, blockposition, flag ? 1 : 0);
        }
    },
    UNLOCKING("unlocking", VaultState.a.LIT) {
        @Override
        protected void onEnter(WorldServer worldserver, BlockPosition blockposition, VaultConfig vaultconfig, VaultSharedData vaultshareddata, boolean flag) {
            worldserver.playSound((EntityHuman) null, blockposition, SoundEffects.VAULT_INSERT_ITEM, SoundCategory.BLOCKS);
        }
    },
    EJECTING("ejecting", VaultState.a.LIT) {
        @Override
        protected void onEnter(WorldServer worldserver, BlockPosition blockposition, VaultConfig vaultconfig, VaultSharedData vaultshareddata, boolean flag) {
            worldserver.playSound((EntityHuman) null, blockposition, SoundEffects.VAULT_OPEN_SHUTTER, SoundCategory.BLOCKS);
        }

        @Override
        protected void onExit(WorldServer worldserver, BlockPosition blockposition, VaultConfig vaultconfig, VaultSharedData vaultshareddata) {
            worldserver.playSound((EntityHuman) null, blockposition, SoundEffects.VAULT_CLOSE_SHUTTER, SoundCategory.BLOCKS);
        }
    };

    private static final int UPDATE_CONNECTED_PLAYERS_TICK_RATE = 20;
    private static final int DELAY_BETWEEN_EJECTIONS_TICKS = 20;
    private static final int DELAY_AFTER_LAST_EJECTION_TICKS = 20;
    private static final int DELAY_BEFORE_FIRST_EJECTION_TICKS = 20;
    private final String stateName;
    private final VaultState.a lightLevel;

    VaultState(final String s, final VaultState.a vaultstate_a) {
        this.stateName = s;
        this.lightLevel = vaultstate_a;
    }

    @Override
    public String getSerializedName() {
        return this.stateName;
    }

    public int lightLevel() {
        return this.lightLevel.value;
    }

    public VaultState tickAndGetNext(WorldServer worldserver, BlockPosition blockposition, VaultConfig vaultconfig, VaultServerData vaultserverdata, VaultSharedData vaultshareddata) {
        VaultState vaultstate;

        switch (this.ordinal()) {
            case 0:
                vaultstate = updateStateForConnectedPlayers(worldserver, blockposition, vaultconfig, vaultserverdata, vaultshareddata, vaultconfig.activationRange());
                break;
            case 1:
                vaultstate = updateStateForConnectedPlayers(worldserver, blockposition, vaultconfig, vaultserverdata, vaultshareddata, vaultconfig.deactivationRange());
                break;
            case 2:
                vaultserverdata.pauseStateUpdatingUntil(worldserver.getGameTime() + 20L);
                vaultstate = VaultState.EJECTING;
                break;
            case 3:
                if (vaultserverdata.getItemsToEject().isEmpty()) {
                    vaultserverdata.markEjectionFinished();
                    vaultstate = updateStateForConnectedPlayers(worldserver, blockposition, vaultconfig, vaultserverdata, vaultshareddata, vaultconfig.deactivationRange());
                } else {
                    float f = vaultserverdata.ejectionProgress();

                    this.ejectResultItem(worldserver, blockposition, vaultserverdata.popNextItemToEject(), f);
                    vaultshareddata.setDisplayItem(vaultserverdata.getNextItemToEject());
                    boolean flag = vaultserverdata.getItemsToEject().isEmpty();
                    int i = flag ? 20 : 20;

                    vaultserverdata.pauseStateUpdatingUntil(worldserver.getGameTime() + (long) i);
                    vaultstate = VaultState.EJECTING;
                }
                break;
            default:
                throw new MatchException((String) null, (Throwable) null);
        }

        return vaultstate;
    }

    private static VaultState updateStateForConnectedPlayers(WorldServer worldserver, BlockPosition blockposition, VaultConfig vaultconfig, VaultServerData vaultserverdata, VaultSharedData vaultshareddata, double d0) {
        vaultshareddata.updateConnectedPlayersWithinRange(worldserver, blockposition, vaultserverdata, vaultconfig, d0);
        vaultserverdata.pauseStateUpdatingUntil(worldserver.getGameTime() + 20L);
        return vaultshareddata.hasConnectedPlayers() ? VaultState.ACTIVE : VaultState.INACTIVE;
    }

    public void onTransition(WorldServer worldserver, BlockPosition blockposition, VaultState vaultstate, VaultConfig vaultconfig, VaultSharedData vaultshareddata, boolean flag) {
        this.onExit(worldserver, blockposition, vaultconfig, vaultshareddata);
        vaultstate.onEnter(worldserver, blockposition, vaultconfig, vaultshareddata, flag);
    }

    protected void onEnter(WorldServer worldserver, BlockPosition blockposition, VaultConfig vaultconfig, VaultSharedData vaultshareddata, boolean flag) {}

    protected void onExit(WorldServer worldserver, BlockPosition blockposition, VaultConfig vaultconfig, VaultSharedData vaultshareddata) {}

    private void ejectResultItem(WorldServer worldserver, BlockPosition blockposition, ItemStack itemstack, float f) {
        DispenseBehaviorItem.spawnItem(worldserver, itemstack, 2, EnumDirection.UP, Vec3D.atBottomCenterOf(blockposition).relative(EnumDirection.UP, 1.2D));
        worldserver.levelEvent(3017, blockposition, 0);
        worldserver.playSound((EntityHuman) null, blockposition, SoundEffects.VAULT_EJECT_ITEM, SoundCategory.BLOCKS, 1.0F, 0.8F + 0.4F * f);
    }

    private static enum a {

        HALF_LIT(6), LIT(12);

        final int value;

        private a(final int i) {
            this.value = i;
        }
    }
}
