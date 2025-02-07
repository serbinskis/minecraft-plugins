package me.serbinskis.smptweaks.custom.betterfurnaces;

import me.serbinskis.smptweaks.custom.betterfurnaces.blocks.furnaces.*;
import me.serbinskis.smptweaks.library.customblocks.CustomBlocks;
import me.serbinskis.smptweaks.tweaks.CustomTweak;

public class BetterFurnaces extends CustomTweak {
    public static CustomTweak tweak;

    public BetterFurnaces() {
        super(BetterFurnaces.class, false, false);
        this.setGameRule("doBetterFurnaces", true, false);
        this.setDescription("Adds more and faster furnaces.");
        BetterFurnaces.tweak = this;
    }

    public void onEnable() {
        CustomBlocks.registerBlock(new IronFurnaceBlock());
        CustomBlocks.registerBlock(new CopperFurnaceBlock());
        CustomBlocks.registerBlock(new GoldenFurnaceBlock());
        CustomBlocks.registerBlock(new DiamondFurnaceBlock());
        CustomBlocks.registerBlock(new EmeraldFurnaceBlock());
        CustomBlocks.registerBlock(new NetheriteFurnaceBlock());
        CustomBlocks.registerBlock(new ExtremeFurnaceBlock());
    }
}
