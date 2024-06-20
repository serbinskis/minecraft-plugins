package net.minecraft.world.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Consumer;
import net.minecraft.EnumChatFormat;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.ChatComponentUtils;
import net.minecraft.network.chat.ChatModifier;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.stats.StatisticList;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.BlockJukeBox;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityJukeBox;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.gameevent.GameEvent;

public record JukeboxPlayable(EitherHolder<JukeboxSong> song, boolean showInTooltip) implements TooltipProvider {

    public static final Codec<JukeboxPlayable> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(EitherHolder.codec(Registries.JUKEBOX_SONG, JukeboxSong.CODEC).fieldOf("song").forGetter(JukeboxPlayable::song), Codec.BOOL.optionalFieldOf("show_in_tooltip", true).forGetter(JukeboxPlayable::showInTooltip)).apply(instance, JukeboxPlayable::new);
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, JukeboxPlayable> STREAM_CODEC = StreamCodec.composite(EitherHolder.streamCodec(Registries.JUKEBOX_SONG, JukeboxSong.STREAM_CODEC), JukeboxPlayable::song, ByteBufCodecs.BOOL, JukeboxPlayable::showInTooltip, JukeboxPlayable::new);

    @Override
    public void addToTooltip(Item.b item_b, Consumer<IChatBaseComponent> consumer, TooltipFlag tooltipflag) {
        HolderLookup.a holderlookup_a = item_b.registries();

        if (this.showInTooltip && holderlookup_a != null) {
            this.song.unwrap(holderlookup_a).ifPresent((holder) -> {
                IChatMutableComponent ichatmutablecomponent = ((JukeboxSong) holder.value()).description().copy();

                ChatComponentUtils.mergeStyles(ichatmutablecomponent, ChatModifier.EMPTY.withColor(EnumChatFormat.GRAY));
                consumer.accept(ichatmutablecomponent);
            });
        }

    }

    public JukeboxPlayable withTooltip(boolean flag) {
        return new JukeboxPlayable(this.song, flag);
    }

    public static ItemInteractionResult tryInsertIntoJukebox(World world, BlockPosition blockposition, ItemStack itemstack, EntityHuman entityhuman) {
        JukeboxPlayable jukeboxplayable = (JukeboxPlayable) itemstack.get(DataComponents.JUKEBOX_PLAYABLE);

        if (jukeboxplayable == null) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        } else {
            IBlockData iblockdata = world.getBlockState(blockposition);

            if (iblockdata.is(Blocks.JUKEBOX) && !(Boolean) iblockdata.getValue(BlockJukeBox.HAS_RECORD)) {
                if (!world.isClientSide) {
                    ItemStack itemstack1 = itemstack.consumeAndReturn(1, entityhuman);
                    TileEntity tileentity = world.getBlockEntity(blockposition);

                    if (tileentity instanceof TileEntityJukeBox) {
                        TileEntityJukeBox tileentityjukebox = (TileEntityJukeBox) tileentity;

                        tileentityjukebox.setTheItem(itemstack1);
                        world.gameEvent((Holder) GameEvent.BLOCK_CHANGE, blockposition, GameEvent.a.of(entityhuman, iblockdata));
                    }

                    entityhuman.awardStat(StatisticList.PLAY_RECORD);
                }

                return ItemInteractionResult.sidedSuccess(world.isClientSide);
            } else {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }
        }
    }
}
