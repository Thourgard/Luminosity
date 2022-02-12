package com.talesu.luminosity.backEnd;

import com.talesu.luminosity.Luminosity;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

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
        Player player = e.getPlayer();
        boolean prof;
        boolean drop = true;
        for (Profession profession : Logic.getPlayerProfList(player)) {
            if (Profession.getHarvesters().contains(profession)) {
                for (Material mat : profession.materialList) {
                    if (e.getBlock().getBlockData().getMaterial().equals(mat)) {
                        prof = true;
                        List<Location> locList = Luminosity.playerPlacedBlocks.get(e.getPlayer().getUniqueId());
                        if (locList != null) {
                            for (Location loc : Luminosity.playerPlacedBlocks.get(e.getPlayer().getUniqueId())) {
                                if (e.getBlock().getLocation().equals(loc))  {
                                    prof = false; break;
                                }
                            }
                        }
                        if ((e.getPlayer().getInventory().getItemInMainHand().hasItemMeta() && e.getPlayer().getInventory().getItemInMainHand().getItemMeta().hasEnchants() &&
                                e.getPlayer().getInventory().getItemInMainHand().getItemMeta().getEnchants().containsKey(Enchantment.SILK_TOUCH)) ||
                                e.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.SHEARS)) {
                            drop = false;
                            prof = false;
                        }
                        if (prof) {
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    Logic.addProfExp(player, profession, 1);
                                }
                            }.runTaskLater(Luminosity.getInstance(), 1);
                        }
                        break;
                    }
                }
                break;
            }
        }
        if (drop) {
            Material material = e.getBlock().getBlockData().getMaterial();
            for (Profession profession : Profession.values()) {
                for (int id : Luminosity.playerSkillz.get(player.getUniqueId()).get(profession).get("drop")) {
                    if (profession.hasDrop(id)) {
                        if (material.equals(profession.getDrop(id).get("material"))) {
                            if (((int) profession.getDrop(id).get("level")) <= profession.getLevel(e.getPlayer())) {
                                int chance = new Random().nextInt(100);
                                if (chance <= ((int) profession.getDrop(id).get("chance"))) {
                                    player.getWorld().dropItem(e.getBlock().getLocation(), ((ItemStack) profession.getDrop(id).get("item")));
                                }
                            }
                        }
                        break;
                    }
                }
                break;
            }
        }
    }
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        boolean prof = false;
        for (Material mat : Luminosity.profDropBlocks) {
            if (e.getBlock().getBlockData().getMaterial().equals(mat)) prof = true; break;
        }
        if (prof)  {
            Luminosity.playerPlacedBlocks.get(e.getPlayer().getUniqueId()).add(e.getBlock().getLocation());
        }
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        ItemStack item = null;
        if (e.getCurrentItem() != null) item = e.getCurrentItem();
        if(e.getView().getTitle().equals("Recipe Ingredients") && e.getInventory().getItem(45).getDurability()==2356) {
            if (e.getSlot() > 44) e.setCancelled(true);
            if (e.getSlot()==49) {
                if (e.getCursor() != null && e.getCursor().getType() != Material.AIR) {
                    e.getInventory().setItem(e.getSlot(), e.getCursor().clone());
                } else {
                    e.getInventory().setItem(e.getSlot(), Logic.createUIButton(new ItemStack(Material.GRAY_STAINED_GLASS_PANE), ChatColor.GRAY + "Put the result here"));
                }
                return;
            }
            if(e.getSlot() == 50) { //Done button pressed
                ArrayList<ItemStack> items = new ArrayList<>();
                for (int i = 0; i<45; i++) {
                    if (e.getInventory().getItem(i) != null) items.add(e.getInventory().getItem(i));
                }
                ItemStack[] t = items.toArray(new ItemStack[0]);
                Bukkit.getServer().getLogger().info("DEBUG: item = " + items);
                Bukkit.getServer().getLogger().info("DEBUG: t = " + Arrays.toString(t));
                Profession profession = Profession.getProfession(((String) Luminosity.tempoBin.get(e.getWhoClicked().getUniqueId()).get("recipeProfession")));
                int level = Integer.parseInt((String) Luminosity.tempoBin.get(e.getWhoClicked().getUniqueId()).get("recipeLevel"));
                e.getWhoClicked().sendMessage("The ID if the new recipe =" + profession.addRecipe(level, items.toArray(new ItemStack[0]),e.getInventory().getItem(49)));
                Luminosity.tempoBin.remove(e.getWhoClicked().getUniqueId());
                e.getWhoClicked().closeInventory();
                return;
            } else if (e.getSlot() == 48) { // close button pressed
                e.getWhoClicked().closeInventory();
                for (int i = 0; i<=44; i++) {
                    if (e.getInventory().getItem(i) != null && !e.getInventory().getItem(i).getType().equals(Material.AIR)) {
                        e.getWhoClicked().getInventory().addItem(e.getInventory().getItem(i));
                        e.getWhoClicked().closeInventory();
                        return;
                    }
                }
                Luminosity.tempoBin.remove(e.getWhoClicked().getUniqueId());
            }
        }
        e.getInventory();
        if (!e.getInventory().getType().equals(InventoryType.PLAYER)) {
            if (e.getInventory().getItem(0) != null) {
                if (e.getInventory().getItem(0).getItemMeta().getPersistentDataContainer().has(Luminosity.myProfessionNameKey, PersistentDataType.STRING)) {
                    if (e.getClickedInventory() != null && !e.getClickedInventory().getType().equals(InventoryType.PLAYER)) {
                        e.setCancelled(true);
                        if (item != null) {
                            if (e.getCurrentItem().getItemMeta().getPersistentDataContainer().has(Luminosity.mySkillIDKey)) {
                                int id = item.getItemMeta().getPersistentDataContainer().get(Luminosity.mySkillIDKey, PersistentDataType.INTEGER);
                                Profession profession = Profession.getProfession(item.getItemMeta().getPersistentDataContainer().get(Luminosity.myProfessionNameKey, PersistentDataType.STRING));
                                e.getWhoClicked().openInventory(Logic.getConfirmationPrompt(profession, id, ((Player) e.getWhoClicked())));
                                return;
                            }
                        }
                    }
                }
            }
        }
        if (e.getView().getTitle().contains(ChatColor.GRAY + ChatColor.BOLD.toString() + "Confirmation prompt")) {
            if (e.getClickedInventory() != null && !e.getClickedInventory().getType().equals(InventoryType.PLAYER)) {
                e.setCancelled(true);
                Profession profession = null;
                if (item != null) {
                    if (item.getItemMeta().getPersistentDataContainer().has(Luminosity.myProfessionNameKey, PersistentDataType.STRING)) {
                        profession = Profession.getProfession(item.getItemMeta().getPersistentDataContainer().get(Luminosity.myProfessionNameKey, PersistentDataType.STRING));
                    }
                    if (e.getSlot() == 21) {
                        Bukkit.getServer().getLogger().info("DEBUG: profession = " + item.getItemMeta().getPersistentDataContainer().get(Luminosity.myProfessionNameKey, PersistentDataType.STRING));
                        if (profession != null) e.getWhoClicked().openInventory(Logic.getCraftingMenu(((Player) e.getWhoClicked()), profession));
                        return;
                    }
                    if (e.getSlot() == 23 && item.getType().equals(Material.LIME_DYE)) {
                        Bukkit.getServer().getLogger().info("DEBUG: Confirm button pressed");
                        boolean a = true;
                        for (int i = 9; i < 18; i++) {
                            if (e.getInventory().getItem(i) != null) {
                                if (e.getInventory().getItem(i).getType().equals(Material.RED_STAINED_GLASS)) a = false;
                            }
                        }
                        if (a) {
                            if (item.getItemMeta().getPersistentDataContainer().has(Luminosity.mySkillIDKey, PersistentDataType.INTEGER)) {
                                int id = item.getItemMeta().getPersistentDataContainer().get(Luminosity.mySkillIDKey, PersistentDataType.INTEGER);
                                if (profession != null) Logic.completeRecipe(profession, id, ((Player) e.getWhoClicked()));
                            }
                            e.getWhoClicked().closeInventory();
                        }
                    }
                }
            }
        }
    }
    @EventHandler
    public void onRightClick(PlayerInteractEvent e) {
        if (e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            ItemStack item = e.getPlayer().getInventory().getItemInMainHand();
            if (item.hasItemMeta()) {
                if (item.getItemMeta().getDisplayName().contains("Codex Page") && item.getItemMeta().getPersistentDataContainer().has(Luminosity.mySkillIDKey, PersistentDataType.INTEGER)) {
                    String type = item.getItemMeta().getPersistentDataContainer().get(Luminosity.mySkillTypeKey, PersistentDataType.STRING);
                    Profession profession = Profession.getProfession(item.getItemMeta().getPersistentDataContainer().get(Luminosity.myProfessionNameKey, PersistentDataType.STRING));
                    int id = item.getItemMeta().getPersistentDataContainer().get(Luminosity.mySkillIDKey, PersistentDataType.INTEGER);
                    String success = ChatColor.translateAlternateColorCodes('&', Luminosity.getInstance().getConfig().getString("codexPageResponse.success").replace("$PROFESSION_NAME$", profession.name));
                    String failure = ChatColor.translateAlternateColorCodes('&', Luminosity.getInstance().getConfig().getString("codexPageResponse.fail"));
                    if (profession.giveSkill(e.getPlayer(), id, type))  {
                        e.getPlayer().sendMessage(success);
                        item.setAmount((item.getAmount()-1));
                        e.getPlayer().getInventory().setItemInMainHand(item);
                    }
                    else e.getPlayer().sendMessage(failure);
                }
            }
        }
    }
}
