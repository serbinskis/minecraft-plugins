package net.minecraft.network.syncher;

import java.util.List;

public interface SyncedDataHolder {

    void onSyncedDataUpdated(DataWatcherObject<?> datawatcherobject);

    void onSyncedDataUpdated(List<DataWatcher.c<?>> list);
}
