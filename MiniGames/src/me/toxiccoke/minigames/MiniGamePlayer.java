package me.toxiccoke.minigames;

import org.bukkit.entity.Player;

public abstract class MiniGamePlayer {

	Player player;
	
	public MiniGamePlayer(Player p) {
		this.player = p;
	}
}
