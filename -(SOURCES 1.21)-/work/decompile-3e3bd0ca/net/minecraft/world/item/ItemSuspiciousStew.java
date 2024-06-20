package net.minecraft.world.item;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.level.World;

public class ItemSuspiciousStew extends Item {

    public static final int DEFAULT_DURATION = 160;

    public ItemSuspiciousStew(Item.Info item_info) {
        super(item_info);
    }

    @Override
    public void appendHoverText(ItemStack itemstack, Item.b item_b, List<IChatBaseComponent> list, TooltipFlag tooltipflag) {
        super.appendHoverText(itemstack, item_b, list, tooltipflag);
        if (tooltipflag.isCreative()) {
            List<MobEffect> list1 = new ArrayList();
            SuspiciousStewEffects suspicioussteweffects = (SuspiciousStewEffects) itemstack.getOrDefault(DataComponents.SUSPICIOUS_STEW_EFFECTS, SuspiciousStewEffects.EMPTY);
            Iterator iterator = suspicioussteweffects.effects().iterator();

            while (iterator.hasNext()) {
                SuspiciousStewEffects.a suspicioussteweffects_a = (SuspiciousStewEffects.a) iterator.next();

                list1.add(suspicioussteweffects_a.createEffectInstance());
            }

            Objects.requireNonNull(list);
            PotionContents.addPotionTooltip(list1, list::add, 1.0F, item_b.tickRate());
        }

    }

    @Override
    public ItemStack finishUsingItem(ItemStack itemstack, World world, EntityLiving entityliving) {
        SuspiciousStewEffects suspicioussteweffects = (SuspiciousStewEffects) itemstack.getOrDefault(DataComponents.SUSPICIOUS_STEW_EFFECTS, SuspiciousStewEffects.EMPTY);
        Iterator iterator = suspicioussteweffects.effects().iterator();

        while (iterator.hasNext()) {
            SuspiciousStewEffects.a suspicioussteweffects_a = (SuspiciousStewEffects.a) iterator.next();

            entityliving.addEffect(suspicioussteweffects_a.createEffectInstance());
        }

        return super.finishUsingItem(itemstack, world, entityliving);
    }
}
