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
			if (args.length != 2) return false;
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
				if (s.getWorldName().equals(worldName) && s.getGameName().equalsIgnoreCase(gameName)) {
					minigame = s;
					break;
				}
			if (minigame == null) {
				p.sendMessage(ChatColor.RED
						+ "Cannot find that minigame world. Do /madmin list to show minigame worlds.");
				return true;
			}
			minigame.signLocation = b.getLocation();
			minigame.save();
			p.sendMessage(ChatColor.BLUE + "Sign Changed");
			return true;
		} else if (cmd.equals("madmin")) {
			if (args.length == 0) {
				p.sendMessage(ChatColor.RED + "/" + label + " <list|set|tp|reset|schematic> (Game Type) (World)");
				return true;
			}
			if (args.length > 0 && args[0].startsWith("list")) {
				p.sendMessage(ChatColor.GRAY + "MiniGame Worlds ----->");
				// list minigames
				for (MiniGameWorld s : MiniGameLobby.lobby.games)
					p.sendMessage(ChatColor.RED + "MiniGame: " + ChatColor.YELLOW + s.getGameName() + ChatColor.RED
							+ " World: " + ChatColor.YELLOW + s.getWorldName());
				return true;
			} else {
				if (args.length < 3) {
					p.sendMessage(ChatColor.RED + "/" + label + " " + args[0] + " <Game Type> <World>");
					return true;
				}
				MiniGameWorld minigame = null;
				for (MiniGameWorld s : MiniGameLobby.lobby.games)
					if (s.getWorldName().equalsIgnoreCase(args[2]) && s.getGameName().equalsIgnoreCase(args[1])) {
						minigame = s;
						break;
					}
				if (minigame == null) {
					p.sendMessage(ChatColor.RED
							+ "Cannot find that minigame world. Do /madmin list to show minigame worlds.");
					return true;
				}
				if (args[0].equals("set")) {
					Location l = p.getLocation();
					minigame.pasteLocation = new Location(l.getWorld(), Math.round(l.getX()), Math.round(l.getY()),
							Math.round(l.getZ()));
					minigame.save();
					p.sendMessage(ChatColor.BLUE + "Set paste location to " + getLocationString(minigame.pasteLocation));
					return true;
				} else if (args[0].equals("tp")) {
					if (minigame.pasteLocation == null) {
						p.sendMessage(ChatColor.RED + "This game world has no paste location set");
						return true;
					}
					p.teleport(minigame.pasteLocation);
					p.sendMessage(ChatColor.BLUE + "Teleported to paste location");
					return true;
				} else if (args[0].equals("reset")) {
					if (minigame.pasteLocation == null) {
						p.sendMessage(ChatColor.RED + "This game world has no paste location set");
						return true;
					}
					minigame.reset();
					p.sendMessage(ChatColor.BLUE + "Minigame reset");
					return true;
				} else if (args[0].startsWith("sc")) {
					if (args.length < 4) {
						p.sendMessage(ChatColor.RED + "Specify a schematic name");
						return true;
					}
					String sc = "";
					for (int i = 3; i < args.length; i++)
						sc = sc + args[i];
					minigame.schematic = sc;
					minigame.save();
					p.sendMessage(ChatColor.BLUE + "Schematic set to " + sc);
					return true;
				}
				
			}

		}

		return false;
	}

	private String getLocationString(Location l) {
		return "World: " + l.getWorld().getName() + " X: " + l.getX() + " Y: " + l.getY() + " Z: " + l.getZ();
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