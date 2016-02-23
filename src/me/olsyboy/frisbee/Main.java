package me.olsyboy.frisbee;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleCollisionEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.bergerkiller.bukkit.common.entity.CommonEntity;

public class Main extends JavaPlugin implements Listener {
	RideFrisbee RF = new RideFrisbee(this);
	closeOrOpenFence CF = new closeOrOpenFence(this);
	ArrayList listenForPlayers = new ArrayList();
	ArrayList playersInCart = new ArrayList();
	String rideName;
	String rideStartName;
	int rideIsJoining = 0;
	int rideHasStarted = 0;
	int runnableID;
	Location rideLocation;
	boolean delete = false;


	private static final HandlerList handlers = new HandlerList();
	public HandlerList getHandlers() {
	    return handlers;
	}
	public static HandlerList getHandlerList() {
		return handlers;
	}

	public void onEnable() {
		PluginManager pm = Bukkit.getServer().getPluginManager();
		pm.registerEvents(this, this);
		loadConfiguration();
		reloadConfig();
		if(delete){RFdelete();
		delete=false;}
	}

	public void onDisable() {
		if(RF != null){
			Bukkit.getScheduler().cancelTask(runnableID);
			delete = true;
			RFdelete();
			RFdelete();}
		saveDefaultConfig();
	}

	private void RFdelete() {
		RF.delete();
		rideHasStarted = 0;
		CF.getBlocksOpen(rideLocation, 10);
	}
	public void loadConfiguration() {
		getConfig().options().copyDefaults(true);
		saveDefaultConfig();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player player = (Player) sender;
		if (player.hasPermission("frisbee.admin")) {
			if (cmd.getName().equalsIgnoreCase("frisbee")) {
				reloadConfig();
				if (args.length == 0) {
				}
				if (args.length == 2) {
					if (args[0].equalsIgnoreCase("set")) {
						if (!(listenForPlayers.contains(player.getName()))) {
							listenForPlayers.add(player.getName());
							player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "[Frisbee]" + ChatColor.GOLD
									+ " Right click the center floor block of the middle pole.");
							rideName = args[1];
						}
					}
					if (args[0].equalsIgnoreCase("delete")) {
						Bukkit.getScheduler().cancelTask(runnableID);
						rideHasStarted = 0;
						RF.delete();
					}
					if (args[0].equalsIgnoreCase("start")) {
						if (rideHasStarted != 1) {
							if (rideHasStarted != 2) {
								rideName = args[1];
								loadConfiguration();
								rideHasStarted = 1;
						        List rideLocationList = getConfig().getList("Frisbees." + rideName);

						        double x = Integer.parseInt(String.valueOf(rideLocationList.get(0))) + 0.5;
						        double y = Integer.parseInt(String.valueOf(rideLocationList.get(1))) + 1;
						        double z = Integer.parseInt(String.valueOf(rideLocationList.get(2))) + 0.5;
						        World worldName = Bukkit.getWorld(String.valueOf(rideLocationList.get(3)));

								rideLocation = new Location(worldName, x, y, z);
								CF.getBlocksOpen(rideLocation, 5);

								RF = new RideFrisbee(rideLocation);
								RF.spawn();
								
                                reloadConfig();
                                List<Double> heightList = (List<Double>) getConfig().getList("Heights");
                                List<Double> spinList = (List<Double>) getConfig().getList("Spins");
                                final double[] heightPercentArray = new double[heightList.size()];
                                final double[] spinArray = new double[heightList.size()];
                                for (int i = 0; i < heightList.size(); i++) {
                                    heightPercentArray[i] = heightList.get(i);
                                    spinArray[i] = spinList.get(i);
                                }
                                
								new BukkitRunnable() {
									int spin = 0;
									int swing = 0;
								    
									int stage = 0;
									
									@Override
									public void run() {
										runnableID = this.getTaskId();
										swing %= 360;
										if(swing == 0 || swing == 180){
											stage += 1;
											stage %= heightPercentArray.length;
											if(stage == 0){
												Bukkit.getScheduler().cancelTask(this.getTaskId());
												RF.delete();
												rideHasStarted = 0;
												return;
											}
										}
										double pendulum = Math.sin(Math.toRadians(swing)) * 90;
										pendulum *= heightPercentArray[stage];
										RF.animate(pendulum, spin, 0);
										spin += spinArray[stage];
										swing++;
										if (heightPercentArray[stage] > 0){
											CF.getBlocksClose(rideLocation, 5);
										}
										
									}
								}.runTaskTimer(this, 0, 1);								
							}
						}
					}
				} else {
					player.sendMessage(
							ChatColor.AQUA + "" + ChatColor.BOLD + "[Frisbee]" + ChatColor.GOLD + " Incorrect Format");
				}
			}
		} else {
			player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "[Frisbee]" + ChatColor.RED + " No Permission!");
		}
		return true;
	}

	@EventHandler
	public void blockClick(PlayerInteractEvent e) {
		reloadConfig();
		Player p = e.getPlayer();
		if (listenForPlayers.contains(p.getName())) {
			if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
				Block b = e.getClickedBlock();
				ArrayList<String> locationList = new ArrayList<String>();
				locationList.add(String.valueOf(b.getLocation().getBlockX()));
				locationList.add(String.valueOf(b.getLocation().getBlockY()));
				locationList.add(String.valueOf(b.getLocation().getBlockZ()));
				locationList.add(String.valueOf(b.getLocation().getWorld().getName()));
				getConfig().set("Frisbees." + (rideName), locationList);
				saveConfig();
				listenForPlayers.remove(p.getName());
				p.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "[Frisbee]" + ChatColor.GOLD
						+ " Successfully added the ride '" + ChatColor.AQUA + "" + ChatColor.BOLD + rideName
						+ ChatColor.RESET + "" + ChatColor.GOLD + "'!");
			}
		}
	}

	@EventHandler
	public void onVehicleEntityCollision(VehicleEntityCollisionEvent event) {
		if (event.getVehicle().getCustomName() != null) {
			if (event.getVehicle().getCustomName().toLowerCase().startsWith("frisbeeminecart")) {
				event.setCollisionCancelled(true);
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
    public void onVehicleExit(VehicleExitEvent event) {
        if (event.getVehicle().getCustomName() != null) {
            if (event.getVehicle().getCustomName().toLowerCase()
                    .startsWith("frisbeeminecart")) {
                event.setCancelled(true);
            }
        }
    }

	@EventHandler
	public void armorStandHeadRemove(PlayerArmorStandManipulateEvent event) {
		if (event.getArmorStandItem().getAmount() == 2) {
			event.setCancelled(true);
		}
	}
}
