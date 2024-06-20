package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.List;
import net.minecraft.world.level.EnumGamemode;

public record GameTypePredicate(List<EnumGamemode> types) {

    public static final GameTypePredicate ANY = of(EnumGamemode.values());
    public static final GameTypePredicate SURVIVAL_LIKE = of(EnumGamemode.SURVIVAL, EnumGamemode.ADVENTURE);
    public static final Codec<GameTypePredicate> CODEC = EnumGamemode.CODEC.listOf().xmap(GameTypePredicate::new, GameTypePredicate::types);

    public static GameTypePredicate of(EnumGamemode... aenumgamemode) {
        return new GameTypePredicate(Arrays.stream(aenumgamemode).toList());
    }

    public boolean matches(EnumGamemode enumgamemode) {
        return this.types.contains(enumgamemode);
    }
}
