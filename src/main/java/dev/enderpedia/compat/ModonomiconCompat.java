package dev.enderpedia.compat;

import com.klikli_dev.modonomicon.book.Book;
import com.klikli_dev.modonomicon.data.BookDataManager;
import com.klikli_dev.modonomicon.registry.DataComponentRegistry;
import dev.enderpedia.GuideBook;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.fml.ModList;

import java.util.ArrayList;
import java.util.List;

/** Only class-loaded when Modonomicon is installed. */
public final class ModonomiconCompat {
    private static final ResourceLocation DEFAULT_ITEM = ResourceLocation.fromNamespaceAndPath("modonomicon", "modonomicon");

    private ModonomiconCompat() {}

    public static List<GuideBook> books() {
        List<GuideBook> out = new ArrayList<>();
        for (Book book : BookDataManager.get().getBooks().values()) {
            ResourceLocation id = book.getId();
            String mod = ModList.get().getModContainerById(id.getNamespace())
                    .map(c -> c.getModInfo().getDisplayName()).orElse(id.getNamespace());
            out.add(new GuideBook(id, Component.translatable(book.getName()), mod, "Modonomicon", icon(book),
                    () -> ModonomiconClient.open(id)));
        }
        return out;
    }

    private static ItemStack icon(Book book) {
        ResourceLocation custom = book.getCustomBookItem();
        Item item = BuiltInRegistries.ITEM.get(custom != null ? custom : DEFAULT_ITEM);
        if (item == Items.AIR) item = Items.BOOK;
        ItemStack stack = new ItemStack(item);
        if (custom == null) stack.set(DataComponentRegistry.BOOK_ID.get(), book.getId());
        return stack;
    }

    /** The Modonomicon book this stack is, or null. */
    public static ResourceLocation bookOf(ItemStack stack) {
        ResourceLocation id = stack.get(DataComponentRegistry.BOOK_ID.get());
        if (id != null) return id;
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        for (Book book : BookDataManager.get().getBooks().values()) {
            if (itemId.equals(book.getCustomBookItem())) return book.getId();
        }
        return null;
    }
}
