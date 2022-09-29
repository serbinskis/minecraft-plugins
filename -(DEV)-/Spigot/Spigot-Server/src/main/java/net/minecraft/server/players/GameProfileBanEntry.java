// mc-dev import
package net.minecraft.server.players;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.network.chat.IChatBaseComponent;

public class GameProfileBanEntry extends ExpirableListEntry<GameProfile> {

    public GameProfileBanEntry(GameProfile gameprofile) {
        this(gameprofile, (Date) null, (String) null, (Date) null, (String) null);
    }

    public GameProfileBanEntry(GameProfile gameprofile, @Nullable Date date, @Nullable String s, @Nullable Date date1, @Nullable String s1) {
        super(gameprofile, date, s, date1, s1);
    }

    public GameProfileBanEntry(JsonObject jsonobject) {
        super(createGameProfile(jsonobject), jsonobject);
    }

    @Override
    protected void serialize(JsonObject jsonobject) {
        if (this.getUser() != null) {
            jsonobject.addProperty("uuid", ((GameProfile) this.getUser()).getId() == null ? "" : ((GameProfile) this.getUser()).getId().toString());
            jsonobject.addProperty("name", ((GameProfile) this.getUser()).getName());
            super.serialize(jsonobject);
        }
    }

    @Override
    public IChatBaseComponent getDisplayName() {
        GameProfile gameprofile = (GameProfile) this.getUser();

        return IChatBaseComponent.literal(gameprofile.getName() != null ? gameprofile.getName() : Objects.toString(gameprofile.getId(), "(Unknown)"));
    }

    private static GameProfile createGameProfile(JsonObject jsonobject) {
        // Spigot start
        // this whole method has to be reworked to account for the fact Bukkit only accepts UUID bans and gives no way for usernames to be stored!
        UUID uuid = null;
        String name = null;
        if (jsonobject.has("uuid")) {
            String s = jsonobject.get("uuid").getAsString();

            try {
                uuid = UUID.fromString(s);
            } catch (Throwable throwable) {
            }

        }
        if ( jsonobject.has("name"))
        {
            name = jsonobject.get("name").getAsString();
        }
        if ( uuid != null || name != null )
        {
            return new GameProfile( uuid, name );
        } else {
            return null;
        }
        // Spigot End
    }
}
