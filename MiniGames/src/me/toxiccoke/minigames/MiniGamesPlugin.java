package me.toxiccoke.minigames;

import java.util.LinkedList;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class MiniGamesPlugin extends JavaPlugin implements Listener {
	public static MiniGamesPlugin	plugin;

	public void onEnable() {
		plugin = this;
		GameLobby l = new GameLobby();
		getServer().getPluginManager().registerEvents(l, this);
		getServer().getPluginManager().registerEvents(new Particles(), this);
		getServer().getPluginManager().registerEvents(new GameEventHandler(), this);

		GameCommands c = new GameCommands();
		getCommand("madmin").setExecutor(c);
		getCommand("leave").setExecutor(c);
		Partys p = new Partys();
		getCommand("party").setExecutor(p);
	}
	
	public void onDisable() {
		for (GameWorld<?> m : GameLobby.lobby.games) {
			@SuppressWarnings("unchecked")
			LinkedList<? extends GamePlayer> cloned = (LinkedList<? extends GamePlayer>) m.getPlayers().clone();
			for (GamePlayer gp : cloned)
					m.notifyQuitGame(gp);	
		}
	}

}
