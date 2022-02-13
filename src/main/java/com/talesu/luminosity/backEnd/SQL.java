package com.talesu.luminosity.backEnd;

import com.talesu.luminosity.Luminosity;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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
        String[] query = {"CREATE TABLE IF NOT EXISTS LuminosityPlayerData " +
                "(UUID VARCHAR(100),Name VARCHAR(30), ProfessionData VARCHAR(100), Skillz VARCHAR(100), PRIMARY KEY(UUID))",
                "CREATE TABLE IF NOT EXISTS LuminosityBlockDropData " +
                "(ID INT(32), Profession VARCHAR(20), Level INT(10), Chance INT(10), Block VARCHAR(10), BlockDrop text, PRIMARY KEY(ID))",
                "CREATE TABLE IF NOT EXISTS LuminosityRecipeData " +
                        "(ID INT(32), Profession VARCHAR(20), Level INT(10), Item text, Ingredients text, PRIMARY KEY(ID))"};
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
    static String getProfData(UUID uuid) {
        Connection connection = null;
        PreparedStatement ps = null;
        String query = "SELECT * FROM LuminosityPlayerData WHERE UUID=?";
        try {
            connection = Luminosity.hikari.getConnection();
            ps = connection.prepareStatement(query);
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("ProfessionData");
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
            if (data != null && !Arrays.toString(data).equals("[]")) {
               ArrayList<String> drop = new ArrayList<>(Arrays.asList((data[0].split(":")[1].replace("[", "").replace("]", "").replace(" ", "")).split(",")));
               ArrayList<String> recipe = new ArrayList<>(Arrays.asList((data[1].split(":")[1].replace("[", "").replace("]", "").replace(" ", "")).split(",")));
               for (Profession profession : Profession.values()) {
                   Luminosity.playerSkillz.get(uuid).putIfAbsent(profession, new HashMap<>());
                   Luminosity.playerSkillz.get(uuid).get(profession).putIfAbsent("drop", new ArrayList<>());
                   Luminosity.playerSkillz.get(uuid).get(profession).putIfAbsent("recipe", new ArrayList<>());
               }
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
        PreparedStatement ps2;
        String query = "SELECT * FROM LuminosityBlockDropData";
        String query2 = "SELECT MAX(ID) AS 'Last ID' FROM LuminosityBlockDropData";
        try {
            connection = Luminosity.hikari.getConnection();
            ps2 = connection.prepareStatement(query2);
            ResultSet rs2 = ps2.executeQuery();
            int lastID = 0;
            if (rs2.next()) lastID = rs2.getInt("Last ID");
            ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            for (int i = 0; i <= lastID; i++) {
                if (rs.next()) {
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
        PreparedStatement ps2 = null;
        String query = "SELECT * FROM LuminosityRecipeData";
        String query2 = "SELECT MAX(ID) AS 'Last ID' FROM LuminosityRecipeData";
        try {
            connection = Luminosity.hikari.getConnection();
            ps2 = connection.prepareStatement(query2);
            ResultSet rs2 = ps2.executeQuery();
            int lastID = 0;
            if (rs2.next()) lastID = rs2.getInt("Last ID");
            ps = connection.prepareStatement(query);
            HashMap<Integer, HashMap<String, Object>> data = new HashMap<>();
            ResultSet rs = ps.executeQuery();
            for (int i = 0; i <= lastID; i++) {
                if (rs.next()) {
                    int id = rs.getInt("ID");
                    data.putIfAbsent(id, new HashMap<>());
                    for (String str : new String[]{"Profession","Level","Item","Ingredients"}) {
                        data.get(id).put(str, rs.getObject(str));
                    }
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
            if (ps != null || ps2 != null) {
                try {
                    ps.close();
                    ps2.close();
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
                ps.setString(2, profession.name);
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
            ps.setString(2, profession.name);
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
                        ps.setString(2, profession.name);
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
        String query = "Update LuminosityPlayerData SET ProfessionData=? WHERE UUID=?";
        try {
            connection = Luminosity.hikari.getConnection();
            ps = connection.prepareStatement(query);
            StringBuilder data = new StringBuilder();
            for (Profession profession : Profession.values()) {
                String profName = profession.name.split("")[0] + profession.name.split("")[1];
                data.append(profName).append("=").append(((boolean) Luminosity.playerData.get(uuid).get(profession).get("status")) ? 1 : 0).append(";").append(Luminosity.playerData.get(uuid).get(profession).get("exp")).append(";").append(Luminosity.playerData.get(uuid).get(profession).get("level")).append(";;");
                if (Luminosity.debug) Bukkit.getServer().getLogger().info(ChatColor.GREEN + "DEBUG: Serialized " + profession + " data for " + player.getName());
            }
            ps.setString(1, data.toString());
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
            if (Luminosity.debug) Bukkit.getServer().getLogger().info(ChatColor.GREEN + "DEBUG: Saved ProfessionData for " + Objects.requireNonNull(Bukkit.getServer().getPlayer(uuid)).getName());
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
                    if (!map.get(profession).get("recipe").isEmpty()) recipe.addAll(map.get(profession).get("recipe"));
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
    static void addRecord(Player player) {
        Connection connection = null;
        PreparedStatement ps = null;
        String query = "INSERT IGNORE INTO LuminosityPlayerData" +
                " (UUID,Name,ProfessionData,Skillz) VALUES(?,?,?,?)";
        try {
            connection = Luminosity.hikari.getConnection();
            ps = connection.prepareStatement(query);
            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, player.getName());
            StringBuilder data = new StringBuilder();
            for (Profession profession : Profession.values()) {
                String profName = profession.name.split("")[0] + profession.name.split("")[1];
                data.append(profName).append("=").append("0;0;0;;");
            }
            ps.setString(3, data.toString());
            ps.setString(4, "");
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
    static ArrayList<Material> getAllBlocks() {
        Connection connection = null;
        PreparedStatement ps = null;
        String query = "SELECT DISTINCT Block FROM LuminosityBlockDropData";
        try {
            connection = Luminosity.hikari.getConnection();
            ps = connection.prepareStatement(query);
            ArrayList<Material> out = new ArrayList<>();
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                out.add(Material.valueOf(rs.getString("Block")));
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
        String query = "DELETE FROM LuminosityPlayerData WHERE UUID=?";
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
}
