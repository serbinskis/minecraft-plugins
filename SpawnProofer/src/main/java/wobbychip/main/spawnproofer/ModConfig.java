package wobbychip.main.spawnproofer;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "spawnproofer")
class ModConfig implements ConfigData {
    @ConfigEntry.Gui.Tooltip()
    public int reachDistance = 5;

    @ConfigEntry.Gui.Tooltip
    public int maxInteractionPerTick = 1;

    @ConfigEntry.Gui.Tooltip
    public int minLighting = 11;

    @ConfigEntry.Gui.Tooltip()
    public boolean replace = true;

    @ConfigEntry.Gui.Tooltip()
    public boolean stack = false;
}
