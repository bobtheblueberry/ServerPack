package me.toxiccoke.minigames.payload;

import me.toxiccoke.minigames.MiniGamesPlugin;

import org.bukkit.scheduler.BukkitRunnable;

public class WeaponTimeout {

	private int				delay, maxlevel, level;
	private PayloadPlayer	player;
	private Task			task;

	public WeaponTimeout(PayloadPlayer player, int ticks, int maxlevel) {
		this.delay = ticks;
		this.maxlevel = maxlevel;
		this.player = player;
		doRun();
	}

	private void doRun() {
		task = new Task();
		task.runTaskLater(MiniGamesPlugin.plugin, delay);
	}

	public void fire() {
		if (level < 1)
			level = 0;
		else level--;
		// setup timer changes xp level
		if (player.game.setup == null || player.game.setup.count < 1)
			player.getPlayer().setLevel(level);
		if (task != null)
			task.cancel();
		doRun();
	}

	public boolean canFire() {
		return level > 0;
	}

	private class Task extends BukkitRunnable {
		@Override
		public void run() {
			level++;
			// setup timer changes xp level
			if (player.game.setup == null || player.game.setup.count < 1)
				player.getPlayer().setLevel(level);
			if (level < maxlevel)
				doRun();
		}
	}
}
