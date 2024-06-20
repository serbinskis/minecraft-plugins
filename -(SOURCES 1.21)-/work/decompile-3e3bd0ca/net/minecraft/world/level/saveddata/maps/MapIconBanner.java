package net.minecraft.world.level.saveddata.maps;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.world.item.EnumColor;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityBanner;

public record MapIconBanner(BlockPosition pos, EnumColor color, Optional<IChatBaseComponent> name) {

    public static final Codec<MapIconBanner> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(BlockPosition.CODEC.fieldOf("pos").forGetter(MapIconBanner::pos), EnumColor.CODEC.lenientOptionalFieldOf("color", EnumColor.WHITE).forGetter(MapIconBanner::color), ComponentSerialization.FLAT_CODEC.lenientOptionalFieldOf("name").forGetter(MapIconBanner::name)).apply(instance, MapIconBanner::new);
    });
    public static final Codec<List<MapIconBanner>> LIST_CODEC = MapIconBanner.CODEC.listOf();

    @Nullable
    public static MapIconBanner fromWorld(IBlockAccess iblockaccess, BlockPosition blockposition) {
        TileEntity tileentity = iblockaccess.getBlockEntity(blockposition);

        if (tileentity instanceof TileEntityBanner tileentitybanner) {
            EnumColor enumcolor = tileentitybanner.getBaseColor();
            Optional<IChatBaseComponent> optional = Optional.ofNullable(tileentitybanner.getCustomName());

            return new MapIconBanner(blockposition, enumcolor, optional);
        } else {
            return null;
        }
    }

    public Holder<MapDecorationType> getDecoration() {
        Holder holder;

        switch (this.color) {
            case WHITE:
                holder = MapDecorationTypes.WHITE_BANNER;
                break;
            case ORANGE:
                holder = MapDecorationTypes.ORANGE_BANNER;
                break;
            case MAGENTA:
                holder = MapDecorationTypes.MAGENTA_BANNER;
                break;
            case LIGHT_BLUE:
                holder = MapDecorationTypes.LIGHT_BLUE_BANNER;
                break;
            case YELLOW:
                holder = MapDecorationTypes.YELLOW_BANNER;
                break;
            case LIME:
                holder = MapDecorationTypes.LIME_BANNER;
                break;
            case PINK:
                holder = MapDecorationTypes.PINK_BANNER;
                break;
            case GRAY:
                holder = MapDecorationTypes.GRAY_BANNER;
                break;
            case LIGHT_GRAY:
                holder = MapDecorationTypes.LIGHT_GRAY_BANNER;
                break;
            case CYAN:
                holder = MapDecorationTypes.CYAN_BANNER;
                break;
            case PURPLE:
                holder = MapDecorationTypes.PURPLE_BANNER;
                break;
            case BLUE:
                holder = MapDecorationTypes.BLUE_BANNER;
                break;
            case BROWN:
                holder = MapDecorationTypes.BROWN_BANNER;
                break;
            case GREEN:
                holder = MapDecorationTypes.GREEN_BANNER;
                break;
            case RED:
                holder = MapDecorationTypes.RED_BANNER;
                break;
            case BLACK:
                holder = MapDecorationTypes.BLACK_BANNER;
                break;
            default:
                throw new MatchException((String) null, (Throwable) null);
        }

        return holder;
    }

    public String getId() {
        int i = this.pos.getX();

        return "banner-" + i + "," + this.pos.getY() + "," + this.pos.getZ();
    }
}
