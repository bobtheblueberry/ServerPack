package me.toxiccoke.minigames.payload;

public class MinecartUpdater implements Runnable {

	PayloadGame	world;
	
	public MinecartUpdater(PayloadGame m) {
		this.world = m;
	}

	public void run() {
		if (!world.isStarted())
			return;
		world.updateCart();
	}

}
