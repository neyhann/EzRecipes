# EzRecipes - Advanced Minecraft Recipe Management Plugin

EzRecipes is a comprehensive Minecraft plugin for Paper/Spigot servers that provides fully customizable recipe management with zero hardcoded strings. Everything is configurable through YAML files.

## Features

- **Zero Hardcoded Strings**: All text, materials, slot numbers, and GUI titles come from configuration files
- **Centralized Text Formatting**: Supports legacy color codes, hex colors, MiniMessage syntax, gradients, rainbows, and PlaceholderAPI
- **Complete Item Serialization**: Captures all item metadata including enchantments, custom model data, skull textures, potion effects, etc.
- **Configurable GUIs**: Three separate GUI layouts fully defined in YAML files
- **Real Bukkit Recipes**: Registered recipes work at actual crafting tables
- **Search Functionality**: Search recipes by name or material
- **Pagination**: Browse recipes with previous/next page buttons
- **Admin Commands**: Add, remove, list, and reload recipes
- **Player Commands**: Browse and view recipes through intuitive GUIs
- **Sound & Particle Effects**: Configurable effects for various actions
- **Plugin Integrations**: Support for PlaceholderAPI, Vault, ItemsAdder, Oraxen, and LuckPerms

## Installation

1. Download the plugin JAR file
2. Place it in your server's `plugins` folder
3. Restart your server
4. Configure the plugin using the generated configuration files

## Configuration Files

### config.yml (Main Configuration)
Located in `plugins/EzRecipes/config.yml`

**General Settings:**
- `language`: Language file to use (default: "en.yml")
- `broadcast-add`: Broadcast when a recipe is added (default: true)
- `allow-overwrite`: Allow overwriting existing recipes (default: false)
- `debug`: Enable debug mode (default: false)
- `console-logging`: Enable console logging (default: true)

