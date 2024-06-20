package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.Particles;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.GeneratorAccess;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3D;

public class JukeboxSongPlayer {

    public static final int PLAY_EVENT_INTERVAL_TICKS = 20;
    private long ticksSinceSongStarted;
    @Nullable
    public Holder<JukeboxSong> song;
    private final BlockPosition blockPos;
    private final JukeboxSongPlayer.a onSongChanged;

    public JukeboxSongPlayer(JukeboxSongPlayer.a jukeboxsongplayer_a, BlockPosition blockposition) {
        this.onSongChanged = jukeboxsongplayer_a;
        this.blockPos = blockposition;
    }

    public boolean isPlaying() {
        return this.song != null;
    }

    @Nullable
    public JukeboxSong getSong() {
        return this.song == null ? null : (JukeboxSong) this.song.value();
    }

    public long getTicksSinceSongStarted() {
        return this.ticksSinceSongStarted;
    }

    public void setSongWithoutPlaying(Holder<JukeboxSong> holder, long i) {
        if (!((JukeboxSong) holder.value()).hasFinished(i)) {
            this.song = holder;
            this.ticksSinceSongStarted = i;
        }
    }

    public void play(GeneratorAccess generatoraccess, Holder<JukeboxSong> holder) {
        this.song = holder;
        this.ticksSinceSongStarted = 0L;
        int i = generatoraccess.registryAccess().registryOrThrow(Registries.JUKEBOX_SONG).getId((JukeboxSong) this.song.value());

        generatoraccess.levelEvent((EntityHuman) null, 1010, this.blockPos, i);
        this.onSongChanged.notifyChange();
    }

    public void stop(GeneratorAccess generatoraccess, @Nullable IBlockData iblockdata) {
        if (this.song != null) {
            this.song = null;
            this.ticksSinceSongStarted = 0L;
            generatoraccess.gameEvent((Holder) GameEvent.JUKEBOX_STOP_PLAY, this.blockPos, GameEvent.a.of(iblockdata));
            generatoraccess.levelEvent(1011, this.blockPos, 0);
            this.onSongChanged.notifyChange();
        }
    }

    public void tick(GeneratorAccess generatoraccess, @Nullable IBlockData iblockdata) {
        if (this.song != null) {
            if (((JukeboxSong) this.song.value()).hasFinished(this.ticksSinceSongStarted)) {
                this.stop(generatoraccess, iblockdata);
            } else {
                if (this.shouldEmitJukeboxPlayingEvent()) {
                    generatoraccess.gameEvent((Holder) GameEvent.JUKEBOX_PLAY, this.blockPos, GameEvent.a.of(iblockdata));
                    spawnMusicParticles(generatoraccess, this.blockPos);
                }

                ++this.ticksSinceSongStarted;
            }
        }
    }

    private boolean shouldEmitJukeboxPlayingEvent() {
        return this.ticksSinceSongStarted % 20L == 0L;
    }

    private static void spawnMusicParticles(GeneratorAccess generatoraccess, BlockPosition blockposition) {
        if (generatoraccess instanceof WorldServer worldserver) {
            Vec3D vec3d = Vec3D.atBottomCenterOf(blockposition).add(0.0D, 1.2000000476837158D, 0.0D);
            float f = (float) generatoraccess.getRandom().nextInt(4) / 24.0F;

            worldserver.sendParticles(Particles.NOTE, vec3d.x(), vec3d.y(), vec3d.z(), 0, (double) f, 0.0D, 0.0D, 1.0D);
        }

    }

    @FunctionalInterface
    public interface a {

        void notifyChange();
    }
}
