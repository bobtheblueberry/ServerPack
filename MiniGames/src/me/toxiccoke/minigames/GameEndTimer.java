package me.toxiccoke.minigames;

import org.bukkit.Bukkit;

public class GameEndTimer implements Runnable{

	boolean canceled;
	MiniGameWorld world;
	int minutes;
	int countdown;
	public GameEndTimer(MiniGameWorld w, int minutes) {
		this.world = w;
		this.minutes = minutes;
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
}