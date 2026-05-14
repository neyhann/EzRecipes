package com.ezrecipes.data.recipe;

import java.util.HashMap;
import java.util.Map;

public class RecipeData {
    
    private String id;
    private String resultItem;
    private Map<String, String> ingredients;
    private boolean shapeless;
    private long createdAt;
    private String createdBy;
    
    public RecipeData() {
        this.ingredients = new HashMap<>();
    }
    
    public RecipeData(String id, String resultItem, Map<String, String> ingredients, 
                     boolean shapeless, long createdAt, String createdBy) {
        this.id = id;
        this.resultItem = resultItem;
        this.ingredients = ingredients != null ? ingredients : new HashMap<>();
        this.shapeless = shapeless;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
    }
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getResultItem() { return resultItem; }
    public void setResultItem(String resultItem) { this.resultItem = resultItem; }
    
    public Map<String, String> getIngredients() { return ingredients; }
    public void setIngredients(Map<String, String> ingredients) { 
        this.ingredients = ingredients; 
    }
    
    public void addIngredient(String position, String itemData) {
        this.ingredients.put(position, itemData);
    }
    
    public boolean isShapeless() { return shapeless; }
    public void setShapeless(boolean shapeless) { this.shapeless = shapeless; }
    
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    
    public Map<String, Object> toMap() {
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("result", resultItem);
        data.put("ingredients", ingredients);
        data.put("shapeless", shapeless);
        data.put("created_at", createdAt);
        data.put("created_by", createdBy);
        return data;
    }
    
    public static RecipeData fromMap(Map<String, Object> data) {
        RecipeData recipe = new RecipeData();
        recipe.setId((String) data.get("id"));
        recipe.setResultItem((String) data.get("result"));
        
        if (data.get("ingredients") instanceof Map) {
            Map<?, ?> ingredientsMap = (Map<?, ?>) data.get("ingredients");
            for (Map.Entry<?, ?> entry : ingredientsMap.entrySet()) {
                recipe.addIngredient(entry.getKey().toString(), entry.getValue().toString());
            }
        }
        
        recipe.setShapeless((Boolean) data.get("shapeless"));
        recipe.setCreatedAt(((Number) data.get("created_at")).longValue());
        recipe.setCreatedBy((String) data.get("created_by"));
        
        return recipe;
    }
}