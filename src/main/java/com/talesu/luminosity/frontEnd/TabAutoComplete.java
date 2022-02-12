package com.talesu.luminosity.frontEnd;

import com.talesu.luminosity.Luminosity;
import com.talesu.luminosity.backEnd.Logic;
import com.talesu.luminosity.backEnd.Profession;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TabAutoComplete implements TabCompleter {
    List<String> subCommands = Arrays.asList("set", "check", "reset", "addDrop", "giveSkill", "addRecipe", "craft", "checkRecipe", "getCodexPage");
    List<String> setSub = Arrays.asList("status", "level");
    List<String> professions = Profession.getNames();
    List<String> skillSub = Arrays.asList("drop", "recipe");
    List<String> hotSwap = new ArrayList<>();
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        // if (playerNames.isEmpty()) for (Player p : Bukkit.getServer().getOnlinePlayers()) playerNames.add(p.getName());
        if (args.length == 1) return subCommands;
        if (args.length == 2) {
            if (args[0].equals("set") || args[0].equals("check")) return professions;
        }
        if (args.length == 3 && args[0].equals("set")) return setSub;
        if (args[0].equals("addDrop")) {
            if (args.length == 2) {
                return Luminosity.materialList;
            }
            if (args.length == 3) return professions;
            if (args.length == 4) return Collections.singletonList("[<level>]");
            if (args.length == 5) return Collections.singletonList("[<drop_chance>]");
        }
        if (args[0].equals("addSkill")) {
            if (args.length == 2) return skillSub;
            if (args.length == 3) {
                if (args[1].equals("drop")) return Collections.singletonList("[<drop_ID>]");
                else if (args[1].equals("recipe")) return Collections.singletonList("<[recipe_ID]>");
            }
        }
        if (args[0].equals("addRecipe")) {
            if (args.length == 2) return professions;
            if (args.length == 3) return Collections.singletonList("[<level>]");
        }
        if (args[0].equals("craft")) {
            if (args.length == 2) return professions;
        }
        if (args[0].equals("checkRecipe")) {
            if (args.length == 2) return professions;
        }
        if (args[0].equals("getCodexPage")) {
            if (args.length == 2) return skillSub;
            if (args.length == 3) return professions;
            if (args.length == 4) return Collections.singletonList("<[ID]>");
        }
        return null;
    }
}
