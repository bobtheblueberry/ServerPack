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
		//getServer().getPluginManager().registerEvents(new PayloadEventHandler(), this);
		//getServer().getPluginManager().registerEvents(new ClassMenu(), this);
		

		GameCommands c = new GameCommands();
		getCommand("bomber").setExecutor(c);
		getCommand("bconfig").setExecutor(c);
		
		getCommand("leave").setExecutor(c);
		//PayloadCommand pc = new PayloadCommand();
		//getCommand("padmin").setExecutor(pc);
		//ClassMenu m = new ClassMenu();
		//getCommand("class").setExecutor(m);
	}
	
	public void onDisable() {
		for (GameArena<?> m : GameLobby.lobby.games) {
			@SuppressWarnings("unchecked")
			LinkedList<? extends GamePlayer> cloned = (LinkedList<? extends GamePlayer>) m.getPlayers().clone();
			for (GamePlayer gp : cloned)
					m.notifyQuitGame(gp);	
		}
	}

}
