package dev.enderpedia.compat;

import com.klikli_dev.modonomicon.book.Book;
import com.klikli_dev.modonomicon.client.gui.BookGuiManager;
import com.klikli_dev.modonomicon.client.gui.book.BookAddress;
import com.klikli_dev.modonomicon.data.BookDataManager;
import net.minecraft.resources.ResourceLocation;

/** Client-only half of the Modonomicon hook, kept apart so servers never load GUI classes. */
public final class ModonomiconClient {
    private ModonomiconClient() {}

    public static void open(ResourceLocation id) {
        Book book = BookDataManager.get().getBook(id);
        if (book != null) BookGuiManager.get().openBook(BookAddress.defaultFor(book));
    }
}
