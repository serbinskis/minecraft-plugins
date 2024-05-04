package net.minecraft.server.network;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ClientInformation;

public record CommonListenerCookie(GameProfile gameProfile, int latency, ClientInformation clientInformation, boolean transferred) {

    public static CommonListenerCookie createInitial(GameProfile gameprofile, boolean flag) {
        return new CommonListenerCookie(gameprofile, 0, ClientInformation.createDefault(), flag);
    }
}
