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
                        "(ID INT(32), Profession VARCHAR(20), Level INT(10), Item VARCHAR(100), Ingredients text, PRIMARY KEY(ID))"};
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
            Luminosity.playerSkillz.get(uuid).putIfAbsent("drop", new ArrayList<>());
            Luminosity.playerSkillz.get(uuid).putIfAbsent("recipe", new ArrayList<>());
           if (data != null && !Arrays.toString(data).equals("[]")) {
               String[] drop = data[0].split(":");
               String[] recipe =  data[1].split(":");
               if (drop.length > 1) {
                   drop = drop[1].split(";");
                   for (String str : drop) {
                       Luminosity.playerSkillz.get(uuid).get("drop").add(Integer.parseInt(str));
                   }
               }
               if (recipe.length > 1) {
                   recipe = recipe[1].split(";");
                   for (String str : recipe) {
                       Luminosity.playerSkillz.get(uuid).get("recipe").add(Integer.parseInt(str));
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
    static HashMap<Integer, HashMap<String, Object>> getBlockDropData() {
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
            HashMap<Integer, HashMap<String, Object>> data = new HashMap<>();
            ResultSet rs = ps.executeQuery();
            for (int i = 0; i <= lastID; i++) {
                if (rs.next()) {
                    int id = rs.getInt("ID");
                    data.putIfAbsent(id, new HashMap<>());
                    for (String str : new String[]{"Profession", "Level", "Chance","Block","BlockDrop"}) {
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
    static void saveBlockDropData(int id, Material material, String profession, int level, int chance, String itemDrop) {
        Connection connection = null;
        PreparedStatement ps = null;
        String query = "INSERT IGNORE INTO LuminosityBlockDropData (ID, Profession, Level, Chance, Block, BlockDrop) VALUES(?,?,?,?,?,?)";
        try {
            connection = Luminosity.hikari.getConnection();
            ps = connection.prepareStatement(query);
            ps.setString(2, profession);
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
            for (Material material : Luminosity.blockDropData.keySet()) {
                for (Profession profession : Luminosity.blockDropData.get(material).keySet()) {
                    for (int id : Luminosity.blockDropData.get(material).get(profession).keySet()) {
                        if (!blockDropExists(id)) {
                            int level = ((Integer) Luminosity.blockDropData.get(material).get(profession).get(id).get("level"));
                            int chance = ((Integer) Luminosity.blockDropData.get(material).get(profession).get(id).get("chance"));
                            ItemStack itemDrop = ((ItemStack) Luminosity.blockDropData.get(material).get(profession).get(id).get("drop"));
                            ps = connection.prepareStatement(query);
                            ps.setString(2, profession);
                            ps.setString(5, material.toString());
                            ps.setString(6, Logic.itemStackToString(itemDrop));
                            ps.setInt(3, level);
                            ps.setInt(4, chance);
                            ps.setInt(1, id);
                            ps.executeUpdate();
                        }
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
            String[] profList = Luminosity.getProfessionsList;
            StringBuilder data = new StringBuilder();
            for (String str : profList) {
                String profName = str.split("")[0] + str.split("")[1];
                data.append(profName).append("=").append(Luminosity.jobStatus.get(uuid).get(str) ? 1 : 0).append(";").append(Luminosity.jobExp.get(uuid).get(str)).append(";").append(Luminosity.jobLevel.get(uuid).get(str)).append(";;");
                if (Luminosity.debug) Bukkit.getServer().getLogger().info(ChatColor.GREEN + "DEBUG: Serialized " + str + " data for " + player.getName());
            }
            ps.setString(1, data.toString());
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
            if (Luminosity.debug) Bukkit.getServer().getLogger().info(ChatColor.GREEN + "DEBUG: Saved ProfessionData for " + Bukkit.getPlayer(uuid).getName());
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
    static void saveProfSkillz(UUID uuid, HashMap<String, List<Integer>> map) {
        Connection connection = null;
        PreparedStatement ps = null;
        String query = "UPDATE LuminosityPlayerData SET Skillz=? WHERE UUID=?";
        try {
            connection = Luminosity.hikari.getConnection();
            StringBuilder drop = new StringBuilder("d:");
            StringBuilder recipe = new StringBuilder("r:");
            if (exists(uuid)) {
                if (!map.get("drop").isEmpty()) {
                    for (int i : map.get("drop")) {
                        drop.append(i).append(";");
                    }
                }
                drop.append(";;");
                if (!map.get("recipe").isEmpty()) {
                    for (int i : map.get("recipe")) {
                        drop.append(i).append(";");
                    }
                }
                String data = drop.toString() + recipe.toString();
                ps = connection.prepareStatement(query);
                ps.setString(1, data);
                ps.setString(2, uuid.toString());
                ps.executeUpdate();
                if (Luminosity.debug) Bukkit.getServer().getLogger().info(ChatColor.GREEN + "DEBUG: Saved skills for " + Bukkit.getPlayer(uuid).getName());
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
            String[] profList = Luminosity.getProfessionsList;
            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, player.getName());
            StringBuilder data = new StringBuilder();
            for (String str : profList) {
                String profName = str.split("")[0] + str.split("")[1];
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
