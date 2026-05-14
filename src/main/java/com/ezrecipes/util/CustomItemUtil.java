package com.ezrecipes.util;

import com.ezrecipes.EzRecipes;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class CustomItemUtil {
    
    private final EzRecipes plugin;
    
    public CustomItemUtil(EzRecipes plugin) {
        this.plugin = plugin;
    }
    
    public String saveItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return "AIR";
        }
        
        YamlConfiguration config = new YamlConfiguration();
        config.set("type", item.getType().name());
        config.set("amount", item.getAmount());
        
        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            
            if (meta.hasDisplayName()) {
                config.set("meta.name", meta.getDisplayName());
            }
            
            if (meta.hasLore()) {
                config.set("meta.lore", meta.getLore());
            }
            
            if (meta.hasEnchants()) {
                Map<String, Integer> enchants = new HashMap<>();
                for (Map.Entry<Enchantment, Integer> entry : meta.getEnchants().entrySet()) {
                    enchants.put(entry.getKey().getKey().getKey(), entry.getValue());
                }
                config.set("meta.enchants", enchants);
            }
            
            if (!meta.getItemFlags().isEmpty()) {
                List<String> flags = new ArrayList<>();
                for (ItemFlag flag : meta.getItemFlags()) {
                    flags.add(flag.name());
                }
                config.set("meta.flags", flags);
            }
            
            if (meta.hasCustomModelData()) {
                config.set("meta.custom_model_data", meta.getCustomModelData());
            }
            
            if (meta.isUnbreakable()) {
                config.set("meta.unbreakable", true);
            }
            
            if (meta instanceof SkullMeta skullMeta) {
                if (skullMeta.hasOwner()) {
                    config.set("meta.skull.owner", skullMeta.getOwner());
                }
                
                PlayerProfile profile = skullMeta.getOwnerProfile();
                if (profile != null) {
                    PlayerTextures textures = profile.getTextures();
                    URL skinUrl = textures.getSkin();
                    if (skinUrl != null) {
                        config.set("meta.skull.texture", skinUrl.toString());
                    }
                }
            }
            
            if (meta instanceof LeatherArmorMeta leatherMeta) {
                config.set("meta.color", leatherMeta.getColor().asRGB());
            }
            
            if (meta instanceof PotionMeta potionMeta) {
                if (potionMeta.hasCustomEffects()) {
                    List<Map<String, Object>> effects = new ArrayList<>();
                    for (PotionEffect effect : potionMeta.getCustomEffects()) {
                        Map<String, Object> effectData = new HashMap<>();
                        effectData.put("type", effect.getType().getName());
                        effectData.put("duration", effect.getDuration());
                        effectData.put("amplifier", effect.getAmplifier());
                        effectData.put("ambient", effect.isAmbient());
                        effectData.put("particles", effect.hasParticles());
                        effectData.put("icon", effect.hasIcon());
                        effects.add(effectData);
                    }
                    config.set("meta.potion_effects", effects);
                }
            }
        }
        
        return config.saveToString();
    }
    
    public ItemStack loadItem(String data) {
        if (data == null || data.isEmpty() || data.equals("AIR")) {
            return new ItemStack(Material.AIR);
        }
        
        try {
            YamlConfiguration config = new YamlConfiguration();
            config.loadFromString(data);
            
            Material material = Material.getMaterial(config.getString("type", "STONE"));
            if (material == null) {
                material = Material.STONE;
            }
            
            int amount = config.getInt("amount", 1);
            ItemStack item = new ItemStack(material, amount);
            
            if (config.contains("meta")) {
                ItemMeta meta = item.getItemMeta();
                if (meta == null) {
                    return item;
                }
                
                if (config.contains("meta.name")) {
                    String name = config.getString("meta.name");
                    meta.setDisplayName(name);
                }
                
                if (config.contains("meta.lore")) {
                    List<String> lore = config.getStringList("meta.lore");
                    meta.setLore(lore);
                }
                
                if (config.contains("meta.enchants")) {
                    ConfigurationSection enchantsSection = config.getConfigurationSection("meta.enchants");
                    if (enchantsSection != null) {
                        for (String enchantKey : enchantsSection.getKeys(false)) {
                            Enchantment enchantment = Enchantment.getByKey(org.bukkit.NamespacedKey.minecraft(enchantKey));
                            if (enchantment != null) {
                                int level = enchantsSection.getInt(enchantKey);
                                meta.addEnchant(enchantment, level, true);
                            }
                        }
                    }
                }
                
                if (config.contains("meta.flags")) {
                    List<String> flags = config.getStringList("meta.flags");
                    for (String flagName : flags) {
                        try {
                            ItemFlag flag = ItemFlag.valueOf(flagName);
                            meta.addItemFlags(flag);
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().warning("Invalid item flag: " + flagName);
                        }
                    }
                }
                
                if (config.contains("meta.custom_model_data")) {
                    meta.setCustomModelData(config.getInt("meta.custom_model_data"));
                }
                
                if (config.contains("meta.unbreakable")) {
                    meta.setUnbreakable(config.getBoolean("meta.unbreakable"));
                }
                
                if (config.contains("meta.skull") && meta instanceof SkullMeta skullMeta) {
                    if (config.contains("meta.skull.owner")) {
                        String owner = config.getString("meta.skull.owner");
                        skullMeta.setOwner(owner);
                    }
                    
                    if (config.contains("meta.skull.texture")) {
                        String textureUrl = config.getString("meta.skull.texture");
                        try {
                            PlayerProfile profile = plugin.getServer().createPlayerProfile(UUID.randomUUID());
                            PlayerTextures textures = profile.getTextures();
                            textures.setSkin(new URL(textureUrl));
                            profile.setTextures(textures);
                            skullMeta.setOwnerProfile(profile);
                        } catch (MalformedURLException e) {
                            plugin.getLogger().warning("Invalid skull texture URL: " + textureUrl);
                        }
                    }
                }
                
                if (config.contains("meta.color") && meta instanceof LeatherArmorMeta leatherMeta) {
                    int color = config.getInt("meta.color");
                    leatherMeta.setColor(org.bukkit.Color.fromRGB(color));
                }
                
                if (config.contains("meta.potion_effects") && meta instanceof PotionMeta potionMeta) {
                    List<Map<?, ?>> effects = config.getMapList("meta.potion_effects");
                    for (Map<?, ?> effectData : effects) {
                        String typeName = (String) effectData.get("type");
                        PotionEffectType type = PotionEffectType.getByName(typeName);
                        if (type != null) {
                            int duration = ((Number) effectData.get("duration")).intValue();
                            int amplifier = ((Number) effectData.get("amplifier")).intValue();
                            boolean ambient = (Boolean) effectData.get("ambient");
                            boolean particles = (Boolean) effectData.get("particles");
                            boolean icon = (Boolean) effectData.get("icon");
                            
                            PotionEffect effect = new PotionEffect(type, duration, amplifier, ambient, particles, icon);
                            potionMeta.addCustomEffect(effect, true);
                        }
                    }
                }
                
                item.setItemMeta(meta);
            }
            
            return item;
            
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load item: " + e.getMessage());
            return new ItemStack(Material.BARRIER);
        }
    }
    
    public Map<String, String> getIngredients(ItemStack[] contents, Map<Integer, String> slotMapping) {
        Map<String, String> ingredients = new HashMap<>();
        
        for (Map.Entry<Integer, String> entry : slotMapping.entrySet()) {
            int slot = entry.getKey();
            String position = entry.getValue();
            
            if (slot >= 0 && slot < contents.length) {
                ItemStack item = contents[slot];
                if (item != null && item.getType() != Material.AIR) {
                    ingredients.put(position, saveItem(item));
                } else {
                    ingredients.put(position, "AIR");
                }
            }
        }
        
        return ingredients;
    }
    
    public boolean hasIngredients(Map<String, String> ingredients) {
        int emptyCount = 0;
        
        for (String itemData : ingredients.values()) {
            if (itemData.equals("AIR")) {
                emptyCount++;
            }
        }
        
        return emptyCount < ingredients.size();
    }
}