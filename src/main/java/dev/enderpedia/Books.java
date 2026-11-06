package dev.enderpedia;

import dev.enderpedia.compat.GuideMeCompat;
import dev.enderpedia.compat.ModonomiconCompat;
import dev.enderpedia.compat.PatchouliCompat;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Gathers guide books from every installed book system. */
public final class Books {
    public static final boolean PATCHOULI = ModList.get().isLoaded("patchouli");
    public static final boolean MODONOMICON = ModList.get().isLoaded("modonomicon");
    public static final boolean GUIDEME = ModList.get().isLoaded("guideme");

    private Books() {}

    /** Books for the index, filtered by the client settings. */
    public static List<GuideBook> forIndex() {
        List<GuideBook> out = new ArrayList<>(all());
        out.removeIf(b -> b.system().equals("Patchouli") && !Config.INCLUDE_PATCHOULI.get()
                || b.system().equals("Modonomicon") && !Config.INCLUDE_MODONOMICON.get()
                || b.system().equals("GuideME") && !Config.INCLUDE_GUIDEME.get()
                || Config.HIDDEN_BOOKS.get().contains(b.id().toString()));
        return out;
    }

    /** Every installed guide book, unfiltered. Safe on servers (openers are only run on clients). */
    public static List<GuideBook> all() {
        List<GuideBook> out = new ArrayList<>();
        if (PATCHOULI) safeAdd(out, "Patchouli", () -> PatchouliCompat.books());
        if (MODONOMICON) safeAdd(out, "Modonomicon", () -> ModonomiconCompat.books());
        if (GUIDEME) safeAdd(out, "GuideME", () -> GuideMeCompat.books());
        out.sort(Comparator.comparing((GuideBook b) -> b.modName().toLowerCase())
                .thenComparing(b -> b.title().getString().toLowerCase()));
        return out;
    }

    /** The guide book a stack is, from any system, or null for ordinary items. */
    public static ResourceLocation bookOf(ItemStack stack) {
        if (stack.isEmpty()) return null;
        try {
            if (PATCHOULI) {
                ResourceLocation id = PatchouliCompat.bookOf(stack);
                if (id != null) return id;
            }
            if (MODONOMICON) {
                ResourceLocation id = ModonomiconCompat.bookOf(stack);
                if (id != null) return id;
            }
            if (GUIDEME) return GuideMeCompat.bookOf(stack);
        } catch (RuntimeException | LinkageError e) {
            Enderpedia.LOGGER.debug("Could not check {} for a guide book", stack, e);
        }
        return null;
    }

    private static void safeAdd(List<GuideBook> out, String system, java.util.function.Supplier<List<GuideBook>> source) {
        try {
            out.addAll(source.get());
        } catch (RuntimeException | LinkageError e) {
            Enderpedia.LOGGER.warn("Could not list {} books", system, e);
        }
    }
}
