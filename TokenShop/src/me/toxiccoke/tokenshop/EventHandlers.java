package me.toxiccoke.tokenshop;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class EventHandlers implements Listener {

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
	}

	@EventHandler
	public void onKill(EntityDeathEvent e) {
		if ((e.getEntity() instanceof Monster)) {
			Monster m = (Monster) e.getEntity();
			if ((m.getKiller() instanceof Player)) {
				Player p = m.getKiller();
				MyAPI.giveCoins(p, 1);
			}
		}
		if ((e.getEntity() instanceof Player)) {
			Player player = (Player) e.getEntity();
			if ((player.getKiller() instanceof Player)) {
				Player p = player.getKiller();
				MyAPI.giveCoins(p, 5);
			}
		}
	}

	@EventHandler
	public static void invClick(InventoryClickEvent e) {
		String invN = e.getInventory().getName();
		if (!invN.equals(TokenShop.hatInvName) && !invN.equals(TokenShop.petInvName)
				&& !invN.equals(TokenShop.toyInvName)) {
			return;
		}

		Player p = (Player) e.getWhoClicked();
		if (e.getCurrentItem() == null) {
			return;
		}

		if (!e.getCurrentItem().hasItemMeta()) {
			return;
		}
		String name = e.getCurrentItem().getItemMeta().getDisplayName();
		Hat h = Hat.getHat(name);
		if (h == null) {
			// might be remove hat
			if (name.equals(Hat.removeHatLabel)) {
				p.getInventory().setHelmet(new ItemStack(Material.AIR, 1));
				HatHandler.resetHat(p);
				p.sendMessage(ChatColor.GOLD + "[AquilaMc]" + ChatColor.RED + " Hat removed");

				p.getPlayer().closeInventory();
				return;
			}
			// Maybe it was pets
			Pet pet = Pet.getPet(name);
			if (pet != null) {
				pet(e, pet);
				return;
			}
			// Maybe it was toys
			Toy toy = Toy.getToy(name);
			if (toy != null) {
				toy(e, toy);
				return;
			}

			return;
		}
		if (h.price < 1) {
			p.getInventory().setHelmet(h.hat());
			p.getPlayer().closeInventory();
			return;
		}
		// See if it was purchased before
		if (!MyAPI.hasHat(p.getName(), h.refCode)) {
			// they haven't bought it
			if (!MyAPI.hasEnough(p, h.price)) {
				p.sendMessage("§bYou are too poor");
				return;
			}
			MyAPI.takeCoins(p, h.price);
			MyAPI.setHat(p.getName(), h.refCode, true);
		}
		if (!h.special) {
			p.getInventory().setHelmet(h.hat());
			HatHandler.setNormalHat(p.getName(), h.refCode);
			HatHandler.resetHat(p);
		} else {
			p.getInventory().setHelmet(new ItemStack(Material.AIR, 1));
			if (h.mat == Material.SLIME_BALL)
				HatHandler.setSlimeHat(p);
			else if (h.mat == Material.MAGMA_CREAM)
				HatHandler.setMagmacubeHat(p);
		}
		p.sendMessage("§aHat Changed to " + h.displayName);

		p.getPlayer().closeInventory();

	}

	private static void pet(InventoryClickEvent e, Pet pet) {
		Player p = (Player) e.getWhoClicked();
		// See if it was purchased before
		if (!MyAPI.hasPet(p.getName(), pet.refCode)) {
			// they haven't bought it
			if (!MyAPI.hasEnough(p, pet.price)) {
				p.sendMessage("§bYou are too poor");
				return;
			}
			MyAPI.takeCoins(p, pet.price);
			MyAPI.setPet(p.getName(), pet.refCode, true);

		}
		p.sendMessage("§aPet Changed to " + pet.displayName);
		spawnPet(p, pet);
		p.getPlayer().closeInventory();
	}

	private static void toy(InventoryClickEvent e, Toy toy) {
		Player p = (Player) e.getWhoClicked();
		// See if it was purchased before
		if (!MyAPI.hasToy(p.getName(), toy.refCode)) {
			// they haven't bought it
			if (!MyAPI.hasEnough(p, toy.price)) {
				p.sendMessage("§bYou are too poor");
				return;
			}
			MyAPI.takeCoins(p, toy.price);
			MyAPI.setToy(p.getName(), toy.refCode, true);
			p.sendMessage("§dToy Purchased: " + toy.displayName);
			p.getPlayer().closeInventory();
			return;
		}
		p.sendMessage("§aYou own that toy");
	}

	@SuppressWarnings("deprecation")
	private static void spawnPet(Player p, Pet pet) {
		EntityType t = null;
		for (EntityType en : EntityType.values())
			if (en.getTypeId() == pet.mobId) {
				t = en;
				break;
			}
		if (t == null)
			return;

		LivingEntity e = (LivingEntity) p.getWorld().spawnEntity(p.getLocation(), t);
		e.getWorld().playEffect(p.getLocation(), Effect.STEP_SOUND, Material.REDSTONE_WIRE);
		e.setCustomName(p.getDisplayName() + "'s "+t.getName());
		e.setCustomNameVisible(true);
		if (e instanceof Creature)
			((Creature) e).setTarget(p);
		e.setLeashHolder(p);
	}
}
