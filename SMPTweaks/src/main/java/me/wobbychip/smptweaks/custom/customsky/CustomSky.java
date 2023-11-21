package me.wobbychip.smptweaks.custom.customsky;

import me.wobbychip.smptweaks.Main;
import me.wobbychip.smptweaks.custom.customsky.commands.Commands;
import me.wobbychip.smptweaks.tweaks.CustomTweak;

public class CustomSky extends CustomTweak {
	public static CustomTweak tweak;
	public Commands comands;

	public CustomSky() {
		super(CustomSky.class, false, true);
		this.comands = new Commands(this, "csky");
		this.setDescription("Sets custom sky for world.");
	}

	public void onEnable() {
		this.setCommand(this.comands);
		new ProtocolEvents(Main.plugin);
		CustomSky.tweak = this;
	}
}
