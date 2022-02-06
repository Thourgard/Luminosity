package com.talesu.luminosity.backEnd;

import com.talesu.luminosity.Luminosity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Random;

public class PlayerListener implements Listener {
    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent e) {
        Logic.loadPlayerData(e.getPlayer());
        if (Luminosity.debug) Bukkit.getServer().getLogger().info("DEBUG: Player Join Event Complete");
    }
    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent e) {
        Logic.savePlayerData(e.getPlayer());
        if (Luminosity.debug) Bukkit.getServer().getLogger().info("DEBUG: Player Quit Event Complete");
    }
    @EventHandler
    public void onPlayerDisconnect(PlayerKickEvent e) {
        Logic.savePlayerData(e.getPlayer());
    }
    @EventHandler
    public void onBlockBroken(BlockBreakEvent e) {
        Player p = e.getPlayer();
        boolean prof = false;
        boolean drop = true;
        boolean mine = false;
        boolean dig = false;
        boolean harvest = false;
        for (Profession profession : Logic.getPlayerProfList(p)) {
            if (Profession.getHarvesters().contains(profession)) {
                for (Material mat : profession.materialList) {
                    if (e.getBlock().getBlockData().getMaterial().equals(mat)) {
                        switch (profession) {
                            case MINER: mine = true; continue;
                            case HERBALIST: harvest = true; continue;
                            case ARCHEOLOGIST: dig = true;
                        }
                        prof = true;
                        break;
                    }
                }
                break;
            }
        }
        List<Location> locList = Luminosity.playerPlacedBlocks.get(e.getPlayer().getUniqueId());
        if (locList != null) {
            for (Location loc : Luminosity.playerPlacedBlocks.get(e.getPlayer().getUniqueId())) {
                if (e.getBlock().getLocation().equals(loc)) prof = false;
            }
        }
        if ((e.getPlayer().getInventory().getItemInMainHand().hasItemMeta() && e.getPlayer().getInventory().getItemInMainHand().getItemMeta().hasEnchants() &&
                e.getPlayer().getInventory().getItemInMainHand().getItemMeta().getEnchants().containsKey(Enchantment.SILK_TOUCH)) ||
                e.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.SHEARS)) {
                drop = false;
                prof = false;
        }
        if (drop) {
            Material material = e.getBlock().getBlockData().getMaterial();
            if (Luminosity.blockDropData.get(material) != null) {
                for (Material mat : Luminosity.blockDropData.keySet()) {
                    if (mat == material) {
                        for (Profession profession : Logic.getPlayerProfList(e.getPlayer())) {
                            for (Profession r : Luminosity.blockDropData.get(material).keySet()) {
                                if (r.equals(profession)) {
                                    if (Luminosity.blockDropData.get(material).get(profession) != null) {
                                        if (!Luminosity.blockDropData.get(material).get(profession).keySet().isEmpty()) {
                                            for (int id : Luminosity.blockDropData.get(material).get(profession).keySet()) {
                                                if (Luminosity.playerSkillz.get(p.getUniqueId()) != null) {
                                                    if (Luminosity.playerSkillz.get(p.getUniqueId()).get("drop") != null) {
                                                        for (int g : Luminosity.playerSkillz.get(p.getUniqueId()).get("drop")) {
                                                            if (id == g) {
                                                                if ((Integer) Luminosity.blockDropData.get(material).get(profession).get(id).get("level") <= ((Integer) Luminosity.playerData.get(p.getUniqueId()).get(profession).get("level"))) {
                                                                    int chance = new Random().nextInt(100);
                                                                    if (chance <= ((Integer) Luminosity.blockDropData.get(material).get(profession).get(id).get("chance"))) {
                                                                        p.getWorld().dropItem(e.getBlock().getLocation(),
                                                                                ((ItemStack) Luminosity.blockDropData.get(material).get(profession).get(id).get("drop")));
                                                                    }
                                                                    break;
                                                                }
                                                            }
                                                        }
                                                        break;
                                                    }
                                                }
                                            }
                                            break;
                                        }
                                    }

                                }
                            }
                        }
                        break;
                    }
                }
            }
        }
        if (prof) {
            for (Profession profession : Logic.getPlayerProfList(e.getPlayer())) {
                if (mine || harvest || dig) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Logic.addProfExp(p, profession, 1);
                        }
                    }.runTaskLater(Luminosity.getInstance(), 1);
                    break;
                }
            }
        }
    }
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        boolean prof = false;
        for (Material mat : Luminosity.profDropBlocks) {
            if (e.getBlock().getBlockData().getMaterial().equals(mat)) prof = true;
        }
        if (prof)  {
            Luminosity.playerPlacedBlocks.get(e.getPlayer().getUniqueId()).add(e.getBlock().getLocation());
        }
    }
}
