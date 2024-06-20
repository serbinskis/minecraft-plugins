package net.minecraft.world.item;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.EnumChatFormat;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumMobSpawn;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.animal.EntityTropicalFish;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidType;

public class MobBucketItem extends ItemBucket {

    private static final MapCodec<EntityTropicalFish.d> VARIANT_FIELD_CODEC = EntityTropicalFish.d.CODEC.fieldOf("BucketVariantTag");
    private final EntityTypes<?> type;
    private final SoundEffect emptySound;

    public MobBucketItem(EntityTypes<?> entitytypes, FluidType fluidtype, SoundEffect soundeffect, Item.Info item_info) {
        super(fluidtype, item_info);
        this.type = entitytypes;
        this.emptySound = soundeffect;
    }

    @Override
    public void checkExtraContent(@Nullable EntityHuman entityhuman, World world, ItemStack itemstack, BlockPosition blockposition) {
        if (world instanceof WorldServer) {
            this.spawn((WorldServer) world, itemstack, blockposition);
            world.gameEvent((Entity) entityhuman, (Holder) GameEvent.ENTITY_PLACE, blockposition);
        }

    }

    @Override
    protected void playEmptySound(@Nullable EntityHuman entityhuman, GeneratorAccess generatoraccess, BlockPosition blockposition) {
        generatoraccess.playSound(entityhuman, blockposition, this.emptySound, SoundCategory.NEUTRAL, 1.0F, 1.0F);
    }

    private void spawn(WorldServer worldserver, ItemStack itemstack, BlockPosition blockposition) {
        Entity entity = this.type.spawn(worldserver, itemstack, (EntityHuman) null, blockposition, EnumMobSpawn.BUCKET, true, false);

        if (entity instanceof Bucketable bucketable) {
            CustomData customdata = (CustomData) itemstack.getOrDefault(DataComponents.BUCKET_ENTITY_DATA, CustomData.EMPTY);

            bucketable.loadFromBucketTag(customdata.copyTag());
            bucketable.setFromBucket(true);
        }

    }

    @Override
    public void appendHoverText(ItemStack itemstack, Item.b item_b, List<IChatBaseComponent> list, TooltipFlag tooltipflag) {
        if (this.type == EntityTypes.TROPICAL_FISH) {
            CustomData customdata = (CustomData) itemstack.getOrDefault(DataComponents.BUCKET_ENTITY_DATA, CustomData.EMPTY);

            if (customdata.isEmpty()) {
                return;
            }

            Optional<EntityTropicalFish.d> optional = customdata.read(MobBucketItem.VARIANT_FIELD_CODEC).result();

            if (optional.isPresent()) {
                EntityTropicalFish.d entitytropicalfish_d = (EntityTropicalFish.d) optional.get();
                EnumChatFormat[] aenumchatformat = new EnumChatFormat[]{EnumChatFormat.ITALIC, EnumChatFormat.GRAY};
                String s = "color.minecraft." + String.valueOf(entitytropicalfish_d.baseColor());
                String s1 = "color.minecraft." + String.valueOf(entitytropicalfish_d.patternColor());
                int i = EntityTropicalFish.COMMON_VARIANTS.indexOf(entitytropicalfish_d);

                if (i != -1) {
                    list.add(IChatBaseComponent.translatable(EntityTropicalFish.getPredefinedName(i)).withStyle(aenumchatformat));
                    return;
                }

                list.add(entitytropicalfish_d.pattern().displayName().plainCopy().withStyle(aenumchatformat));
                IChatMutableComponent ichatmutablecomponent = IChatBaseComponent.translatable(s);

                if (!s.equals(s1)) {
                    ichatmutablecomponent.append(", ").append((IChatBaseComponent) IChatBaseComponent.translatable(s1));
                }

                ichatmutablecomponent.withStyle(aenumchatformat);
                list.add(ichatmutablecomponent);
            }
        }

    }
}
