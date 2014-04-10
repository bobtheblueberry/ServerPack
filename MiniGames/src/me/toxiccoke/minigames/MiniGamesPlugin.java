package me.toxiccoke.minigames;

import java.util.LinkedList;

import me.toxiccoke.minigames.payload.PayloadCommand;
import me.toxiccoke.minigames.payload.PayloadEventHandler;

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
		getServer().getPluginManager().registerEvents(new PayloadEventHandler(), this);

		GameCommands c = new GameCommands();
		getCommand("madmin").setExecutor(c);
		getCommand("leave").setExecutor(c);
		Partys p = new Partys();
		getCommand("party").setExecutor(p);
		PayloadCommand pc = new PayloadCommand();
		getCommand("padmin").setExecutor(pc);
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
