package net.minecraft.advancements;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.minecraft.commands.CacheableFunction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameterSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParameters;

public record AdvancementRewards(int experience, List<ResourceKey<LootTable>> loot, List<MinecraftKey> recipes, Optional<CacheableFunction> function) {

    public static final Codec<AdvancementRewards> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(Codec.INT.optionalFieldOf("experience", 0).forGetter(AdvancementRewards::experience), ResourceKey.codec(Registries.LOOT_TABLE).listOf().optionalFieldOf("loot", List.of()).forGetter(AdvancementRewards::loot), MinecraftKey.CODEC.listOf().optionalFieldOf("recipes", List.of()).forGetter(AdvancementRewards::recipes), CacheableFunction.CODEC.optionalFieldOf("function").forGetter(AdvancementRewards::function)).apply(instance, AdvancementRewards::new);
    });
    public static final AdvancementRewards EMPTY = new AdvancementRewards(0, List.of(), List.of(), Optional.empty());

    public void grant(EntityPlayer entityplayer) {
        entityplayer.giveExperiencePoints(this.experience);
        LootParams lootparams = (new LootParams.a(entityplayer.serverLevel())).withParameter(LootContextParameters.THIS_ENTITY, entityplayer).withParameter(LootContextParameters.ORIGIN, entityplayer.position()).create(LootContextParameterSets.ADVANCEMENT_REWARD);
        boolean flag = false;
        Iterator iterator = this.loot.iterator();

        while (iterator.hasNext()) {
            ResourceKey<LootTable> resourcekey = (ResourceKey) iterator.next();
            ObjectListIterator objectlistiterator = entityplayer.server.reloadableRegistries().getLootTable(resourcekey).getRandomItems(lootparams).iterator();

            while (objectlistiterator.hasNext()) {
                ItemStack itemstack = (ItemStack) objectlistiterator.next();

                if (entityplayer.addItem(itemstack)) {
                    entityplayer.level().playSound((EntityHuman) null, entityplayer.getX(), entityplayer.getY(), entityplayer.getZ(), SoundEffects.ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, ((entityplayer.getRandom().nextFloat() - entityplayer.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
                    flag = true;
                } else {
                    EntityItem entityitem = entityplayer.drop(itemstack, false);

                    if (entityitem != null) {
                        entityitem.setNoPickUpDelay();
                        entityitem.setTarget(entityplayer.getUUID());
                    }
                }
            }
        }

        if (flag) {
            entityplayer.containerMenu.broadcastChanges();
        }

        if (!this.recipes.isEmpty()) {
            entityplayer.awardRecipesByKey(this.recipes);
        }

        MinecraftServer minecraftserver = entityplayer.server;

        this.function.flatMap((cacheablefunction) -> {
            return cacheablefunction.get(minecraftserver.getFunctions());
        }).ifPresent((commandfunction) -> {
            minecraftserver.getFunctions().execute(commandfunction, entityplayer.createCommandSourceStack().withSuppressedOutput().withPermission(2));
        });
    }

    public static class a {

        private int experience;
        private final Builder<ResourceKey<LootTable>> loot = ImmutableList.builder();
        private final Builder<MinecraftKey> recipes = ImmutableList.builder();
        private Optional<MinecraftKey> function = Optional.empty();

        public a() {}

        public static AdvancementRewards.a experience(int i) {
            return (new AdvancementRewards.a()).addExperience(i);
        }

        public AdvancementRewards.a addExperience(int i) {
            this.experience += i;
            return this;
        }

        public static AdvancementRewards.a loot(ResourceKey<LootTable> resourcekey) {
            return (new AdvancementRewards.a()).addLootTable(resourcekey);
        }

        public AdvancementRewards.a addLootTable(ResourceKey<LootTable> resourcekey) {
            this.loot.add(resourcekey);
            return this;
        }

        public static AdvancementRewards.a recipe(MinecraftKey minecraftkey) {
            return (new AdvancementRewards.a()).addRecipe(minecraftkey);
        }

        public AdvancementRewards.a addRecipe(MinecraftKey minecraftkey) {
            this.recipes.add(minecraftkey);
            return this;
        }

        public static AdvancementRewards.a function(MinecraftKey minecraftkey) {
            return (new AdvancementRewards.a()).runs(minecraftkey);
        }

        public AdvancementRewards.a runs(MinecraftKey minecraftkey) {
            this.function = Optional.of(minecraftkey);
            return this;
        }

        public AdvancementRewards build() {
            return new AdvancementRewards(this.experience, this.loot.build(), this.recipes.build(), this.function.map(CacheableFunction::new));
        }
    }
}
