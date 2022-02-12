package com.talesu.luminosity.frontEnd;

import com.talesu.luminosity.Luminosity;
import com.talesu.luminosity.backEnd.Logic;
import com.talesu.luminosity.backEnd.Profession;
import com.talesu.luminosity.backEnd.SQL;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.xml.sax.ext.Locator2Impl;

import java.util.HashMap;

public class CommandHandler implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        try {
            if (args.length >= 1 && args[0].equals("set")) {
                if (args.length == 1) {
                    sender.sendMessage("/" + command + " set {professionsName} status/level");
                    return true;
                } else if (args.length == 3 && args[2].equals("status")) {
                    Logic.updateProfData(((Player) sender), Profession.getProfession(args[1]), !((boolean) Luminosity.playerData.get(((Player) sender).getUniqueId()).get(Profession.getProfession(args[1])).get("status")));
                    return true;
                } else if (args.length >= 4 && args[2].equals("level")) {
                    try {
                        Logic.updateProfData(((Player) sender), Profession.getProfession(args[1]), Integer.parseInt(args[3]));
                    } catch (Exception e) {
                        return false;
                    }
                    return true;
                }
            } else if (args.length >= 1 && args[0].equals("check")) {
                sender.sendMessage("Status: " + Profession.getProfession(args[1]).getStatus(((Player) sender)) +
                        "; EXP: " + Profession.getProfession(args[1]).getExp(((Player) sender)) +
                        "; Level: " + Profession.getProfession(args[1]).getLevel(((Player) sender)));
            } else if (args.length == 1 && args[0].equals("reset")) {
                Player p = ((Player) sender);
                SQL.fullReset(p);
                for (Profession profession : Profession.values()) {
                    Logic.updateProfData(p, profession, false, 0);
                    Logic.updateProfData(p, profession, "drop", null);
                    Logic.updateProfData(p, profession, "recipe", null);
                }
                SQL.addRecord(p);
                Logic.loadPlayerData(p);
            } else if (args[0].equals("addDrop")) {
                if (!((Player) sender).getInventory().getItemInMainHand().getType().equals(Material.AIR)) {
                    sender.sendMessage("DEBUG: The New Drop's ID = " +
                            Profession.getProfession(args[1]).addRecipe(Integer.parseInt(args[2]),null, ((Player) sender).getInventory().getItemInMainHand()));

                }
            } else if (args[0].equals("addRecipe")) {
                Luminosity.tempoBin.putIfAbsent(((Player) sender).getUniqueId(), new HashMap<>());
                Luminosity.tempoBin.get(((Player) sender).getUniqueId()).put("recipeProfession", args[1]);
                Luminosity.tempoBin.get(((Player) sender).getUniqueId()).put("recipeLevel", args[2]);
                ((Player) sender).openInventory(Logic.getIngredientsInv());
            } else if (args[0].equals("craft")) {
                ((Player) sender).openInventory(Logic.getCraftingMenu(((Player) sender), Profession.getProfession(args[1])));
            } else if (args[0].equals("checkRecipe")) {
                sender.sendMessage(Profession.getProfession(args[1]).getRecipes().toString());
            }
            else if (args[0].equals("giveSkill")) {
                int id = Integer.parseInt(args[2]);
                if (args[1].equals("drop") || args[1].equals("recipe")) {
                    String type = args[1];
                    if (Profession.getProfessionByDrop(id).giveSkill(((Player) sender), id, type))
                        sender.sendMessage(ChatColor.WHITE + "Successfully given " + type + " #" + args[2] + " to " + ((Player) sender).getName());
                    else sender.sendMessage(ChatColor.RED + sender.getName() + " already has " + type + " #" + args[2]);
                }
            }
            else if (args[0].equals("getCodexPage")) {
                if (args[1].equals("drop") || args[1].equals("recipe")) {
                    ((Player) sender).getInventory().addItem(Logic.getCodexPage(Integer.parseInt(args[3]), Profession.getProfession(args[2]), args[1].equals("recipe")));
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