**Formatting Settings:**
- `formatting.legacy.enabled`: Enable legacy Bukkit color codes (&a, &b, etc.)
- `formatting.hex.enabled`: Enable hex color codes (&#RRGGBB and <#RRGGBB>)
- `formatting.minimessage.enabled`: Enable MiniMessage formatting

**Integration Settings:**
- `integrations.placeholderapi.enabled`: Enable PlaceholderAPI integration
- `integrations.vault.enabled`: Enable Vault economy integration
- `integrations.itemsadder.enabled`: Enable ItemsAdder integration
- `integrations.oraxen.enabled`: Enable Oraxen integration
- `integrations.luckperms.enabled`: Enable LuckPerms integration

**Permission Settings:**
- `permissions.admin`: Admin permission node (default: "ezrecipes.admin")
- `permissions.use`: Use permission node (default: "ezrecipes.use")
- `permissions.add`: Add permission node (default: "ezrecipes.add")
- `permissions.remove`: Remove permission node (default: "ezrecipes.remove")
- `permissions.reload`: Reload permission node (default: "ezrecipes.reload")

**Command Settings:**
- `commands.ezrecipes`: Aliases for admin command (default: ["er", "recipeadmin"])
- `commands.recipes`: Aliases for player command (default: ["recipelist", "recipemenu"])

**Search Settings:**
- `search.timeout`: Search prompt timeout in milliseconds (default: 30000)
- `search.cancel-keyword`: Keyword to cancel search (default: "cancel")
- `search.action-bar-prompt`: Show search prompt in action bar (default: true)

**Pagination Settings:**
- `pagination.recipes-per-page`: Recipes per page in GUI (default: 28)
- `pagination.chat-per-page`: Recipes per page in chat list (default: 10)

**Sound Settings:**
- `sounds.recipe_added`: Sound when recipe is added
- `sounds.gui_open`: Sound when GUI opens
- `sounds.gui_close`: Sound when GUI closes
- `sounds.button_click`: Sound when button is clicked

**Particle Settings:**
- `particles.recipe_added`: Particles when recipe is added
- `particles.gui_open`: Particles when GUI opens
- `particles.recipe_view`: Particles when viewing recipe

### lang/en.yml (Language File)
Located in `plugins/EzRecipes/lang/en.yml`

Contains all text messages shown to players, including:
- Permission messages
- Command responses
- GUI titles and messages
- Search prompts and results
- Broadcast messages

All messages support the full formatting engine including gradients and MiniMessage syntax.

### GUI Configuration Files
Located in `plugins/EzRecipes/gui/`

**add-recipe-gui.yml**: Defines the layout for adding new recipes
**recipe-browser-gui.yml**: Defines the layout for browsing recipes
**recipe-view-gui.yml**: Defines the layout for viewing individual recipes

Each GUI file defines:
- Title and number of rows
- Filler item material, name, lore, and glow effect
- Slot mappings for crafting grid
- Button definitions with materials, names, lore, and glow effects
- Item definitions for special slots

### recipes.yml (Recipe Storage)
Located in `plugins/EzRecipes/recipes.yml`

Automatically generated and managed by the plugin. Stores all saved recipes with complete item serialization.

## Placeholder Variables

The plugin supports the following placeholder variables in text:

### General Variables
- `{prefix}`: Plugin prefix from config
- `{player}`: Player name
- `{count}`: Number of items (e.g., ingredients in grid)

### Recipe Variables
- `{recipe_id}`: Unique recipe identifier
- `{item_name}`: Display name of result item
- `{recipe_type}`: "Shaped" or "Shapeless"
- `{created_by}`: Player who created the recipe
- `{created_date}`: Creation date of recipe
- `{ingredient_count}`: Number of ingredients in recipe

### Pagination Variables
- `{current}`: Current page number
- `{total}`: Total number of pages
- `{prev}`: Previous page number
- `{next}`: Next page number

### Search Variables
- `{query}`: Search query text
- `{timeout}`: Search timeout in seconds
- `{cancel_keyword}`: Keyword to cancel search

## Commands

### Admin Commands (`/ezrecipes`)
- `/ezrecipes add` - Add a new recipe from held item (opens GUI)
- `/ezrecipes remove <recipe_id>` - Remove a recipe by ID
- `/ezrecipes list [page]` - List all recipes in chat (paginated)
- `/ezrecipes reload` - Reload all configuration files
- `/ezrecipes help` - Show command help

**Aliases**: `er`, `recipeadmin`

**Permission**: `ezrecipes.admin` (default: op)

### Player Commands (`/recipes`)
- `/recipes` - Open recipe browser GUI (page 1)
- `/recipes <page>` - Open recipe browser GUI at specific page
- `/recipes search` - Start search session (prompt in chat)

**Aliases**: `recipelist`, `recipemenu`

**Permission**: `ezrecipes.use` (default: true)

## Permissions

- `ezrecipes.admin` - Access to all admin commands (default: op)
- `ezrecipes.use` - Use player commands (default: true)
- `ezrecipes.add` - Add new recipes (default: op)
- `ezrecipes.remove` - Remove recipes (default: op)
- `ezrecipes.reload` - Reload configuration (default: op)

## Usage Guide

### Adding a Recipe
1. Hold the item you want to be the result
2. Run `/ezrecipes add`
3. Fill the crafting grid with ingredients in the GUI
4. Toggle between shaped/shapeless mode if needed
5. Click the green confirm button
6. Recipe is saved and registered at crafting tables

### Browsing Recipes
1. Run `/recipes`
2. Browse recipes using the GUI
3. Use arrow buttons to navigate pages
4. Click any recipe to view details
5. Use the compass to search for specific recipes

### Searching Recipes
1. Click the compass in the browser GUI
2. Type your search term in chat
3. Results will be shown in a filtered GUI
4. Type "cancel" to cancel search

### Removing a Recipe
1. Run `/ezrecipes remove <recipe_id>`
2. Recipe ID can be found in the recipe list or view GUI

## Developer API

The plugin provides a clean API for developers to interact with:

```java
// Get plugin instance
EzRecipes plugin = EzRecipes.getInstance();

// Access managers
RecipeManager recipeManager = plugin.getRecipeManager();
GuiManager guiManager = plugin.getGuiManager();
FileManager fileManager = plugin.getFileManager();
TextUtil textUtil = plugin.getTextUtil();

// Example: Get all recipes
List<RecipeData> allRecipes = recipeManager.getAllRecipes();

// Example: Add a recipe programmatically
RecipeData recipe = new RecipeData("custom_sword", itemData, ingredients, false, 
    System.currentTimeMillis(), "Console");
recipeManager.addRecipe(recipe);

// Example: Format text with placeholders
String formatted = textUtil.formatForPlayer("Hello {player}!", player);
```

## Building from Source

1. Clone the repository
2. Ensure you have Maven installed
3. Run `mvn clean package`
4. Find the JAR in `target/` directory

## Dependencies

- **Required**: Paper/Spigot 1.17+
- **Optional**: 
  - PlaceholderAPI (for placeholder support)
  - Vault (for economy integration)
  - ItemsAdder (for custom items)
  - Oraxen (for custom items)
  - LuckPerms (for advanced permissions)

## Support

For issues, feature requests, or questions:
1. Check the [GitHub Issues](https://github.com/your-repo/issues)
2. Join our [Discord Server](https://discord.gg/your-invite)
3. Email: support@example.com

## License

This plugin is licensed under the MIT License. See LICENSE file for details.

## Credits

- Developed by EzRecipes Team
- Special thanks to the PaperMC community
- Inspired by various recipe management plugins

## Version History

- **1.0.0** (2026-05-14): Initial release with all core features