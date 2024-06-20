package net.minecraft.world.damagesource;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.ChatClickable;
import net.minecraft.network.chat.ChatComponentUtils;
import net.minecraft.network.chat.ChatHoverable;
import net.minecraft.network.chat.ChatModifier;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.item.ItemStack;

public class CombatTracker {

    public static final int RESET_DAMAGE_STATUS_TIME = 100;
    public static final int RESET_COMBAT_STATUS_TIME = 300;
    private static final ChatModifier INTENTIONAL_GAME_DESIGN_STYLE = ChatModifier.EMPTY.withClickEvent(new ChatClickable(ChatClickable.EnumClickAction.OPEN_URL, "https://bugs.mojang.com/browse/MCPE-28723")).withHoverEvent(new ChatHoverable(ChatHoverable.EnumHoverAction.SHOW_TEXT, IChatBaseComponent.literal("MCPE-28723")));
    private final List<CombatEntry> entries = Lists.newArrayList();
    private final EntityLiving mob;
    private int lastDamageTime;
    private int combatStartTime;
    private int combatEndTime;
    private boolean inCombat;
    private boolean takingDamage;

    public CombatTracker(EntityLiving entityliving) {
        this.mob = entityliving;
    }

    public void recordDamage(DamageSource damagesource, float f) {
        this.recheckStatus();
        FallLocation falllocation = FallLocation.getCurrentFallLocation(this.mob);
        CombatEntry combatentry = new CombatEntry(damagesource, f, falllocation, this.mob.fallDistance);

        this.entries.add(combatentry);
        this.lastDamageTime = this.mob.tickCount;
        this.takingDamage = true;
        if (!this.inCombat && this.mob.isAlive() && shouldEnterCombat(damagesource)) {
            this.inCombat = true;
            this.combatStartTime = this.mob.tickCount;
            this.combatEndTime = this.combatStartTime;
            this.mob.onEnterCombat();
        }

    }

    private static boolean shouldEnterCombat(DamageSource damagesource) {
        return damagesource.getEntity() instanceof EntityLiving;
    }

    private IChatBaseComponent getMessageForAssistedFall(Entity entity, IChatBaseComponent ichatbasecomponent, String s, String s1) {
        ItemStack itemstack;

        if (entity instanceof EntityLiving entityliving) {
            itemstack = entityliving.getMainHandItem();
        } else {
            itemstack = ItemStack.EMPTY;
        }

        ItemStack itemstack1 = itemstack;

        return !itemstack1.isEmpty() && itemstack1.has(DataComponents.CUSTOM_NAME) ? IChatBaseComponent.translatable(s, this.mob.getDisplayName(), ichatbasecomponent, itemstack1.getDisplayName()) : IChatBaseComponent.translatable(s1, this.mob.getDisplayName(), ichatbasecomponent);
    }

    private IChatBaseComponent getFallMessage(CombatEntry combatentry, @Nullable Entity entity) {
        DamageSource damagesource = combatentry.source();

        if (!damagesource.is(DamageTypeTags.IS_FALL) && !damagesource.is(DamageTypeTags.ALWAYS_MOST_SIGNIFICANT_FALL)) {
            IChatBaseComponent ichatbasecomponent = getDisplayName(entity);
            Entity entity1 = damagesource.getEntity();
            IChatBaseComponent ichatbasecomponent1 = getDisplayName(entity1);

            return (IChatBaseComponent) (ichatbasecomponent1 != null && !ichatbasecomponent1.equals(ichatbasecomponent) ? this.getMessageForAssistedFall(entity1, ichatbasecomponent1, "death.fell.assist.item", "death.fell.assist") : (ichatbasecomponent != null ? this.getMessageForAssistedFall(entity, ichatbasecomponent, "death.fell.finish.item", "death.fell.finish") : IChatBaseComponent.translatable("death.fell.killer", this.mob.getDisplayName())));
        } else {
            FallLocation falllocation = (FallLocation) Objects.requireNonNullElse(combatentry.fallLocation(), FallLocation.GENERIC);

            return IChatBaseComponent.translatable(falllocation.languageKey(), this.mob.getDisplayName());
        }
    }

    @Nullable
    private static IChatBaseComponent getDisplayName(@Nullable Entity entity) {
        return entity == null ? null : entity.getDisplayName();
    }

    public IChatBaseComponent getDeathMessage() {
        if (this.entries.isEmpty()) {
            return IChatBaseComponent.translatable("death.attack.generic", this.mob.getDisplayName());
        } else {
            CombatEntry combatentry = (CombatEntry) this.entries.get(this.entries.size() - 1);
            DamageSource damagesource = combatentry.source();
            CombatEntry combatentry1 = this.getMostSignificantFall();
            DeathMessageType deathmessagetype = damagesource.type().deathMessageType();

            if (deathmessagetype == DeathMessageType.FALL_VARIANTS && combatentry1 != null) {
                return this.getFallMessage(combatentry1, damagesource.getEntity());
            } else if (deathmessagetype == DeathMessageType.INTENTIONAL_GAME_DESIGN) {
                String s = "death.attack." + damagesource.getMsgId();
                IChatMutableComponent ichatmutablecomponent = ChatComponentUtils.wrapInSquareBrackets(IChatBaseComponent.translatable(s + ".link")).withStyle(CombatTracker.INTENTIONAL_GAME_DESIGN_STYLE);

                return IChatBaseComponent.translatable(s + ".message", this.mob.getDisplayName(), ichatmutablecomponent);
            } else {
                return damagesource.getLocalizedDeathMessage(this.mob);
            }
        }
    }

    @Nullable
    private CombatEntry getMostSignificantFall() {
        CombatEntry combatentry = null;
        CombatEntry combatentry1 = null;
        float f = 0.0F;
        float f1 = 0.0F;

        for (int i = 0; i < this.entries.size(); ++i) {
            CombatEntry combatentry2 = (CombatEntry) this.entries.get(i);
            CombatEntry combatentry3 = i > 0 ? (CombatEntry) this.entries.get(i - 1) : null;
            DamageSource damagesource = combatentry2.source();
            boolean flag = damagesource.is(DamageTypeTags.ALWAYS_MOST_SIGNIFICANT_FALL);
            float f2 = flag ? Float.MAX_VALUE : combatentry2.fallDistance();

            if ((damagesource.is(DamageTypeTags.IS_FALL) || flag) && f2 > 0.0F && (combatentry == null || f2 > f1)) {
                if (i > 0) {
                    combatentry = combatentry3;
                } else {
                    combatentry = combatentry2;
                }

                f1 = f2;
            }

            if (combatentry2.fallLocation() != null && (combatentry1 == null || combatentry2.damage() > f)) {
                combatentry1 = combatentry2;
                f = combatentry2.damage();
            }
        }

        if (f1 > 5.0F && combatentry != null) {
            return combatentry;
        } else if (f > 5.0F && combatentry1 != null) {
            return combatentry1;
        } else {
            return null;
        }
    }

    public int getCombatDuration() {
        return this.inCombat ? this.mob.tickCount - this.combatStartTime : this.combatEndTime - this.combatStartTime;
    }

    public void recheckStatus() {
        int i = this.inCombat ? 300 : 100;

        if (this.takingDamage && (!this.mob.isAlive() || this.mob.tickCount - this.lastDamageTime > i)) {
            boolean flag = this.inCombat;

            this.takingDamage = false;
            this.inCombat = false;
            this.combatEndTime = this.mob.tickCount;
            if (flag) {
                this.mob.onLeaveCombat();
            }

            this.entries.clear();
        }

    }
}
