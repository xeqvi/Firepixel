package net.firepixel.fun.report;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.Map;

public class ReportMenuHolder implements InventoryHolder {

    public enum Type {
        REASON, PLAYER, CONFIRM
    }

    private final Type type;
    private final Map<Integer, String> slots = new HashMap<Integer, String>();
    private String target;
    private String reason;
    private Inventory inventory;

    public ReportMenuHolder(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public void setSlot(int slot, String value) {
        slots.put(slot, value);
    }

    public String getSlot(int slot) {
        return slots.get(slot);
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
}