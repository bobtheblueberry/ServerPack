package me.toxiccoke.minigames;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public abstract class MiniGamePlayer {

	protected String player;
	
	public MiniGamePlayer(Player p) {
		this.player = p.getName();
	}
	
	public abstract Material getFeetParticle();
	
	public Player getPlayer() {
		return Bukkit.getPlayer(player);
	}
}
