package net.minecraft.world.item;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.stats.StatisticList;
import net.minecraft.world.EnumHand;
import net.minecraft.world.InteractionResultWrapper;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.crafting.CraftingManager;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.World;
import org.slf4j.Logger;

public class ItemKnowledgeBook extends Item {

    private static final Logger LOGGER = LogUtils.getLogger();

    public ItemKnowledgeBook(Item.Info item_info) {
        super(item_info);
    }

    @Override
    public InteractionResultWrapper<ItemStack> use(World world, EntityHuman entityhuman, EnumHand enumhand) {
        ItemStack itemstack = entityhuman.getItemInHand(enumhand);

        if (!entityhuman.hasInfiniteMaterials()) {
            entityhuman.setItemInHand(enumhand, ItemStack.EMPTY);
        }

        List<MinecraftKey> list = (List) itemstack.getOrDefault(DataComponents.RECIPES, List.of());

        if (list.isEmpty()) {
            return InteractionResultWrapper.fail(itemstack);
        } else {
            if (!world.isClientSide) {
                CraftingManager craftingmanager = world.getServer().getRecipeManager();
                List<RecipeHolder<?>> list1 = new ArrayList(list.size());
                Iterator iterator = list.iterator();

                while (iterator.hasNext()) {
                    MinecraftKey minecraftkey = (MinecraftKey) iterator.next();
                    Optional<RecipeHolder<?>> optional = craftingmanager.byKey(minecraftkey);

                    if (!optional.isPresent()) {
                        ItemKnowledgeBook.LOGGER.error("Invalid recipe: {}", minecraftkey);
                        return InteractionResultWrapper.fail(itemstack);
                    }

                    list1.add((RecipeHolder) optional.get());
                }

                entityhuman.awardRecipes(list1);
                entityhuman.awardStat(StatisticList.ITEM_USED.get(this));
            }

            return InteractionResultWrapper.sidedSuccess(itemstack, world.isClientSide());
        }
    }
}
