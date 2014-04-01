package me.toxiccoke.minigames;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

public class MiniGameCommands implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "This command can only be run by a player.");
			return true;
		}
		Player p = (Player) sender;
		String cmd = command.getName().toLowerCase();
		if (cmd.equals("sign")) {
			if (args.length != 2) {
				if (args.length == 1 && args[0].startsWith("l")) {
					p.sendMessage(ChatColor.GRAY + "MiniGame Worlds ----->");
					// list minigames
					for (MiniGameWorld s : MiniGameLobby.lobby.games)
						p.sendMessage(ChatColor.RED + "MiniGame: " + ChatColor.YELLOW + s.getGameName() + ChatColor.RED
								+ " World: " + ChatColor.YELLOW + s.getWorldName());
					return true;
				} else return false;
			}
			Block b = getTargetBlock(p, 5);
			if (b == null || !(b.getState() instanceof Sign)) {
				p.sendMessage(ChatColor.RED + "You are not looking at a sign");
				return true;
			}
			String gameName = args[0];
			String worldName = args[1];
			// World..
			MiniGameWorld minigame = null;

			for (MiniGameWorld s : MiniGameLobby.lobby.games)
				if (s.getWorldName().equals(worldName) && s.getGameName().equals(gameName)) {
					minigame = s;
					break;
				}
			if (minigame == null) {
				p.sendMessage(ChatColor.RED + "Cannot find that minigame world. Do /sign list to show minigame worlds.");
				return true;
			}
			minigame.signLocation = b.getLocation();
			minigame.save();
			p.sendMessage(ChatColor.BLUE + "Sign Changed");
			return true;
		}
		return false;
	}

	private Block getTargetBlock(Player player, int range) {
		Location loc = player.getEyeLocation();
		Vector dir = loc.getDirection().normalize();

		for (int i = 0; i <= range; i++) {
			Block b = loc.add(dir).getBlock();
			if (b != null && b.getType() != Material.AIR) return b;
		}

		return null;
	}

}
