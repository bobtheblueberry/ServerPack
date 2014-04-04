package me.toxiccoke.minigames;

import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerQuitEvent;
public class MiniGameEventHandler implements Listener {

	@EventHandler
	public void onEntityDamage(EntityDamageByEntityEvent event) {
		Entity t = event.getEntity();
		if (!(t instanceof Player)) return;
		Player player = (Player) t;

		if (((Damageable) player).getHealth() - event.getDamage() > 0) return;
		for (MiniGameWorld m : MiniGameLobby.lobby.games)
			for (MiniGamePlayer gp : m.getPlayers())
				if (gp.player.equals(player.getName())) {
					m.notifyDeath(gp, event.getDamager(), event.getCause());
					event.setCancelled(true);
					return;
				}
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.getCause() == DamageCause.ENTITY_ATTACK)
			return;// handled by EntityDamageByEntityEvent
		Entity t = event.getEntity();
		if (!(t instanceof Player)) return;
		Player player = (Player) t;

		if (((Damageable) player).getHealth() - event.getDamage() > 0) return;
		for (MiniGameWorld m : MiniGameLobby.lobby.games)
			for (MiniGamePlayer gp : m.getPlayers())
				if (gp.player.equals(player.getName())) {
					m.notifyDeath(gp, event.getCause());
					event.setCancelled(true);
					return;
				}
	}
	@EventHandler
	public void onEntityQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		for (MiniGameWorld m : MiniGameLobby.lobby.games)
			for (MiniGamePlayer gp : m.getPlayers())
				if (gp.player.equals(player.getName())) {
					m.notifyQuitGame(gp);
					return;
				}
	}
}
