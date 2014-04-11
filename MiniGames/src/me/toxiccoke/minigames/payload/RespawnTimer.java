package me.toxiccoke.minigames.payload;

import me.toxiccoke.minigames.MiniGamesPlugin;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class RespawnTimer extends BukkitRunnable {

	PayloadPlayer	player;
	double			respawnTime;
	double				count;

	public RespawnTimer(PayloadPlayer player, int respawntime) {
		this.player = player;
		this.respawnTime = respawntime;
		this.count = respawntime;
		runTaskTimer(MiniGamesPlugin.plugin, 1, 1);
	}

	private void update() {
		Player p = player.getPlayer();
		p.setLevel((int)count);
		if (count == 0)
			p.setExp(0);
		else
			p.setExp((int)(count/respawnTime));
	}

	@Override
	public void run() {
		if (!player.isInGame()) {
			cancel();
			return;
		}
		update();
		if (count == 0)
			player.respawn();
		count--;
		if (count < 0)
			cancel();
	}
}
