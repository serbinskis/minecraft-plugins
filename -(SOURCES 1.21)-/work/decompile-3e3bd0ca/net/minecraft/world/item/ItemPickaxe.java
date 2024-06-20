package net.minecraft.world.item;

import net.minecraft.tags.TagsBlock;

public class ItemPickaxe extends ItemTool {

    public ItemPickaxe(ToolMaterial toolmaterial, Item.Info item_info) {
        super(toolmaterial, TagsBlock.MINEABLE_WITH_PICKAXE, item_info);
    }
}
