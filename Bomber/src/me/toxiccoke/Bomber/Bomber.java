package me.toxiccoke.Bomber;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Bomber extends JavaPlugin implements Listener {

	@Override
	public void onEnable() {
		System.out.print("ServerCore Enabled");
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new Team(), this);
		pm.registerEvents(this, this);
		Team.clearTeams();
	}

	public void onDisable() {
		Team.clearTeams();
	}

	@EventHandler
	// exploding arrows
	public void p(ProjectileHitEvent e) {
		Projectile proj = e.getEntity();

		if (proj instanceof Arrow) {
			Arrow arrow = (Arrow) proj;
			if (arrow.getShooter() instanceof Player) {
				Player p = (Player) arrow.getShooter();
				arrow.getWorld().createExplosion(arrow.getLocation(), 2);
			}
		}
	}

	@EventHandler
	// double jump
	public void onPlayerToggleFlight(PlayerToggleFlightEvent e) {
		Player p = e.getPlayer();
		if (p.getGameMode() == GameMode.CREATIVE) return;
		e.setCancelled(true);
		p.setAllowFlight(false);
		p.setFlying(false);
		p.setVelocity(p.getLocation().getDirection().multiply(1.5).setY(1));
		p.getWorld().playEffect(p.getLocation(), Effect.STEP_SOUND, Material.REDSTONE_WIRE);

	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		Player p = e.getPlayer();
		if ((p.getGameMode() != GameMode.CREATIVE)
				&& (p.getLocation().subtract(0, 1, 0).getBlock().getType() != Material.AIR) && (!p.isFlying())) {
			p.setAllowFlight(true);
			p.getWorld().playEffect(p.getLocation(), Effect.STEP_SOUND, Material.REDSTONE_WIRE);

		}
	}

	public ItemStack createItem(Material material, int amount, short shrt, String displayname, String lore) {
		ItemStack item = new ItemStack(material, amount, (short) shrt);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(displayname);
		ArrayList<String> Lore = new ArrayList<String>();
		Lore.add(lore);
		meta.setLore(Lore);

		item.setItemMeta(meta);
		return item;

	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player p = (Player) sender;

		if (cmd.getName().equalsIgnoreCase("b")) {
			p.sendMessage(ChatColor.GOLD + "-----------------------------------------------------");
			p.sendMessage(ChatColor.GRAY + "[" + ChatColor.BLUE + "BomberPVP" + ChatColor.GRAY + "]");
			p.sendMessage(ChatColor.GREEN + "/bjoin" + ChatColor.RED + " - Puts you in to a bomber game");
			p.sendMessage(ChatColor.GREEN + "/bteam" + ChatColor.RED + " - Shows what team you are currently in");
			p.sendMessage(ChatColor.GOLD + "-----------------------------------------------------");

		} else if (cmd.getName().equalsIgnoreCase("bjoin")) {
			int i = 0;
			for (Player player : Bukkit.getOnlinePlayers()) {
				if (i < Bukkit.getOnlinePlayers().length / 2) {
					Team.addToTeam(TeamType.RED, player);
				} else {
					Team.addToTeam(TeamType.BLUE, player);

				}
				i++;

			}
		} else if (cmd.getName().equalsIgnoreCase("bteam")) {
			sender.sendMessage(ChatColor.GRAY + Team.getTeamType(((Player) sender)).name());

		}
		return true;

	}

}
