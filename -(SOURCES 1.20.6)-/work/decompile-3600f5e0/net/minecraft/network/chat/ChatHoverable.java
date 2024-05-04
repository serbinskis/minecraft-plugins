package net.minecraft.network.chat;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.MojangsonParser;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.INamable;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ChatHoverable {

    public static final Codec<ChatHoverable> CODEC = Codec.withAlternative(ChatHoverable.e.CODEC.codec(), ChatHoverable.e.LEGACY_CODEC.codec()).xmap(ChatHoverable::new, (chathoverable) -> {
        return chathoverable.event;
    });
    private final ChatHoverable.e<?> event;

    public <T> ChatHoverable(ChatHoverable.EnumHoverAction<T> chathoverable_enumhoveraction, T t0) {
        this(new ChatHoverable.e<>(chathoverable_enumhoveraction, t0));
    }

    private ChatHoverable(ChatHoverable.e<?> chathoverable_e) {
        this.event = chathoverable_e;
    }

    public ChatHoverable.EnumHoverAction<?> getAction() {
        return this.event.action;
    }

    @Nullable
    public <T> T getValue(ChatHoverable.EnumHoverAction<T> chathoverable_enumhoveraction) {
        return this.event.action == chathoverable_enumhoveraction ? chathoverable_enumhoveraction.cast(this.event.value) : null;
    }

    public boolean equals(Object object) {
        return this == object ? true : (object != null && this.getClass() == object.getClass() ? ((ChatHoverable) object).event.equals(this.event) : false);
    }

    public String toString() {
        return this.event.toString();
    }

    public int hashCode() {
        return this.event.hashCode();
    }

    private static record e<T>(ChatHoverable.EnumHoverAction<T> action, T value) {

        public static final MapCodec<ChatHoverable.e<?>> CODEC = ChatHoverable.EnumHoverAction.CODEC.dispatchMap("action", ChatHoverable.e::action, (chathoverable_enumhoveraction) -> {
            return chathoverable_enumhoveraction.codec;
        });
        public static final MapCodec<ChatHoverable.e<?>> LEGACY_CODEC = ChatHoverable.EnumHoverAction.CODEC.dispatchMap("action", ChatHoverable.e::action, (chathoverable_enumhoveraction) -> {
            return chathoverable_enumhoveraction.legacyCodec;
        });
    }

    public static class EnumHoverAction<T> implements INamable {

        public static final ChatHoverable.EnumHoverAction<IChatBaseComponent> SHOW_TEXT = new ChatHoverable.EnumHoverAction<>("show_text", true, ComponentSerialization.CODEC, (ichatbasecomponent, registryops) -> {
            return DataResult.success(ichatbasecomponent);
        });
        public static final ChatHoverable.EnumHoverAction<ChatHoverable.c> SHOW_ITEM = new ChatHoverable.EnumHoverAction<>("show_item", true, ChatHoverable.c.CODEC, ChatHoverable.c::legacyCreate);
        public static final ChatHoverable.EnumHoverAction<ChatHoverable.b> SHOW_ENTITY = new ChatHoverable.EnumHoverAction<>("show_entity", true, ChatHoverable.b.CODEC, ChatHoverable.b::legacyCreate);
        public static final Codec<ChatHoverable.EnumHoverAction<?>> UNSAFE_CODEC = INamable.fromValues(() -> {
            return new ChatHoverable.EnumHoverAction[]{ChatHoverable.EnumHoverAction.SHOW_TEXT, ChatHoverable.EnumHoverAction.SHOW_ITEM, ChatHoverable.EnumHoverAction.SHOW_ENTITY};
        });
        public static final Codec<ChatHoverable.EnumHoverAction<?>> CODEC = ChatHoverable.EnumHoverAction.UNSAFE_CODEC.validate(ChatHoverable.EnumHoverAction::filterForSerialization);
        private final String name;
        private final boolean allowFromServer;
        final MapCodec<ChatHoverable.e<T>> codec;
        final MapCodec<ChatHoverable.e<T>> legacyCodec;

        public EnumHoverAction(String s, boolean flag, Codec<T> codec, final ChatHoverable.d<T> chathoverable_d) {
            this.name = s;
            this.allowFromServer = flag;
            this.codec = codec.xmap((object) -> {
                return new ChatHoverable.e<>(this, object);
            }, (chathoverable_e) -> {
                return chathoverable_e.value;
            }).fieldOf("contents");
            this.legacyCodec = (new Codec<ChatHoverable.e<T>>() {
                public <D> DataResult<Pair<ChatHoverable.e<T>, D>> decode(DynamicOps<D> dynamicops, D d0) {
                    return ComponentSerialization.CODEC.decode(dynamicops, d0).flatMap((pair) -> {
                        DataResult dataresult;

                        if (dynamicops instanceof RegistryOps<D> registryops) {
                            dataresult = chathoverable_d.parse((IChatBaseComponent) pair.getFirst(), registryops);
                        } else {
                            dataresult = chathoverable_d.parse((IChatBaseComponent) pair.getFirst(), (RegistryOps) null);
                        }

                        return dataresult.map((object) -> {
                            return Pair.of(new ChatHoverable.e<>(EnumHoverAction.this, object), pair.getSecond());
                        });
                    });
                }

                public <D> DataResult<D> encode(ChatHoverable.e<T> chathoverable_e, DynamicOps<D> dynamicops, D d0) {
                    return DataResult.error(() -> {
                        return "Can't encode in legacy format";
                    });
                }
            }).fieldOf("value");
        }

        public boolean isAllowedFromServer() {
            return this.allowFromServer;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        T cast(Object object) {
            return object;
        }

        public String toString() {
            return "<action " + this.name + ">";
        }

        private static DataResult<ChatHoverable.EnumHoverAction<?>> filterForSerialization(@Nullable ChatHoverable.EnumHoverAction<?> chathoverable_enumhoveraction) {
            return chathoverable_enumhoveraction == null ? DataResult.error(() -> {
                return "Unknown action";
            }) : (!chathoverable_enumhoveraction.isAllowedFromServer() ? DataResult.error(() -> {
                return "Action not allowed: " + String.valueOf(chathoverable_enumhoveraction);
            }) : DataResult.success(chathoverable_enumhoveraction, Lifecycle.stable()));
        }
    }

    public interface d<T> {

        DataResult<T> parse(IChatBaseComponent ichatbasecomponent, @Nullable RegistryOps<?> registryops);
    }

    public static class c {

        public static final Codec<ChatHoverable.c> FULL_CODEC = ItemStack.CODEC.xmap(ChatHoverable.c::new, ChatHoverable.c::getItemStack);
        private static final Codec<ChatHoverable.c> SIMPLE_CODEC = ItemStack.SIMPLE_ITEM_CODEC.xmap(ChatHoverable.c::new, ChatHoverable.c::getItemStack);
        public static final Codec<ChatHoverable.c> CODEC = Codec.withAlternative(ChatHoverable.c.FULL_CODEC, ChatHoverable.c.SIMPLE_CODEC);
        private final Holder<Item> item;
        private final int count;
        private final DataComponentPatch components;
        @Nullable
        private ItemStack itemStack;

        c(Holder<Item> holder, int i, DataComponentPatch datacomponentpatch) {
            this.item = holder;
            this.count = i;
            this.components = datacomponentpatch;
        }

        public c(ItemStack itemstack) {
            this(itemstack.getItemHolder(), itemstack.getCount(), itemstack.getComponentsPatch());
        }

        public boolean equals(Object object) {
            if (this == object) {
                return true;
            } else if (object != null && this.getClass() == object.getClass()) {
                ChatHoverable.c chathoverable_c = (ChatHoverable.c) object;

                return this.count == chathoverable_c.count && this.item.equals(chathoverable_c.item) && this.components.equals(chathoverable_c.components);
            } else {
                return false;
            }
        }

        public int hashCode() {
            int i = this.item.hashCode();

            i = 31 * i + this.count;
            i = 31 * i + this.components.hashCode();
            return i;
        }

        public ItemStack getItemStack() {
            if (this.itemStack == null) {
                this.itemStack = new ItemStack(this.item, this.count, this.components);
            }

            return this.itemStack;
        }

        private static DataResult<ChatHoverable.c> legacyCreate(IChatBaseComponent ichatbasecomponent, @Nullable RegistryOps<?> registryops) {
            try {
                NBTTagCompound nbttagcompound = MojangsonParser.parseTag(ichatbasecomponent.getString());
                DynamicOps<NBTBase> dynamicops = registryops != null ? registryops.withParent(DynamicOpsNBT.INSTANCE) : DynamicOpsNBT.INSTANCE;

                return ItemStack.CODEC.parse((DynamicOps) dynamicops, nbttagcompound).map(ChatHoverable.c::new);
            } catch (CommandSyntaxException commandsyntaxexception) {
                return DataResult.error(() -> {
                    return "Failed to parse item tag: " + commandsyntaxexception.getMessage();
                });
            }
        }
    }

    public static class b {

        public static final Codec<ChatHoverable.b> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("type").forGetter((chathoverable_b) -> {
                return chathoverable_b.type;
            }), UUIDUtil.LENIENT_CODEC.fieldOf("id").forGetter((chathoverable_b) -> {
                return chathoverable_b.id;
            }), ComponentSerialization.CODEC.lenientOptionalFieldOf("name").forGetter((chathoverable_b) -> {
                return chathoverable_b.name;
            })).apply(instance, ChatHoverable.b::new);
        });
        public final EntityTypes<?> type;
        public final UUID id;
        public final Optional<IChatBaseComponent> name;
        @Nullable
        private List<IChatBaseComponent> linesCache;

        public b(EntityTypes<?> entitytypes, UUID uuid, @Nullable IChatBaseComponent ichatbasecomponent) {
            this(entitytypes, uuid, Optional.ofNullable(ichatbasecomponent));
        }

        public b(EntityTypes<?> entitytypes, UUID uuid, Optional<IChatBaseComponent> optional) {
            this.type = entitytypes;
            this.id = uuid;
            this.name = optional;
        }

        public static DataResult<ChatHoverable.b> legacyCreate(IChatBaseComponent ichatbasecomponent, @Nullable RegistryOps<?> registryops) {
            try {
                NBTTagCompound nbttagcompound = MojangsonParser.parseTag(ichatbasecomponent.getString());
                DynamicOps<JsonElement> dynamicops = registryops != null ? registryops.withParent(JsonOps.INSTANCE) : JsonOps.INSTANCE;
                DataResult<IChatBaseComponent> dataresult = ComponentSerialization.CODEC.parse((DynamicOps) dynamicops, JsonParser.parseString(nbttagcompound.getString("name")));
                EntityTypes<?> entitytypes = (EntityTypes) BuiltInRegistries.ENTITY_TYPE.get(new MinecraftKey(nbttagcompound.getString("type")));
                UUID uuid = UUID.fromString(nbttagcompound.getString("id"));

                return dataresult.map((ichatbasecomponent1) -> {
                    return new ChatHoverable.b(entitytypes, uuid, ichatbasecomponent1);
                });
            } catch (Exception exception) {
                return DataResult.error(() -> {
                    return "Failed to parse tooltip: " + exception.getMessage();
                });
            }
        }

        public List<IChatBaseComponent> getTooltipLines() {
            if (this.linesCache == null) {
                this.linesCache = new ArrayList();
                Optional optional = this.name;
                List list = this.linesCache;

                Objects.requireNonNull(this.linesCache);
                optional.ifPresent(list::add);
                this.linesCache.add(IChatBaseComponent.translatable("gui.entity_tooltip.type", this.type.getDescription()));
                this.linesCache.add(IChatBaseComponent.literal(this.id.toString()));
            }

            return this.linesCache;
        }

        public boolean equals(Object object) {
            if (this == object) {
                return true;
            } else if (object != null && this.getClass() == object.getClass()) {
                ChatHoverable.b chathoverable_b = (ChatHoverable.b) object;

                return this.type.equals(chathoverable_b.type) && this.id.equals(chathoverable_b.id) && this.name.equals(chathoverable_b.name);
            } else {
                return false;
            }
        }

        public int hashCode() {
            int i = this.type.hashCode();

            i = 31 * i + this.id.hashCode();
            i = 31 * i + this.name.hashCode();
            return i;
        }
    }
}
