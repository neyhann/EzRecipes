package com.ezrecipes.config;

import com.ezrecipes.EzRecipes;
import com.ezrecipes.data.gui.GuiConfig;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class FileManager {

    private final EzRecipes plugin;
    private final Map<String, GuiConfig> guiConfigs;

    private FileConfiguration langConfig;
    private FileConfiguration recipesConfig;

    public FileManager(EzRecipes plugin) {
        this.plugin = plugin;
        this.guiConfigs = new HashMap<>();
    }

    public void loadAllConfigs() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        loadLang();
        loadRecipes();
        loadGuis();
    }

    private void loadLang() {
        File lang = new File(plugin.getDataFolder() + File.separator + "lang", "en.yml");
        if (!lang.exists()) {
            plugin.saveResource("lang" + File.separator + "en.yml", false);
        }
        langConfig = YamlConfiguration.loadConfiguration(lang);
    }

    private void loadRecipes() {
        File recipesFile = new File(plugin.getDataFolder(), "recipes.yml");
        if (!recipesFile.exists()) {
            try {
                recipesFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create recipes.yml", e);
            }
        }
        recipesConfig = YamlConfiguration.loadConfiguration(recipesFile);
    }

    private void loadGuis() {
        guiConfigs.clear();
        String[] guiFiles = {"add-recipe-gui.yml", "recipe-browser-gui.yml", "recipe-view-gui.yml"};

        for (String guiFile : guiFiles) {
            File file = new File(plugin.getDataFolder() + File.separator + "gui", guiFile);
            if (!file.exists()) {
                plugin.saveResource("gui" + File.separator + guiFile, false);
            }

            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            guiConfigs.put(guiFile, parseGui(config));
        }
    }

    private GuiConfig parseGui(FileConfiguration config) {
        GuiConfig gui = new GuiConfig();

        gui.setTitle(config.getString("title", "GUI"));
        gui.setRows(config.getInt("rows", 5));

        GuiConfig.FillerItem filler = new GuiConfig.FillerItem();
        filler.setMaterial(config.getString("filler.material", "BLACK_STAINED_GLASS_PANE"));
        filler.setName(config.getString("filler.name", " "));
        filler.setLore(config.getStringList("filler.lore"));
        filler.setGlow(config.getBoolean("filler.glow", false));
        gui.setFiller(filler);

        if (config.contains("slots.crafting")) {
            Map<Integer, String> craftingSlots = new HashMap<>();
            for (String key : config.getConfigurationSection("slots.crafting").getKeys(false)) {
                int slot = config.getInt("slots.crafting." + key);
                craftingSlots.put(slot, key);
            }
            gui.setCraftingSlots(craftingSlots);
        }

        if (config.contains("buttons")) {
            for (String buttonKey : config.getConfigurationSection("buttons").getKeys(false)) {
                String path = "buttons." + buttonKey;

                GuiConfig.Button button = new GuiConfig.Button();
                button.setSlot(config.getInt(path + ".slot", 0));
                button.setMaterial(config.getString(path + ".material", "STONE"));
                button.setName(config.getString(path + ".name", "Button"));
                button.setLore(config.getStringList(path + ".lore"));
                button.setGlow(config.getBoolean(path + ".glow", false));

                gui.addButton(buttonKey, button);
            }
        }

        if (config.contains("items")) {
            for (String itemKey : config.getConfigurationSection("items").getKeys(false)) {
                String path = "items." + itemKey;

                GuiConfig.DisplayItem item = new GuiConfig.DisplayItem();
                item.setSlot(config.getInt(path + ".slot", 0));
                item.setMaterial(config.getString(path + ".material", "PAPER"));
                item.setName(config.getString(path + ".name", "Item"));
                item.setLore(config.getStringList(path + ".lore"));
                item.setGlow(config.getBoolean(path + ".glow", false));

                gui.addItem(itemKey, item);
            }
        }

        return gui;
    }

    public FileConfiguration getLang() {
        return langConfig;
    }

    public FileConfiguration getRecipes() {
        return recipesConfig;
    }

    public GuiConfig getGuiConfig(String guiName) {
        return guiConfigs.get(guiName);
    }

    public void saveRecipes() {
        File recipesFile = new File(plugin.getDataFolder(), "recipes.yml");
        try {
            recipesConfig.save(recipesFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save recipes.yml", e);
        }
    }
}