package net.minecraft.world.entity;

public enum EnumMobSpawn {

    NATURAL, CHUNK_GENERATION, SPAWNER, STRUCTURE, BREEDING, MOB_SUMMONED, JOCKEY, EVENT, CONVERSION, REINFORCEMENT, TRIGGERED, BUCKET, SPAWN_EGG, COMMAND, DISPENSER, PATROL, TRIAL_SPAWNER;

    private EnumMobSpawn() {}

    public static boolean isSpawner(EnumMobSpawn enummobspawn) {
        return enummobspawn == EnumMobSpawn.SPAWNER || enummobspawn == EnumMobSpawn.TRIAL_SPAWNER;
    }

    public static boolean ignoresLightRequirements(EnumMobSpawn enummobspawn) {
        return enummobspawn == EnumMobSpawn.TRIAL_SPAWNER;
    }
}
