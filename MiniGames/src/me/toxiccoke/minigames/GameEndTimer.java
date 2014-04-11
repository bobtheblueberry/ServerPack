package me.toxiccoke.minigames;

import org.bukkit.Bukkit;

public class GameEndTimer implements Runnable{

	boolean canceled;
	GameWorld<?> world;
	int countdown;
	public GameEndTimer(GameWorld<?> w, int minutes) {
		this.world = w;
		this.countdown = minutes;
		schedule();
		world.endUpdate(countdown);
	}
	public void run() {
		if (canceled) return;
		countdown--;
		world.endUpdate(countdown);
		if (countdown > 0) {
			schedule();
		}
	}

	private void schedule() {
		Bukkit.getScheduler().scheduleSyncDelayedTask(MiniGamesPlugin.plugin, this, 1200L);// 1 minute
	}
	public void cancelTimer() {
		canceled = true;
	}
	
	public int getCountdown() {
		return countdown;
	}
}
