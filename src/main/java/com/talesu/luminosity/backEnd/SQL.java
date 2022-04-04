package com.talesu.luminosity.backEnd;

import com.talesu.luminosity.Luminosity;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public interface SQL {
    static void createTable() {
        Connection connection = null;
        PreparedStatement ps = null;
        String[] query = {
                "CREATE TABLE IF NOT EXISTS LuminosityPlayerData " +
                        "(UUID VARCHAR(100),Name VARCHAR(30), Profession VARCHAR(30), Status INT(1), EXP INT(30), Level INT(10), PRIMARY KEY(UUID, Profession))",
                "CREATE TABLE IF NOT EXISTS LuminosityBlockDropData " +
                        "(ID VARCHAR(30), Profession VARCHAR(20), Level INT(10), Chance INT(10), Block VARCHAR(10), BlockDrop text, PRIMARY KEY(ID, Profession))",
                "CREATE TABLE IF NOT EXISTS LuminosityPlayerPlacedBlocks " +
                        "(World VARCHAR(50), ChunkKey BIGINT, Location VARCHAR(30), PRIMARY KEY(World, Location))",
                "CREATE TABLE IF NOT EXISTS LuminosityPlayerRecipesData " +
                        "(UUID VARCHAR(100), Name VARCHAR(30), Profession VARCHAR(30), RecipeID VARCHAR(30), PRIMARY KEY(UUID, Profession, RecipeID))",
                "CREATE TABLE IF NOT EXISTS LuminosityPlayerDropsData " +
                        "(UUID VARCHAR(100), Name VARCHAR(30), Profession VARCHAR(30), DropID VARCHAR(30), PRIMARY KEY(UUID, Profession, DropID))",
                "CREATE TABLE IF NOT EXISTS LuminosityRecipeData " +
                        "(ID VARCHAR(30), Profession VARCHAR(20), Level INT(10), Item text, Ingredients text, Actions VARCHAR(100), PRIMARY KEY(ID, Profession))"};

        try {
            connection = Luminosity.hikari.getConnection();
            for (String str : query) {
                ps = connection.prepareStatement(str);
                ps.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    static boolean exists(UUID uuid) {
        Connection connection = null;
        PreparedStatement ps = null;
        String query  = "SELECT * FROM LuminosityPlayerData WHERE UUID=?";
        try {
            connection = Luminosity.hikari.getConnection();
            ps = connection.prepareStatement(query);
            ps.setString(1, uuid.toString());
            ResultSet results = ps.executeQuery();
            return results.next();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }
    static boolean blockDropExists(int id) {
        Connection connection = null;
        PreparedStatement ps = null;
        String query  = "SELECT * FROM LuminosityBlockDropData WHERE ID=?";
        try {
            connection = Luminosity.hikari.getConnection();
            ps = connection.prepareStatement(query);
            ps.setInt(1, id);
            ResultSet results = ps.executeQuery();
            return results.next();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }
    static void loadProfData(Player player) {
        UUID uuid = player.getUniqueId();
        Connection connection = null;
        PreparedStatement ps = null;
        String query = "SELECT * FROM LuminosityPlayerData WHERE UUID=?";
        try {
            connection = Luminosity.hikari.getConnection();
            ps = connection.prepareStatement(query);
            ps.setString(1, uuid.toString());
            Luminosity.playerData.putIfAbsent(uuid, new HashMap<>());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Profession profession = Profession.getProfession(rs.getString("Profession"));
                Luminosity.playerData.get(uuid).putIfAbsent(profession, new HashMap<>());
                for (int i=0; i<2; i++) {
                    Luminosity.playerData.get(uuid).get(profession).put(new String[]{"exp","level"}[i], rs.getInt(new String[]{"EXP","Level"}[i]));
                }
                Luminosity.playerData.get(uuid).get(profession).put("status", rs.getInt("Status") == 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    static void loadProfSkillz(UUID uuid) {
        Connection connection = null;
        PreparedStatement ps = null;
        String query = "SELECT * FROM LuminosityPlayerData WHERE UUID=?";
        try {
            connection = Luminosity.hikari.getConnection();
            ps = connection.prepareStatement(query);
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            Luminosity.playerSkillz.putIfAbsent(uuid, new HashMap<>());
            String[] data = null;
            if (rs.next()) data = rs.getString("Skillz").split(";;");
            for (Profession profession : Profession.values()) {
                Luminosity.playerSkillz.get(uuid).putIfAbsent(profession, new HashMap<>());
                Luminosity.playerSkillz.get(uuid).get(profession).putIfAbsent("drop", new ArrayList<>());
                Luminosity.playerSkillz.get(uuid).get(profession).putIfAbsent("recipe", new ArrayList<>());
            }
            if (data != null && !Arrays.toString(data).equals("[]")) {
               ArrayList<String> drop = new ArrayList<>(Arrays.asList((data[0].split(":")[1].replace("[", "").replace("]", "").replace(" ", "")).split(",")));
               ArrayList<String> recipe = new ArrayList<>(Arrays.asList((data[1].split(":")[1].replace("[", "").replace("]", "").replace(" ", "")).split(",")));
               if (drop.size() > 1) {
                   for (String str : drop) {
                       int id = Integer.parseInt(str);
                       Profession profession = Profession.getProfessionByDrop(id);
                       Luminosity.playerSkillz.get(uuid).get(profession).get("drop").add(id);
                   }
               }
               if (recipe.size() > 1) {
                   for (String str : recipe) {
                       int id = Integer.parseInt(str);
                       Profession profession = Profession.getProfessionByRecipe(id);
                       Luminosity.playerSkillz.get(uuid).get(profession).get("recipe").add(id);
                   }
               }
               if (Luminosity.debug) Bukkit.getServer().getLogger().info(ChatColor.GREEN + "DEBUG: Profession Skillz of " + Bukkit.getPlayer(uuid).getName() + " retrieved");
           }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    static void loadBlockDropData() {
        Connection connection = null;
        PreparedStatement ps = null;
        String query = "SELECT * FROM LuminosityBlockDropData";
        try {
            connection = Luminosity.hikari.getConnection();
            ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("ID");
                for (Profession profession : Profession.values()) {
                    Luminosity.blockDropData.putIfAbsent(profession, new HashMap<>());
                }
                Profession profession = Profession.getProfession(rs.getString("Profession"));
                Luminosity.blockDropData.putIfAbsent(profession, new HashMap<>());
                Luminosity.blockDropData.get(profession).putIfAbsent(id, new HashMap<>());
                Luminosity.blockDropData.get(profession).get(id).put("level", rs.getInt("Level"));
                Luminosity.blockDropData.get(profession).get(id).put("material", Material.getMaterial(rs.getString("Block")));
                Luminosity.blockDropData.get(profession).get(id).put("item", Logic.getItemStack(rs.getString("BlockDrop")));
                Luminosity.blockDropData.get(profession).get(id).put("chance", rs.getInt("Chance"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    static HashMap<Integer, HashMap<String, Object>> getRecipeData() {
        Connection connection = null;
        PreparedStatement ps = null;
        String query = "SELECT * FROM LuminosityRecipeData";
        try {
            connection = Luminosity.hikari.getConnection();
            ps = connection.prepareStatement(query);
            HashMap<Integer, HashMap<String, Object>> data = new HashMap<>();
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("ID");
                data.putIfAbsent(id, new HashMap<>());
                for (String str : new String[]{"Profession","Level","Item","Ingredients"}) {
                    data.get(id).put(str, rs.getObject(str));
                }
            }
            return data.keySet().isEmpty() ? null : data;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
    static void saveRecipeData() {
        Connection connection = null;
        PreparedStatement ps = null;
        String query = "INSERT IGNORE INTO LuminosityRecipeData (ID, Profession, Level, Item, Ingredients) VALUES(?,?,?,?,?)";
        try {
            connection = Luminosity.hikari.getConnection();
            for (Profession profession : Profession.values()) {
                ps = connection.prepareStatement(query);
                ps.setString(2, profession.getName());
                for (int id : profession.getRecipes().keySet()) {
                    ps.setInt(1, id);
                    ps.setInt(3, ((Integer) profession.getRecipe(id).get("level")));
                    ps.setString(4, Logic.itemStackToString(((ItemStack) profession.getRecipe(id).get("item"))));
                    ps.setString(5, Logic.itemStackArrayToString(((ItemStack[]) profession.getRecipe(id).get("ingredients"))));
                    ps.executeUpdate();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    static void saveBlockDropData(int id, Material material, Profession profession, int level, int chance, String itemDrop) {
        Connection connection = null;
        PreparedStatement ps = null;
        String query = "INSERT IGNORE INTO LuminosityBlockDropData (ID, Profession, Level, Chance, Block, BlockDrop) VALUES(?,?,?,?,?,?)";
        try {
            connection = Luminosity.hikari.getConnection();
            ps = connection.prepareStatement(query);
            ps.setString(2, profession.getName());
            ps.setString(5, material.toString());
            ps.setString(6, itemDrop);
            ps.setInt(3, level);
            ps.setInt(4, chance);
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    static void saveAllBlockDropData() {
        Connection connection = null;
        PreparedStatement ps = null;
        String query = "INSERT IGNORE INTO LuminosityBlockDropData (ID, Profession, Level, Chance, Block, BlockDrop) VALUES(?,?,?,?,?,?)";
        try {
            connection = Luminosity.hikari.getConnection();
            for (Profession profession : Luminosity.blockDropData.keySet()) {
                for (int id : Luminosity.blockDropData.get(profession).keySet()) {
                    if (!blockDropExists(id)) {
                        int level = ((Integer) Luminosity.blockDropData.get(profession).get(id).get("level"));
                        int chance = ((Integer) Luminosity.blockDropData.get(profession).get(id).get("chance"));
                        ItemStack itemDrop = ((ItemStack) Luminosity.blockDropData.get(profession).get(id).get("item"));
                        Material material = ((Material) Luminosity.blockDropData.get(profession).get(id).get("material"));
                        ps = connection.prepareStatement(query);
                        ps.setString(2, profession.getName());
                        ps.setString(5, material.toString());
                        ps.setString(6, Logic.itemStackToString(itemDrop));
                        ps.setInt(3, level);
                        ps.setInt(4, chance);
                        ps.setInt(1, id);
                        ps.executeUpdate();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    static void saveProfData(Player player) {
        UUID uuid = player.getUniqueId();
        Connection connection = null;
        PreparedStatement ps = null;
        String query = "UPDATE LuminosityPlayerData SET Status=?, EXP=?, Level=? WHERE UUID=? AND Profession=?";
        try {
            connection = Luminosity.hikari.getConnection();
            ps = connection.prepareStatement(query);
            ps.setString(4, player.getUniqueId().toString());
            for (Profession profession : Profession.values()) {
                ps.setString(5, profession.getName());
                ps.setInt(1, ((boolean) Luminosity.playerData.get(uuid).get(profession).get("status")) ? 1 : 0);
                ps.setInt(2, ((int) Luminosity.playerData.get(uuid).get(profession).get("exp")));
                ps.setInt(3, ((int) Luminosity.playerData.get(uuid).get(profession).get("level")));
                ps.executeUpdate();
            }
            if (Luminosity.debug) Bukkit.getServer().getLogger().info(ChatColor.GREEN + "DEBUG: Saved " + Objects.requireNonNull(Bukkit.getServer().getPlayer(uuid)).getName() + " Player Data");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    static void saveProfSkillz(UUID uuid, HashMap<Profession, HashMap<String, List<Integer>>> map) {
        if (map!=null) {
            Connection connection = null;
            PreparedStatement ps = null;
            String query = "UPDATE LuminosityPlayerData SET Skillz=? WHERE UUID=?";
            try {
                connection = Luminosity.hikari.getConnection();
                ArrayList<Integer> drop = new ArrayList<>();
                ArrayList<Integer> recipe = new ArrayList<>();
                if (exists(uuid)) {
                    for (Profession profession : map.keySet()) {
                        if (!map.get(profession).get("drop").isEmpty()) drop.addAll(map.get(profession).get("drop"));
                        if (!map.get(profession).get("recipe").isEmpty())
                            recipe.addAll(map.get(profession).get("recipe"));
                        String data = "d:" + drop + ";;r:" + recipe;
                        ps = connection.prepareStatement(query);
                        ps.setString(1, data);
                        ps.setString(2, uuid.toString());
                        ps.executeUpdate();
                        if (Luminosity.debug)
                            Bukkit.getServer().getLogger().info(ChatColor.GREEN + "DEBUG: Saved skills for " + Bukkit.getPlayer(uuid).getName());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                if (ps != null) {
                    try {
                        ps.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    static void addRecord(Player player) {
        UUID uuid = player.getUniqueId();
        if (!exists(uuid)) {
            Connection connection = null;
            PreparedStatement ps = null;
            String query = "INSERT IGNORE INTO LuminosityPlayerData" +
                    " (UUID,Name,Profession,Status,EXP,Level) VALUES(?,?,?,?,?,?)";
            try {
                connection = Luminosity.hikari.getConnection();
                ps = connection.prepareStatement(query);
                ps.setString(1, uuid.toString());
                ps.setString(2, player.getName());
                for (Profession profession : Profession.values()) {
                    ps.setString(3, profession.getName());
                    ps.setInt(4, 0);
                    ps.setInt(5, 0);
                    ps.setInt(6, 0);
                    ps.executeUpdate();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                if (ps != null) {
                    try {
                        ps.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    static ArrayList<Material> getAllBlocks() {
        Connection connection = null;
        PreparedStatement ps = null;
        String query = "SELECT DISTINCT Block FROM LuminosityBlockDropData";
        try {
            connection = Luminosity.hikari.getConnection();
            ps = connection.prepareStatement(query);
            ArrayList<Material> out = new ArrayList<>();
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Material mat = Material.valueOf(rs.getString("Block"));
                if (!out.contains(mat)) out.add(mat);
            }
            return out;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
    static void fullReset(Player player) {
        Connection connection = null;
        PreparedStatement ps = null;
        String query = "DELETE * FROM LuminosityPlayerData WHERE UUID=?";
        try {
            connection = Luminosity.hikari.getConnection();
            ps = connection.prepareStatement(query);
            ps.setString(1, player.getUniqueId().toString());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    static void savePlayerPlacedBlocks() {
        Connection connection = null;
        PreparedStatement ps = null;
        String query = "INSERT IGNORE INTO LuminosityPlayerPlacedBlocks (World, ChunkKey, Location) VALUES(?,?,?)";
        try {
            connection = Luminosity.hikari.getConnection();
            ps = connection.prepareStatement(query);
            for (World world : Luminosity.playerPlacedBlocks.keySet()) {
                ps.setString(1, world.getName());
                for (long chunkKey : Luminosity.playerPlacedBlocks.get(world).keySet()) {
                    ps.setLong(2, chunkKey);
                    for (Location entry : Luminosity.playerPlacedBlocks.get(world).get(chunkKey)) {
                        String value = ("X="+entry.getBlockX()+";Y="+entry.getBlockY()+";Z="+entry.getBlockZ()+";");
                        ps.setString(3, value);
                        ps.executeUpdate();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    static void loadPlayerPlacedBlocks() {
        Connection connection = null;
        PreparedStatement ps = null;
        String query = "SELECT * FROM LuminosityPlayerPlacedBlocks";
        try {
            connection = Luminosity.hikari.getConnection();
            ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                World world = Bukkit.getServer().getWorld(rs.getString("World"));
                long chunkKey = rs.getLong("ChunkKey");
                Luminosity.playerPlacedBlocks.putIfAbsent(world, new HashMap<>());
                Luminosity.playerPlacedBlocks.get(world).putIfAbsent(chunkKey, new ArrayList<>());
                String str = rs.getString("Location");
                HashMap<String, Integer> xyz = new HashMap<>();
                for (int i=0; i<3; i++) {
                    xyz.put(new String[]{"x","y","z"}[i],Integer.parseInt(str.split(";")[i].split("=")[1]));
                }
                Location location = new Location(world, xyz.get("x"), xyz.get("y"), xyz.get("z"));
                Luminosity.playerPlacedBlocks.get(world).get(chunkKey).add(location);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
