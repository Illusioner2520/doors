package net.illusioncraft.Doors;

import org.bukkit.plugin.java.JavaPlugin;

public class Doors extends JavaPlugin {

	public DoorsBot bot;
	@Override
	public void onEnable() {
		this.bot = new DoorsBot();
	}
	@Override
	public void onDisable() {
		bot.saveData();
	}
}
