package me.toxiccoke.kitpvp;

import java.util.ArrayList;




import me.toxiccoke.tokenshop.MyAPI;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class menu extends JavaPlugin implements Listener{
	
	public void onEnable(){
		System.out.print("ServerCore Enabled");
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new menu(),this);
		
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e){
		Action a = e.getAction();
		ItemStack is = e.getItem();
		
		if(a == Action.PHYSICAL || is == null || is.getType() == Material.AIR )
			return;
		
		if(is.getType() == Material.DIAMOND)
			e.getPlayer().openInventory(menu);
	}
	
	  @EventHandler
	  public void invClick1(InventoryClickEvent e)
	  {
	    Player p = (Player)e.getWhoClicked();
	    if (e.getInventory().getName().equals(menu.getName())) {
	      e.setCancelled(true);
	    }
		  
}
	
	  static Inventory kits;
	  static Inventory menu;
	  static Inventory wepons;

	  public menu()
	  {
		menu = Bukkit.createInventory(null, 9, "§cKit Menu");
	    kits = Bukkit.createInventory(null, 18, "§aKits");
	    wepons = Bukkit.createInventory(null, 18, "§bWepons");
	    // menu items
	    menu.setItem(
	  	      0, 
	  	      createItem(Material.DIAMOND_CHESTPLATE, 1, (short)0, "§aKits","Click to enter!"));
	    menu.setItem(
		  	      1, 
		  	      createItem(Material.BOW, 1, (short)0, "§bWepons","Click to enter!"));
	    
	    //kit items
	    kits.setItem(
	      0, 
	      createItem(Material.DIAMOND_CHESTPLATE, 1, (short)0, "§aDiamond Kit", "§cPrice 1000"));
	    kits.setItem(	
	      1, 
	      createItem(Material.GOLD_CHESTPLATE, 1, (short)0, "§bGold Kit", "§cPrice 400"));
	    kits.setItem(
	      2, 
	      createItem(Material.IRON_CHESTPLATE, 1, (short)0, "§cIron Kit", "§cPrice 700"));
	    kits.setItem(	
	      3, 
	  	  createItem(Material.CHAINMAIL_CHESTPLATE, 1, (short)0, "§dChain Kit", "§cPrice 400"));
	  	kits.setItem(
	      4, 
	      createItem(Material.LEATHER_CHESTPLATE, 1, (short)0, "§eLeather Kit", "§cPrice Free"));
	  	//wepons
	  	wepons.setItem(
	  	           0, 
	  	      createItem(Material.DIAMOND_SWORD, 1, (short)0, "§aDiamond Sword", "§cPrice 500"));
	  	wepons.setItem(
		  	      1, 
		  	      createItem(Material.GOLD_SWORD, 1, (short)0, "§bGold Sword", "§cPrice 200"));
	  	wepons.setItem(
		  	      2, 
		  	      createItem(Material.IRON_SWORD, 1, (short)0, "§cIron Sword", "§cPrice 300"));
	  	wepons.setItem(
		  	      3, 
		  	      createItem(Material.STONE_SWORD, 1, (short)0, "§dStone Sword", "§cPrice 100"));
	  	wepons.setItem(
		  	      4, 
		  	      createItem(Material.WOOD_SWORD, 1, (short)0, "§eWood Sword", "§cPrice Free"));
	  	wepons.setItem(
		  	      5, 
		  	      createItem(Material.BOW, 1, (short)0, "§aBow", "§cPrice 100"));

}
	  
	  public ItemStack createItem(Material material, int amount, short shrt, String displayname, String lore)
	  {
	    ItemStack item = new ItemStack(material, amount, shrt);
	    ItemMeta meta = item.getItemMeta();
	    meta.setDisplayName(displayname);
	    ArrayList Lore = new ArrayList();
	    Lore.add(lore);
	    meta.setLore(Lore);

	    item.setItemMeta(meta);
	    return item;
	  }


	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	  {
	  	Player p = (Player)sender;
	  	
	  	if(cmd.getName().equalsIgnoreCase("menu")){
	  		p.openInventory(menu);
	      
	}
		return false;
  }
	
	  @EventHandler
	  public void invClick(InventoryClickEvent e)
	  {
	    Player p = (Player)e.getWhoClicked();
	    if (e.getInventory().getName().equals(kits.getName())) {
		      e.setCancelled(true);
	    }
		if (e.getInventory().getName().equals(menu.getName())) {
		      e.setCancelled(true);
		}
		if (e.getInventory().getName().equals(wepons.getName())) {
		      e.setCancelled(true);
	    }
	    if (e.getCurrentItem() == null) {
	      return;
	    }

	    if (!e.getCurrentItem().hasItemMeta()) {
	      return;
	    }

	    if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§aDiamond Kit"))
	    {
	    	if (MyAPI.hasEnough(p, 1000)) {
	             MyAPI.takeCoins(p, 1000);
	           p.getPlayer().getInventory().addItem(new ItemStack(Material.DIAMOND_BOOTS));
	           p.getPlayer().getInventory().addItem(new ItemStack(Material.DIAMOND_LEGGINGS));
	           p.getPlayer().getInventory().addItem(new ItemStack(Material.DIAMOND_CHESTPLATE));
	           p.getPlayer().getInventory().addItem(new ItemStack(Material.DIAMOND_HELMET));
	           }
	          
	    }
	          
	  	if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§bGold Kit"))
	  	{
	        if (MyAPI.hasEnough(p, 400)){
	            MyAPI.takeCoins(p, 400);
		      p.getPlayer().getInventory().addItem(new ItemStack(Material.GOLD_BOOTS));
		      p.getPlayer().getInventory().addItem(new ItemStack(Material.GOLD_LEGGINGS));
	          p.getPlayer().getInventory().addItem(new ItemStack(Material.GOLD_CHESTPLATE));
              p.getPlayer().getInventory().addItem(new ItemStack(Material.GOLD_HELMET));
              
	    }
        
	  	if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§cIron Kit"))
	  	{
	        if (MyAPI.hasEnough(p, 700)){
	            MyAPI.takeCoins(p, 700);
		      p.getPlayer().getInventory().addItem(new ItemStack(Material.IRON_BOOTS));
		      p.getPlayer().getInventory().addItem(new ItemStack(Material.IRON_LEGGINGS));
	          p.getPlayer().getInventory().addItem(new ItemStack(Material.IRON_CHESTPLATE));
              p.getPlayer().getInventory().addItem(new ItemStack(Material.IRON_HELMET));
              
	    }
        
	  	if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§dChain Kit"))
	  	{
	        if (MyAPI.hasEnough(p, 400)){
	            MyAPI.takeCoins(p, 400);
		      p.getPlayer().getInventory().addItem(new ItemStack(Material.CHAINMAIL_BOOTS));
		      p.getPlayer().getInventory().addItem(new ItemStack(Material.CHAINMAIL_LEGGINGS));
	          p.getPlayer().getInventory().addItem(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
              p.getPlayer().getInventory().addItem(new ItemStack(Material.CHAINMAIL_HELMET));
              
	    }
        
	  	if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§eLeather Kit"))
	  	{
		      p.getPlayer().getInventory().addItem(new ItemStack(Material.LEATHER_BOOTS));
		      p.getPlayer().getInventory().addItem(new ItemStack(Material.LEATHER_LEGGINGS));
	          p.getPlayer().getInventory().addItem(new ItemStack(Material.LEATHER_CHESTPLATE));
              p.getPlayer().getInventory().addItem(new ItemStack(Material.LEATHER_HELMET));
              
	    }
	  	
	  	if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§aDiamond Sword"))
	    {
	        if (MyAPI.hasEnough(p, 500))
	            MyAPI.takeCoins(p, 500);
		      p.getPlayer().getInventory().addItem(new ItemStack(Material.DIAMOND_SWORD));
		      
	    }
	  	if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§bGold Sword"))
	    {
	        if (MyAPI.hasEnough(p, 200))
	            MyAPI.takeCoins(p, 200);
		      p.getPlayer().getInventory().addItem(new ItemStack(Material.GOLD_SWORD));
		      
	    }
	  	if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§cIron Sword"))
	    {
	        if (MyAPI.hasEnough(p, 300))
	            MyAPI.takeCoins(p, 300);
		      p.getPlayer().getInventory().addItem(new ItemStack(Material.IRON_SWORD));
		      
	    }
	  	if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§dStone Sword"))
	    {
	        if (MyAPI.hasEnough(p, 100))
	            MyAPI.takeCoins(p, 100);
		      p.getPlayer().getInventory().addItem(new ItemStack(Material.STONE_SWORD));
		      
	    }
	  	if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§eWood Sword"))
	    {
		      p.getPlayer().getInventory().addItem(new ItemStack(Material.WOOD_SWORD));
	    }
	  	if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§aBow")){
	    {
	        if (MyAPI.hasEnough(p, 100))
	            MyAPI.takeCoins(p, 100);
		      p.getPlayer().getInventory().addItem(new ItemStack(Material.BOW));
		      
	    }
	  	if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§aKits"))
	    {
		      p.getPlayer().openInventory(kits);
	    }
	  	if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§bWepons")){
	    {
		      p.getPlayer().openInventory(wepons);
		 
		 }
         }
	  	}
	  	}
}
}
}
}