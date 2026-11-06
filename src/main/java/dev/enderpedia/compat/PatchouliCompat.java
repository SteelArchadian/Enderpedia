package dev.enderpedia.compat;

import dev.enderpedia.GuideBook;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import vazkii.patchouli.api.PatchouliAPI;
import vazkii.patchouli.common.book.Book;
import vazkii.patchouli.common.book.BookRegistry;
import vazkii.patchouli.common.item.ItemModBook;

import java.util.ArrayList;
import java.util.List;

/** Only class-loaded when Patchouli is installed. */
public final class PatchouliCompat {
    private PatchouliCompat() {}

    public static List<GuideBook> books() {
        List<GuideBook> out = new ArrayList<>();
        for (Book book : BookRegistry.INSTANCE.books.values()) {
            ResourceLocation id = book.id;
            ItemStack icon;
            try {
                icon = book.getBookItem();
            } catch (RuntimeException e) {
                icon = ItemStack.EMPTY;
            }
            out.add(new GuideBook(id, Component.translatable(book.name), book.getOwnerName(), "Patchouli", icon,
                    () -> PatchouliAPI.get().openBookGUI(id)));
        }
        return out;
    }

    /** The Patchouli book this stack is, or null. Covers the shared guide book item and books with their own item. */
    public static ResourceLocation bookOf(ItemStack stack) {
        if (stack.getItem() instanceof ItemModBook) {
            Book book = ItemModBook.getBook(stack);
            return book == null ? null : book.id;
        }
        for (Book book : BookRegistry.INSTANCE.books.values()) {
            if (book.noBook) continue;
            ItemStack item;
            try {
                item = book.getBookItem();
            } catch (RuntimeException e) {
                continue;
            }
            if (!item.isEmpty() && !(item.getItem() instanceof ItemModBook) && item.is(stack.getItem())) return book.id;
        }
        return null;
    }
}
