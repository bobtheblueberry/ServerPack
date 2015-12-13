package me.toxiccoke.minigames;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import me.toxiccoke.minigames.bomber.BomberGame;

public class GameLobby implements Runnable, Listener {

	public ArrayList<GameArena<? extends GamePlayer>> games;
	public static GameLobby lobby;

	public GameLobby() {
		games = new ArrayList<GameArena<? extends GamePlayer>>(3);
		lobby = this;
		load();
		// 20 ticks per second
		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(MiniGamesPlugin.plugin, this, 0L, 20L);
	}

	private File getSaveFile() {
		File parent = MiniGamesPlugin.plugin.getDataFolder();
		if (!parent.exists())
			parent.mkdirs();
		return new File(parent, "Worlds.ini");
	}

	private void load() {
		File data = getSaveFile();
		if (!data.exists()) {
			// make a new file
			save();
		} else {
			// load data
			try {
				Scanner sc = new Scanner(new BufferedReader(new FileReader(data)));
				while (sc.hasNext()) {
					String game = sc.next();
					if (sc.hasNext()) {
						String arena = sc.next();
						if (game.equalsIgnoreCase("Bomber"))
							games.add(new BomberGame(arena));
					}
				}
				sc.close();
			} catch (FileNotFoundException exc) {
				System.err.println(exc.getMessage());
				exc.printStackTrace();
			}
		}
	}

	public void createNewArena(String name) {
		games.add(new BomberGame(name));
		save();
	}

	private void save() {
		try {
			PrintWriter p = new PrintWriter(new BufferedWriter(new FileWriter(getSaveFile())));
			for (GameArena<? extends GamePlayer> ga : games) {
				p.println(ga.gameName + " " + ga.arenaName);
			}
			p.println();
			p.close();
		} catch (IOException exc) {
			System.err.println(exc.getMessage());
			exc.printStackTrace();
		}

	}
	
	public boolean removeArena(String name) {
		GameArena<? extends GamePlayer> ga = getArena(name);
		if (ga == null) return false;
		boolean t = games.remove(ga);
		save();
		return t;
	}

	public GameArena<? extends GamePlayer> getArena(String name) {
		for (GameArena<? extends GamePlayer> g : games)
			if (g.getArenaName().equalsIgnoreCase(name))
				return g;
		return null;
	}

	public void updateSigns() {
		for (GameArena<?> w : games) {
			Sign s = w.getSign();
			if (s == null)
				continue;
			String[] old = w.getSignText();
			String[] newText = new String[4];
			if (w.isFull())
				newText[0] = ChatColor.BLUE + "[Full]";
			else
				newText[0] = (w.isJoinable()) ? ChatColor.GREEN + "[Join]" : ChatColor.RED + "[NotJoinable]";
			newText[1] = ChatColor.DARK_GRAY + w.getGameName();
			newText[2] = ChatColor.DARK_GRAY + "" + w.getPlayerCount() + "/" + w.getMaxPlayers();
			newText[3] = ChatColor.DARK_GRAY + w.getArenaName();

			boolean changed = false;
			if (old != null)
				for (int i = 0; i < 4; i++)
					if (!newText[i].equals(old[i])) {
						changed = true;
						break;
					}

			if (!changed && old != null)
				continue;
			for (int i = 0; i < 4; i++)
				s.setLine(i, newText[i]);
			s.update();
			w.setSignText(newText);
		}
	}

	@Override
	public void run() {
		updateSigns();
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Block b = event.getClickedBlock();
		if (b == null || b.getState() == null)
			return;
		if (!(b.getState() instanceof Sign))
			return;
		Sign s = (Sign) b.getState();
		GameArena<?> game = null;
		for (GameArena<?> w : games)
			if (w.signLocation != null && w.signLocation.equals(s.getLocation())) {
				game = w;
				break;
			}
		if (game == null)
			return;
		event.setCancelled(true);

		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			player.sendMessage(ChatColor.GOLD + "Use the other mouse button");
			return;
		}
		if (!game.isJoinable()) {
			player.sendMessage(ChatColor.GOLD + "That minigame is unavailable.");
			return;
		}
		if (isInGame(player)) {
			player.sendMessage(ChatColor.GOLD + "You are in a game!");
			return;
		}

		if (game.join(player))
			;
		else
			player.sendMessage(ChatColor.GOLD + "Can't join " + game.getGameName());

	}

	public boolean isInGame(Player p) {
		for (GameArena<?> w : games)
			for (GamePlayer gp : w.getPlayers())
				if (p.equals(gp.player))
					return true;
		return false;
	}

	public boolean isInGame(Player p, GameArena<?> w) {
		for (GamePlayer gp : w.getPlayers())
			if (p.equals(gp.player))
				return true;
		return false;
	}
}
