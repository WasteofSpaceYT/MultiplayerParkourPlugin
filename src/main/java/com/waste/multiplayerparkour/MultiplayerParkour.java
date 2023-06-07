package com.waste.multiplayerparkour;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public final class MultiplayerParkour extends JavaPlugin implements Listener {

    public Player[] playerList = new Player[100];
    public Checkpoint[] checkpointList = new Checkpoint[100];
    public Location start;
    public Location end;
    public int players = 0;
    public int checkpoints = 0;
    public int editingCheckpoint = -1;
    public Leaderboard[] leaderboards = new Leaderboard[getServer().getWorlds().size()];

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
        Player player = event.getPlayer();
        if (event.getPlayer().getInventory().getItem(event.getNewSlot()).getItemMeta().getDisplayName().equalsIgnoreCase("Checkpoint Stick")) {
            player.sendMessage("Checkpoint stick equipped");
            for (int i = 0; i <= checkpoints; i++) {
                if (checkpointList[i] != null) {
                    player.sendMessage(checkpointList[i].start.toString());
                    Location[] locations = checkpointList[i].getAirBlocks(true, player);
                    player.sendMessage("meep");
                    for (int j = 0; j < locations.length; j++) {
                        if (locations[j] != null) {
                            player.sendMessage(String.valueOf(j));
                        }
                        if (locations[j] != null) {
                            player.spawnParticle(Particle.REDSTONE, locations[j], 1, 0, 0, 0, 0, new Particle.DustOptions(Color.GREEN, 1));
                        }
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
        } else if (label.equalsIgnoreCase("removecheckpoint")) {
            Player player = sender.getServer().getPlayer(sender.getName());
            for (int i = 0; i <= checkpoints; i++) {
                player.sendMessage("Checkpoint " + i);
                if (checkpointList[i] != null) {
                    player.sendMessage(checkpointList[i].start.toString());
                    Location[] locations = checkpointList[i].getAirBlocks(false, player);
                    if (locations[0] != null) {
                        player.sendMessage("Checkpoint " + i + " removed");
                        checkpointList[i] = checkpointList[checkpoints - 1];
                        checkpointList[checkpoints - 1] = null;
                        checkpoints--;
                    } else {
                        player.sendMessage("Please stand in a checkpoint");
                    }
                }
            }
            return false;
        } else if (label.equalsIgnoreCase("checkpointlist")) {
            Player player = sender.getServer().getPlayer(sender.getName());
            for (int i = 0; i <= checkpoints; i++) {
                if (checkpointList[i] != null) {
                    player.sendMessage(checkpointList[i].start.toString());
                }
            }
        } else if (label.equalsIgnoreCase("checkpoint")) {
            Player player = sender.getServer().getPlayer(sender.getName());
            player.sendMessage(String.valueOf(args[0]));
            if (args.length == 0) {
                player.sendMessage("<add | remove | manage>");
                return false;
            } else if (args[0].equalsIgnoreCase("manage")) {
                player.sendMessage("donk");
                Inventory managerInventory = Bukkit.createInventory(null, 54, "Checkpoint Manager");
                addToManager(managerInventory, Material.LIME_STAINED_GLASS, 49, ChatColor.GREEN + "Add Checkpoint", "Click to add a checkpoint");
                player.sendMessage(String.valueOf(checkpoints));
                if (checkpoints > 45) {
                    addToManager(managerInventory, Material.LIME_DYE, 50, ChatColor.GREEN + "Next Page", "");
                }
                populateInventory(managerInventory, checkpointList);
                player.openInventory(managerInventory);
            }
        }
        return false;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equalsIgnoreCase("Checkpoint Manager")) {
            Player player = (Player) event.getWhoClicked();
            if (event.getCurrentItem() == null) {
                return;
            }
            if (event.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.GREEN + "Add Checkpoint")) {
                ItemStack stick = new ItemStack(Material.STICK);
                ItemMeta meta = stick.getItemMeta();
                meta.setDisplayName("Checkpoint Stick");
                meta.setLore(Collections.singletonList("Right click to set a checkpoint"));
                stick.setItemMeta(meta);
                player.getInventory().addItem(stick);
                event.getView().close();
            } else if (event.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.RED + "Remove Checkpoint")) {
                for (int i = 0; i <= checkpoints; i++) {
                    player.sendMessage("Checkpoint " + i);
                    if (checkpointList[i] != null) {
                        player.sendMessage(checkpointList[i].start.toString());
                        Location[] locations = checkpointList[i].getAirBlocks(false, player);
                        if (locations[0] != null) {
                            player.sendMessage("Checkpoint " + i + " removed");
                            checkpointList[i] = checkpointList[checkpoints - 1];
                            checkpointList[checkpoints - 1] = null;
                            checkpoints--;
                        } else {
                            player.sendMessage("Please stand in a checkpoint");
                        }
                    }
                }
            } else if (event.getCurrentItem().getItemMeta().getDisplayName().contains(ChatColor.GOLD + "Checkpoint") && event.getClick() == ClickType.RIGHT) {
                int checkpointIndex = Integer.valueOf(event.getCurrentItem().getItemMeta().getDisplayName().split(" ")[1]);
                checkpointList[checkpointIndex] = null;
                if (checkpointIndex == checkpoints) {
                    checkpoints--;
                }
                populateInventory(event.getInventory(), checkpointList);
            } else if (event.getCurrentItem().getItemMeta().getDisplayName().contains(ChatColor.RED + "Checkpoint") && event.getClick() == ClickType.LEFT) {
                int checkpointIndex = Integer.valueOf(event.getCurrentItem().getItemMeta().getDisplayName().split(" ")[1]);
                ItemStack stick = new ItemStack(Material.STICK);
                ItemMeta meta = stick.getItemMeta();
                meta.setDisplayName("Checkpoint Stick");
                meta.setLore(Collections.singletonList("Right click to set a checkpoint"));
                stick.setItemMeta(meta);
                player.getInventory().addItem(stick);
                editingCheckpoint = checkpointIndex;
                event.getView().close();
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event) {
        if (event.getPlayer().getItemInHand().getItemMeta().getDisplayName().equalsIgnoreCase("Checkpoint Stick") && event.getHand() == EquipmentSlot.HAND) {
            if (start != null && end != null) {
                start = null;
                end = null;
            } else if (start != null) {
                end = event.getClickedBlock().getLocation();
                if (editingCheckpoint == -1) {
                    checkpointList[checkpoints] = new Checkpoint(start, end);
                    checkpoints++;
                } else {
                    checkpointList[editingCheckpoint] = new Checkpoint(start, end);
                    editingCheckpoint = -1;
                }
                event.getPlayer().sendMessage("Checkpoint end set");
                start = null;
                end = null;
                event.getPlayer().getInventory().getItemInMainHand().setAmount(0);
            } else {
                start = event.getClickedBlock().getLocation();
                event.getPlayer().sendMessage("Checkpoint start set");
                System.out.println(start);
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event){
        Player player = event.getPlayer();
        for (int i = 0; i <= checkpoints; i++) {
            player.sendMessage("Checkpoint " + i);
            if (checkpointList[i] != null) {
                player.sendMessage(checkpointList[i].start.toString());
                Location[] locations = checkpointList[i].getAirBlocks(false, player);
                if (locations[0] != null) {
                    player.sendMessage("Reached Checkpoint " + i);
                    for(int j=0; j < getServer().getWorlds().size(); j++){
                        if(getServer().getWorlds().get(j).getName().equalsIgnoreCase(event.getPlayer().getWorld().getName())){
                            for(int k=0; k < leaderboards[j].participants.length; k++){
                                if(leaderboards[j].participants[k].name == player.getName()){
                                    leaderboards[j].participants[k].lastCheckpoint = i;

                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public Inventory addToManager(Inventory manager, Material item, int slot, String name, String lore) {
        ItemStack itemStack = new ItemStack(item);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(name);
        if (lore.contains("\n")) {
            String[] lores = lore.split("\n");
            itemMeta.setLore(Arrays.asList(lores));
        } else {
            itemMeta.setLore(Collections.singletonList(lore));
        }
        itemStack.setItemMeta(itemMeta);
        manager.setItem(slot, itemStack);
        return manager;
    }

    public Inventory populateInventory(Inventory inventory, Checkpoint[] checkpointList) {
        Checkpoint[] checkpointslst = checkpointList.clone();
        for (int i = 0; i < checkpointslst.length; i++) {
            if (checkpointslst[i] != null) {
                System.out.println(checkpointslst[i]);
                addToManager(inventory, Material.GOLD_BLOCK, i, ChatColor.GOLD + "Checkpoint " + i, "Click to edit checkpoint " + i + "\n" + "Right-click to remove checkpoint " + i);
            } else if (i != checkpointslst.length - 1) {
                if (checkpointslst[i + 1] != null || i < checkpoints) {
                    addToManager(inventory, Material.REDSTONE_BLOCK, i, ChatColor.RED + "Checkpoint " + i, "Click to add checkpoint " + i);
                }
            }
        }
        return inventory;
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

            int absmaxX;
            int absmaxY;
            int absmaxZ;

            int absminX;
            int absminY;
            int absminZ;

            if (x1 > x2) {
                maxX = x1;
                minX = x2;
            } else {
                maxX = x2;
                minX = x1;
            }

            if (y1 > y2) {
                maxY = y1;
                minY = y2;
            } else {
                maxY = y2;
                minY = y1;
            }

            if (z1 > z2) {
                maxZ = z1;
                minZ = z2;
            } else {
                maxZ = z2;
                minZ = z1;
            }

            if (Math.abs(x1) > Math.abs(x2)) {
                absmaxX = Math.abs(x1);
                absminX = Math.abs(x2);
            } else {
                absmaxX = Math.abs(x2);
                absminX = Math.abs(x1);
            }

            if (Math.abs(y1) > Math.abs(y2)) {
                absmaxY = Math.abs(y1);
                absminY = Math.abs(y2);
            } else {
                absmaxY = Math.abs(y2);
                absminY = Math.abs(y1);
            }

            if (Math.abs(z1) > Math.abs(z2)) {
                absmaxZ = Math.abs(z1);
                absminZ = Math.abs(z2);
            } else {
                absmaxZ = Math.abs(z2);
                absminZ = Math.abs(z1);
            }


            player.sendMessage("maxX: " + absmaxX + " maxY: " + absmaxY + " maxZ: " + absmaxZ);
            player.sendMessage("minX: " + absminX + " minY: " + absminY + " minZ: " + absminZ);
            int area = (absmaxX - absminX) * (absmaxY - absminY) * (absmaxZ - absminZ);
            int airblocks = 0;
            player.sendMessage("area: " + area);
            Location[] locations = new Location[area * 2];

            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        player.sendMessage("x: " + x + " y: " + y + " z: " + z);
                        // Perform your search or operations on the current (x, y, z) coordinate
                        // Example: Print the coordinate
                        if (start.getWorld().getBlockAt(x, y, z).getType() == Material.AIR && actuallyAir) {
                            locations[airblocks] = start.getWorld().getBlockAt(x, y, z).getLocation();
                            airblocks++;
                        } else if (!actuallyAir) {
                            Location playerloc = player.getLocation().clone();
                            playerloc.setY(playerloc.getBlockY());
                            playerloc.setX(playerloc.getBlockX());
                            playerloc.setZ(playerloc.getBlockZ());
                            playerloc.setPitch(0);
                            playerloc.setYaw(0);
                            if (playerloc.equals(new Location(start.getWorld(), x, y, z))) {
                                return new Location[]{new Location(start.getWorld(), x, y, z)};
                            }
                        }
                    }
                }
            }
            return locations;
        }
    }

    public class Leaderboard {
        public Participant[] participants;
        public int time;
        public void reCalcLeaderboard(){
            Arrays.sort(participants, new Comparator<Participant>() {
                @Override
                public int compare(Participant o1, Participant o2) {
                    return o1.time - o2.time;
                }
            });
        }
        public Leaderboard(int participants) {
            this.participants = new Participant[participants];
        }
    }

    public class Participant {
        public String name;
        public int time;
        public int lastCheckpoint;
        public HashMap<Integer, Integer> checkpointTimes;

        public Participant(String name, int time) {
            this.name = name;
            this.time = time;
            this.lastCheckpoint = 0;
            this.checkpointTimes = new HashMap<>();
            for(int i = 0; i < checkpoints; i++) {
                this.checkpointTimes.put(i + 1, -1);
            }
        }
    }
}
