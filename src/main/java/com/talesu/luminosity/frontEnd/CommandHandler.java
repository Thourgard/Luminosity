package com.talesu.luminosity.frontEnd;

import com.talesu.luminosity.Luminosity;
import com.talesu.luminosity.backEnd.Logic;
import com.talesu.luminosity.backEnd.SQL;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CommandHandler implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        try {
            if (args.length >= 1 && args[0].equals("set")) {
                if (args.length == 1) {
                    sender.sendMessage("/" + command + " set {professionsName} status/level");
                    return true;
                } else if (args.length == 3 && args[2].equals("status")) {
                    Logic.updateProfData(((Player) sender), args[1], !Luminosity.jobStatus.get(((Player) sender).getUniqueId()).get(args[1]));
                    return true;
                } else if (args.length >= 4 && args[2].equals("level")) {
                    try {
                        Logic.updateProfData(((Player) sender), args[1], Integer.parseInt(args[3]));
                    } catch (Exception e) {
                        return false;
                    }
                    return true;
                }
            } else if (args.length >= 1 && args[0].equals("check")) {
                sender.sendMessage("Status: " + Luminosity.jobStatus.get(((Player) sender).getUniqueId()).get(args[1]).toString() + "; EXP: " + Luminosity.jobExp.get(((Player) sender).getUniqueId()).get(args[1]));
            } else if (args.length == 1 && args[0].equals("reset")) {
                Player p = ((Player) sender);
                SQL.fullReset(p);
                String[] profList = Luminosity.getProfessionsList;
                for (String str : profList) {
                    Logic.updateProfData(p, str, false, 0, null);
                }
                SQL.addRecord(p);
                Logic.loadPlayerData(p);
            } else if (args[0].equals("addDrop")) {
                if (!((Player) sender).getInventory().getItemInMainHand().getType().equals(Material.AIR)) {
                    sender.sendMessage("DEBUG: The New Drop ID = " +
                            Logic.addBlockDrop(Material.valueOf(args[1].toUpperCase()), args[2], Integer.parseInt(args[3]),
                                    Integer.parseInt(args[4]), ((Player) sender).getInventory().getItemInMainHand()));
                }
            } else if (args[0].equals("addSkill")) {
                if (args[1].equals("drop")) Logic.addDrop(((Player) sender), Integer.parseInt(args[2]));
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
