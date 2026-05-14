package com.ezrecipes.command;

import com.ezrecipes.EzRecipes;
import com.ezrecipes.data.recipe.RecipeData;
import com.ezrecipes.gui.GuiManager;
import com.ezrecipes.manager.RecipeManager;
import com.ezrecipes.util.CustomItemUtil;
import com.ezrecipes.util.TextUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class CommandHandler implements CommandExecutor, TabCompleter {

    private final EzRecipes plugin;
    private final TextUtil textUtil;
    private final RecipeManager recipeManager;
    private final GuiManager guiManager;

    public CommandHandler(EzRecipes plugin) {
        this.plugin = plugin;
        this.textUtil = plugin.getTextUtil();
        this.recipeManager = plugin.getRecipeManager();
        this.guiManager = plugin.getGuiManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("ezrecipes")) {
            return handleAdminCommand(sender, args);
        } else if (command.getName().equalsIgnoreCase("recipes")) {
            return handlePlayerCommand(sender, args);
        }
        return false;
    }


    private boolean handleAdminCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ezrecipes.admin")) {
            sender.sendMessage(textUtil.getMessage("no_permission"));
            return true;
        }

        if (args.length == 0) {
            showHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "add":
                return handleAdd(sender);
            case "remove":
                return handleRemove(sender, args);
            case "list":
                return handleList(sender, args);
            case "reload":
                return handleReload(sender);
            case "help":
                showHelp(sender);
                return true;
            default:
                sender.sendMessage(textUtil.getMessage("unknown_command"));
                return true;
        }
    }

    private boolean handleAdd(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(textUtil.getMessage("player_only"));
            return true;
        }

        ItemStack heldItem = player.getInventory().getItemInMainHand();
        if (heldItem.getType().isAir()) {
            player.sendMessage(textUtil.getMessage("add.no_item"));
            return true;
        }

        player.openInventory(guiManager.createAddGui(player, heldItem.clone()));
        return true;
    }

    private boolean handleRemove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(textUtil.getMessage("remove.usage"));
            return true;
        }

        String recipeId = args[1];
        if (!recipeManager.removeRecipe(recipeId)) {
            sender.sendMessage(textUtil.getMessage("remove.not_found", "recipe_id", recipeId));
            return true;
        }

        sender.sendMessage(textUtil.getMessage("remove.success", "recipe_id", recipeId));
        return true;
    }

    private boolean handleList(CommandSender sender, String[] args) {
        List<RecipeData> allRecipes = recipeManager.getAllRecipes();

        if (allRecipes.isEmpty()) {
            sender.sendMessage(textUtil.getMessage("list.empty"));
            return true;
        }

        int page = 1;
        if (args.length > 1) {
            try {
                page = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(textUtil.getMessage("list.invalid_page"));
                return true;
            }
        }

        int perPage = plugin.getConfig().getInt("pagination.chat-per-page", 10);
        int totalPages = (int) Math.ceil((double) allRecipes.size() / perPage);

        if (page < 1 || page > totalPages) {
            sender.sendMessage(textUtil.getMessage("list.invalid_page"));
            return true;
        }

        int start = (page - 1) * perPage;
        int end = Math.min(start + perPage, allRecipes.size());

        sender.sendMessage(textUtil.getMessage("list.header",
                "current", String.valueOf(page),
                "total", String.valueOf(totalPages)));

        CustomItemUtil itemUtil = new CustomItemUtil(plugin);
        for (int i = start; i < end; i++) {
            RecipeData recipe = allRecipes.get(i);
            sender.sendMessage(textUtil.getMessage("list.entry",
                    "recipe_id", recipe.getId(),
                    "item_name", getItemName(recipe, itemUtil)));
        }

        if (page < totalPages) {
            sender.sendMessage(textUtil.getMessage("list.footer",
                    "next_page", String.valueOf(page + 1)));
        }

        return true;
    }

    private String getItemName(RecipeData recipe, CustomItemUtil itemUtil) {
        ItemStack item = itemUtil.loadItem(recipe.getResultItem());
        if (item == null) return "Unknown";
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return item.getItemMeta().getDisplayName();
        }
        return item.getType().toString();
    }

    private boolean handleReload(CommandSender sender) {
        plugin.reloadConfig();
        plugin.getFileManager().loadAllConfigs();
        recipeManager.loadAllRecipes();
        sender.sendMessage(textUtil.getMessage("reload.success"));
        return true;
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage(textUtil.getMessage("help.header"));
        sender.sendMessage(textUtil.getMessage("help.add"));
        sender.sendMessage(textUtil.getMessage("help.remove"));
        sender.sendMessage(textUtil.getMessage("help.list"));
        sender.sendMessage(textUtil.getMessage("help.reload"));
        sender.sendMessage(textUtil.getMessage("help.player"));
        sender.sendMessage(textUtil.getMessage("help.footer"));
    }


    private boolean handlePlayerCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(textUtil.getMessage("player_only"));
            return true;
        }

        if (!player.hasPermission("ezrecipes.use")) {
            player.sendMessage(textUtil.getMessage("no_permission"));
            return true;
        }

        int page = 1;
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                player.sendMessage(textUtil.getMessage("browser.invalid_page"));
                return true;
            }
        }

        player.openInventory(guiManager.createBrowserGui(player, page));
        return true;
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (command.getName().equalsIgnoreCase("ezrecipes")) {
            if (args.length == 1) {
                List<String> subs = Arrays.asList("add", "remove", "list", "reload", "help");
                for (String sub : subs) {
                    if (sub.startsWith(args[0].toLowerCase())) {
                        completions.add(sub);
                    }
                }
            } else if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
                for (RecipeData recipe : recipeManager.getAllRecipes()) {
                    if (recipe.getId().startsWith(args[1].toLowerCase())) {
                        completions.add(recipe.getId());
                    }
                }
            }
        }

        return completions;
    }
}