package com.ezrecipes.data.gui;

import java.util.*;

public class GuiConfig {
    
    private String title;
    private int rows;
    
    private FillerItem filler;
    private Map<Integer, String> craftingSlots;
    private Map<String, Button> buttons;
    private Map<String, DisplayItem> items;
    
    public GuiConfig() {
        this.filler = new FillerItem();
        this.craftingSlots = new HashMap<>();
        this.buttons = new HashMap<>();
        this.items = new HashMap<>();
    }
    
    public static class FillerItem {
        private String material = "GRAY_STAINED_GLASS_PANE";
        private String name = " ";
        private List<String> lore = new ArrayList<>();
        private boolean glow = false;
        
        public String getMaterial() { return material; }
        public void setMaterial(String material) { this.material = material; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public List<String> getLore() { return lore; }
        public void setLore(List<String> lore) { this.lore = lore; }
        
        public boolean isGlow() { return glow; }
        public void setGlow(boolean glow) { this.glow = glow; }
    }
    
    public static class Button {
        private int slot;
        private String material;
        private String name;
        private List<String> lore;
        private boolean glow;
        
        public Button() {
            this.lore = new ArrayList<>();
        }
        
        public int getSlot() { return slot; }
        public void setSlot(int slot) { this.slot = slot; }
        
        public String getMaterial() { return material; }
        public void setMaterial(String material) { this.material = material; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public List<String> getLore() { return lore; }
        public void setLore(List<String> lore) { this.lore = lore; }
        
        public boolean isGlow() { return glow; }
        public void setGlow(boolean glow) { this.glow = glow; }
    }
    
    public static class DisplayItem {
        private int slot;
        private String material;
        private String name;
        private List<String> lore;
        private boolean glow;
        
        public DisplayItem() {
            this.lore = new ArrayList<>();
        }
        
        public int getSlot() { return slot; }
        public void setSlot(int slot) { this.slot = slot; }
        
        public String getMaterial() { return material; }
        public void setMaterial(String material) { this.material = material; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public List<String> getLore() { return lore; }
        public void setLore(List<String> lore) { this.lore = lore; }
        
        public boolean isGlow() { return glow; }
        public void setGlow(boolean glow) { this.glow = glow; }
    }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public int getRows() { return rows; }
    public void setRows(int rows) { this.rows = rows; }
    
    public FillerItem getFiller() { return filler; }
    public void setFiller(FillerItem filler) { this.filler = filler; }
    
    public Map<Integer, String> getCraftingSlots() { return craftingSlots; }
    public void setCraftingSlots(Map<Integer, String> craftingSlots) { 
        this.craftingSlots = craftingSlots; 
    }
    
    public Map<String, Button> getButtons() { return buttons; }
    public void addButton(String key, Button button) { 
        this.buttons.put(key, button); 
    }
    
    public Button getButton(String key) { 
        return buttons.get(key); 
    }
    
    public Map<String, DisplayItem> getItems() { return items; }
    public void addItem(String key, DisplayItem item) { 
        this.items.put(key, item); 
    }
    
    public DisplayItem getItem(String key) { 
        return items.get(key); 
    }
}