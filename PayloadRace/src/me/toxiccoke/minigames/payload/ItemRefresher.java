package me.toxiccoke.minigames.payload;

import me.toxiccoke.minigames.MiniGamesPlugin;

import org.bukkit.scheduler.BukkitRunnable;

public class ItemRefresher extends BukkitRunnable {

	PayloadGame	game;

	public ItemRefresher(PayloadGame game) {
		this.game = game;
		runTaskTimer(MiniGamesPlugin.plugin, 6000L, 6000L);// five minutes
	}

	@Override
	public void run() {
		for (ItemPack p : game.ammoitems)
			if (!p.respawning)
				p.respawn();
		for (ItemPack p : game.healthitems)
			if (!p.respawning)
				p.respawn();
	}

}
