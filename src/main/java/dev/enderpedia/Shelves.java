package dev.enderpedia;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Hands books and kept items back to players who ask for them from the index. */
public final class Shelves {
    /** Game time of each player's last request, so a held click can't flood the inventory. */
    private static final Map<UUID, Long> LAST = new ConcurrentHashMap<>();

    private Shelves() {}

    public static void handOut(ServerPlayer player, ResourceLocation book, int shelfSlot) {
        long now = player.level().getGameTime();
        Long last = LAST.get(player.getUUID());
        if (last != null && now - last < 10) return;
        LAST.put(player.getUUID(), now);
        if (shelfSlot >= 0) {
            PlayerBooks data = player.getData(PlayerBooks.TYPE);
            if (shelfSlot >= data.shelf().size()) return;
            ItemStack stack = data.shelf().get(shelfSlot).copy();
            player.setData(PlayerBooks.TYPE, data.withRemoved(shelfSlot));
            give(player, stack);
            Network.sendShelf(player);
            return;
        }
        if (!Config.ALLOW_COPIES.get()) return;
        for (GuideBook b : Books.all()) {
            if (b.id().equals(book)) {
                if (b.hasItem()) give(player, b.icon().copy());
                return;
            }
        }
        // GuideME guides are client resources, so a dedicated server may have no list to check against.
        if (Books.GUIDEME) {
            try {
                ItemStack item = dev.enderpedia.compat.GuideMeCompat.item(book);
                if (!item.isEmpty()) give(player, item);
            } catch (RuntimeException | LinkageError e) {
                Enderpedia.LOGGER.debug("Could not make a GuideME item for {}", book, e);
            }
        }
    }

    /** Puts a kept item on the player's shelf. */
    public static void keep(ServerPlayer player, ItemStack stack) {
        player.setData(PlayerBooks.TYPE, player.getData(PlayerBooks.TYPE).withAdded(stack));
    }

    public static void give(ServerPlayer player, ItemStack stack) {
        if (!player.getInventory().add(stack) && !stack.isEmpty()) player.drop(stack, false);
    }

    public static void forget(UUID player) {
        LAST.remove(player);
    }
}
