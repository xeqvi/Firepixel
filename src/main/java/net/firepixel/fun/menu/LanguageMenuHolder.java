package net.firepixel.fun.menu;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.Map;

public class LanguageMenuHolder implements InventoryHolder {

    private final Map<Integer, String> slots = new HashMap<Integer, String>();
    private Inventory inventory;

    public void setLanguage(int slot, String code) {
        slots.put(slot, code);
    }

    public String getLanguage(int slot) {
        return slots.get(slot);
    }

    public Map<Integer, String> getSlots() {
        return slots;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
}