package me.toxiccoke.minigames.payload;

import me.toxiccoke.minigames.MiniGamesPlugin;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class EndofGameTimer extends BukkitRunnable {

	float		seconds, countdown;
	PayloadGame	game;

	public EndofGameTimer(PayloadGame game, int seconds) {
		this.seconds = this.countdown = seconds*2;
		this.game = game;
		this.runTaskTimer(MiniGamesPlugin.plugin, 0, 10);// smoother update
	}

	@Override
	public void run() {
		float t = countdown / seconds;
		for (PayloadPlayer pp : game.getPlayers()) {
			if (pp.dead)
				continue;
			Player p = pp.getPlayer();
			if (countdown % 2 == 0) 
			p.setLevel((int) countdown/2);
			if (countdown == 0)
				p.setExp(0);
			else p.setExp(t);
			countdown-=0.5;
			if (countdown < 0) {
				this.cancel();
				game.actuallyEndGame();
			}
		}
	}

}
