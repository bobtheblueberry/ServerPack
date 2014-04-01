package me.toxiccoke.minigames.impl;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import me.toxiccoke.minigames.MiniGameWorld;

public class BomberGameWorld extends MiniGameWorld {

	public BomberGameWorld(BomberGame g, String worldName) {
		super(g, worldName);
		load();
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
		return !broken;
	}

	@Override
	public boolean join(Player p) {
		if (broken) return false;

		return false;
	}

}
