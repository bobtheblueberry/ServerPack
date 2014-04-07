package me.toxiccoke.minigames;

import java.util.LinkedList;

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
		Partys p = new Partys();
		getCommand("party").setExecutor(p);
	}
	
	public void onDisable() {
		for (MiniGameWorld m : MiniGameLobby.lobby.games) {
			@SuppressWarnings("unchecked")
			LinkedList<? extends MiniGamePlayer> cloned = (LinkedList<? extends MiniGamePlayer>) m.getPlayers().clone();
			for (MiniGamePlayer gp : cloned)
					m.notifyQuitGame(gp);	
		}
	}

}
