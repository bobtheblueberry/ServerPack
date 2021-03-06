package me.toxiccoke.minigames.bomber;

import me.toxiccoke.minigames.MiniGamesPlugin;

import org.bukkit.Bukkit;

public class BomberLobbyTimer implements Runnable {

	BomberGame	world;
	int				countdown	= 5;
	boolean canceled;
	public BomberLobbyTimer(BomberGame w) {
		this.world = w;
		world.lobbyUpdate(countdown);
		schedule();
	}

	public void run() {
		if (canceled) return;
		countdown--;
		world.lobbyUpdate(countdown);
		if (countdown > 0) {
			schedule();
		}
	}

	private void schedule() {
		Bukkit.getScheduler().scheduleSyncDelayedTask(MiniGamesPlugin.plugin, this, 20L);
																							
	}
	
	public void cancelTimer() {
		canceled = true;
	}
}
