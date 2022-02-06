package com.talesu.luminosity.backEnd;

import com.talesu.luminosity.Luminosity;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public interface Logic {
    static void loadPlayerData(Player player) {
        final UUID uuid = player.getUniqueId();
        if (!SQL.exists(uuid)) SQL.addRecord(player);
        String data = SQL.getProfData(player.getUniqueId());
        SQL.loadProfSkillz(player.getUniqueId());
        Luminosity.playerData.putIfAbsent(uuid, new HashMap<>());
        if (data != null) {
            for (String str : data.split(";;")) {
                String profName = str.split("=")[0];
                boolean status = Integer.parseInt(str.split("=")[1].split(";")[0]) == 1;
                int exp = Integer.parseInt(str.split("=")[1].split(";")[1]);
                int level = Integer.parseInt(str.split("=")[1].split(";")[2]);
                for (Profession profession : Profession.values()) {
                    if(profession.name.startsWith(profName.split("")[0] + profName.split("")[1])) {
                        if (Luminosity.debug) Bukkit.getServer().getLogger().info("Loading " + profession + " data for " + player.getName());
                        Luminosity.playerData.get(uuid).putIfAbsent(profession, new HashMap<>());
                        Luminosity.playerData.get(uuid).get(profession).put("status", status);
                        Luminosity.playerData.get(uuid).get(profession).put("exp", exp);
                        Luminosity.playerData.get(uuid).get(profession).put("level", level);
                    }
                }
            }
        }
        Luminosity.playerPlacedBlocks.putIfAbsent(player.getUniqueId(), new ArrayList<>());
    }
    static void savePlayerData(Player player) {
        UUID uuid = player.getUniqueId();
        SQL.saveProfData(player);
        SQL.saveProfSkillz(uuid, Luminosity.playerSkillz.get(uuid));
        Luminosity.playerData.remove(uuid);
    }
    static void loadBlockDropData() {
        HashMap<Integer, HashMap<String, Object>> data = SQL.getBlockDropData();
        if (data != null) {
            for (int id : data.keySet()) {
                Material material = Material.valueOf((String) data.get(id).get("Block"));
                String profession = ((String) data.get(id).get("Profession"));
                int level = (int) data.get(id).get("Level");
                int chance = ((int) data.get(id).get("Chance"));
                //if (Luminosity.debug) Bukkit.getServer().getLogger().info("DEBUG: data.get(id).get(\"BlockDrop\") = \n" + ((String) data.get(id).get("BlockDrop")));
                ItemStack drop = itemStackFromString(((String) data.get(id).get("BlockDrop")));
                Luminosity.blockDropData.putIfAbsent(material, new HashMap<>());
                Luminosity.blockDropData.get(material).putIfAbsent(profession, new HashMap<>());
                Luminosity.blockDropData.get(material).get(profession).putIfAbsent(id, new HashMap<>());
                Luminosity.blockDropData.get(material).get(profession).get(id).put("level", level);
                Luminosity.blockDropData.get(material).get(profession).get(id).put("chance", chance);
                Luminosity.blockDropData.get(material).get(profession).get(id).put("drop", drop);
                if (Luminosity.debug) Bukkit.getServer().getLogger().info("DEBUG: Loaded BlockDropData WHERE ID=" + id);
            }
        }
    }
    static void loadRecipeData() {
        HashMap<Integer, HashMap<String, Object>> data = SQL.getBlockDropData();
        if (data != null) {
            for (int id : data.keySet()) {
                String profession = ((String) data.get(id).get("Profession"));
                int level = (int) data.get(id).get("Level");
                ItemStack[] ingredients = invFromString(String.valueOf(data.get(id).get("Ingredients"))).getContents();
                //if (Luminosity.debug) Bukkit.getServer().getLogger().info("DEBUG: data.get(id).get(\"BlockDrop\") = \n" + ((String) data.get(id).get("BlockDrop")));
                ItemStack item = itemStackFromString(((String) data.get(id).get("Item")));
                Luminosity.recipeData.putIfAbsent(profession, new HashMap<>());
                Luminosity.recipeData.get(profession).putIfAbsent(id, new HashMap<>());
                Luminosity.recipeData.get(profession).get(id).put("level", level);
                Luminosity.recipeData.get(profession).get(id).put("ingredients", ingredients);
                Luminosity.recipeData.get(profession).get(id).put("item", item);
                if (Luminosity.debug) Bukkit.getServer().getLogger().info("DEBUG: Loaded RecipeData WHERE ID=" + id);
            }
        }
    }
    static void updateProfData(Player player, String professionName, Boolean status, int lvl, List<Integer> skillz) {
        updateProfData(player, professionName, status);
        updateProfData(player, professionName, lvl);
        updateProfData(player, professionName, skillz);
    }
    static void updateProfData(Player player, String professionName, Boolean status) {
        Luminosity.jobStatus.get(player.getUniqueId()).replace(professionName, status);
    }
    static void updateProfData(Player player, String professionName, Integer lvl) {
        Luminosity.jobLevel.get(player.getUniqueId()).replace(professionName, lvl);
    }
    static void updateProfData(Player player, String professionName, List<Integer> skillz) {
        Luminosity.playerSkillz.get(player.getUniqueId()).replace(professionName, skillz);
    }
    static boolean playerHasProf(Player player, Profession profession) {
        return Luminosity.playerData.get(player.getUniqueId()).containsKey(profession);
    }
    static void addProfExp(Player player, Profession profession, int value) {
        final int exp = ((Integer) Luminosity.playerData.get(player.getUniqueId()).get(profession).get("exp"));
        Luminosity.playerData.get(player.getUniqueId()).get(profession).replace("exp", exp+value);
        if (Luminosity.debug) Bukkit.getServer().getLogger().info("DEBUG: Added " + value +" exp to " + profession.name + " to " + player.getName());
    }
    static String itemStackToString(ItemStack item) {
        YamlConfiguration cfg  = new YamlConfiguration();
        cfg.set("i", item);
        return cfg.saveToString();
    }
    static String invToString(Inventory inv) {
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("size", inv.getSize());
        for (int i = 0; i < inv.getSize(); i++) {
            cfg.set(String.valueOf(i), inv.getItem(i));
        }
        return cfg.saveToString();
    }
    static ItemStack itemStackFromString(String data) {
        YamlConfiguration cfg = new YamlConfiguration();
        try {
            cfg.loadFromString(data);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return cfg.getItemStack("i", null);
    }
    static Inventory invFromString(String data) {
        YamlConfiguration cfg = new YamlConfiguration();
        try {
            cfg.loadFromString(data);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        Inventory inv = Bukkit.createInventory(null, cfg.getInt("size"));
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, cfg.getItemStack(String.valueOf(i)));
        return inv;
    }
    static ArrayList<Profession> getPlayerProfList(Player p) {
        UUID uuid = p.getUniqueId();
        ArrayList<Profession> out = new ArrayList<>();
        for (Profession profession : Profession.values()) {
            if (Luminosity.jobStatus.get(uuid).get(profession)) out.add(profession);
        }
        return out;
    }
    static int addBlockDrop(Material material, String profession, int lvl, int chance, ItemStack drop) {
        Luminosity.blockDropData.computeIfAbsent(material, k -> new HashMap<>());
        Luminosity.blockDropData.get(material).computeIfAbsent(profession, k -> new HashMap<>());
        int id = 0; for (int i : Luminosity.blockDropData.get(material).get(profession).keySet()) if (i >= id) id = i+1;
        Luminosity.blockDropData.get(material).get(profession).computeIfAbsent(id, k -> new HashMap<>());
        Luminosity.blockDropData.get(material).get(profession).get(id).put("level", lvl);
        Luminosity.blockDropData.get(material).get(profession).get(id).put("chance", chance);
        Luminosity.blockDropData.get(material).get(profession).get(id).put("drop", drop.clone());
        try {
            SQL.saveBlockDropData(id, material, profession, lvl, chance, Logic.itemStackToString(drop));
        } catch (Exception e) {
            if (Luminosity.debug) Bukkit.getServer().getLogger().info("[Luminosity] DEBUG: Could not save blockDropData");
            e.printStackTrace();
        }
        return id;
    }
    static List<String> getMaterialList() {
        List<String> out = new ArrayList<>();
        for (Material m : Material.values()) {
            if (m.isBlock()) out.add(m.getData().getName());
        }
        return out;
    }
    static void addDrop(Player player, int skill) {
        UUID uuid = player.getUniqueId();
        Luminosity.playerSkillz.putIfAbsent(uuid, new HashMap<>());
        Luminosity.playerSkillz.get(uuid).putIfAbsent("drop", new ArrayList<>());
        Luminosity.playerSkillz.get(uuid).get("drop").add(skill);
    }
    static Inventory getCraftingMenu(Player player, String profession) {
        if (profession.equals(Profession.MINER.name)) {
            Inventory inv = Bukkit.createInventory(null, 54, ChatColor.DARK_GRAY + ChatColor.BOLD.toString() + "Ore Refinery");
        }
        return null;
    }
}
