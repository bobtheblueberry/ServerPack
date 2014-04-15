package me.toxiccoke.minigames.payload;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class ItemPack {

	private Item		item;
	private Location	location;
	private String		player;
	private boolean		updatedPosition	= false;
	private String		type;
	private Material	material;
	private short		data;
	private String		extraLoad		= "";
	private int			stackSize;
	boolean respawning;

	public ItemPack(Location loc, Material mat, int data) {
		this(loc, mat, data, 1);
	}

	public ItemPack(Location loc, Material mat, int data, int stackSize) {
		setMaterial(mat);
		setData((short) data);
		setPlayer(player);
		setType(type);
		this.stackSize = stackSize;
		setLocation(loc);
		setItem(loc.getWorld().dropItemNaturally(getLocation(), new ItemStack(mat, stackSize, (short) data)));
		getItem().setVelocity(new Vector(0, 0.1, 0));
		checkForDupedItem();
	}

	public void setExtraLoad(String s) {
		extraLoad = s;
	}

	public String getExtraLoad() {
		return extraLoad;
	}

	/**
	 * @param item
	 *            the item to set
	 */
	public void setItem(Item item) {
		this.item = item;
		updatedPosition = false;
	}

	/**
	 * @return the item
	 */
	public Item getItem() {
		return item;
	}

	/**
	 * @param location
	 *            the location to set
	 */
	public void setLocation(Location location) {
		location = location.getBlock().getLocation();
		Vector vec = location.toVector();
		vec.add(new Vector(0.5, 0.6, 0.5));
		location = vec.toLocation(location.getWorld());
		this.location = location;
		if (item != null) {
			item.teleport(location);
		}
	}

	/**
	 * @return the location
	 */
	public Location getLocation() {
		return location;
	}

	public void remove() {
		checkForDupedItem();
		item.remove();
	}

	public void respawn() {
		if (item != null) {
			item.remove();
		}
		ItemStack stack = new ItemStack(getMaterial(), stackSize, getData());
		item = getLocation().getWorld().dropItemNaturally(location, stack);
		updatedPosition = false;
		respawning = false;
	}

	public void updatePosition() {
		if (item != null && (!updatedPosition || item.getLocation().getY() <= getLocation().getBlockY() + 0.4)) {
			item.teleport(location);
			item.setVelocity(new Vector(0, 0.1, 0));
			updatedPosition = true;
		}
	}

	/**
	 * @return the block
	 */
	public Block getBlock() {
		return location.getBlock();
	}

	/**
	 * @param player
	 *            the player to set
	 */
	public void setPlayer(String player) {
		this.player = player;
	}

	/**
	 * @return the player
	 */
	public String getPlayer() {
		return player;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param material
	 *            the material to set
	 */
	public void setMaterial(Material material) {
		this.material = material;
	}

	/**
	 * @return the material
	 */
	public Material getMaterial() {
		return material;
	}

	/**
	 * @param data
	 *            the data to set
	 */
	public void setData(short data) {
		this.data = data;
	}

	/**
	 * @return the data
	 */
	public short getData() {
		return data;
	}

	public void checkForDupedItem() {
		Chunk c = getBlock().getChunk();
		for (Entity e : c.getEntities()) {
			if (e.getLocation().distance(location) < 2 && e instanceof Item && !e.equals(item)) {
				e.remove();
			}
		}
	}
}
