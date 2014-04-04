package me.toxiccoke.minigames;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class MiniGamesPlugin extends JavaPlugin implements Listener {
	public static MiniGamesPlugin	plugin;

	public void onEnable() {
		plugin = this;
		MiniGameLobby l = new MiniGameLobby();
		getServer().getPluginManager().registerEvents(l, this);
		getServer().getPluginManager().registerEvents(new Particles(), this);
		getServer().getPluginManager().registerEvents(new MiniGameEventHandler(), this);

		MiniGameCommands c = new MiniGameCommands();
		getCommand("madmin").setExecutor(c);
		getCommand("leave").setExecutor(c);
	}
	
	public void onDisable() {
		for (MiniGameWorld m : MiniGameLobby.lobby.games)
			for (MiniGamePlayer gp : m.getPlayers())
					m.notifyQuitGame(gp);
				
	
	}

}
