package me.toxiccoke.minigames.payload;

import java.util.ArrayList;

import me.toxiccoke.minigames.GameLobby;
import me.toxiccoke.minigames.GamePlayer;
import me.toxiccoke.minigames.GameWorld;
import me.toxiccoke.minigames.MiniGamesPlugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ClassMenu implements CommandExecutor, Listener {

	private final static String	INV_NAME	= ChatColor.GREEN + "Select Class";

	public ClassMenu() {}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!command.getName().equalsIgnoreCase("class"))
			return false;
		Player p = (Player) sender;
		p.openInventory(getMenu(p));
		return true;
	}

	@EventHandler
	public void onInvClick(final InventoryClickEvent event) {
		Inventory i = event.getInventory();
		if (i.getName() == null || !i.getName().equals(INV_NAME))
			return;
		PayloadPlayer p = null;
		// Find the player
		main: for (GameWorld<? extends GamePlayer> w : GameLobby.lobby.games) {
			if (!(w instanceof PayloadGame))
				continue;
			for (GamePlayer plr : w.getPlayers())
				if (plr.getName().equals(event.getWhoClicked().getName())) {
					p = (PayloadPlayer) plr;
					break main;
				}
		}
		event.setCancelled(true);
		if ( p == null || p.team.lost || !p.isInGame())
			return;
		PayloadClass cl = null;
		Material m = event.getCurrentItem().getType();
		if (m == Material.MINECART)
			return; // cancel
		
		if (m == Material.RED_ROSE)
			cl = PayloadClass.SCOUT;
		else if (m == Material.FIRE)
		cl = PayloadClass.PYRO;
		else if (m == Material.ANVIL)
			cl = PayloadClass.HEAVY;
		else if (m == Material.IRON_BLOCK)
			cl = PayloadClass.ENGINEER;
		else if (m == Material.GOLDEN_APPLE)
			cl = PayloadClass.MEDIC;
		else if (m == Material.BOW)
			cl = PayloadClass.SNIPER;
		if (cl == p.playerClass || cl == null)
			return;
		p.playerClass = cl;
		p.classChange = true;
		if (!p.dealtDmg)
			p.respawn();
		else
			p.getPlayer().sendMessage(ChatColor.GREEN + "Class will change upon respawn");
		Bukkit.getScheduler().scheduleSyncDelayedTask(MiniGamesPlugin.plugin, new Runnable() {
			@Override
			public void run() {
				event.getWhoClicked().closeInventory();
			}
		});
	}

	protected static Inventory getMenu(InventoryHolder holder) {
		Inventory i = Bukkit.createInventory(holder, 36, INV_NAME);
		// scout,#soldier,pyro #demo,heavy,engie medic,sniper,#spy
		i.addItem(createItem(Material.RED_ROSE, 1, 2, ChatColor.GREEN + "Scout", "Waste 'em"));
		i.addItem(createItem(Material.FIRE, 1, 0, ChatColor.GREEN + "Pyro", "Burn 'em up"));
		i.addItem(createItem(Material.ANVIL, 1, 0, ChatColor.GREEN + "Heavy", "Destroy tiny baby men"));
		i.addItem(createItem(Material.IRON_BLOCK, 1, 0, ChatColor.GREEN + "Engineer", "Build Sentries"));
		i.addItem(createItem(Material.GOLDEN_APPLE, 1, 0, ChatColor.GREEN + "Medic", "Heal 'em"));
		i.addItem(createItem(Material.BOW, 1, 0, ChatColor.GREEN + "Sniper", "Snipe 'em"));
	
		i.addItem(createItem(Material.MINECART, 1, 0, ChatColor.RED + "Cancel", "Close this menu"));
		return i;
	}

	private static ItemStack createItem(Material material, int amount, int dmg, String displayname, String lore) {
		ItemStack item = new ItemStack(material, amount, (short) dmg);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(displayname);
		ArrayList<String> Lore = new ArrayList<String>();
		Lore.add(lore);
		meta.setLore(Lore);

		item.setItemMeta(meta);
		return item;
	}

}
