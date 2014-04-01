package me.toxiccoke.minigames;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class MiniGamesPlugin extends JavaPlugin implements Listener {
	public static MiniGamesPlugin	plugin;

	public void onEnable() {

		getServer().getPluginManager().registerEvents(this, this);
		plugin = this;
		MiniGameCommands c = new MiniGameCommands();
		getCommand("sign").setExecutor(c);
		
		new MiniGameLobby();
	}

}
