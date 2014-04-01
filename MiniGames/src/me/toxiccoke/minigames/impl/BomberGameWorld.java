package me.toxiccoke.minigames.impl;

import java.util.LinkedList;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import me.toxiccoke.minigames.MiniGameWorld;

public class BomberGameWorld extends MiniGameWorld {

	private LinkedList<BomberGamePlayer> players;
	
	public BomberGameWorld(BomberGame g, String worldName) {
		super(g, worldName);
		load();
		players = new LinkedList<BomberGamePlayer>();
	}

	private void load() {
		// YamlConfiguration yml =
		super.getLoadYML();
	}

	public void save() {
		YamlConfiguration yml = super.getSaveYML();
		// add other stuff here
		super.save(yml);
	}

	@Override
	public int getPlayerCount() {
		return 0;
	}

	@Override
	public boolean isJoinable() {
		return !broken && spawnLocations.size() > 0;
	}

	@Override
	public boolean join(Player p) {
		if (broken) return false;
		players.add(new BomberGamePlayer(p));
		p.teleport(spawnLocations.get((int)(Math.random()*2)));
		return true;
		
	}

}
