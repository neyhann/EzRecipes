package com.ezrecipes.listener;

import com.ezrecipes.EzRecipes;
import com.ezrecipes.data.gui.GuiConfig;
import com.ezrecipes.data.recipe.RecipeData;
import com.ezrecipes.gui.GuiManager;
import com.ezrecipes.manager.RecipeManager;
import com.ezrecipes.util.CustomItemUtil;
import com.ezrecipes.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GuiListener implements Listener {

    private final EzRecipes plugin;
    private final TextUtil textUtil;
    private final RecipeManager recipeManager;
    private final CustomItemUtil itemUtil;

    private final Map<UUID, Boolean> recipeModes;

    public GuiListener(EzRecipes plugin) {
        this.plugin = plugin;
        this.textUtil = plugin.getTextUtil();
        this.recipeManager = plugin.getRecipeManager();
        this.itemUtil = new CustomItemUtil(plugin);
        this.recipeModes = new HashMap<>();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (!(event.getInventory().getHolder() instanceof GuiManager.GuiHolder guiHolder)) {
            return;
        }

        String guiType = guiHolder.getType();

        switch (guiType) {
            case "add-recipe":
                handleAddGuiClick(event, player);
                break;
            case "browser":
                event.setCancelled(true);
                handleBrowserClick(event, player);
                break;
            case "view":
                event.setCancelled(true);
                handleViewClick(event, player);
                break;
        }
    }


    private void handleAddGuiClick(InventoryClickEvent event, Player player) {
        int slot = event.getRawSlot();

        // If clicking in player's own inventory, allow it
        if (slot >= event.getInventory().getSize()) {
            return;
        }

        GuiConfig guiConfig = plugin.getFileManager().getGuiConfig("add-recipe-gui.yml");
        if (guiConfig == null) {
            event.setCancelled(true);
            return;
        }

        Map<Integer, String> craftingSlots = guiConfig.getCraftingSlots();

        if (craftingSlots.containsKey(slot)) {
            // Don't cancel — let the player place/pick up items freely
            return;
        }

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        Map<String, GuiConfig.Button> buttons = guiConfig.getButtons();
        for (Map.Entry<String, GuiConfig.Button> entry : buttons.entrySet()) {
            if (entry.getValue().getSlot() == slot) {
                switch (entry.getKey()) {
                    case "confirm":
                        handleConfirm(player, event.getInventory());
                        break;
                    case "cancel":
                        player.closeInventory();
                        player.sendMessage(textUtil.getMessage("add.cancelled"));
                        break;
                    case "mode_toggle":
                        toggleMode(player);
                        updateModeButton(event.getInventory(), player, guiConfig);
                        break;
                }
                break;
            }
        }
    }

    private void handleConfirm(Player player, Inventory inventory) {
        GuiConfig guiConfig = plugin.getFileManager().getGuiConfig("add-recipe-gui.yml");
        if (guiConfig == null) return;

        Map<Integer, String> craftingSlots = guiConfig.getCraftingSlots();
        ItemStack[] contents = inventory.getContents();
        Map<String, String> ingredients = itemUtil.getIngredients(contents, craftingSlots);

        if (!itemUtil.hasIngredients(ingredients)) {
            player.sendMessage(textUtil.getMessage("add.empty_grid"));
            return;
        }

        GuiConfig.DisplayItem resultSlot = guiConfig.getItem("result");
        if (resultSlot == null) return;

        ItemStack resultItem = inventory.getItem(resultSlot.getSlot());
        if (resultItem == null || resultItem.getType() == Material.AIR) {
            player.sendMessage(textUtil.getMessage("add.no_result"));
            return;
        }

        boolean isShapeless = recipeModes.getOrDefault(player.getUniqueId(), false);
        String recipeId = recipeManager.createId(resultItem);

        RecipeData recipe = new RecipeData(
                recipeId,
                itemUtil.saveItem(resultItem),
                ingredients,
                isShapeless,
                System.currentTimeMillis(),
                player.getName()
        );

        if (recipeManager.addRecipe(recipe)) {
            player.closeInventory();
            player.sendMessage(textUtil.getMessage("add.success", "recipe_id", recipeId));
            broadcastAdd(recipe, player);
        } else {
            player.sendMessage(textUtil.getMessage("add.overwrite_failed"));
        }

        recipeModes.remove(player.getUniqueId());
    }

    private void toggleMode(Player player) {
        UUID id = player.getUniqueId();
        boolean current = recipeModes.getOrDefault(id, false);
        recipeModes.put(id, !current);

        String msg = !current ? "add.mode_shapeless" : "add.mode_shaped";
        player.sendMessage(textUtil.getMessage(msg));
    }

    private void updateModeButton(Inventory inventory, Player player, GuiConfig guiConfig) {
        GuiConfig.Button modeButton = guiConfig.getButton("mode_toggle");
        if (modeButton == null) return;

        boolean isShapeless = recipeModes.getOrDefault(player.getUniqueId(), false);
        String modeText = isShapeless ? "Shapeless" : "Shaped";

        Material mat = Material.getMaterial(modeButton.getMaterial());
        if (mat == null) mat = Material.REPEATER;

        ItemStack item = new ItemStack(mat);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String name = textUtil.replaceVariables(modeButton.getName(), "recipe_type", modeText);
            meta.setDisplayName(textUtil.format(name));

            java.util.List<String> lore = new java.util.ArrayList<>();
            for (String line : modeButton.getLore()) {
                lore.add(textUtil.format(textUtil.replaceVariables(line, "recipe_type", modeText)));
            }
            meta.setLore(lore);

            item.setItemMeta(meta);
        }
        inventory.setItem(modeButton.getSlot(), item);
    }

    private void broadcastAdd(RecipeData recipe, Player player) {
        if (plugin.getConfig().getBoolean("general.broadcast-add", true)) {
            String msg = textUtil.getMessage("broadcast.recipe_added",
                    "player", player.getName(),
                    "recipe_id", recipe.getId());

            for (Player p : plugin.getServer().getOnlinePlayers()) {
                if (p.hasPermission("ezrecipes.admin")) {
                    p.sendMessage(msg);
                }
            }
        }
    }


    private void handleBrowserClick(InventoryClickEvent event, Player player) {
        int slot = event.getSlot();
        ItemStack clickedItem = event.getCurrentItem();

        GuiConfig guiConfig = plugin.getFileManager().getGuiConfig("recipe-browser-gui.yml");
        if (guiConfig == null) return;

        for (Map.Entry<String, GuiConfig.Button> entry : guiConfig.getButtons().entrySet()) {
            if (entry.getValue().getSlot() == slot) {
                switch (entry.getKey()) {
                    case "previous":
                        navigatePage(player, event.getInventory(), -1);
                        break;
                    case "next":
                        navigatePage(player, event.getInventory(), 1);
                        break;
                }
                return;
            }
        }

        if (clickedItem != null && clickedItem.getType() != Material.AIR) {
            for (Map.Entry<String, GuiConfig.DisplayItem> entry : guiConfig.getItems().entrySet()) {
                if (entry.getKey().startsWith("recipe_") && entry.getValue().getSlot() == slot) {
                    handleRecipeClick(player, clickedItem);
                    return;
                }
            }
        }
    }

    private void navigatePage(Player player, Inventory inventory, int direction) {
        int currentPage = getPage(inventory);
        int newPage = currentPage + direction;

        if (newPage < 1) return;

        int totalRecipes = recipeManager.getAllRecipes().size();
        int perPage = plugin.getConfig().getInt("pagination.recipes-per-page", 21);
        int totalPages = Math.max(1, (int) Math.ceil((double) totalRecipes / perPage));

        if (newPage > totalPages) return;

        player.openInventory(plugin.getGuiManager().createBrowserGui(player, newPage));
    }

    private int getPage(Inventory inventory) {
        GuiConfig guiConfig = plugin.getFileManager().getGuiConfig("recipe-browser-gui.yml");
        if (guiConfig == null) return 1;

        GuiConfig.DisplayItem indicator = guiConfig.getItem("page_indicator");
        if (indicator == null) return 1;

        ItemStack item = inventory.getItem(indicator.getSlot());
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return 1;
        }

        String name = org.bukkit.ChatColor.stripColor(item.getItemMeta().getDisplayName());
        try {
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(\\d+)/(\\d+)").matcher(name);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
        } catch (Exception ignored) {}

        return 1;
    }

    private void handleRecipeClick(Player player, ItemStack clickedItem) {
        String recipeId = getRecipeId(clickedItem);
        if (recipeId != null) {
            RecipeData recipe = recipeManager.getRecipe(recipeId);
            if (recipe != null) {
                player.openInventory(plugin.getGuiManager().createViewGui(player, recipe));
            }
        }
    }

    private String getRecipeId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;

        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        if (!meta.hasLore() || meta.getLore() == null) return null;

        for (String line : meta.getLore()) {
            String stripped = org.bukkit.ChatColor.stripColor(line);
            if (stripped.startsWith("ID: ")) {
                return stripped.substring(4).trim();
            }
        }
        return null;
    }


    private void handleViewClick(InventoryClickEvent event, Player player) {
        int slot = event.getSlot();

        GuiConfig guiConfig = plugin.getFileManager().getGuiConfig("recipe-view-gui.yml");
        if (guiConfig == null) return;

        for (Map.Entry<String, GuiConfig.Button> entry : guiConfig.getButtons().entrySet()) {
            if (entry.getValue().getSlot() == slot && entry.getKey().equals("back")) {
                player.openInventory(plugin.getGuiManager().createBrowserGui(player, 1));
                return;
            }
        }
    }


    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!(event.getInventory().getHolder() instanceof GuiManager.GuiHolder guiHolder)) return;

        String guiType = guiHolder.getType();

        if (guiType.equals("add-recipe")) {
            GuiConfig guiConfig = plugin.getFileManager().getGuiConfig("add-recipe-gui.yml");
            if (guiConfig != null) {
                Map<Integer, String> craftingSlots = guiConfig.getCraftingSlots();
                for (int dragSlot : event.getRawSlots()) {
                    if (dragSlot < event.getInventory().getSize() && !craftingSlots.containsKey(dragSlot)) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        } else {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (!(event.getInventory().getHolder() instanceof GuiManager.GuiHolder guiHolder)) return;

        if (guiHolder.getType().equals("add-recipe")) {
            recipeModes.remove(player.getUniqueId());
        }
    }
}