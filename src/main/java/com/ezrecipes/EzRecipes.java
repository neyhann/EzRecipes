package com.ezrecipes;

import com.ezrecipes.command.CommandHandler;
import com.ezrecipes.config.FileManager;
import com.ezrecipes.gui.GuiManager;
import com.ezrecipes.listener.GuiListener;
import com.ezrecipes.manager.RecipeManager;
import com.ezrecipes.util.TextUtil;
import org.bukkit.plugin.java.JavaPlugin;

public class EzRecipes extends JavaPlugin {

    private static EzRecipes instance;
    private FileManager fileManager;
    private TextUtil textUtil;
    private RecipeManager recipeManager;
    private GuiManager guiManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        fileManager = new FileManager(this);
        textUtil = new TextUtil(this);
        recipeManager = new RecipeManager(this);
        guiManager = new GuiManager(this);

        CommandHandler commandHandler = new CommandHandler(this);
        getCommand("ezrecipes").setExecutor(commandHandler);
        getCommand("ezrecipes").setTabCompleter(commandHandler);
        getCommand("recipes").setExecutor(commandHandler);
        getCommand("recipes").setTabCompleter(commandHandler);

        getServer().getPluginManager().registerEvents(new GuiListener(this), this);

        fileManager.loadAllConfigs();
        recipeManager.loadAllRecipes();

        getLogger().info("EzRecipes enabled! Simple recipes for your SMP.");
    }

    public void onDisable() {
        recipeManager.unregisterAllRecipes();
    }

    public static EzRecipes getInstance() {
        return instance;
    }

    public FileManager getFileManager() {
        return fileManager;
    }

    public TextUtil getTextUtil() {
        return textUtil;
    }

    public RecipeManager getRecipeManager() {
        return recipeManager;
    }

    public GuiManager getGuiManager() {
        return guiManager;
    }
}