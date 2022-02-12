
package com.talesu.luminosity.backEnd;

import com.talesu.luminosity.Luminosity;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public enum Profession {
    ALCHEMIST("Alchemist", null),
    METALLURGIST("Metallurgist", getMinerMats()),
    HERBALIST("Herbalist", getHerbalistMats()),
    ARCHEOLOGIST("Archeologist", getArcheologistMats()),
    JEWELER("Jeweler", null),
    SMITH("Smith", null),
    ENCHANTER("Enchanter", null);

    private static final HashMap<String, Profession> byName = new HashMap<>();
    private static final HashMap<Integer, Profession> byDrop = new HashMap<>();
    private static final HashMap<Integer, Profession> byRecipe = new HashMap<>();
    final String name;
    final ArrayList<Material> materialList;
    public boolean getStatus(Player player) {
        return ((boolean) Luminosity.playerData.get(player.getUniqueId()).get(this).get("status"));
    }
    public int getExp(Player player) {
        return ((int) Luminosity.playerData.get(player.getUniqueId()).get(this).get("exp"));
    }
    public int getLevel(Player player) {
        return ((int) Luminosity.playerData.get(player.getUniqueId()).get(this).get("level"));
    }
    public HashMap<Integer, HashMap<String, Object>> getRecipes() {
        return Luminosity.recipeData.get(this);
    }
    public HashMap<String, Object> getRecipe(int id) {
        return Luminosity.recipeData.get(this).get(id);
    }
    public HashMap<String, Object> getDrop(int id) {
        return Luminosity.blockDropData.get(this).get(id);
    }
    public int addRecipe(int level, ItemStack[] ingredients, ItemStack item) {
        int id = 0;
        for (int i : Luminosity.recipeData.get(this).keySet()) {
            if (id<=i) id = i+1;
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("level", level); map.put("ingredients", ingredients); map.put("item", item);
        Luminosity.recipeData.get(this).put(id, map);
        return id;
    }
    public boolean giveSkill(Player player, int id, String type) {
        UUID uuid = player.getUniqueId();
        Luminosity.playerSkillz.putIfAbsent(uuid, new HashMap<>());
        Luminosity.playerSkillz.get(uuid).putIfAbsent(this, new HashMap<>());
        Luminosity.playerSkillz.get(uuid).get(this).putIfAbsent(type, new ArrayList<>());
        boolean contains = Luminosity.playerSkillz.get(uuid).get(this).get(type).contains(id);
        if (!contains) Luminosity.playerSkillz.get(uuid).get(this).get(type).add(id);
        return !contains;
    }
    public boolean hasRecipe(int id) {
        return Luminosity.recipeData.get(this).containsKey(id);
    }
    public boolean hasDrop(int id) {
        return Luminosity.blockDropData.get(this).containsKey(id);
    }
    public static ArrayList<Profession> getHarvesters() {
        return new ArrayList<>(List.of(METALLURGIST, HERBALIST, ARCHEOLOGIST));
    }
    Profession(String name, ArrayList<Material> materialList) {
        this.name = name; this.materialList = materialList;
    }
    public static List<String> getNames() {
        return new ArrayList<>(byName.keySet());
    }
    public static Profession getProfession(String name) {
        return byName.get(name);
    }
    public static Profession getProfessionByDrop(int id) {
        return byDrop.get(id);
    }
    public static Profession getProfessionByRecipe(int id) {
        return byRecipe.get(id);
    }
    private static ArrayList<Material> getMinerMats() {
        ArrayList<Material> ores = new ArrayList<>();
        for (Material mat : Material.values()) {
            if (mat.getKey().toString().toLowerCase().contains("ore")) ores.add(mat);
        }
        ores.add(Material.ANCIENT_DEBRIS);
        return ores;
    }
    private static ArrayList<Material> getHerbalistMats() {
        ArrayList<Material> herbs = new ArrayList<>();
        for (Material mat : Material.values()) {
            String key = mat.getKey().toString().toLowerCase();
            if ((key.contains("grass") && !key.contains("block")) || key.contains("flower") || key.contains("fern")) herbs.add(mat);
        }
        herbs.add(Material.BROWN_MUSHROOM); herbs.add(Material.RED_MUSHROOM);
        herbs.add(Material.ALLIUM); herbs.add(Material.AZURE_BLUET);
        return herbs;
    }
    private static ArrayList<Material> getArcheologistMats() {
        ArrayList<Material> arch = new ArrayList<>(List.of(Material.GRASS_BLOCK, Material.DIRT, Material.SAND, Material.DIORITE, Material.ANDESITE, Material.GRANITE, Material.GRAVEL, Material.DEEPSLATE));
        for (Material mat : Material.values()) {
            String key = mat.getKey().toString().toLowerCase();
            if (key.contains("stone")) arch.add(mat);
        }
        return arch;
    }
    static {
        for (Profession profession : values()) {
            byName.put(profession.name, profession);
        }
    }
    public static void initialize() {
        for (Profession profession : values()) {
            for (int i : Luminosity.blockDropData.get(profession).keySet()) {
                byDrop.put(i, profession);
            }
            for (int i : Luminosity.recipeData.get(profession).keySet()) {
                byRecipe.put(i, profession);
            }
        }
    }
}
