package me.toxiccoke.servercore;

import java.util.ArrayList;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class Hats extends JavaPlugin implements Listener {

	private final ArrayList<String> players = new ArrayList<String>(); // ARRAYLIST
																		// WITH
																		// ALL
																		// THE
																		// PLAYERS
																		// WHICH
																		// HAS
																		// MAGMACUBE
																		// OVER
																		// HEAD

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		if (p.getItemInHand() != null) { // CHECK IF ITEM IN HAND IS NOT NULL
			ItemStack itemInHand = p.getItemInHand();
			if (itemInHand.getType().equals(Material.MAGMA_CREAM)) {
				if (players.contains(p.getName())) {
					/* Remove passenger */
					p.setPassenger(null);
					/* Remove from ArrayList */
					players.remove(p.getName());
				} else {
					/* SPAWN MAGMACUBE */
					MagmaCube magmacube = (MagmaCube) p.getWorld()
							.spawnCreature(p.getLocation(), /* ENTITY_TYPE_TO_SPAWN */
									EntityType.MAGMA_CUBE);
					magmacube.setSize(2);
					magmacube.setMaxHealth(100);
					magmacube.getWorld().playEffect(magmacube.getLocation(),
							Effect.STEP_SOUND, Material.REDSTONE_WIRE);
					p.setPassenger(magmacube);
					/* Add to ArrayList */
					players.add(p.getName());
				}
			}
		}
	} // YOU MISSED THIS }

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onInteract1(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		if (p.getItemInHand() != null) { // CHECK IF ITEM IN HAND IS NOT NULL
			ItemStack itemInHand = p.getItemInHand();
			if (itemInHand.getType().equals(Material.SLIME_BALL)) {
				if (players.contains(p.getName())) {
					/* Remove passenger */
					p.setPassenger(null);
					/* Remove from ArrayList */
					players.remove(p.getName());
				} else {
					/* SPAWN MAGMACUBE */
					Slime slime = (Slime) p.getWorld().spawnCreature(
							p.getLocation(), /* ENTITY_TYPE_TO_SPAWN */
							EntityType.SLIME);
					slime.setSize(2);
					slime.getWorld().playEffect(slime.getLocation(),
							Effect.STEP_SOUND, Material.REDSTONE_WIRE);
					p.setPassenger(slime);
					/* Add to ArrayList */
					players.add(p.getName());
				}
			}
		}
	}
}