package dev.enderpedia.client;

import dev.enderpedia.Books;
import dev.enderpedia.Config;
import dev.enderpedia.Enderpedia;
import dev.enderpedia.GuideBook;
import dev.enderpedia.Network;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.WrittenBookContent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Lists every guide book plus the player's shelf. Click reads a book, shift-click hands out the
 * item, right-click pins a book to the top.
 */
public final class IndexScreen extends Screen {
    private final Screen parent;
    private List<GuideBook> books = List.of();
    private EditBox search;
    private BookList list;

    public IndexScreen(Screen parent) {
        super(Component.translatable("enderpedia.index.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        books = gather();
        int w = Math.min(260, width - 20);
        String query = search == null ? "" : search.getValue();
        search = new EditBox(font, (width - w) / 2, 22, w, 18, Component.translatable("enderpedia.index.search"));
        search.setHint(Component.translatable("enderpedia.index.search").withStyle(ChatFormatting.DARK_GRAY));
        search.setValue(query);
        search.setResponder(q -> refill());
        addRenderableWidget(search);
        list = new BookList(minecraft, width, height - 88, 44);
        addRenderableWidget(list);
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> onClose())
                .bounds(width / 2 - 75, height - 26, 150, 20).build());
        refill();
        setInitialFocus(search);
    }

    /** Rebuilds the list, e.g. after the shelf changed. */
    public void reload() {
        books = gather();
        refill();
    }

    private static List<GuideBook> gather() {
        List<GuideBook> out = new ArrayList<>();
        try {
            out.addAll(Books.forIndex());
        } catch (RuntimeException e) {
            Enderpedia.LOGGER.warn("Could not build the book index", e);
        }
        if (Config.SHOW_SHELF.get()) {
            List<ItemStack> shelf = EnderpediaClient.shelf();
            for (int i = 0; i < shelf.size(); i++) out.add(shelfEntry(shelf.get(i), i));
        }
        List<? extends String> favs = Config.FAVORITE_BOOKS.get();
        // Stable sort: pinned books first, the rest keep their order (by mod, then title; shelf last).
        out.sort(Comparator.comparing((GuideBook b) -> !favs.contains(b.id().toString())));
        return out;
    }

    private static GuideBook shelfEntry(ItemStack stack, int slot) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Enderpedia.MODID, "shelf/" + slot);
        WrittenBookContent written = stack.get(DataComponents.WRITTEN_BOOK_CONTENT);
        String from = written != null
                ? Component.translatable("enderpedia.index.by", written.author()).getString()
                : ModList.get().getModContainerById(stack.getItem().builtInRegistryHolder().key().location().getNamespace())
                        .map(c -> c.getModInfo().getDisplayName()).orElse("Minecraft");
        BookViewScreen.BookAccess access = BookViewScreen.BookAccess.fromItem(stack);
        Runnable opener = access == null || access.getPageCount() == 0 ? null
                : () -> Minecraft.getInstance().setScreen(new BookViewScreen(access));
        return new GuideBook(id, stack.getHoverName(), from, Component.translatable("enderpedia.index.shelf").getString(),
                stack, opener, slot);
    }

    private void refill() {
        String q = search.getValue().trim().toLowerCase(Locale.ROOT);
        list.clear();
        for (GuideBook b : books) {
            if (q.isEmpty() || b.title().getString().toLowerCase(Locale.ROOT).contains(q)
                    || b.modName().toLowerCase(Locale.ROOT).contains(q)) {
                list.add(new Row(b));
            }
        }
        list.setScrollAmount(0);
    }

    private static void toggleFavorite(GuideBook book) {
        String id = book.id().toString();
        List<String> favs = new ArrayList<>(Config.FAVORITE_BOOKS.get());
        if (!favs.remove(id)) favs.add(id);
        Config.FAVORITE_BOOKS.set(favs);
        Config.FAVORITE_BOOKS.save();
    }

    private static void requestItem(GuideBook book) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() == null || !mc.getConnection().hasChannel(Network.GetBook.TYPE)) return;
        PacketDistributor.sendToServer(new Network.GetBook(book.id(), book.shelfSlot()));
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        g.drawCenteredString(font, title, width / 2, 8, 0xFFFFFF);
        if (books.isEmpty()) {
            g.drawCenteredString(font, Component.translatable("enderpedia.index.empty"), width / 2, height / 2 - 4, 0xAAAAAA);
        } else if (list.children().isEmpty()) {
            g.drawCenteredString(font, Component.translatable("enderpedia.index.nomatch"), width / 2, height / 2 - 4, 0xAAAAAA);
        }
        g.drawCenteredString(font, Component.translatable("enderpedia.index.hint"), width / 2, height - 38, 0x808080);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    private final class BookList extends ObjectSelectionList<Row> {
        BookList(Minecraft mc, int width, int height, int y) {
            super(mc, width, height, y, 24);
        }

        void clear() {
            clearEntries();
        }

        void add(Row row) {
            addEntry(row);
        }

        @Override
        public int getRowWidth() {
            return Math.min(260, IndexScreen.this.width - 20);
        }
    }

    private final class Row extends ObjectSelectionList.Entry<Row> {
        private final GuideBook book;

        Row(GuideBook book) {
            this.book = book;
        }

        @Override
        public void render(GuiGraphics g, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            if (!book.icon().isEmpty()) g.renderItem(book.icon(), left + 2, top + 2);
            g.drawString(font, book.title(), left + 24, top + 2, hovering ? 0xFFFF80 : 0xFFFFFF);
            g.drawString(font, book.modName() + "  ·  " + book.system(), left + 24, top + 12, 0x909090);
            if (Config.FAVORITE_BOOKS.get().contains(book.id().toString())) {
                g.drawString(font, "★", left + width - 12, top + 2, 0xFFD040);
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            try {
                if (button == 1) {
                    if (book.shelfSlot() < 0) {
                        toggleFavorite(book);
                        reload();
                    }
                } else if (button == 0) {
                    if (hasShiftDown() || !book.readable()) requestItem(book);
                    else book.open();
                } else {
                    return false;
                }
            } catch (RuntimeException | LinkageError e) {
                Enderpedia.LOGGER.warn("Could not open {}", book.id(), e);
            }
            return true;
        }

        @Override
        public Component getNarration() {
            return book.title();
        }
    }
}
