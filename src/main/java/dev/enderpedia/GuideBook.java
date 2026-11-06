package dev.enderpedia;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * One entry in the index: a guide book from a book system, or an item on the player's shelf
 * (shelfSlot >= 0). The opener is null for shelf items that can't be read in place.
 */
public record GuideBook(ResourceLocation id, Component title, String modName, String system, ItemStack icon, Runnable opener, int shelfSlot) {
    public GuideBook(ResourceLocation id, Component title, String modName, String system, ItemStack icon, Runnable opener) {
        this(id, title, modName, system, icon, opener, -1);
    }

    public boolean readable() {
        return opener != null;
    }

    /** Whether there is a real item to hand out (books without one fall back to a plain book icon). */
    public boolean hasItem() {
        return !icon.isEmpty() && !icon.is(Items.BOOK);
    }

    public void open() {
        if (opener != null) opener.run();
    }
}
