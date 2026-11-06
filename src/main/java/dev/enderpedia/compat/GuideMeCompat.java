package dev.enderpedia.compat;

import dev.enderpedia.GuideBook;
import guideme.Guide;
import guideme.Guides;
import guideme.GuidesCommon;
import guideme.internal.item.GuideItem;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

import java.util.ArrayList;
import java.util.List;

/** Only class-loaded when GuideME is installed. Guides are client resources, so the list is client-side. */
public final class GuideMeCompat {
    private GuideMeCompat() {}

    public static List<GuideBook> books() {
        List<GuideBook> out = new ArrayList<>();
        for (Guide guide : Guides.getAll()) {
            ResourceLocation id = guide.getId();
            ItemStack icon = Guides.createGuideItem(id);
            String mod = ModList.get().getModContainerById(id.getNamespace())
                    .map(c -> c.getModInfo().getDisplayName()).orElse(id.getNamespace());
            out.add(new GuideBook(id, icon.getHoverName(), mod, "GuideME", icon, () -> GuideMeClient.open(id)));
        }
        return out;
    }

    /** The guide this stack is, or null. */
    public static ResourceLocation bookOf(ItemStack stack) {
        return stack.getItem() instanceof GuideItem ? GuideItem.getGuideId(stack) : null;
    }

    /** A guide item for any id; used when the server has no guide list of its own. */
    public static ItemStack item(ResourceLocation id) {
        return Guides.createGuideItem(id);
    }

    static final class GuideMeClient {
        static void open(ResourceLocation id) {
            GuidesCommon.openGuide(Minecraft.getInstance().player, id);
        }
    }
}
