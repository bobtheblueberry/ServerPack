package me.toxiccoke.servercore;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class Events implements Listener {

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		e.setJoinMessage(ChatColor.DARK_GRAY + p.getDisplayName() + ChatColor.GRAY + "");

		Firework f = (Firework) e.getPlayer().getWorld().spawn(e.getPlayer().getLocation(), Firework.class);

		FireworkMeta fm = f.getFireworkMeta();
		fm.addEffect(FireworkEffect.builder().flicker(false).trail(true).with(Type.CREEPER).withColor(Color.GREEN)
				.withFade(Color.BLUE).build());
		fm.setPower(3);
		f.setFireworkMeta(fm);

	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		e.setQuitMessage(" ");
	}

	private final ArrayList<String> players = new ArrayList<String>();

	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		if (p.getItemInHand() != null) {
			ItemStack itemInHand = p.getItemInHand();
			if (itemInHand.getType().equals(Material.FIREBALL)) {
				if (players.contains(p.getName())) {
					p.setPassenger(null);
				} else {
					p.setPassenger(p);

				}
			}
		}
	}
}
