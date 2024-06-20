package net.minecraft.world.item;

import net.minecraft.SystemUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;

public interface JukeboxSongs {

    ResourceKey<JukeboxSong> THIRTEEN = create("13");
    ResourceKey<JukeboxSong> CAT = create("cat");
    ResourceKey<JukeboxSong> BLOCKS = create("blocks");
    ResourceKey<JukeboxSong> CHIRP = create("chirp");
    ResourceKey<JukeboxSong> FAR = create("far");
    ResourceKey<JukeboxSong> MALL = create("mall");
    ResourceKey<JukeboxSong> MELLOHI = create("mellohi");
    ResourceKey<JukeboxSong> STAL = create("stal");
    ResourceKey<JukeboxSong> STRAD = create("strad");
    ResourceKey<JukeboxSong> WARD = create("ward");
    ResourceKey<JukeboxSong> ELEVEN = create("11");
    ResourceKey<JukeboxSong> WAIT = create("wait");
    ResourceKey<JukeboxSong> PIGSTEP = create("pigstep");
    ResourceKey<JukeboxSong> OTHERSIDE = create("otherside");
    ResourceKey<JukeboxSong> FIVE = create("5");
    ResourceKey<JukeboxSong> RELIC = create("relic");
    ResourceKey<JukeboxSong> PRECIPICE = create("precipice");
    ResourceKey<JukeboxSong> CREATOR = create("creator");
    ResourceKey<JukeboxSong> CREATOR_MUSIC_BOX = create("creator_music_box");

    private static ResourceKey<JukeboxSong> create(String s) {
        return ResourceKey.create(Registries.JUKEBOX_SONG, MinecraftKey.withDefaultNamespace(s));
    }

    private static void register(BootstrapContext<JukeboxSong> bootstrapcontext, ResourceKey<JukeboxSong> resourcekey, Holder.c<SoundEffect> holder_c, int i, int j) {
        bootstrapcontext.register(resourcekey, new JukeboxSong(holder_c, IChatBaseComponent.translatable(SystemUtils.makeDescriptionId("jukebox_song", resourcekey.location())), (float) i, j));
    }

    static void bootstrap(BootstrapContext<JukeboxSong> bootstrapcontext) {
        register(bootstrapcontext, JukeboxSongs.THIRTEEN, SoundEffects.MUSIC_DISC_13, 178, 1);
        register(bootstrapcontext, JukeboxSongs.CAT, SoundEffects.MUSIC_DISC_CAT, 185, 2);
        register(bootstrapcontext, JukeboxSongs.BLOCKS, SoundEffects.MUSIC_DISC_BLOCKS, 345, 3);
        register(bootstrapcontext, JukeboxSongs.CHIRP, SoundEffects.MUSIC_DISC_CHIRP, 185, 4);
        register(bootstrapcontext, JukeboxSongs.FAR, SoundEffects.MUSIC_DISC_FAR, 174, 5);
        register(bootstrapcontext, JukeboxSongs.MALL, SoundEffects.MUSIC_DISC_MALL, 197, 6);
        register(bootstrapcontext, JukeboxSongs.MELLOHI, SoundEffects.MUSIC_DISC_MELLOHI, 96, 7);
        register(bootstrapcontext, JukeboxSongs.STAL, SoundEffects.MUSIC_DISC_STAL, 150, 8);
        register(bootstrapcontext, JukeboxSongs.STRAD, SoundEffects.MUSIC_DISC_STRAD, 188, 9);
        register(bootstrapcontext, JukeboxSongs.WARD, SoundEffects.MUSIC_DISC_WARD, 251, 10);
        register(bootstrapcontext, JukeboxSongs.ELEVEN, SoundEffects.MUSIC_DISC_11, 71, 11);
        register(bootstrapcontext, JukeboxSongs.WAIT, SoundEffects.MUSIC_DISC_WAIT, 238, 12);
        register(bootstrapcontext, JukeboxSongs.PIGSTEP, SoundEffects.MUSIC_DISC_PIGSTEP, 149, 13);
        register(bootstrapcontext, JukeboxSongs.OTHERSIDE, SoundEffects.MUSIC_DISC_OTHERSIDE, 195, 14);
        register(bootstrapcontext, JukeboxSongs.FIVE, SoundEffects.MUSIC_DISC_5, 178, 15);
        register(bootstrapcontext, JukeboxSongs.RELIC, SoundEffects.MUSIC_DISC_RELIC, 218, 14);
        register(bootstrapcontext, JukeboxSongs.PRECIPICE, SoundEffects.MUSIC_DISC_PRECIPICE, 299, 13);
        register(bootstrapcontext, JukeboxSongs.CREATOR, SoundEffects.MUSIC_DISC_CREATOR, 176, 12);
        register(bootstrapcontext, JukeboxSongs.CREATOR_MUSIC_BOX, SoundEffects.MUSIC_DISC_CREATOR_MUSIC_BOX, 73, 11);
    }
}
