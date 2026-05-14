package com.ezrecipes.util;

import com.ezrecipes.EzRecipes;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtil {

    private final EzRecipes plugin;
    
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public TextUtil(EzRecipes plugin) {
        this.plugin = plugin;
    }

    public String format(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        String result = translateHex(text);
        result = ChatColor.translateAlternateColorCodes('&', result);
        return result;
    }

    public String replaceVariables(String text, String... replacements) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        String result = text;
        for (int i = 0; i < replacements.length - 1; i += 2) {
            String key = replacements[i];
            String value = replacements[i + 1];
            result = result.replace("{" + key + "}", value != null ? value : "");
        }
        return result;
    }

    public String getPrefix() {
        String prefix = plugin.getConfig().getString("prefix", "&8[&6EzRecipes&8] &7");
        return format(prefix);
    }

    public String getMessage(String path) {
        String message = plugin.getFileManager().getLang().getString(path, "");
        if (message.isEmpty()) {
            return getPrefix() + ChatColor.RED + "Missing message: " + path;
        }
        return format(message.replace("{prefix}", getPrefix()));
    }

    public String getMessage(String path, String... replacements) {
        String message = getMessage(path);
        return replaceVariables(message, replacements);
    }

    private String translateHex(String text) {
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder replacement = new StringBuilder("§x");
            for (char c : hex.toCharArray()) {
                replacement.append("§").append(c);
            }
            matcher.appendReplacement(sb, replacement.toString());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}