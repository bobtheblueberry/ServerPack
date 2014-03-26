package me.toxiccoke.tokenshop;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TokenCommand implements CommandExecutor {

	public static boolean validName(String name) {
		return name.length() > 2 && name.length() < 17
				&& !name.matches("(?i).*[^a-z0-9_].*");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {

		if (!(sender instanceof Player)) {
			sender.sendMessage("This command can only be run by a player.");
			return true;
		}
		Player p = (Player) sender;

		String cmd = command.getName().toLowerCase();
		if (cmd.equals("shop")) {
			p.openInventory(TokenShop.shop);
			return true;
		}
		if (cmd.equals("givecoins")) {
			if (args.length != 1 && args.length != 2)
				return false;
			try {
				int a = Integer.parseInt(args[0]);
				if (args.length == 2) {
					String name = args[1];
					if (!validName(name))
						return false;
					Player player = TokenShop.plugin.getServer().getPlayer(name);
					if (player == null) {
						p.sendMessage("Player " + name + " is not online.");
						return true;
					}
					
					MyAPI.giveCoins(player, a);
				} else {
					MyAPI.giveCoins(p, a);
				}
				return true;
			} catch (NumberFormatException exc) {
				return false;
			}
		}
		if (cmd.equals("toyshop")) {
			p.openInventory(TokenShop.plugin.generateToys(p.getName()));
			return true;
		}
		if (cmd.equals("coins")) {
			p.sendMessage(ChatColor.GOLD + "§6" + " [AquilaMc] " + "§c"
					+ " Coin balance: §a" + MyAPI.getCoinCount(p));
			return true;
		}

		return false;
	}

}
