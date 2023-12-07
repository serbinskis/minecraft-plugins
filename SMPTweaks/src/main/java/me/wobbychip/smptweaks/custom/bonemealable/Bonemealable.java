package me.wobbychip.smptweaks.custom.bonemealable;

import me.wobbychip.smptweaks.nms.CustomSugarCaneBlock;
import me.wobbychip.smptweaks.tweaks.CustomTweak;

public class Bonemealable extends CustomTweak {
	public Bonemealable() {
		super(Bonemealable.class, false, false);
		this.setDescription("");
		this.setStartup(true);
	}

	public void onEnable() {
		CustomSugarCaneBlock.register();
	}
}
