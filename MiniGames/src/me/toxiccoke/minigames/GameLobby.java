package me.toxiccoke.minigames;

import java.util.ArrayList;

import me.toxiccoke.minigames.Partys.Party;
import me.toxiccoke.minigames.bomber.BomberGame;
import me.toxiccoke.minigames.payload.PayloadGame;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class GameLobby implements Runnable, Listener {

	public ArrayList<GameWorld>	games;
	public static GameLobby		lobby;

	public GameLobby() {
		games = new ArrayList<GameWorld>(2);
		lobby = this;

		games.add(new BomberGame("Greenland"));
		games.add(new BomberGame("Amazon"));
		games.add(new PayloadGame("Badwater"));
		// 20 ticks per second
		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(MiniGamesPlugin.plugin, this, 0L, 20L);
	}

	public void updateSigns() {
		for (GameWorld w : games) {
			Sign s = w.getSign();
			if (s == null)
				continue;
			String[] old = w.getSignText();
			String[] newText = new String[4];
			if (w.isFull())
				newText[0] = ChatColor.BLUE + "[Full]";
			else newText[0] = (w.isJoinable()) ? ChatColor.GREEN + "[Join]" : ChatColor.RED + "[NotJoinable]";
			newText[1] = ChatColor.DARK_GRAY + w.getGameName();
			newText[2] = ChatColor.DARK_GRAY + "" + w.getPlayerCount() + "/" + w.getMaxPlayers();
			newText[3] = ChatColor.DARK_GRAY + w.getWorldName();

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
		Player player = (Player) event.getPlayer();
		Block b = event.getClickedBlock();
		if (b == null || b.getState() == null)
			return;
		if (!(b.getState() instanceof Sign))
			return;
		Sign s = (Sign) b.getState();
		GameWorld game = null;
		for (GameWorld w : games)
			if (w.signLocation != null && w.signLocation.equals(s.getLocation())) {
				game = w;
				break;
			}
		if (game == null)
			return;
		event.setCancelled(true);
		if (!game.isJoinable()) {
			player.sendMessage(ChatColor.GOLD + "That minigame is unavailable.");
			return;
		}
		if (isInGame(player)) {
			player.sendMessage(ChatColor.GOLD + "You are in a game!");
			return;
		}
		// Parties!!
		if (Partys.isInParty(player)) {
			Party p = Partys.getParty(player);
			if (!Partys.isPartyOwner(player)) {
				player.sendMessage(ChatColor.GOLD + "Your party owner [" + p.getOwner().getDisplayName()
						+ ChatColor.GOLD + "] must decide what game for you to join."
						+ " Do /party leave to leave your party.");
				return;
			}
			// is there enough room?
			if ((game.getPlayerCount() + p.players.size() + 1) > game.getMaxPlayers()) {
				player.sendMessage("There is not enough room in this minigame for your party");
				return;
			}
			game.join(p.getOwner());
			for (Player plr : p.getPlayers())
				if (!isInGame(plr))
					game.join(plr);
		} else {
			if (game.join(player))
				;
			else player.sendMessage(ChatColor.GOLD + "Can't join " + game.getGameName());
		}

	}

	public boolean isInGame(Player p) {
		for (GameWorld w : games)
			for (GamePlayer gp : w.getPlayers())
				if (p.getName().equals(gp.player))
					return true;
		return false;
	}

	public boolean isInGame(Player p, GameWorld w) {
		for (GamePlayer gp : w.getPlayers())
			if (p.getName().equals(gp.player))
				return true;
		return false;
	}
}
