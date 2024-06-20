package net.minecraft.network.syncher;

public record DataWatcherObject<T>(int id, DataWatcherSerializer<T> serializer) {

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else if (object != null && this.getClass() == object.getClass()) {
            DataWatcherObject<?> datawatcherobject = (DataWatcherObject) object;

            return this.id == datawatcherobject.id;
        } else {
            return false;
        }
    }

    public int hashCode() {
        return this.id;
    }

    public String toString() {
        return "<entity data: " + this.id + ">";
    }
}
