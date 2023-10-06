package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import net.minecraft.nbt.GameProfileSerializer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootTableInfo;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameter;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LootItemFunctionFillPlayerHead extends LootItemFunctionConditional {

    public static final Codec<LootItemFunctionFillPlayerHead> CODEC = RecordCodecBuilder.create((instance) -> {
        return commonFields(instance).and(LootTableInfo.EntityTarget.CODEC.fieldOf("entity").forGetter((lootitemfunctionfillplayerhead) -> {
            return lootitemfunctionfillplayerhead.entityTarget;
        })).apply(instance, LootItemFunctionFillPlayerHead::new);
    });
    private final LootTableInfo.EntityTarget entityTarget;

    public LootItemFunctionFillPlayerHead(List<LootItemCondition> list, LootTableInfo.EntityTarget loottableinfo_entitytarget) {
        super(list);
        this.entityTarget = loottableinfo_entitytarget;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.FILL_PLAYER_HEAD;
    }

    @Override
    public Set<LootContextParameter<?>> getReferencedContextParams() {
        return ImmutableSet.of(this.entityTarget.getParam());
    }

    @Override
    public ItemStack run(ItemStack itemstack, LootTableInfo loottableinfo) {
        if (itemstack.is(Items.PLAYER_HEAD)) {
            Object object = loottableinfo.getParamOrNull(this.entityTarget.getParam());

            if (object instanceof EntityHuman) {
                EntityHuman entityhuman = (EntityHuman) object;
                GameProfile gameprofile = entityhuman.getGameProfile();

                itemstack.getOrCreateTag().put("SkullOwner", GameProfileSerializer.writeGameProfile(new NBTTagCompound(), gameprofile));
            }
        }

        return itemstack;
    }

    public static LootItemFunctionConditional.a<?> fillPlayerHead(LootTableInfo.EntityTarget loottableinfo_entitytarget) {
        return simpleBuilder((list) -> {
            return new LootItemFunctionFillPlayerHead(list, loottableinfo_entitytarget);
        });
    }
}
