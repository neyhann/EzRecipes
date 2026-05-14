package com.ezrecipes.manager;

import com.ezrecipes.EzRecipes;
import com.ezrecipes.data.recipe.RecipeData;
import com.ezrecipes.util.CustomItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import java.util.*;

public class RecipeManager {

    private final EzRecipes plugin;
    private final CustomItemUtil itemUtil;
    private final Map<String, RecipeData> recipes;
    private final Map<String, NamespacedKey> recipeKeys;

    public RecipeManager(EzRecipes plugin) {
        this.plugin = plugin;
        this.itemUtil = new CustomItemUtil(plugin);
        this.recipes = new LinkedHashMap<>();
        this.recipeKeys = new HashMap<>();
    }

    public void loadAllRecipes() {
        unregisterAllRecipes();

        ConfigurationSection section = plugin.getFileManager().getRecipes().getConfigurationSection("recipes");
        if (section == null) return;

        for (String recipeId : section.getKeys(false)) {
            ConfigurationSection recipeSection = section.getConfigurationSection(recipeId);
            if (recipeSection != null) {
                try {
                    Map<String, Object> data = recipeSection.getValues(false);
                    RecipeData recipe = RecipeData.fromMap(data);
                    recipes.put(recipeId, recipe);
                    registerRecipe(recipe);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to load recipe: " + recipeId + " - " + e.getMessage());
                }
            }
        }

        plugin.getLogger().info("Loaded " + recipes.size() + " recipe(s).");
    }

    public void registerRecipe(RecipeData recipe) {
        ItemStack result = itemUtil.loadItem(recipe.getResultItem());
        if (result == null || result.getType() == Material.AIR) {
            plugin.getLogger().warning("Skipping recipe " + recipe.getId() + ": invalid result item");
            return;
        }

        NamespacedKey key = new NamespacedKey(plugin, recipe.getId());
        recipeKeys.put(recipe.getId(), key);

        try {
            if (recipe.isShapeless()) {
                registerShapeless(recipe, result, key);
            } else {
                registerShaped(recipe, result, key);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to register recipe " + recipe.getId() + ": " + e.getMessage());
            recipeKeys.remove(recipe.getId());
        }
    }

    private void registerShapeless(RecipeData recipe, ItemStack result, NamespacedKey key) {
        ShapelessRecipe bukkitRecipe = new ShapelessRecipe(key, result);

        for (Map.Entry<String, String> entry : recipe.getIngredients().entrySet()) {
            String itemData = entry.getValue();
            if (!itemData.equals("AIR")) {
                ItemStack ingredient = itemUtil.loadItem(itemData);
                if (ingredient != null && ingredient.getType() != Material.AIR) {
                    bukkitRecipe.addIngredient(ingredient.getType());
                }
            }
        }

        Bukkit.addRecipe(bukkitRecipe);
    }

    private void registerShaped(RecipeData recipe, ItemStack result, NamespacedKey key) {
        ShapedRecipe bukkitRecipe = new ShapedRecipe(key, result);

        Map<String, String> ingredients = recipe.getIngredients();

        char[][] grid = new char[3][3];
        Map<Character, Material> charToMaterial = new HashMap<>();
        char nextChar = 'a';

        String[] rows = {"A", "B", "C"};
        String[] cols = {"1", "2", "3"};

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                String position = rows[r] + cols[c];
                String itemData = ingredients.get(position);

                if (itemData != null && !itemData.equals("AIR")) {
                    ItemStack ingredient = itemUtil.loadItem(itemData);
                    if (ingredient != null && ingredient.getType() != Material.AIR) {
                        grid[r][c] = nextChar;
                        charToMaterial.put(nextChar, ingredient.getType());
                        nextChar++;
                    } else {
                        grid[r][c] = ' ';
                    }
                } else {
                    grid[r][c] = ' ';
                }
            }
        }

        int minRow = 3, maxRow = -1, minCol = 3, maxCol = -1;
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (grid[r][c] != ' ') {
                    minRow = Math.min(minRow, r);
                    maxRow = Math.max(maxRow, r);
                    minCol = Math.min(minCol, c);
                    maxCol = Math.max(maxCol, c);
                }
            }
        }

        if (maxRow < 0) return;

        String[] shape = new String[maxRow - minRow + 1];
        for (int r = minRow; r <= maxRow; r++) {
            StringBuilder sb = new StringBuilder();
            for (int c = minCol; c <= maxCol; c++) {
                sb.append(grid[r][c]);
            }
            shape[r - minRow] = sb.toString();
        }

        bukkitRecipe.shape(shape);

        for (Map.Entry<Character, Material> entry : charToMaterial.entrySet()) {
            bukkitRecipe.setIngredient(entry.getKey(), entry.getValue());
        }

        Bukkit.addRecipe(bukkitRecipe);
    }

    public boolean addRecipe(RecipeData recipe) {
        String recipeId = recipe.getId();

        if (recipes.containsKey(recipeId)) {
            if (!plugin.getConfig().getBoolean("general.allow-overwrite", false)) {
                return false;
            }
            removeRecipe(recipeId);
        }

        recipes.put(recipeId, recipe);
        registerRecipe(recipe);
        saveRecipe(recipe);

        return true;
    }

    public boolean removeRecipe(String recipeId) {
        if (!recipes.containsKey(recipeId)) {
            return false;
        }

        recipes.remove(recipeId);

        NamespacedKey key = recipeKeys.remove(recipeId);
        if (key != null) {
            Bukkit.removeRecipe(key);
        }

        plugin.getFileManager().getRecipes().set("recipes." + recipeId, null);
        plugin.getFileManager().saveRecipes();

        return true;
    }

    public void unregisterAllRecipes() {
        for (NamespacedKey key : recipeKeys.values()) {
            Bukkit.removeRecipe(key);
        }
        recipeKeys.clear();
        recipes.clear();
    }

    private void saveRecipe(RecipeData recipe) {
        ConfigurationSection section = plugin.getFileManager().getRecipes()
                .createSection("recipes." + recipe.getId());

        for (Map.Entry<String, Object> entry : recipe.toMap().entrySet()) {
            section.set(entry.getKey(), entry.getValue());
        }

        plugin.getFileManager().saveRecipes();
    }

    public RecipeData getRecipe(String recipeId) {
        return recipes.get(recipeId);
    }

    public List<RecipeData> getAllRecipes() {
        return new ArrayList<>(recipes.values());
    }

    public String createId(ItemStack resultItem) {
        String baseId;

        if (resultItem.hasItemMeta() && resultItem.getItemMeta().hasDisplayName()) {
            baseId = resultItem.getItemMeta().getDisplayName()
                    .replaceAll("§[0-9a-fk-or]", "")
                    .replaceAll("[^a-zA-Z0-9]", "_")
                    .toLowerCase()
                    .replaceAll("_+", "_")
                    .replaceAll("^_|_$", "");

            if (baseId.isEmpty()) {
                baseId = resultItem.getType().name().toLowerCase();
            }
        } else {
            baseId = resultItem.getType().name().toLowerCase();
        }

        String recipeId = baseId;
        int counter = 1;

        while (recipes.containsKey(recipeId)) {
            recipeId = baseId + "_" + counter;
            counter++;
        }

        return recipeId;
    }
}