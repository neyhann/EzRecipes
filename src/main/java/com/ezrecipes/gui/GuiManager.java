package com.ezrecipes.gui;

import com.ezrecipes.EzRecipes;
import com.ezrecipes.data.gui.GuiConfig;
import com.ezrecipes.data.recipe.RecipeData;
import com.ezrecipes.util.CustomItemUtil;
import com.ezrecipes.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class GuiManager {

    private final EzRecipes plugin;
    private final TextUtil textUtil;
    private final CustomItemUtil itemUtil;

    private static final int[] BORDER_SLOTS_5 = {
        0, 1, 2, 3, 4, 5, 6, 7, 8,     // top row
        9, 17,                             // left/right edges row 2
        18, 26,                            // left/right edges row 3
        27, 35,                            // left/right edges row 4
        36, 37, 38, 39, 40, 41, 42, 43, 44 // bottom row
    };

    public GuiManager(EzRecipes plugin) {
        this.plugin = plugin;
        this.textUtil = plugin.getTextUtil();
        this.itemUtil = new CustomItemUtil(plugin);
    }


    public Inventory createAddGui(Player player, ItemStack resultItem) {
        GuiConfig config = plugin.getFileManager().getGuiConfig("add-recipe-gui.yml");
        if (config == null) return null;

        String title = textUtil.format(config.getTitle());
        int size = config.getRows() * 9;
        Inventory inv = Bukkit.createInventory(new GuiHolder("add-recipe"), size, title);

        fillBorder(inv, config, size);

        for (Map.Entry<Integer, String> entry : config.getCraftingSlots().entrySet()) {
            inv.setItem(entry.getKey(), new ItemStack(Material.AIR));
        }

        for (Map.Entry<String, GuiConfig.Button> entry : config.getButtons().entrySet()) {
            GuiConfig.Button btn = entry.getValue();
            inv.setItem(btn.getSlot(), createButton(btn));
        }

        for (Map.Entry<String, GuiConfig.DisplayItem> entry : config.getItems().entrySet()) {
            GuiConfig.DisplayItem item = entry.getValue();
            if (entry.getKey().equals("result") && resultItem != null) {
                inv.setItem(item.getSlot(), resultItem);
            } else if (!entry.getKey().equals("result")) {
                inv.setItem(item.getSlot(), createDisplayItem(item));
            }
        }

        return inv;
    }


    public Inventory createBrowserGui(Player player, int page) {
        GuiConfig config = plugin.getFileManager().getGuiConfig("recipe-browser-gui.yml");
        if (config == null) return null;

        String title = textUtil.format(config.getTitle());
        int size = config.getRows() * 9;
        Inventory inv = Bukkit.createInventory(new GuiHolder("browser"), size, title);

        // Fill border
        fillBorder(inv, config, size);

        List<RecipeData> allRecipes = plugin.getRecipeManager().getAllRecipes();
        int perPage = plugin.getConfig().getInt("pagination.recipes-per-page", 21);
        int totalPages = Math.max(1, (int) Math.ceil((double) allRecipes.size() / perPage));

        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;

        if (allRecipes.isEmpty()) {
            GuiConfig.DisplayItem emptyItem = config.getItem("empty_state");
            if (emptyItem != null) {
                inv.setItem(emptyItem.getSlot(), createDisplayItem(emptyItem));
            }
        } else {
            int start = (page - 1) * perPage;
            int end = Math.min(start + perPage, allRecipes.size());
            List<RecipeData> pageRecipes = allRecipes.subList(start, end);

            List<Integer> recipeSlots = getRecipeSlots(config);
            for (int i = 0; i < pageRecipes.size() && i < recipeSlots.size(); i++) {
                RecipeData recipe = pageRecipes.get(i);
                inv.setItem(recipeSlots.get(i), createRecipeDisplayItem(recipe, config));
            }
        }

        for (Map.Entry<String, GuiConfig.Button> entry : config.getButtons().entrySet()) {
            GuiConfig.Button btn = entry.getValue();
            String name = textUtil.replaceVariables(btn.getName(),
                    "current", String.valueOf(page),
                    "total", String.valueOf(totalPages),
                    "prev", String.valueOf(page - 1),
                    "next", String.valueOf(page + 1));

            ItemStack item = createButton(btn);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(textUtil.format(name));

                List<String> lore = new ArrayList<>();
                for (String line : btn.getLore()) {
                    lore.add(textUtil.format(textUtil.replaceVariables(line,
                            "current", String.valueOf(page),
                            "total", String.valueOf(totalPages),
                            "prev", String.valueOf(page - 1),
                            "next", String.valueOf(page + 1))));
                }
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            inv.setItem(btn.getSlot(), item);
        }

        GuiConfig.DisplayItem indicator = config.getItem("page_indicator");
        if (indicator != null) {
            String name = textUtil.replaceVariables(indicator.getName(),
                    "current", String.valueOf(page),
                    "total", String.valueOf(totalPages));

            ItemStack item = createDisplayItem(indicator);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(textUtil.format(name));
                item.setItemMeta(meta);
            }
            inv.setItem(indicator.getSlot(), item);
        }

        return inv;
    }


    public Inventory createViewGui(Player player, RecipeData recipe) {
        GuiConfig config = plugin.getFileManager().getGuiConfig("recipe-view-gui.yml");
        if (config == null) return null;

        String title = textUtil.format(config.getTitle());
        int size = config.getRows() * 9;
        Inventory inv = Bukkit.createInventory(new GuiHolder("view"), size, title);

        // Fill border
        fillBorder(inv, config, size);

        for (Map.Entry<Integer, String> entry : config.getCraftingSlots().entrySet()) {
            int slot = entry.getKey();
            String position = entry.getValue();

            String ingredientData = recipe.getIngredients().get(position);
            if (ingredientData != null && !ingredientData.equals("AIR")) {
                ItemStack ingredient = itemUtil.loadItem(ingredientData);
                if (ingredient != null) {
                    inv.setItem(slot, ingredient);
                }
            } else {
                inv.setItem(slot, new ItemStack(Material.AIR));
            }
        }

        GuiConfig.DisplayItem resultSlot = config.getItem("result");
        if (resultSlot != null) {
            ItemStack result = itemUtil.loadItem(recipe.getResultItem());
            if (result != null) {
                inv.setItem(resultSlot.getSlot(), result);
            }
        }

        for (Map.Entry<String, GuiConfig.DisplayItem> entry : config.getItems().entrySet()) {
            GuiConfig.DisplayItem item = entry.getValue();
            if (entry.getKey().equals("result")) continue;

            if (entry.getKey().equals("recipe_type")) {
                String typeText = recipe.isShapeless() ? "Shapeless" : "Shaped";
                ItemStack typeItem = createDisplayItem(item);
                ItemMeta meta = typeItem.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(textUtil.format(textUtil.replaceVariables(item.getName(), "recipe_type", typeText)));
                    List<String> lore = new ArrayList<>();
                    for (String line : item.getLore()) {
                        lore.add(textUtil.format(textUtil.replaceVariables(line, "recipe_type", typeText)));
                    }
                    meta.setLore(lore);
                    typeItem.setItemMeta(meta);
                }
                inv.setItem(item.getSlot(), typeItem);
            } else {
                inv.setItem(item.getSlot(), createDisplayItem(item));
            }
        }

        for (Map.Entry<String, GuiConfig.Button> entry : config.getButtons().entrySet()) {
            inv.setItem(entry.getValue().getSlot(), createButton(entry.getValue()));
        }

        return inv;
    }


    private void fillBorder(Inventory inv, GuiConfig config, int size) {
        ItemStack filler = createFillerItem(config);

        int rows = size / 9;
        for (int i = 0; i < size; i++) {
            int row = i / 9;
            int col = i % 9;

            if (row == 0 || row == rows - 1 || col == 0 || col == 8) {
                inv.setItem(i, filler);
            }
        }
    }

    private ItemStack createFillerItem(GuiConfig config) {
        Material mat = Material.getMaterial(config.getFiller().getMaterial());
        if (mat == null) mat = Material.BLACK_STAINED_GLASS_PANE;

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(textUtil.format(config.getFiller().getName()));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createButton(GuiConfig.Button button) {
        Material mat = Material.getMaterial(button.getMaterial());
        if (mat == null) mat = Material.STONE;

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(textUtil.format(button.getName()));

            List<String> lore = new ArrayList<>();
            for (String line : button.getLore()) {
                lore.add(textUtil.format(line));
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createDisplayItem(GuiConfig.DisplayItem itemConfig) {
        Material mat = Material.getMaterial(itemConfig.getMaterial());
        if (mat == null) mat = Material.PAPER;

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(textUtil.format(itemConfig.getName()));

            List<String> lore = new ArrayList<>();
            for (String line : itemConfig.getLore()) {
                lore.add(textUtil.format(line));
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private List<Integer> getRecipeSlots(GuiConfig config) {
        List<Integer> slots = new ArrayList<>();
        for (Map.Entry<String, GuiConfig.DisplayItem> entry : config.getItems().entrySet()) {
            if (entry.getKey().startsWith("recipe_") && !entry.getKey().equals("recipe_display")) {
                slots.add(entry.getValue().getSlot());
            }
        }
        Collections.sort(slots);
        return slots;
    }

    private ItemStack createRecipeDisplayItem(RecipeData recipe, GuiConfig config) {
        ItemStack item = itemUtil.loadItem(recipe.getResultItem());
        if (item == null || item.getType() == Material.AIR) {
            return new ItemStack(Material.BARRIER);
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            List<String> lore = meta.getLore();
            if (lore == null) lore = new ArrayList<>();

            GuiConfig.DisplayItem displayConfig = config.getItem("recipe_display");
            if (displayConfig != null) {
                for (String line : displayConfig.getLore()) {
                    String formatted = textUtil.replaceVariables(line,
                            "recipe_id", recipe.getId(),
                            "item_name", meta.hasDisplayName() ? meta.getDisplayName() : item.getType().toString(),
                            "recipe_type", recipe.isShapeless() ? "Shapeless" : "Shaped");
                    lore.add(textUtil.format(formatted));
                }
            }

            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }


    public static class GuiHolder implements InventoryHolder {
        private final String type;

        public GuiHolder(String type) {
            this.type = type;
        }

        @Override
        public Inventory getInventory() {
            return null;
        }

        public String getType() {
            return type;
        }
    }
}