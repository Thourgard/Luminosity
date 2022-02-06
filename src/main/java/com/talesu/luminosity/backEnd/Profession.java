package com.talesu.luminosity.backEnd;

import org.bukkit.Material;

import java.util.*;

public enum Profession {
    ALCHEMIST("Alchemist", null),
    MINER("Miner", getMinerMats()),
    HERBALIST("Herbalist", getHerbalistMats()),
    ARCHEOLOGIST("Archeologist", getArcheologistMats()),
    JEWELER("Jeweler", null),
    SMITH("Smith", null),
    ENCHANTER("Enchanter", null);

    private static final HashMap<String, Profession> byName = new HashMap<>();
    final String name;
    final ArrayList<Material> materialList;
    public static final ArrayList<Profession> getHarvesters() {
        return new ArrayList<>(List.of(MINER, HERBALIST, ARCHEOLOGIST));
    }
    Profession(String name, ArrayList<Material> materialList) {
        this.name = name; this.materialList = materialList;
    }
    public static List<String> getNames() {
        List<String> t = new ArrayList<>();
        for (Profession p : Profession.values()) {
            t.add(p.name);
        }
        return t;
    }
    public static Profession getProfession(String name) {
        return byName.get(name);
    }
    private static ArrayList<Material> getMinerMats() {
        Material.ACACIA_BOAT.getKey();
        ArrayList<Material> ores = new ArrayList<>();
        for (Material mat : Material.values()) {
            if (mat.getKey().toString().toLowerCase().contains("ore")) ores.add(mat);
        }
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
        for (Profession p : values()) {
            byName.put(p.name, p);
        }
    }
}
