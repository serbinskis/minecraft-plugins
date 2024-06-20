package net.minecraft.world.item.enchantment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

public class ItemEnchantments implements TooltipProvider {

    public static final ItemEnchantments EMPTY = new ItemEnchantments(new Object2IntOpenHashMap(), true);
    private static final Codec<Integer> LEVEL_CODEC = Codec.intRange(0, 255);
    private static final Codec<Object2IntOpenHashMap<Holder<Enchantment>>> LEVELS_CODEC = Codec.unboundedMap(Enchantment.CODEC, ItemEnchantments.LEVEL_CODEC).xmap(Object2IntOpenHashMap::new, Function.identity());
    private static final Codec<ItemEnchantments> FULL_CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(ItemEnchantments.LEVELS_CODEC.fieldOf("levels").forGetter((itemenchantments) -> {
            return itemenchantments.enchantments;
        }), Codec.BOOL.optionalFieldOf("show_in_tooltip", true).forGetter((itemenchantments) -> {
            return itemenchantments.showInTooltip;
        })).apply(instance, ItemEnchantments::new);
    });
    public static final Codec<ItemEnchantments> CODEC = Codec.withAlternative(ItemEnchantments.FULL_CODEC, ItemEnchantments.LEVELS_CODEC, (object2intopenhashmap) -> {
        return new ItemEnchantments(object2intopenhashmap, true);
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemEnchantments> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.map(Object2IntOpenHashMap::new, Enchantment.STREAM_CODEC, ByteBufCodecs.VAR_INT), (itemenchantments) -> {
        return itemenchantments.enchantments;
    }, ByteBufCodecs.BOOL, (itemenchantments) -> {
        return itemenchantments.showInTooltip;
    }, ItemEnchantments::new);
    final Object2IntOpenHashMap<Holder<Enchantment>> enchantments;
    public final boolean showInTooltip;

    ItemEnchantments(Object2IntOpenHashMap<Holder<Enchantment>> object2intopenhashmap, boolean flag) {
        this.enchantments = object2intopenhashmap;
        this.showInTooltip = flag;
        ObjectIterator objectiterator = object2intopenhashmap.object2IntEntrySet().iterator();

        Entry entry;
        int i;

        do {
            if (!objectiterator.hasNext()) {
                return;
            }

            entry = (Entry) objectiterator.next();
            i = entry.getIntValue();
        } while (i >= 0 && i <= 255);

        String s = String.valueOf(entry.getKey());

        throw new IllegalArgumentException("Enchantment " + s + " has invalid level " + i);
    }

    public int getLevel(Holder<Enchantment> holder) {
        return this.enchantments.getInt(holder);
    }

    @Override
    public void addToTooltip(Item.b item_b, Consumer<IChatBaseComponent> consumer, TooltipFlag tooltipflag) {
        if (this.showInTooltip) {
            HolderLookup.a holderlookup_a = item_b.registries();
            HolderSet<Enchantment> holderset = getTagOrEmpty(holderlookup_a, Registries.ENCHANTMENT, EnchantmentTags.TOOLTIP_ORDER);
            Iterator iterator = holderset.iterator();

            while (iterator.hasNext()) {
                Holder<Enchantment> holder = (Holder) iterator.next();
                int i = this.enchantments.getInt(holder);

                if (i > 0) {
                    consumer.accept(Enchantment.getFullname(holder, i));
                }
            }

            ObjectIterator objectiterator = this.enchantments.object2IntEntrySet().iterator();

            while (objectiterator.hasNext()) {
                Entry<Holder<Enchantment>> entry = (Entry) objectiterator.next();
                Holder<Enchantment> holder1 = (Holder) entry.getKey();

                if (!holderset.contains(holder1)) {
                    consumer.accept(Enchantment.getFullname((Holder) entry.getKey(), entry.getIntValue()));
                }
            }

        }
    }

    private static <T> HolderSet<T> getTagOrEmpty(@Nullable HolderLookup.a holderlookup_a, ResourceKey<IRegistry<T>> resourcekey, TagKey<T> tagkey) {
        if (holderlookup_a != null) {
            Optional<HolderSet.Named<T>> optional = holderlookup_a.lookupOrThrow(resourcekey).get(tagkey);

            if (optional.isPresent()) {
                return (HolderSet) optional.get();
            }
        }

        return HolderSet.direct();
    }

    public ItemEnchantments withTooltip(boolean flag) {
        return new ItemEnchantments(this.enchantments, flag);
    }

    public Set<Holder<Enchantment>> keySet() {
        return Collections.unmodifiableSet(this.enchantments.keySet());
    }

    public Set<Entry<Holder<Enchantment>>> entrySet() {
        return Collections.unmodifiableSet(this.enchantments.object2IntEntrySet());
    }

    public int size() {
        return this.enchantments.size();
    }

    public boolean isEmpty() {
        return this.enchantments.isEmpty();
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else if (!(object instanceof ItemEnchantments)) {
            return false;
        } else {
            ItemEnchantments itemenchantments = (ItemEnchantments) object;

            return this.showInTooltip == itemenchantments.showInTooltip && this.enchantments.equals(itemenchantments.enchantments);
        }
    }

    public int hashCode() {
        int i = this.enchantments.hashCode();

        i = 31 * i + (this.showInTooltip ? 1 : 0);
        return i;
    }

    public String toString() {
        String s = String.valueOf(this.enchantments);

        return "ItemEnchantments{enchantments=" + s + ", showInTooltip=" + this.showInTooltip + "}";
    }

    public static class a {

        private final Object2IntOpenHashMap<Holder<Enchantment>> enchantments = new Object2IntOpenHashMap();
        public boolean showInTooltip;

        public a(ItemEnchantments itemenchantments) {
            this.enchantments.putAll(itemenchantments.enchantments);
            this.showInTooltip = itemenchantments.showInTooltip;
        }

        public void set(Holder<Enchantment> holder, int i) {
            if (i <= 0) {
                this.enchantments.removeInt(holder);
            } else {
                this.enchantments.put(holder, Math.min(i, 255));
            }

        }

        public void upgrade(Holder<Enchantment> holder, int i) {
            if (i > 0) {
                this.enchantments.merge(holder, Math.min(i, 255), Integer::max);
            }

        }

        public void removeIf(Predicate<Holder<Enchantment>> predicate) {
            this.enchantments.keySet().removeIf(predicate);
        }

        public int getLevel(Holder<Enchantment> holder) {
            return this.enchantments.getOrDefault(holder, 0);
        }

        public Set<Holder<Enchantment>> keySet() {
            return this.enchantments.keySet();
        }

        public ItemEnchantments toImmutable() {
            return new ItemEnchantments(this.enchantments, this.showInTooltip);
        }
    }
}
