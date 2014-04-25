package me.toxiccoke.minigames.payload;

import me.toxiccoke.minigames.MiniGamesPlugin;

import org.bukkit.scheduler.BukkitRunnable;

public class TickUpdater extends BukkitRunnable {

	PayloadGame game;
	
	public TickUpdater(PayloadGame game) {
		this.game = game;
		runTaskTimer(MiniGamesPlugin.plugin, 1, 1);
	}

	@Override
	public void run() {
		for (Bullet b : game.bullets)
			b.update(game);
		for (PayloadPlayer p : game.getPlayers())
			p.tick();
	}

}
