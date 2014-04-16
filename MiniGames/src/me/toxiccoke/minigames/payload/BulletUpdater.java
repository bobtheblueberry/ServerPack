package me.toxiccoke.minigames.payload;

import me.toxiccoke.minigames.MiniGamesPlugin;

import org.bukkit.scheduler.BukkitRunnable;

public class BulletUpdater extends BukkitRunnable {

	PayloadGame game;
	
	public BulletUpdater(PayloadGame game) {
		this.game = game;
		runTaskTimer(MiniGamesPlugin.plugin, 1, 1);
	}

	@Override
	public void run() {
		for (Bullet b : game.bullets)
			b.update(game);
	}

}
