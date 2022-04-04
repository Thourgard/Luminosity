package com.talesu.luminosity.backEnd;

import com.talesu.luminosity.Luminosity;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public interface Logic {
    static void loadData() {
        loadRecipeData();
        SQL.loadBlockDropData();
        SQL.loadPlayerPlacedBlocks();
        Profession.initialize();
        if (!Bukkit.getServer().getOnlinePlayers().isEmpty()) {
            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                SQL.addRecord(player);
                SQL.loadProfData(player);
            }
        }
    }
    static void saveData() {
        if (!Bukkit.getServer().getOnlinePlayers().isEmpty()) Bukkit.getServer().getOnlinePlayers().forEach(Logic::savePlayerData);
        SQL.saveAllBlockDropData();
        SQL.saveRecipeData();
        SQL.savePlayerPlacedBlocks();
    }
    static void savePlayerData(Player player) {
        UUID uuid = player.getUniqueId();
        SQL.saveProfData(player);
        SQL.saveProfSkillz(uuid, Luminosity.playerSkillz.get(uuid));
        Luminosity.playerData.remove(uuid);
        Luminosity.playerSkillz.remove(uuid);
    }
    private static void loadRecipeData() {
        HashMap<Integer, HashMap<String, Object>> data = SQL.getRecipeData();
        for (Profession profession : Profession.values()) {
            Luminosity.recipeData.putIfAbsent(profession, new HashMap<>());
        }
        if (data != null) {
            for (int id : data.keySet()) {
                Profession profession = Profession.getProfession(((String) data.get(id).get("Profession")));
                int level = (int) data.get(id).get("Level");
                ItemStack[] ingredients = itemStackArrayFromString(String.valueOf(data.get(id).get("Ingredients")));
                //if (Luminosity.debug) Bukkit.getServer().getLogger().info("DEBUG: data.get(id).get(\"BlockDrop\") = \n" + ((String) data.get(id).get("BlockDrop")));
                ItemStack item = getItemStack(((String) data.get(id).get("Item")));
                Luminosity.recipeData.get(profession).putIfAbsent(id, new HashMap<>());
                Luminosity.recipeData.get(profession).get(id).put("level", level);
                Luminosity.recipeData.get(profession).get(id).put("ingredients", ingredients);
                Luminosity.recipeData.get(profession).get(id).put("item", item);
                if (Luminosity.debug)
                    Bukkit.getServer().getLogger().info("DEBUG: Loaded RecipeData WHERE ID=" + id);
            }
        } else {
            Bukkit.getServer().getLogger().info("[Luminosity] RecipeData is null! Creating empty RecipeData...");
            for (Profession profession : Profession.values()) Luminosity.recipeData.putIfAbsent(profession, new HashMap<>());
        }
    }
    static void updateProfData(Player player, Profession profession, Boolean status, int lvl) {
        updateProfData(player, profession, status);
        updateProfData(player, profession, lvl);
    }
    static void updateProfData(Player player, Profession profession, Boolean status) {
        Luminosity.playerData.get(player.getUniqueId()).get(profession).replace("status", status);
    }
    static void updateProfData(Player player, Profession profession, Integer level) {
        Luminosity.playerData.get(player.getUniqueId()).get(profession).replace("level", level);
    }
    static void updateProfData(Player player, Profession profession, String skillType, List<Integer> skillz) {
        Luminosity.playerSkillz.get(player.getUniqueId()).get(profession).replace(skillType, skillz);
    }
    static boolean playerHasProf(Player player, Profession profession) {
        return Luminosity.playerData.get(player.getUniqueId()).containsKey(profession);
    }
    static void addProfExp(Player player, Profession profession, int value) {
        final int exp = ((Integer) Luminosity.playerData.get(player.getUniqueId()).get(profession).get("exp"));
        Luminosity.playerData.get(player.getUniqueId()).get(profession).replace("exp", exp+value);
        if (Luminosity.debug) Bukkit.getServer().getLogger().info("DEBUG: Added " + value +" exp to " + profession.getName() + " to " + player.getName());
    }
    static String itemStackToString(ItemStack item) {
        YamlConfiguration cfg  = new YamlConfiguration();
        cfg.set("i", item);
        return cfg.saveToString();
    }
    static String itemStackArrayToString(ItemStack[] itemStacks) {
        YamlConfiguration cfg = new YamlConfiguration();
        for (int i = 0; i < itemStacks.length; i++) {
            cfg.set(String.valueOf(i), itemStacks[i]);
        }
        cfg.set("length", itemStacks.length);
        return cfg.saveToString();
    }
    static ItemStack getItemStack(String data) {
        YamlConfiguration cfg = new YamlConfiguration();
        try {
            cfg.loadFromString(data);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return cfg.getItemStack("i", null);
    }
    static ItemStack[] itemStackArrayFromString(String data) {
        YamlConfiguration cfg = new YamlConfiguration();
        try {
            cfg.loadFromString(data);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        ArrayList<ItemStack> out = new ArrayList<>();
        for (int i = 0; i < ((Integer) cfg.get("length")); i++) {
            out.add(cfg.getItemStack(String.valueOf(i)));
        }
        return out.toArray(new ItemStack[0]);
    }
    static ArrayList<Profession> getPlayerProfList(Player p) {
        ArrayList<Profession> out = new ArrayList<>();
        for (Profession profession : Profession.values()) {
            if (profession.getStatus(p)) out.add(profession);
        }
        return out;
    }
    static int addBlockDrop(Material material, Profession profession, int lvl, int chance, ItemStack drop) {
        Luminosity.blockDropData.putIfAbsent(profession, new HashMap<>());
        int id = 0; for (int i : Luminosity.blockDropData.get(profession).keySet()) if (i >= id) id = i+1;
        Luminosity.blockDropData.get(profession).putIfAbsent(id, new HashMap<>());
        Luminosity.blockDropData.get(profession).get(id).put("level", lvl);
        Luminosity.blockDropData.get(profession).get(id).put("chance", chance);
        Luminosity.blockDropData.get(profession).get(id).put("drop", drop.clone());
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
            if (m.isBlock()) out.add(m.toString());
        }
        return out;
    }
    static ItemStack createUIButton(ItemStack itemStack, int model, String displayName, int durability) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
        itemMeta.setCustomModelData(model);
        itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemStack.setItemMeta(itemMeta);
        itemStack.setDurability(((short) durability));
        itemStack.getItemMeta().setUnbreakable(true);
        return itemStack;
    }
    static ItemStack createUIButton(ItemStack itemStack, String displayName, int durability) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
        itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemStack.setItemMeta(itemMeta);
        itemStack.setDurability(((short) durability));
        itemStack.getItemMeta().setUnbreakable(true);
        return itemStack;
    }
    static ItemStack createUIButton(ItemStack itemStack, String displayName) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
        itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemStack.setItemMeta(itemMeta);
        itemStack.getItemMeta().setUnbreakable(true);
        return itemStack;
    }
    static ArrayList<String> sortStringArrayByWeight(ArrayList<String> input, boolean lore) {
        LinkedList<String> output;
        if (lore) {
            output = new LinkedList<>();
            for (int i = input.indexOf(ChatColor.GRAY + "--- Materials ---")+1; i<input.size(); i++) {
                output.add(input.get(i));
            }
        } else output = new LinkedList<>(input);
        HashMap<String, ChatColor> map = new HashMap<>();
        for (String str : output) {
            for (ChatColor colour : ChatColor.values()) {
                if (str.contains(colour.toString())) {
                    if (colour.equals(ChatColor.BOLD)) map.put(str, colour);
                    else map.putIfAbsent(str, colour);
                    break;
                }
            }
        }
        if (output.size()>1) {
            for (int i = 0; i < output.size() - 1; i++) {
                Bukkit.getServer().getLogger().info("DEBUG: String= " + output.get(i) + " (i=" + i + "; comparingTo=" + (i+1) + "; lastIndex=" + (output.size()-1) + ")");
                int iWeight = Luminosity.colourWeight.get(map.get(output.get(i)));
                if (output.get(i).contains(ChatColor.BOLD.toString())) iWeight++;
                if (iWeight > Luminosity.colourWeight.get(map.get(output.get(i+1)))) {
                    output.addLast(output.get(i));
                    output.remove(i);
                    i = -1;
                    Bukkit.getServer().getLogger().info("DEBUG: Check Passed && loop is reset");
                }
            }
        }
        return (new ArrayList<>(output));
    }
    static Inventory getCraftingMenu(Player player, Profession profession) {
        String title;
        switch (profession) {
            case METALLURGIST: title = "Ore Refinery"; break;
            case BLACKSMITH: title = "Smithy"; break;
            case ALCHEMIST: title = "Alchemy Table"; break;
            default: title = "Professions Crafting";
        }
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.DARK_GRAY + ChatColor.BOLD.toString() + title);
        ArrayList<Integer> borders = new ArrayList<>(List.of(0,1,2,3,4,5,6,7,8,9,18,27,36,45,46,47,48,49,50,51,52,53,44,35,26,17));
        ItemStack filler = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.getPersistentDataContainer().set(Luminosity.myProfessionNameKey, PersistentDataType.STRING, profession.getName());
        filler.setItemMeta(fillerMeta);
        for (int i : borders) inv.setItem(i, createUIButton(filler, " "));
        for (int recipe : Luminosity.playerSkillz.get(player.getUniqueId()).get(profession).get("recipe")) { // loop though all the recipes of the player
            if (profession.hasRecipe(recipe)) {
                for (int slot = 10; slot < 44; slot ++) {
                    if (inv.getItem(slot) == null) {
                        ItemStack item = ((ItemStack) profession.getRecipe(recipe).get("item")).clone();
                        ItemMeta im = item.getItemMeta();
                        im.getPersistentDataContainer().set(Luminosity.mySkillIDKey, PersistentDataType.INTEGER, recipe);
                        im.getPersistentDataContainer().set(Luminosity.myProfessionNameKey, PersistentDataType.STRING, profession.getName());
                        List<String> lore;
                        if (im.hasLore()) {
                            lore = im.getLore();
                        } else {
                            lore = new ArrayList<>();
                        }
                        ArrayList<String> newLore = new ArrayList<>();
                        if (newLore != null) {
                            newLore.add(ChatColor.GRAY + "--- Materials ---");
                            ItemStack[] ingredients = ((ItemStack[]) profession.getRecipe(recipe).get("ingredients"));
                            HashMap<String, Integer> itemCount = new HashMap<>();
                            for (ItemStack i : ingredients) {
                                String itemName = getItemName(i);
                                itemCount.putIfAbsent(itemName, 0);
                                itemCount.replace(itemName, itemCount.get(itemName) + i.getAmount());
                            }
                            itemCount.forEach((k, v) -> {
                                newLore.add(ChatColor.GRAY + k + " x " + v);
                            });
                        }
                        if (lore != null) {
                            lore.add(ChatColor.GRAY + "--- Materials ---");
                            lore.addAll(sortStringArrayByWeight(newLore, true));
                        }
                        im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                        im.setLore(lore);
                        item.setItemMeta(im);
                        inv.setItem(slot, item);
                        break;
                    }
                }
            }
        }
        return inv;
    }
    static Inventory getConfirmationPrompt(Profession profession, int id, Player player) {
        Inventory inventory = player.getInventory();
        HashMap<String, HashMap<String, Object>> items = new HashMap<>();
        for (ItemStack i : ((ItemStack[]) profession.getRecipe(id).get("ingredients"))) {
            String iName = Logic.getItemName(i);
            items.putIfAbsent(iName, new HashMap<>());
            ItemStack iClone = i.clone();
            iClone.setAmount(1);
            items.get(iName).putIfAbsent("countA", 0);
            items.get(iName).putIfAbsent("countB", 0);
            items.get(iName).putIfAbsent("item", iClone);
            items.get(iName).putIfAbsent("enough", false);
            items.get(iName).put("countA", (((Integer) items.get(iName).get("countA")) + i.getAmount()));
        }
        for (ItemStack i : inventory) {
            if (i != null && !i.getType().equals(Material.AIR)) {
                String iName = Logic.getItemName(i);
                if (items.containsKey(iName)) {
                    ItemStack iClone = i.clone();
                    iClone.setAmount(1);
                    if (iClone.equals(items.get(iName).get("item"))) {
                        items.get(iName).put("countB", (((Integer) items.get(iName).get("countB")) + i.getAmount()));
                    }
                }
            }
        }
        for (String str : items.keySet()) {
            if (items.containsKey(str)) {
                if (((int) items.get(str).get("countA")) <= ((int) items.get(str).get("countB"))) {
                    items.get(str).put("enough", true);
                }
            }
        }
        ArrayList<String> order = new ArrayList<>(items.keySet());
        order = sortStringArrayByWeight(order, false);
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.GRAY + ChatColor.BOLD.toString() + "Confirmation prompt");
        for (int i = 0; i < 27; i++) {
            if (i<9) {
                try {
                    inv.setItem(i, ((ItemStack) items.get(order.get(i)).get("item")));
                } catch (IndexOutOfBoundsException e) {
                    continue;
                }
                continue;
            }
            if (i < 18) {
                if (order.size() > (i-9)) {
                    ItemStack t = new ItemStack(Material.RED_STAINED_GLASS_PANE);
                    ItemMeta tm = t.getItemMeta();
                    ArrayList<String> rLore = new ArrayList<>(List.of(ChatColor.WHITE + "Required: " + ChatColor.GRAY + ((int) items.get(order.get(i - 9)).get("countA"))));
                    tm.setLore(rLore);
                    t.setItemMeta(tm);
                    if (((boolean) items.get(order.get(i - 9)).get("enough"))) {
                        t.setType(Material.LIME_STAINED_GLASS_PANE);
                        inv.setItem(i, createUIButton(t, ChatColor.WHITE + "Available: " + ChatColor.GREEN + ((int) items.get(order.get(i - 9)).get("countB"))));
                    } else
                        inv.setItem(i, createUIButton(t, ChatColor.WHITE + "Available: " + ChatColor.RED + ((int) items.get(order.get(i - 9)).get("countB"))));
                }
                continue;
            }
            if (i == 21) {
                ItemStack t = new ItemStack(Material.RED_DYE);
                ItemMeta tm = t.getItemMeta();
                tm.getPersistentDataContainer().set(Luminosity.myProfessionNameKey, PersistentDataType.STRING, profession.getName());
                t.setItemMeta(tm);
                inv.setItem(i, createUIButton(t, ChatColor.RED + "Cancel"));
                continue;
            }
            if (i == 23) {
                boolean a = true;
                for (int f = 9; f < 18; f++) {
                    if (order.size() > (f-9)) {
                        if (inv.getItem(f).getType().equals(Material.RED_STAINED_GLASS_PANE)) a = false;
                    }
                }
                if (a) {
                    ItemStack t = new ItemStack(Material.LIME_DYE);
                    ItemMeta tm = t.getItemMeta();
                    tm.getPersistentDataContainer().set(Luminosity.mySkillIDKey, PersistentDataType.INTEGER, id);
                    tm.getPersistentDataContainer().set(Luminosity.myProfessionNameKey, PersistentDataType.STRING, profession.getName());
                    t.setItemMeta(tm);
                    inv.setItem(i, createUIButton(t, ChatColor.GREEN + "Confirm"));
                    continue;
                }
            }
            inv.setItem(i, createUIButton(new ItemStack(Material.WHITE_STAINED_GLASS_PANE), " "));
        }
        return inv;
    }
    static ItemStack getCodexPage(int id, Profession profession, boolean isRecipe) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.getPersistentDataContainer().set(Luminosity.mySkillIDKey, PersistentDataType.INTEGER, id);
        itemMeta.getPersistentDataContainer().set(Luminosity.myProfessionNameKey, PersistentDataType.STRING, profession.getName());
        String type;
        if (isRecipe) type = "recipe";
        else type = "drop";
        itemMeta.getPersistentDataContainer().set(Luminosity.mySkillTypeKey, PersistentDataType.STRING, type);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "Profession: " + ChatColor.GRAY + profession.getName());
        lore.add(ChatColor.WHITE + "Level: " + ChatColor.GRAY + ((int) profession.getRecipe(id).get("level")));
        itemMeta.setLore(lore);
        if (isRecipe) {
            if (profession.hasRecipe(id)) {
                itemMeta.setDisplayName(ChatColor.GRAY + "Codex Page: " + getItemName((ItemStack) profession.getRecipe(id).get("item")));
            } else return null;
        }
        else {
            if (profession.hasDrop(id)) {
                itemMeta.setDisplayName(ChatColor.GRAY + "Codex Page: " + getItemName((ItemStack) profession.getDrop(id).get("item")));
            } else return null;
        }
        item.setItemMeta(itemMeta);
        item.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
        item.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        return item;
    }
    static void completeRecipe(Profession profession, int id, Player player) {
        ItemStack[] ingredients = ((ItemStack[]) profession.getRecipe(id).get("ingredients"));
        ItemStack item = ((ItemStack) profession.getRecipe(id).get("item")).clone();
        HashMap<String, HashMap<String, Object>> items = new HashMap<>();
        for (ItemStack i : ingredients) {
            String iName = getItemName(i);
            items.putIfAbsent(iName, new HashMap<>());
            ItemStack iClone = i.clone();
            iClone.setAmount(1);
            items.get(iName).putIfAbsent("amountNeeded", 0);
            items.get(iName).putIfAbsent("item", iClone);
            items.get(iName).putIfAbsent("enough", false);
            items.get(iName).put("amountNeeded", (((Integer) items.get(iName).get("amountNeeded")) + i.getAmount()));
        }
        for (ItemStack i : player.getInventory()) {
            if (i != null && !i.getType().equals(Material.AIR)) {
                String iName = getItemName(i);
                if (items.containsKey(iName)) {
                    ItemStack iClone = i.clone();
                    iClone.setAmount(1);
                    if (iClone.equals(items.get(iName).get("item"))) {
                        int needed = ((int) items.get(iName).get("amountNeeded"));
                        if (i.getAmount() <= needed) i.setAmount(0);
                        else i.setAmount((i.getAmount() - needed));
                    }
                }
            }
        }
        player.getInventory().addItem(item);
        profession.giveExp(player, ((int) profession.getRecipe(id).get("level")));
    }
    static public String getItemName(ItemStack i) {
        return i.getItemMeta().hasDisplayName() ? i.getItemMeta().getDisplayName() : WordUtils.capitalize(i.getType().name().replace("_", " ").toLowerCase());
    }
    static Inventory getIngredientsInv() {
        Inventory inv = Bukkit.createInventory(null, 54, "Recipe Ingredients");
        for (int i = 45; i<=53; i++) {
            if (i==45) {
                inv.setItem(i,createUIButton(new ItemStack(Material.WHITE_STAINED_GLASS_PANE), " ", 2356));
                continue;
            }
            if (i==48) {
                inv.setItem(i, createUIButton(new ItemStack(Material.RED_DYE), ChatColor.RED + "Cancel"));
                continue;
            }
            if (i==49) {
                inv.setItem(i, createUIButton(new ItemStack(Material.GRAY_STAINED_GLASS_PANE), ChatColor.GRAY + "Put the result here"));
                continue;
            }
            if (i==50) {
                inv.setItem(i, createUIButton(new ItemStack(Material.LIME_DYE), ChatColor.GREEN + "Done"));
                continue;
            }
            inv.setItem(i,createUIButton(new ItemStack(Material.WHITE_STAINED_GLASS_PANE), " "));
        }
        return inv;
    }
    static void weighColours() {
        Luminosity.colourWeight = new HashMap<>();
        ArrayList<String> colours = new ArrayList<>(List.of(Luminosity.getInstance().getConfig().getString("colourWeight").replace("&", "").split("<")));
        for (String str : colours) {
            if (str.equals("l")) {
                Luminosity.colourWeight.put(ChatColor.BOLD, colours.indexOf(str));
                continue;
            }
            Luminosity.colourWeight.putIfAbsent(ChatColor.getByChar(str), colours.indexOf(str));
        }
    }
}
