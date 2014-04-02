package me.toxiccoke.minigames;

import org.bukkit.Material;
import org.bukkit.entity.Player;

public abstract class MiniGamePlayer {

	protected Player player;
	
	public MiniGamePlayer(Player p) {
		this.player = p;
	}
	
	public abstract Material getFeetParticle();
	
	public Player getPlayer() {
		return player;
	}
}
