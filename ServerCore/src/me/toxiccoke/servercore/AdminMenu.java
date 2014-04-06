package me.toxiccoke.servercore;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class AdminMenu implements CommandExecutor {

	static Inventory	ap;

	public AdminMenu() {
		ap = Bukkit.createInventory(null, 18, "§cAdminPanel");
		// menu items
		ap.setItem(0, createItem(Material.WOOD_AXE, 1, (short) 0, "§aLumberJack", "§cFree!"));

	}

	public ItemStack createItem(Material material, int amount, short shrt, String displayname, String lore) {
		ItemStack item = new ItemStack(material, amount, shrt);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(displayname);
		item.setItemMeta(meta);
		return item;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		Player p = (Player) sender;

		if (cmd.getName().equalsIgnoreCase("ap")) {
			p.openInventory(ap);
			p.sendMessage("Menu opened");
			
		}
		return true;

	}

}
