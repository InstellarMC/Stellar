package org.bukkit.block;

import org.bukkit.DyeColor;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a captured state of a ShulkerBox.
 */
public interface ShulkerBox extends Container, com.destroystokyo.paper.loottable.LootableBlockInventory, Lidded { // Paper - LootTable API

    /**
     * Get the {@link DyeColor} corresponding to this ShulkerBox
     *
     * @return the {@link DyeColor} of this ShulkerBox, or null if default
     */
    @Nullable
    public DyeColor getColor();
}
