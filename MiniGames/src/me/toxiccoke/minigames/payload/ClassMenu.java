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
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
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
		PayloadPlayer plr = PayloadEventHandler.getPlayer(p);//make sure they are in the game
		if (plr != null)
			p.openInventory(getMenu());
		return true;
	}

	@EventHandler
	public void onInvClick(InventoryClickEvent event) {
		if (event.getWhoClicked() == null || event.getClick() == ClickType.MIDDLE || event.getCurrentItem() == null)
			return;
		Inventory i = event.getInventory();
		final Player player = (Player) event.getWhoClicked();
		boolean isPlayerInv = (i.getType() == InventoryType.PLAYER || i.getType() == InventoryType.CRAFTING);
		if ((i.getName() == null || !i.getName().equals(INV_NAME)) && !isPlayerInv)
			return;
		PayloadPlayer p = null;
		// Find the player
		main: for (GameWorld<? extends GamePlayer> w : GameLobby.lobby.games) {
			if (!(w instanceof PayloadGame))
				continue;
			for (GamePlayer plr : w.getPlayers())
				if (plr.getName().equals(player.getName())) {
					p = (PayloadPlayer) plr;
					break main;
				}
		}
		if (p == null)
			return;
		if (isPlayerInv) {
			if (playerInvClick(event, p, player))
				event.setCancelled(true);
			return;
		}
		event.setCancelled(true);
		if (p.team.lost || !p.isInGame())
			return;
		PayloadClass cl = null;
		Material m = event.getCurrentItem().getType();
		if (m == Material.MINECART) {
			closeInv(player);
			return; // cancel
		}

		if (m == Material.AIR)
			return;
		if (m == Material.RED_ROSE)
			cl = PayloadClass.SCOUT;
		else if (m == Material.FIRE)
			cl = PayloadClass.PYRO;
		else if (m == Material.ANVIL)
			cl = PayloadClass.SOLDIER;
		else if (m == Material.IRON_BLOCK)
			cl = PayloadClass.ENGINEER;
		else if (m == Material.GOLDEN_APPLE)
			cl = PayloadClass.MEDIC;
		else if (m == Material.BOW)
			cl = PayloadClass.SNIPER;
		if (cl == p.getPlayerClass() || cl == null) {
			p.classChange = false;
			closeInv(player);
			return;
		}
		p.tempClass = cl;
		p.classChange = true;
		if (!p.dealtDmg)
			p.respawn();
		else p.getPlayer().sendMessage(ChatColor.GREEN + "Class will change upon respawn");
		closeInv(player);
	}

	private void closeInv(final Player player) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(MiniGamesPlugin.plugin, new Runnable() {
			@Override
			public void run() {
				player.closeInventory();
			}
		});
	}

	/**
	 * 
	 * @param pp
	 * @param p
	 * @return true to cancel the event
	 */
	private boolean playerInvClick(InventoryClickEvent event, PayloadPlayer pp, Player p) {
		int slot = event.getSlot();
		return slot < 9;
	}

	protected static Inventory getMenu() {
		Inventory i = Bukkit.createInventory(null, 36, INV_NAME);
		// scout,#soldier,pyro #demo,heavy,engie medic,sniper,#spy
		i.addItem(createItem(Material.RED_ROSE, 1, 2, ChatColor.GREEN + "Scout", "Waste 'em"));
		i.addItem(createItem(Material.FIRE, 1, 0, ChatColor.GREEN + "Pyro", "Burn 'em up"));
		i.addItem(createItem(Material.ANVIL, 1, 0, ChatColor.GREEN + "Soldier", "Destroy tiny baby men"));
		i.addItem(createItem(Material.IRON_BLOCK, 1, 0, ChatColor.GREEN + "Engineer", "Build Sentries"));
		i.addItem(createItem(Material.GOLDEN_APPLE, 1, 0, ChatColor.GREEN + "Medic", "Heal 'em"));
		i.addItem(createItem(Material.BOW, 1, 0, ChatColor.GREEN + "Sniper", "Snipe 'em"));

		i.setItem(35, createItem(Material.MINECART, 1, 0, ChatColor.RED + "Cancel", "Close this menu"));
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
