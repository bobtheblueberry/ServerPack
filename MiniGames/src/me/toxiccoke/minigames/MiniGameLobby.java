package me.toxiccoke.minigames;

import java.util.ArrayList;

import me.toxiccoke.minigames.impl.BomberGameWorld;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class MiniGameLobby implements Runnable, Listener {

	public ArrayList<MiniGameWorld>	games;
	public static MiniGameLobby		lobby;

	public MiniGameLobby() {
		games = new ArrayList<MiniGameWorld>();
		lobby = this;

		games.add(new BomberGameWorld("Greenland"));

		games.add(new BomberGameWorld("Amazon"));
		// 20 ticks per second
		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(MiniGamesPlugin.plugin, this, 0L, 2L);
	}

	public void updateSigns() {
		for (MiniGameWorld w : games) {
			Sign s = w.getSign();
			if (s == null) continue;
			if (w.isFull())
				s.setLine(0, ChatColor.BLUE + "[Full]");
			else
			s.setLine(0, (w.isJoinable()) ? ChatColor.GREEN + "[Join]" : ChatColor.RED + "[NotJoinable]");
			s.setLine(1, ChatColor.DARK_GRAY + w.getGameName());
			s.setLine(2, ChatColor.DARK_GRAY + "" + w.getPlayerCount() + "/" + w.getMaxPlayers());
			s.setLine(3, ChatColor.DARK_GRAY + w.getWorldName());
			s.update();
		}
	}

	@Override
	public void run() {
		updateSigns();
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = (Player) event.getPlayer();
		Block b = event.getClickedBlock();
		if (b == null || b.getState() == null) return;
		if (!(b.getState() instanceof Sign)) return;
		Sign s = (Sign) b.getState();
		MiniGameWorld game = null;
		for (MiniGameWorld w : games)
			if (w.signLocation != null && w.signLocation.equals(s.getLocation())) {
				game = w;
				break;
			}
		if (game == null) return;
		event.setCancelled(true);
		if (!game.isJoinable()) {
			player.sendMessage(ChatColor.GOLD + "That minigame is unavailable.");
			return;
		}
		if (game.join(player)) ;// player.sendMessage(ChatColor.GOLD +
								// "Joining " + game.getGameName());
		else player.sendMessage(ChatColor.GOLD + "Can't join " + game.getGameName());

	}
}
