package me.toxiccoke.minigames.payload;

import me.toxiccoke.minigames.MiniGamesPlugin;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class SetupTimer extends BukkitRunnable {

	PayloadGame	game;
	float		setupTime;
	float		count;

	public SetupTimer(PayloadGame game, int setuptime) {
		this.game = game;
		this.setupTime = setuptime;
		this.count = setuptime;
		runTaskTimer(MiniGamesPlugin.plugin, 0, 20);
	}

	private void update() {
		for (PayloadPlayer pl : game.players) {
			Player p = pl.getPlayer();
			p.setLevel((int) count);
			if (count == 0)
				p.setExp(0);
			else p.setExp(count / setupTime);
		}
	}

	@Override
	public void run() {
		if (!game.isStarted()) {
			cancel();
			return;
		}
		update();
		if (count == 0)
			game.setupDone();
		count--;
		if (count < 0)
			cancel();
	}
}
