package me.toxiccoke.minigames;

import java.util.ArrayList;

import me.toxiccoke.minigames.impl.BomberGame;
import me.toxiccoke.minigames.impl.BomberGameWorld;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

public class MiniGameLobby implements Runnable {

	public ArrayList<MiniGameWorld>	games;
	public static MiniGameLobby		lobby;

	public MiniGameLobby() {
		games = new ArrayList<MiniGameWorld>();
		lobby = this;

		games.add(new BomberGameWorld(new BomberGame(), "Greenland"));

		games.add(new BomberGameWorld(new BomberGame(), "Amazon"));
		// 20 ticks per second
		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(MiniGamesPlugin.plugin, this, 0L, 2L);
	}

	public void updateSigns() {
		for (MiniGameWorld w : games) {
			Sign s = w.getSign();
			if (s == null) continue;
			s.setLine(0, colorize((w.isJoinable()) ? "[Join]" : "[Not Joinable]"));
			s.setLine(1, colorize(w.getGameName()));
			s.setLine(2, colorize(((int) (Math.random() * w.getMaxPlayers())) + "/" + w.getMaxPlayers()));
			s.setLine(3, colorize(w.getWorldName()));
			s.update();
		}
	}

	private String colorize(String s) {
		if (s == null) return null;
		if (s.length() > 14) return s;// no room for colors :(
		int colorCount = (16 - s.length()) / 2;
		String temp = randomColor() + "" + s.charAt(0);
		for (int i = 1; i < s.length(); i++) {
			if (((s.length()/colorCount) % i) == 0)
				temp = temp + randomColor();
			temp = temp + s.charAt(i);
		}
		return temp;
	}

	private ChatColor randomColor() {
		int r = (int) (Math.random() * 16);
		if (r == 0) return ChatColor.BLACK;
		if (r == 1) return ChatColor.DARK_BLUE;
		if (r == 2) return ChatColor.DARK_GREEN;
		if (r == 3) return ChatColor.DARK_AQUA;
		if (r == 4) return ChatColor.DARK_RED;
		if (r == 5) return ChatColor.DARK_PURPLE;
		if (r == 6) return ChatColor.GOLD;
		if (r == 7) return ChatColor.GRAY;
		if (r == 8) return ChatColor.DARK_GRAY;
		if (r == 9) return ChatColor.BLUE;
		if (r == 10) return ChatColor.GREEN;
		if (r == 11) return ChatColor.AQUA;
		if (r == 12) return ChatColor.RED;
		if (r == 13) return ChatColor.LIGHT_PURPLE;
		if (r == 14) return ChatColor.YELLOW;
		if (r == 15) return ChatColor.WHITE;
		return ChatColor.WHITE;
	}

	@Override
	public void run() {
		updateSigns();
	}
}
