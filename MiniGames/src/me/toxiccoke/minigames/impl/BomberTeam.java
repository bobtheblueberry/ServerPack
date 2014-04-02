package me.toxiccoke.minigames.impl;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class BomberTeam implements Listener {

	public enum TeamType {
		RED, BLUE
	}

	public TeamType	team;

	public BomberTeam(TeamType type) {
		this.team = type;
	}

}