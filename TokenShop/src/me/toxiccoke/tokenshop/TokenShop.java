package me.toxiccoke.tokenshop;

import java.util.ArrayList;
import java.util.HashMap;

import me.toxiccoke.tokenshop.MyAPI.BooleanArray32;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public class TokenShop extends JavaPlugin implements Listener {
	public static TokenShop	plugin		= null;
	static Inventory		shop;

	static String			hatInvName	= "§bHats";
	static String			petInvName	= "§cPets";
	static String			toyInvName	= "§bToys";
	static String			shopInvName	= "§aHub Shop";

	Inventory generateHats(String player) {

		Inventory hats = Bukkit.createInventory(null, 18, hatInvName);
		int vals = MyAPI.getVal(player, "hats");
		int ind = 0;
		BooleanArray32 b = new BooleanArray32(vals);
		if (vals != 0)
		// Add the hats that are owned first
			for (Hat h : Hat.hats) {
				if (b.get(h.refCode))
					hats.setItem(ind++, createItem(h.mat, 1, (short) 0, h.displayName, "§bYou own this hat"));

			}
		for (Hat h : Hat.hats)
			if (!b.get(h.refCode))
				hats.setItem(ind++, createItem(h.mat, 1, (short) 0, h.displayName, "§cPrice: " + h.price));

		hats.setItem(16,
				createItem(Material.FIRE, 1, (short) 0, Hat.removeHatLabel, "§aClick to remove your current hat"));
		hats.setItem(17, getCloseButton());
		return hats;

	}

	Inventory generatePets(String player) {

		Inventory pets = Bukkit.createInventory(null, 18, petInvName);
		int vals = MyAPI.getVal(player, "pets");
		int ind = 0;
		BooleanArray32 b = new BooleanArray32(vals);
		if (vals != 0)
		// Add the hats that are owned first
			for (Pet p : Pet.pets) {
				if (b.get(p.refCode)) pets.setItem(ind++, createItem(p.getEgg(), p.displayName, "§bYou own this pet"));

			}
		for (Pet p : Pet.pets)
			if (!b.get(p.refCode)) pets.setItem(ind++, createItem(p.getEgg(), p.displayName, "§cPrice: " + p.price));
		pets.setItem(17, getCloseButton());
		return pets;

	}

	Inventory generateToys(String player) {
		Inventory toys = Bukkit.createInventory(null, 18, toyInvName);
		int vals = MyAPI.getVal(player, "toys");
		int ind = 0;
		BooleanArray32 b = new BooleanArray32(vals);
		if (vals != 0)
		// Add the hats that are owned first
			for (Toy t : Toy.toys) {
				if (b.get(t.refCode)) toys.setItem(ind++, createItem(t.mat(), t.displayName, "§bYou own this toy"));

			}
		for (Toy t : Toy.toys)
			if (!b.get(t.refCode)) toys.setItem(ind++, createItem(t.mat(), t.displayName, "§cPrice: " + t.price));
		toys.setItem(17, getCloseButton());
		return toys;
	}

	private ItemStack getCloseButton() {
		return createItem(Material.MINECART, 1, (short) 0, "§aClose", "§aClick to got back to the game");
	}

	public TokenShop() {
		shop = Bukkit.createInventory(null, 9, shopInvName);

		// menus
		shop.setItem(0, createItem(Material.STICK, 1, (short) 0, "§aToys", "§aClick to see"));
		shop.setItem(1, createItem(Material.SKULL_ITEM, 1, (short) 0, "§bHats", "§bClick to see"));
		shop.setItem(2, createItem(Material.BONE, 1, (short) 0, "§cPets", "§cClick to see"));

		shop.setItem(8, createItem(Material.MINECART, 1, (short) 0, "§aClose", "§aClick to got back to the game"));

	}

	Scoreboard	coins;

	public void onEnable() {
		getServer().getPluginManager().registerEvents(new EventHandlers(), this);
		getServer().getPluginManager().registerEvents(this, this);
		getServer().getPluginManager().registerEvents(new HatHandler(), this);

		plugin = this;
		if (!MyAPI.init()) {
			System.err.println("Failed to initiate TokenShop");
			this.getPluginLoader().disablePlugin(this);
		}
		TokenCommand t = new TokenCommand();
		getCommand("shop").setExecutor(t);
		getCommand("coins").setExecutor(t);
		getCommand("givecoin").setExecutor(t);
		getCommand("hatshop").setExecutor(t);
		getCommand("petshop").setExecutor(t);
	}

	public static void teleportAdvanced(final Player player, final Location location) {
		Entity e = player.getVehicle();
		if (e != null)
			e.eject();
		e = player.getPassenger();
		if (e == null) {
			player.teleport(location);
			return;
		}
		if (!(e instanceof Player)) e.remove();
		player.eject();
		
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(TokenShop.plugin, new Runnable() {
			public void run() {
				player.teleport(location);
				HatHandler.loadHat(player);
			}
		}, 3L);
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		showScoreboard(player);
	}

	private static HashMap<String, Scoreboard>	scoreboard	= new HashMap<String, Scoreboard>();

	private static Scoreboard getScoreboard(Player p) {
		Scoreboard s = scoreboard.get(p.getName());
		if (s == null) {
			s = Bukkit.getScoreboardManager().getNewScoreboard();
			scoreboard.put(p.getName(), s);
			s.registerNewObjective("money", "dummy");
		}
		return s;
	}

	static boolean	showScoreboard	= false;

	public static void showScoreboard(Player player) {
		if (!showScoreboard) return;
		Scoreboard board = getScoreboard(player);
		Objective objective = board.getObjective("money");
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		objective.setDisplayName(ChatColor.GOLD + "[AquilaMc Bank] ");
		Score score = objective.getScore(Bukkit.getOfflinePlayer(ChatColor.GREEN + "Coins:"));
		score.setScore(MyAPI.getCoinCount(player));
		player.setScoreboard(board);
	}

	public ItemStack createItem(Material material, int amount, short shrt, String displayname, String lore) {
		ItemStack item = new ItemStack(material, amount, shrt);
		return createItem(item, displayname, lore);
	}

	public ItemStack createItem(ItemStack item, String displayname, String lore) {
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(displayname);
		ArrayList<String> Lore = new ArrayList<String>();
		Lore.add(lore);
		meta.setLore(Lore);

		item.setItemMeta(meta);
		return item;
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEntityEvent e) {
		Player p = e.getPlayer();
		if (e.getRightClicked().getType() == EntityType.VILLAGER) {
			e.setCancelled(true);
			p.chat("/shop");
		}
	}

	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent e) {
		MyAPI.checkPlayer(e.getPlayer().getName());
	}

	@EventHandler
	public void invClick(InventoryClickEvent e) {
		Player p = (Player) e.getWhoClicked();
		if (e.getInventory() == null) return;
		if (e.getInventory().getName().equals(TokenShop.shop.getName())) {
			e.setCancelled(true);
		}
		if (e.getInventory().getName().equals(hatInvName)) {
			e.setCancelled(true);
		}
		if (e.getInventory().getName().equals(toyInvName)) {
			e.setCancelled(true);
		}
		if (e.getInventory().getName().equals(petInvName)) {
			e.setCancelled(true);
		}
		if (e.getCurrentItem() == null) { return; }

		if (!e.getCurrentItem().hasItemMeta()) { return; }
		if (e.getCurrentItem().getItemMeta() == null || e.getCurrentItem().getItemMeta().getDisplayName() == null)
			return;
		// menu openers / closers
		if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§aToys"))
			p.getPlayer().openInventory(generateToys(p.getName()));

		if (e.getCurrentItem().getItemMeta().getDisplayName().equals(hatInvName))
			p.getPlayer().openInventory(generateHats(p.getName()));

		if (e.getCurrentItem().getItemMeta().getDisplayName().equals(petInvName))
			p.getPlayer().openInventory(generatePets(p.getName()));

		if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§aClose")) p.getPlayer().closeInventory();
	}
}
