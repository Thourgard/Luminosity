package com.talesu.luminosity;

import com.talesu.luminosity.backEnd.Logic;
import com.talesu.luminosity.backEnd.PlayerListener;
import com.talesu.luminosity.backEnd.Profession;
import com.talesu.luminosity.backEnd.SQL;
import com.talesu.luminosity.frontEnd.CommandHandler;
import com.talesu.luminosity.frontEnd.TabAutoComplete;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class Luminosity extends JavaPlugin {

    public static HikariDataSource hikari;
    private static Luminosity instance;

    public static HashMap<UUID, HashMap<Profession, HashMap<String, Object>>> playerData;
    public static HashMap<UUID, HashMap<String, List<Integer>>> playerSkillz;

    public static HashMap<Material, HashMap<Profession, HashMap<Integer, HashMap<String, Object>>>> blockDropData;
    public static HashMap<String, HashMap<Integer, HashMap<String, Object>>> recipeData;

    public static List<Material> profDropBlocks;
    public static List<String> materialList;
    public static HashMap<UUID, List<Location>> playerPlacedBlocks;

    public static boolean debug;

    @Override
    public void onEnable() {
        initialSetUp();
        getCommand("lumen").setExecutor(new CommandHandler());
        getCommand("lumen").setTabCompleter(new TabAutoComplete());
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
        ArrayList<Material> t = SQL.getAllBlocks();
        if (t!=null) profDropBlocks = t;
        Logic.loadBlockDropData();
        Logic.loadRecipeData();
        if (!getServer().getOnlinePlayers().isEmpty()) {
            for (Player p : getServer().getOnlinePlayers()) {
                Logic.loadPlayerData(p);
            }
        }
    }
    @Override
    public void onDisable() {
        for (Player p : getServer().getOnlinePlayers()) {
            Logic.savePlayerData(p);
        }
        SQL.saveAllBlockDropData();
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
    private void initialSetUp() {
        saveDefaultConfig();
        instance = this;
        playerSkillz = new HashMap<>();
        playerData = new HashMap<>();
        blockDropData = new HashMap<>();
        playerPlacedBlocks = new HashMap<>();
        profDropBlocks = new ArrayList<>();
        debug = getConfig().getBoolean("debug");
        connectDB();
        SQL.createTable();
        materialList = Logic.getMaterialList();
    }
    public static Luminosity getInstance() {
        return instance;
    }
}
