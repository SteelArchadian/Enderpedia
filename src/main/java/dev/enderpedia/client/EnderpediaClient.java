package dev.enderpedia.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.enderpedia.Config;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class EnderpediaClient {
    /** Unbound by default so it never clashes; players can bind it in Controls. */
    public static final KeyMapping OPEN_KEY = new KeyMapping("key.enderpedia.open", InputConstants.UNKNOWN.getValue(), "key.categories.enderpedia");

    /** The player's shelf, as last sent by the server. */
    private static List<ItemStack> shelf = List.of();

    private EnderpediaClient() {}

    public static List<ItemStack> shelf() {
        return shelf;
    }

    public static void setShelf(List<ItemStack> items) {
        shelf = List.copyOf(items);
        if (Minecraft.getInstance().screen instanceof IndexScreen index) index.reload();
    }

    public static void init(IEventBus modBus, ModContainer container) {
        if (ModList.get().isLoaded("configpanel")) dev.configpanel.ConfigPanel.register(container);
        else container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        modBus.addListener((RegisterKeyMappingsEvent e) -> e.register(OPEN_KEY));
        NeoForge.EVENT_BUS.addListener(EnderpediaClient::onScreenInit);
        NeoForge.EVENT_BUS.addListener(EnderpediaClient::onClientTick);
        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut e) -> shelf = List.of());
    }

    public static void openIndex() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        dev.enderpedia.Enderpedia.LOGGER.info("Opening the book index (over {})", mc.screen == null ? "nothing" : mc.screen.getClass().getSimpleName());
        mc.setScreen(new IndexScreen(mc.screen));
    }

    private static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!Config.SHOW_BUTTON.get()) return;
        if (event.getScreen() instanceof CreativeModeInventoryScreen creative) {
            if (Config.SHOW_IN_CREATIVE.get()) event.addListener(new BookButton(creative, true));
        } else if (event.getScreen() instanceof InventoryScreen inv) {
            event.addListener(new BookButton(inv, false));
        }
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        while (OPEN_KEY.consumeClick()) {
            if (mc.player != null && (mc.screen == null || mc.screen instanceof AbstractContainerScreen<?>)) {
                mc.setScreen(new IndexScreen(mc.screen));
            }
        }
    }
}
