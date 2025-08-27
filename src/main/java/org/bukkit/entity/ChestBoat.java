package org.bukkit.entity;

import com.destroystokyo.paper.lootable.LootableEntityInventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * A {@link Boat} with a chest.
 */
public interface ChestBoat extends Boat, InventoryHolder, LootableEntityInventory {
}
