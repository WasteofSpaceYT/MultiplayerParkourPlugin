package com.waste.multiplayerparkour;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

public final class MultiplayerParkour extends JavaPlugin implements Listener {

    public Player[] playerList = new Player[100];
    public Checkpoint[] checkpointList = new Checkpoint[100];
    public Location start;
    public Location end;
    public int players = 0;
    public int checkpoints = 0;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        playerList[players] = player;
        players++;
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        for (int i = 0; i <= players; i++) {
            if (playerList[i] == player) {
                playerList[i] = playerList[players - 1];
                playerList[players - 1] = null;
            }
        }
    }

    @EventHandler
    public void onPlayerInventoryChange(PlayerItemHeldEvent event) {
        if(event.getPlayer().getItemInHand() == null){
            return;
        }
        Player player = event.getPlayer();
        if (event.getPlayer().getInventory().getItem(event.getNewSlot()).getItemMeta().getDisplayName().equalsIgnoreCase("Checkpoint Stick")) {
            player.sendMessage("Checkpoint stick equipped");
            for (int i = 0; i <= checkpoints; i++) {
                if(checkpointList[i] != null) {
                    player.sendMessage(checkpointList[i].start.toString());
                    Location[] locations = checkpointList[i].getAirBlocks(true, null);
                    player.sendMessage(Arrays.toString(locations));
                    for (int j = 0; j < locations.length; j++) {
                        player.spawnParticle(Particle.REDSTONE, locations[j], 1, 0, 0, 0, 0, new Particle.DustOptions(Color.GREEN, 1));
                    }
                }
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("checkpointstick")) {
            ItemStack stick = new ItemStack(Material.STICK);
            ItemMeta meta = stick.getItemMeta();
            meta.setDisplayName("Checkpoint Stick");
            meta.setLore(Collections.singletonList("Right click to set a checkpoint"));
            stick.setItemMeta(meta);
            ((Player) sender).getInventory().addItem(stick);
            return true;
        } else if(label.equalsIgnoreCase("removecheckpoint")){
            Player player = sender.getServer().getPlayer(sender.getName());
            for (int i = 0; i <= checkpoints; i++) {
                player.sendMessage("Checkpoint " + i);
                if(checkpointList[i] != null) {
                    player.sendMessage(checkpointList[i].start.toString());
                    Location[] locations = checkpointList[i].getAirBlocks(false, player);
                    if(locations[0] != null){
                        checkpointList[i] = checkpointList[checkpoints - 1];
                        checkpointList[checkpoints - 1] = null;
                        checkpoints--;
                    }
                }
            }
            player.sendMessage("Please stand in a checkpoint");
            return false;
        } else if(label.equalsIgnoreCase("checkpointlist")){
            Player player = sender.getServer().getPlayer(sender.getName());
            for (int i = 0; i <= checkpoints; i++) {
                if(checkpointList[i] != null) {
                    player.sendMessage(checkpointList[i].start.toString());
                }
            }
        }
        return false;
    }
    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event) {
        if (event.getPlayer().getItemInHand().getItemMeta().getDisplayName().equalsIgnoreCase("Checkpoint Stick") && event.getHand() == EquipmentSlot.HAND) {
            if(start != null && end != null){
                start = null;
                end = null;
            }
            else if(start != null){
                end = event.getClickedBlock().getLocation();
                checkpointList[checkpoints] = new Checkpoint(start, end);
                checkpoints++;
                event.getPlayer().sendMessage("Checkpoint end set");
                start = null;
                end = null;
            } else {
                start = event.getClickedBlock().getLocation();
                event.getPlayer().sendMessage("Checkpoint start set");
                System.out.println(start);
            }
        }
    }

    public class Checkpoint {
        public Location start;
        public Location end;

        public Checkpoint(Location start, Location end) {
            this.start = start;
            this.end = end;
        }

        public Location[] getAirBlocks(boolean actuallyAir, Player player) {
            System.out.println("getAirBlocks called");
            int x1 = start.getBlockX();
            int y1 = start.getBlockY();
            int z1 = start.getBlockZ();

            int x2 = end.getBlockX();
            int y2 = end.getBlockY();
            int z2 = end.getBlockZ();

            int maxX;
            int maxY;
            int maxZ;

            int minX;
            int minY;
            int minZ;

            if(x1 > x2){
                maxX = x1;
                minX = x2;
            } else {
                maxX = x2;
                minX = x1;
            }

            if(y1 > y2){
                maxY = y1;
                minY = y2;
            } else {
                maxY = y2;
                minY = y1;
            }

            if(z1 > z2){
                maxZ = z1;
                minZ = z2;
            } else {
                maxZ = z2;
                minZ = z1;
            }

            int airblocks = 0;

            Location[] locations = new Location[100];

            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        // Perform your search or operations on the current (x, y, z) coordinate
                        // Example: Print the coordinate
                        if(start.getWorld().getBlockAt(x, y, z).getType() == Material.AIR && actuallyAir){
                            locations[airblocks] = start.getWorld().getBlockAt(x, y, z).getLocation();
                            airblocks++;
                        } else if (!actuallyAir) {
                            if(player.getLocation() == new Location(start.getWorld(), x, y, z)){
                                return new Location[]{new Location(start.getWorld(), x, y, z)};
                            }
                        }
                    }
                }
            }
            return locations;
        }
    }
}
