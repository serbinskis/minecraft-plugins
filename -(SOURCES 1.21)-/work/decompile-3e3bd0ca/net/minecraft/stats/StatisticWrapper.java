package net.minecraft.stats;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.core.IRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class StatisticWrapper<T> implements Iterable<Statistic<T>> {

    private final IRegistry<T> registry;
    private final Map<T, Statistic<T>> map = new IdentityHashMap();
    private final IChatBaseComponent displayName;
    private final StreamCodec<RegistryFriendlyByteBuf, Statistic<T>> streamCodec;

    public StatisticWrapper(IRegistry<T> iregistry, IChatBaseComponent ichatbasecomponent) {
        this.registry = iregistry;
        this.displayName = ichatbasecomponent;
        this.streamCodec = ByteBufCodecs.registry(iregistry.key()).map(this::get, Statistic::getValue);
    }

    public StreamCodec<RegistryFriendlyByteBuf, Statistic<T>> streamCodec() {
        return this.streamCodec;
    }

    public boolean contains(T t0) {
        return this.map.containsKey(t0);
    }

    public Statistic<T> get(T t0, Counter counter) {
        return (Statistic) this.map.computeIfAbsent(t0, (object) -> {
            return new Statistic<>(this, object, counter);
        });
    }

    public IRegistry<T> getRegistry() {
        return this.registry;
    }

    public Iterator<Statistic<T>> iterator() {
        return this.map.values().iterator();
    }

    public Statistic<T> get(T t0) {
        return this.get(t0, Counter.DEFAULT);
    }

    public IChatBaseComponent getDisplayName() {
        return this.displayName;
    }
}
