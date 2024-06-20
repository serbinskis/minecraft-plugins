package net.minecraft.world.item;

import java.util.List;
import java.util.Optional;
import net.minecraft.EnumChatFormat;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.decoration.EntityHanging;
import net.minecraft.world.entity.decoration.EntityItemFrame;
import net.minecraft.world.entity.decoration.EntityPainting;
import net.minecraft.world.entity.decoration.GlowItemFrame;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.ItemActionContext;
import net.minecraft.world.level.World;
import net.minecraft.world.level.gameevent.GameEvent;

public class ItemHanging extends Item {

    private static final IChatBaseComponent TOOLTIP_RANDOM_VARIANT = IChatBaseComponent.translatable("painting.random").withStyle(EnumChatFormat.GRAY);
    private final EntityTypes<? extends EntityHanging> type;

    public ItemHanging(EntityTypes<? extends EntityHanging> entitytypes, Item.Info item_info) {
        super(item_info);
        this.type = entitytypes;
    }

    @Override
    public EnumInteractionResult useOn(ItemActionContext itemactioncontext) {
        BlockPosition blockposition = itemactioncontext.getClickedPos();
        EnumDirection enumdirection = itemactioncontext.getClickedFace();
        BlockPosition blockposition1 = blockposition.relative(enumdirection);
        EntityHuman entityhuman = itemactioncontext.getPlayer();
        ItemStack itemstack = itemactioncontext.getItemInHand();

        if (entityhuman != null && !this.mayPlace(entityhuman, enumdirection, itemstack, blockposition1)) {
            return EnumInteractionResult.FAIL;
        } else {
            World world = itemactioncontext.getLevel();
            Object object;

            if (this.type == EntityTypes.PAINTING) {
                Optional<EntityPainting> optional = EntityPainting.create(world, blockposition1, enumdirection);

                if (optional.isEmpty()) {
                    return EnumInteractionResult.CONSUME;
                }

                object = (EntityHanging) optional.get();
            } else if (this.type == EntityTypes.ITEM_FRAME) {
                object = new EntityItemFrame(world, blockposition1, enumdirection);
            } else {
                if (this.type != EntityTypes.GLOW_ITEM_FRAME) {
                    return EnumInteractionResult.sidedSuccess(world.isClientSide);
                }

                object = new GlowItemFrame(world, blockposition1, enumdirection);
            }

            CustomData customdata = (CustomData) itemstack.getOrDefault(DataComponents.ENTITY_DATA, CustomData.EMPTY);

            if (!customdata.isEmpty()) {
                EntityTypes.updateCustomEntityTag(world, entityhuman, (Entity) object, customdata);
            }

            if (((EntityHanging) object).survives()) {
                if (!world.isClientSide) {
                    ((EntityHanging) object).playPlacementSound();
                    world.gameEvent((Entity) entityhuman, (Holder) GameEvent.ENTITY_PLACE, ((EntityHanging) object).position());
                    world.addFreshEntity((Entity) object);
                }

                itemstack.shrink(1);
                return EnumInteractionResult.sidedSuccess(world.isClientSide);
            } else {
                return EnumInteractionResult.CONSUME;
            }
        }
    }

    protected boolean mayPlace(EntityHuman entityhuman, EnumDirection enumdirection, ItemStack itemstack, BlockPosition blockposition) {
        return !enumdirection.getAxis().isVertical() && entityhuman.mayUseItemAt(blockposition, enumdirection, itemstack);
    }

    @Override
    public void appendHoverText(ItemStack itemstack, Item.b item_b, List<IChatBaseComponent> list, TooltipFlag tooltipflag) {
        super.appendHoverText(itemstack, item_b, list, tooltipflag);
        HolderLookup.a holderlookup_a = item_b.registries();

        if (holderlookup_a != null && this.type == EntityTypes.PAINTING) {
            CustomData customdata = (CustomData) itemstack.getOrDefault(DataComponents.ENTITY_DATA, CustomData.EMPTY);

            if (!customdata.isEmpty()) {
                customdata.read(holderlookup_a.createSerializationContext(DynamicOpsNBT.INSTANCE), EntityPainting.VARIANT_MAP_CODEC).result().ifPresentOrElse((holder) -> {
                    holder.unwrapKey().ifPresent((resourcekey) -> {
                        list.add(IChatBaseComponent.translatable(resourcekey.location().toLanguageKey("painting", "title")).withStyle(EnumChatFormat.YELLOW));
                        list.add(IChatBaseComponent.translatable(resourcekey.location().toLanguageKey("painting", "author")).withStyle(EnumChatFormat.GRAY));
                    });
                    list.add(IChatBaseComponent.translatable("painting.dimensions", ((PaintingVariant) holder.value()).width(), ((PaintingVariant) holder.value()).height()));
                }, () -> {
                    list.add(ItemHanging.TOOLTIP_RANDOM_VARIANT);
                });
            } else if (tooltipflag.isCreative()) {
                list.add(ItemHanging.TOOLTIP_RANDOM_VARIANT);
            }
        }

    }
}
