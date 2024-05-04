package me.wobbychip.smptweaks.library.tinyprotocol;

public enum PacketType {
    UNKNOW,
    BUNDLE,
    COMMANDS,
    CONTAINER_SET_CONTENT,
    CONTAINER_SET_SLOT,
    ENTITY_EVENT,
    GAME_EVENT,
    INITIALIZE_BORDER,
    LEVEL_CHUNK_WITH_LIGHT,
    MOVE_ENTITY_POS,
    MOVE_ENTITY_POS_ROT,
    PLAYER_INFO_UPDATE,
    PLAYER_POSITION,
    REMOVE_ENTITIES,
    RESOURCE_PACK_PUSH,
    ROTATE_HEAD,
    SET_CHUNK_CACHE_CENTER,
    SET_CHUNK_CACHE_RADIUS,
    SET_DEFAULT_SPAWN_POSITION,
    SET_ENTITY_DATA,
    SET_ENTITY_MOTION,
    SET_EXPERIENCE,
    SET_HEALTH,
    SET_SIMULATION_DISTANCE,
    SET_TIME,
    SOUND,
    SYSTEM_CHAT,
    TELEPORT_ENTITY,
    TICKING_STATE,
    TICKING_STEP,
    UPDATE_ADVANCEMENTS,
    UPDATE_ATTRIBUTES;

    public enum Flow { CLIENTBOUND, SERVERBOUND }

    public static PacketType getType(String id) {
        try {
            return PacketType.valueOf(id.replace("minecraft:", "").toUpperCase());
        } catch (Exception ex) {
            return UNKNOW;
        }
    }
}
