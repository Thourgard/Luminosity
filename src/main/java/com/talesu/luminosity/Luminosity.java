/*
TODO Crafting Mini-Game;
TODO Admin item creation menu;
TODO Professions score;
TODO Leaderboards;
TODO Gathering prof limiter
TODO Give-out permissions on prof score
 */
package com.talesu.luminosity;

import com.talesu.luminosity.backEnd.Logic;
import com.talesu.luminosity.backEnd.PlayerListener;
import com.talesu.luminosity.backEnd.Profession;
import com.talesu.luminosity.backEnd.SQL;
import com.talesu.luminosity.frontEnd.CommandHandler;
import com.talesu.luminosity.frontEnd.TabAutoComplete;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class Luminosity extends JavaPlugin {

    public static NamespacedKey mySkillIDKey = null;
    public static NamespacedKey mySkillTypeKey = null;
    public static NamespacedKey myProfessionNameKey = null;
    public static HikariDataSource hikari;
    private static Luminosity instance;

    public static HashMap<UUID, HashMap<Profession, HashMap<String, Object>>> playerData;
    public static HashMap<UUID, HashMap<Profession, HashMap<String, List<Integer>>>> playerSkillz;

    public static HashMap<Profession, HashMap<Integer, HashMap<String, Object>>> blockDropData;
    public static HashMap<Profession, HashMap<Integer, HashMap<String, Object>>> recipeData;

    public static HashMap<UUID, HashMap<String, Object>> tempoBin;
    public static HashMap<ChatColor, Integer> colourWeight;

    public static List<Material> profDropBlocks;
    public static List<String> materialList;
    public static HashMap<World, HashMap<Long, ArrayList<Location>>> playerPlacedBlocks;

    public static boolean debug;

    @Override
    public void onEnable() {
        initialize();
        getCommand("lumen").setExecutor(new CommandHandler());
        getCommand("lumen").setTabCompleter(new TabAutoComplete());
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
        ArrayList<Material> t = SQL.getAllBlocks();
        if (t!=null) profDropBlocks = t;
    }
    @Override
    public void onDisable() {
        Logic.saveData();
        hikari.close();
    }

    private void connectDB() {
        String host = getConfig().getString("sql.host");
        String port = getConfig().getString("sql.port");
        String database = getConfig().getString("sql.database");
        String username = getConfig().getString("sql.username");
        String password = getConfig().getString("sql.password");
        hikari = new HikariDataSource();
        hikari.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false");
        hikari.setUsername(username);
        hikari.setPassword(password);
        hikari.setPoolName("Luminosity");
    }
    private void initialize() {
        saveDefaultConfig();
        instance = this;
        playerSkillz = new HashMap<>();
        playerData = new HashMap<>();
        blockDropData = new HashMap<>();
        playerPlacedBlocks = new HashMap<>();
        tempoBin = new HashMap<>();
        Logic.weighColours();
        profDropBlocks = new ArrayList<>();
        recipeData = new HashMap<>();
        debug = getConfig().getBoolean("debug");
        connectDB();
        SQL.createTable();
        materialList = Logic.getMaterialList();
        mySkillIDKey = new NamespacedKey(getInstance(), "recipeID");
        mySkillTypeKey = new NamespacedKey(getInstance(), "skillType");
        myProfessionNameKey = new NamespacedKey(getInstance(), "professionID");
        Logic.loadData();
    }
    public static Luminosity getInstance() {
        return instance;
    }
}
