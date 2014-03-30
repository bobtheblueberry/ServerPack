package me.toxiccoke.servercore;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class Menu extends JavaPlugin implements Listener {
	
	static private final String GAME_MENU_STR = ChatColor.GOLD + "Game Selector";
	private void teleportInWorld(Player player, int x, int y, int z){
		
	}
	
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this,this);
	}
	
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Action a = e.getAction();
        ItemStack is = e.getItem();
        Player p = e.getPlayer();
       
        if(a == Action.PHYSICAL || is == null || is.getType() == Material.AIR) return;
       
        if(is.getType() == Material.REDSTONE_TORCH_ON)
      for(Player player : Bukkit.getOnlinePlayers())
      player.showPlayer(p);
    }
       
	private void openGUI(Player player){
		Inventory gameselecter = Bukkit.createInventory(null, 9, GAME_MENU_STR);
		
		ItemStack survivalgames = new ItemStack (Material.CHEST);
		ItemMeta survivalgamesMeta = survivalgames.getItemMeta();
		
		ItemStack bomberpvp = new ItemStack (Material.BOW);
		ItemMeta bomberpvpMeta = bomberpvp.getItemMeta();
		
		ItemStack kitpvp = new ItemStack (Material.DIAMOND_SWORD);
		ItemMeta kitpvpMeta = kitpvp.getItemMeta();
		
		ItemStack supersmashmobs = new ItemStack (Material.BONE);
		ItemMeta supersmashmobsMeta = supersmashmobs.getItemMeta();
		
		survivalgamesMeta.setDisplayName(ChatColor.RED + "SurvivalGames");
		survivalgames.setItemMeta(survivalgamesMeta);
		
		bomberpvpMeta.setDisplayName(ChatColor.GREEN + "BomberPVP");
		bomberpvp.setItemMeta(bomberpvpMeta);
		
		kitpvpMeta.setDisplayName(ChatColor.GOLD + "KitPVP");
		kitpvp.setItemMeta(kitpvpMeta);
		
		supersmashmobsMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "SuperSmashMobs");
		supersmashmobs.setItemMeta(supersmashmobsMeta);
		
		gameselecter.setItem(1, survivalgames);
		gameselecter.setItem(4, bomberpvp);
		gameselecter.setItem(6, kitpvp);
		gameselecter.setItem(8, supersmashmobs);
		
		player.openInventory(gameselecter);
	}
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		e.getPlayer().getInventory().addItem(new ItemStack(Material.NETHER_STAR));
	}
	/*
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
    	Action a = e.getAction();
        ItemStack is = e.getItem();
        
        if(a == Action.PHYSICAL || is == null || is.getType() == Material.AIR) return;
        
        if(is.getType() == Material.NETHER_STAR)
        	openGUI(e.getPlayer());
    }*/
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e){
    	if(!e.getInventory().getName().equals(GAME_MENU_STR)) {
    		System.out.println("END1 " +e.getInventory().getName());
    		System.out.println("     " + GAME_MENU_STR);
    		return;
    	}
    	Player p = (Player) e.getWhoClicked();
    			e.setCancelled(true);
    			
    	if(e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR ||!e.getCurrentItem().hasItemMeta()){
    		p.closeInventory();
    		return;
    	}
    	switch(e.getCurrentItem().getType()){
    	case CHEST:
    		teleportInWorld(p, 530, 103, 123);
    		p.closeInventory();p.sendMessage(String.format("%sTeleported to Survival Games", ChatColor.GREEN));
    	    break;
    	    
    	case BOW:
    		teleportInWorld(p, 542, 103, 123);
    		p.closeInventory();p.sendMessage(String.format("%sTeleported to Survival Games", ChatColor.GREEN));
    	    break;
    	    
    	case DIAMOND_SWORD:
    		teleportInWorld(p, 530, 103, 23);
    		p.closeInventory();p.sendMessage(String.format("%sTeleported to Survival Games", ChatColor.GREEN));
    	    break;
    	    
    	case BONE:
    		teleportInWorld(p, 542, 103, 23);
    		p.closeInventory();p.sendMessage(String.format("%sTeleported to Survival Games", ChatColor.GREEN));
    	    break;
    	    
    	default:
    		p.closeInventory();
    		break;
    	
    		
    	
    	}
    }

}