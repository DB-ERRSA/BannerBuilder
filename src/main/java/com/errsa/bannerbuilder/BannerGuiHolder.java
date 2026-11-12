package com.errsa.bannerbuilder;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import java.util.UUID;

/** Server-created menu identity. Never identify a GUI by its display title. */
public final class BannerGuiHolder implements InventoryHolder {
    private final UUID owner;
    private Inventory inventory;
    public BannerGuiHolder(UUID owner) { this.owner = owner; }
    public UUID owner() { return owner; }
    public void attach(Inventory inventory) { this.inventory = inventory; }
    @Override public Inventory getInventory() { return inventory; }
    public static boolean isEditor(UUID player, Inventory inventory) {
        return inventory != null && inventory.getHolder(false) instanceof BannerGuiHolder holder
            && holder.owner.equals(player) && holder.inventory == inventory;
    }
}
