package net.minecraft.world.item;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.minecraft.EnumChatFormat;
import net.minecraft.SystemUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.stats.StatisticList;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.EnumHand;
import net.minecraft.world.InteractionResultWrapper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.World;
import net.minecraft.world.level.gameevent.GameEvent;

public class InstrumentItem extends Item {

    private final TagKey<Instrument> instruments;

    public InstrumentItem(Item.Info item_info, TagKey<Instrument> tagkey) {
        super(item_info);
        this.instruments = tagkey;
    }

    @Override
    public void appendHoverText(ItemStack itemstack, Item.b item_b, List<IChatBaseComponent> list, TooltipFlag tooltipflag) {
        super.appendHoverText(itemstack, item_b, list, tooltipflag);
        Optional<ResourceKey<Instrument>> optional = this.getInstrument(itemstack).flatMap(Holder::unwrapKey);

        if (optional.isPresent()) {
            IChatMutableComponent ichatmutablecomponent = IChatBaseComponent.translatable(SystemUtils.makeDescriptionId("instrument", ((ResourceKey) optional.get()).location()));

            list.add(ichatmutablecomponent.withStyle(EnumChatFormat.GRAY));
        }

    }

    public static ItemStack create(Item item, Holder<Instrument> holder) {
        ItemStack itemstack = new ItemStack(item);

        itemstack.set(DataComponents.INSTRUMENT, holder);
        return itemstack;
    }

    public static void setRandom(ItemStack itemstack, TagKey<Instrument> tagkey, RandomSource randomsource) {
        Optional<Holder<Instrument>> optional = BuiltInRegistries.INSTRUMENT.getRandomElementOf(tagkey, randomsource);

        optional.ifPresent((holder) -> {
            itemstack.set(DataComponents.INSTRUMENT, holder);
        });
    }

    @Override
    public InteractionResultWrapper<ItemStack> use(World world, EntityHuman entityhuman, EnumHand enumhand) {
        ItemStack itemstack = entityhuman.getItemInHand(enumhand);
        Optional<? extends Holder<Instrument>> optional = this.getInstrument(itemstack);

        if (optional.isPresent()) {
            Instrument instrument = (Instrument) ((Holder) optional.get()).value();

            entityhuman.startUsingItem(enumhand);
            play(world, entityhuman, instrument);
            entityhuman.getCooldowns().addCooldown(this, instrument.useDuration());
            entityhuman.awardStat(StatisticList.ITEM_USED.get(this));
            return InteractionResultWrapper.consume(itemstack);
        } else {
            return InteractionResultWrapper.fail(itemstack);
        }
    }

    @Override
    public int getUseDuration(ItemStack itemstack, EntityLiving entityliving) {
        Optional<Holder<Instrument>> optional = this.getInstrument(itemstack);

        return (Integer) optional.map((holder) -> {
            return ((Instrument) holder.value()).useDuration();
        }).orElse(0);
    }

    private Optional<Holder<Instrument>> getInstrument(ItemStack itemstack) {
        Holder<Instrument> holder = (Holder) itemstack.get(DataComponents.INSTRUMENT);

        if (holder != null) {
            return Optional.of(holder);
        } else {
            Iterator<Holder<Instrument>> iterator = BuiltInRegistries.INSTRUMENT.getTagOrEmpty(this.instruments).iterator();

            return iterator.hasNext() ? Optional.of((Holder) iterator.next()) : Optional.empty();
        }
    }

    @Override
    public EnumAnimation getUseAnimation(ItemStack itemstack) {
        return EnumAnimation.TOOT_HORN;
    }

    private static void play(World world, EntityHuman entityhuman, Instrument instrument) {
        SoundEffect soundeffect = (SoundEffect) instrument.soundEvent().value();
        float f = instrument.range() / 16.0F;

        world.playSound(entityhuman, (Entity) entityhuman, soundeffect, SoundCategory.RECORDS, f, 1.0F);
        world.gameEvent((Holder) GameEvent.INSTRUMENT_PLAY, entityhuman.position(), GameEvent.a.of((Entity) entityhuman));
    }
}
