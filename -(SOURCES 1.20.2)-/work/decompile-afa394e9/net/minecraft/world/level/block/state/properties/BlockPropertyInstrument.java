package net.minecraft.world.level.block.state.properties;

import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.util.INamable;

public enum BlockPropertyInstrument implements INamable {

    HARP("harp", SoundEffects.NOTE_BLOCK_HARP, BlockPropertyInstrument.a.BASE_BLOCK), BASEDRUM("basedrum", SoundEffects.NOTE_BLOCK_BASEDRUM, BlockPropertyInstrument.a.BASE_BLOCK), SNARE("snare", SoundEffects.NOTE_BLOCK_SNARE, BlockPropertyInstrument.a.BASE_BLOCK), HAT("hat", SoundEffects.NOTE_BLOCK_HAT, BlockPropertyInstrument.a.BASE_BLOCK), BASS("bass", SoundEffects.NOTE_BLOCK_BASS, BlockPropertyInstrument.a.BASE_BLOCK), FLUTE("flute", SoundEffects.NOTE_BLOCK_FLUTE, BlockPropertyInstrument.a.BASE_BLOCK), BELL("bell", SoundEffects.NOTE_BLOCK_BELL, BlockPropertyInstrument.a.BASE_BLOCK), GUITAR("guitar", SoundEffects.NOTE_BLOCK_GUITAR, BlockPropertyInstrument.a.BASE_BLOCK), CHIME("chime", SoundEffects.NOTE_BLOCK_CHIME, BlockPropertyInstrument.a.BASE_BLOCK), XYLOPHONE("xylophone", SoundEffects.NOTE_BLOCK_XYLOPHONE, BlockPropertyInstrument.a.BASE_BLOCK), IRON_XYLOPHONE("iron_xylophone", SoundEffects.NOTE_BLOCK_IRON_XYLOPHONE, BlockPropertyInstrument.a.BASE_BLOCK), COW_BELL("cow_bell", SoundEffects.NOTE_BLOCK_COW_BELL, BlockPropertyInstrument.a.BASE_BLOCK), DIDGERIDOO("didgeridoo", SoundEffects.NOTE_BLOCK_DIDGERIDOO, BlockPropertyInstrument.a.BASE_BLOCK), BIT("bit", SoundEffects.NOTE_BLOCK_BIT, BlockPropertyInstrument.a.BASE_BLOCK), BANJO("banjo", SoundEffects.NOTE_BLOCK_BANJO, BlockPropertyInstrument.a.BASE_BLOCK), PLING("pling", SoundEffects.NOTE_BLOCK_PLING, BlockPropertyInstrument.a.BASE_BLOCK), ZOMBIE("zombie", SoundEffects.NOTE_BLOCK_IMITATE_ZOMBIE, BlockPropertyInstrument.a.MOB_HEAD), SKELETON("skeleton", SoundEffects.NOTE_BLOCK_IMITATE_SKELETON, BlockPropertyInstrument.a.MOB_HEAD), CREEPER("creeper", SoundEffects.NOTE_BLOCK_IMITATE_CREEPER, BlockPropertyInstrument.a.MOB_HEAD), DRAGON("dragon", SoundEffects.NOTE_BLOCK_IMITATE_ENDER_DRAGON, BlockPropertyInstrument.a.MOB_HEAD), WITHER_SKELETON("wither_skeleton", SoundEffects.NOTE_BLOCK_IMITATE_WITHER_SKELETON, BlockPropertyInstrument.a.MOB_HEAD), PIGLIN("piglin", SoundEffects.NOTE_BLOCK_IMITATE_PIGLIN, BlockPropertyInstrument.a.MOB_HEAD), CUSTOM_HEAD("custom_head", SoundEffects.UI_BUTTON_CLICK, BlockPropertyInstrument.a.CUSTOM);

    private final String name;
    private final Holder<SoundEffect> soundEvent;
    private final BlockPropertyInstrument.a type;

    private BlockPropertyInstrument(String s, Holder holder, BlockPropertyInstrument.a blockpropertyinstrument_a) {
        this.name = s;
        this.soundEvent = holder;
        this.type = blockpropertyinstrument_a;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public Holder<SoundEffect> getSoundEvent() {
        return this.soundEvent;
    }

    public boolean isTunable() {
        return this.type == BlockPropertyInstrument.a.BASE_BLOCK;
    }

    public boolean hasCustomSound() {
        return this.type == BlockPropertyInstrument.a.CUSTOM;
    }

    public boolean worksAboveNoteBlock() {
        return this.type != BlockPropertyInstrument.a.BASE_BLOCK;
    }

    private static enum a {

        BASE_BLOCK, MOB_HEAD, CUSTOM;

        private a() {}
    }
}
