package net.minecraft.world.item;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.SystemUtils;
import net.minecraft.core.EnumDirection;
import net.minecraft.nbt.GameProfileSerializer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.TileEntitySkull;

public class ItemSkullPlayer extends ItemBlockWallable {

    public static final String TAG_SKULL_OWNER = "SkullOwner";

    public ItemSkullPlayer(Block block, Block block1, Item.Info item_info) {
        super(block, block1, item_info, EnumDirection.DOWN);
    }

    @Override
    public IChatBaseComponent getName(ItemStack itemstack) {
        if (itemstack.is(Items.PLAYER_HEAD) && itemstack.hasTag()) {
            String s = null;
            NBTTagCompound nbttagcompound = itemstack.getTag();

            if (nbttagcompound.contains("SkullOwner", 8)) {
                s = nbttagcompound.getString("SkullOwner");
            } else if (nbttagcompound.contains("SkullOwner", 10)) {
                NBTTagCompound nbttagcompound1 = nbttagcompound.getCompound("SkullOwner");

                if (nbttagcompound1.contains("Name", 8)) {
                    s = nbttagcompound1.getString("Name");
                }
            }

            if (s != null) {
                return IChatBaseComponent.translatable(this.getDescriptionId() + ".named", s);
            }
        }

        return super.getName(itemstack);
    }

    @Override
    public void verifyTagAfterLoad(NBTTagCompound nbttagcompound) {
        super.verifyTagAfterLoad(nbttagcompound);
        if (nbttagcompound.contains("SkullOwner", 8) && !SystemUtils.isBlank(nbttagcompound.getString("SkullOwner"))) {
            GameProfile gameprofile = new GameProfile((UUID) null, nbttagcompound.getString("SkullOwner"));

            TileEntitySkull.updateGameprofile(gameprofile, (gameprofile1) -> {
                nbttagcompound.put("SkullOwner", GameProfileSerializer.writeGameProfile(new NBTTagCompound(), gameprofile1));
            });
            // CraftBukkit start
        } else {
            net.minecraft.nbt.NBTTagList textures = nbttagcompound.getCompound("SkullOwner").getCompound("Properties").getList("textures", 10); // Safe due to method contracts
            for (int i = 0; i < textures.size(); i++) {
                if (textures.get(i) instanceof NBTTagCompound && !((NBTTagCompound) textures.get(i)).contains("Signature", 8) && ((NBTTagCompound) textures.get(i)).getString("Value").trim().isEmpty()) {
                    nbttagcompound.remove("SkullOwner");
                    break;
                }
            }
            // CraftBukkit end
        }

    }
}
