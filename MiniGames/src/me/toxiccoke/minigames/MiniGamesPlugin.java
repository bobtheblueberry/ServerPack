package me.toxiccoke.minigames;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class MiniGamesPlugin extends JavaPlugin implements Listener {
	public static MiniGamesPlugin	plugin;

	public void onEnable() {
		plugin = this;
		MiniGameLobby l = new MiniGameLobby();
		getServer().getPluginManager().registerEvents(l, this);

		MiniGameCommands c = new MiniGameCommands();
		getCommand("sign").setExecutor(c);
		getCommand("madmin").setExecutor(c);

	}

}
