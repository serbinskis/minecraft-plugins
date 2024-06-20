package net.minecraft.world.level.storage.loot.parameters;

import net.minecraft.resources.MinecraftKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.phys.Vec3D;

public class LootContextParameters {

    public static final LootContextParameter<Entity> THIS_ENTITY = create("this_entity");
    public static final LootContextParameter<EntityHuman> LAST_DAMAGE_PLAYER = create("last_damage_player");
    public static final LootContextParameter<DamageSource> DAMAGE_SOURCE = create("damage_source");
    public static final LootContextParameter<Entity> ATTACKING_ENTITY = create("attacking_entity");
    public static final LootContextParameter<Entity> DIRECT_ATTACKING_ENTITY = create("direct_attacking_entity");
    public static final LootContextParameter<Vec3D> ORIGIN = create("origin");
    public static final LootContextParameter<IBlockData> BLOCK_STATE = create("block_state");
    public static final LootContextParameter<TileEntity> BLOCK_ENTITY = create("block_entity");
    public static final LootContextParameter<ItemStack> TOOL = create("tool");
    public static final LootContextParameter<Float> EXPLOSION_RADIUS = create("explosion_radius");
    public static final LootContextParameter<Integer> ENCHANTMENT_LEVEL = create("enchantment_level");
    public static final LootContextParameter<Boolean> ENCHANTMENT_ACTIVE = create("enchantment_active");

    public LootContextParameters() {}

    private static <T> LootContextParameter<T> create(String s) {
        return new LootContextParameter<>(MinecraftKey.withDefaultNamespace(s));
    }
}
