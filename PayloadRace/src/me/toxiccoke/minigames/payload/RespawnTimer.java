package me.toxiccoke.minigames.payload;

import me.toxiccoke.minigames.MiniGamesPlugin;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class RespawnTimer extends BukkitRunnable {

	PayloadPlayer	player;
	float			respawnTime;
	float				count;

	public RespawnTimer(PayloadPlayer player, int respawntime) {
		this.player = player;
		this.respawnTime = respawntime;
		this.count = respawntime;
		runTaskTimer(MiniGamesPlugin.plugin, 0, 20);
	}

	private void update() {
		Player p = player.getPlayer();
		p.setLevel((int)count);
		if (count == 0)
			p.setExp(0);
		else
			p.setExp(count/respawnTime);
	}

	@Override
	public void run() {
		if (!player.isInGame()) {
			cancel();
			return;
		}
		if (player.team.lost) {
			Player p = player.getPlayer();
			p.setExp(0);
			p.setLevel(0);
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
