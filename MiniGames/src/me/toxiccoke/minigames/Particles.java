package me.toxiccoke.minigames;

import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class Particles implements Listener {

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		if (Math.random() * 10 > 1)
			return;
		Player p = e.getPlayer();
		if ((p.getGameMode() != GameMode.CREATIVE)
				&& (p.getLocation().subtract(0, 1, 0).getBlock().getType() != Material.AIR) && (!p.isFlying())) {
			for (GameWorld<?> w : GameLobby.lobby.games)
				for (GamePlayer gp : w.getPlayers())
					if (gp.player.equals(e.getPlayer().getName())) {
						Material m = gp.getFeetParticle();
						if (m != null)
							p.getWorld().playEffect(p.getLocation(), Effect.STEP_SOUND, m);
						return;
					}

		}
	}
}
