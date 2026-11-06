package dev.enderpedia;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Takes away guide books handed out on a player's first join. Mods do this from their own join
 * handlers, advancement rewards or a few ticks later, so instead of hooking each one we watch the
 * inventory for a short window after the first join.
 *
 * Books from a book system (Patchouli, Modonomicon) are simply removed, since the index opens them
 * without the item. Anything else (written books, the config list, the #enderpedia:join_items tag)
 * goes on the player's shelf, so nothing is lost.
 */
public final class JoinBooks {
    /** Where 0.1.0 kept the first-join flag; read once to carry it over. */
    private static final String OLD_TAG = "enderpedia";
    public static final TagKey<Item> JOIN_ITEMS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(Enderpedia.MODID, "join_items"));
    /** Ticks left in each player's window. */
    private static final Map<UUID, Integer> WINDOWS = new ConcurrentHashMap<>();
    /** Books removed during the current window, for the chat line at the end. */
    private static final Map<UUID, Integer> REMOVED = new ConcurrentHashMap<>();

    private JoinBooks() {}

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Network.sendShelf(player);
        CompoundTag old = player.getPersistentData().getCompound(OLD_TAG);
        if (old.getBoolean("seen")) {
            player.getPersistentData().remove(OLD_TAG);
            player.setData(PlayerBooks.TYPE, player.getData(PlayerBooks.TYPE).withSeen(true));
        }
        if (hasJoined(player)) return;
        player.setData(PlayerBooks.TYPE, player.getData(PlayerBooks.TYPE).withSeen(true));
        if (Config.BLOCK_JOIN_BOOKS.get()) startWindow(player);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        WINDOWS.remove(event.getEntity().getUUID());
        REMOVED.remove(event.getEntity().getUUID());
        Shelves.forget(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onTick(PlayerTickEvent.Post event) {
        if (WINDOWS.isEmpty() || !(event.getEntity() instanceof ServerPlayer player)) return;
        Integer left = WINDOWS.get(player.getUUID());
        if (left == null) return;
        int removed = sweep(player);
        if (removed > 0) REMOVED.merge(player.getUUID(), removed, Integer::sum);
        if (left <= 1) {
            WINDOWS.remove(player.getUUID());
            int total = REMOVED.getOrDefault(player.getUUID(), 0);
            REMOVED.remove(player.getUUID());
            if (total > 0 && Config.TELL_PLAYER.get()) {
                player.sendSystemMessage(Component.translatable("enderpedia.message.moved", total));
            }
        } else {
            WINDOWS.put(player.getUUID(), left - 1);
        }
    }

    public static void startWindow(ServerPlayer player) {
        startWindow(player, Config.JOIN_WINDOW_SECONDS.get());
    }

    public static void startWindow(ServerPlayer player, int seconds) {
        WINDOWS.put(player.getUUID(), seconds * 20);
    }

    public static int ticksLeft(ServerPlayer player) {
        return WINDOWS.getOrDefault(player.getUUID(), 0);
    }

    public static void clearWindow(ServerPlayer player) {
        WINDOWS.remove(player.getUUID());
        REMOVED.remove(player.getUUID());
    }

    public static boolean hasJoined(ServerPlayer player) {
        return player.getData(PlayerBooks.TYPE).seen();
    }

    /** Forgets the first join, so the next login counts as a first join again. */
    public static void resetJoined(ServerPlayer player) {
        player.setData(PlayerBooks.TYPE, player.getData(PlayerBooks.TYPE).withSeen(false));
    }

    /** Removes guide books from the inventory and the cursor. Returns how many stacks were taken. */
    public static int sweep(ServerPlayer player) {
        int removed = 0;
        boolean shelved = false;
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            Taken t = take(player, inv.getItem(i));
            if (t != Taken.NO) {
                inv.setItem(i, ItemStack.EMPTY);
                removed++;
                shelved |= t == Taken.SHELVED;
            }
        }
        Taken t = take(player, player.containerMenu.getCarried());
        if (t != Taken.NO) {
            player.containerMenu.setCarried(ItemStack.EMPTY);
            removed++;
            shelved |= t == Taken.SHELVED;
        }
        if (shelved) Network.sendShelf(player);
        return removed;
    }

    private enum Taken { NO, REMOVED, SHELVED }

    private static Taken take(ServerPlayer player, ItemStack stack) {
        if (stack.isEmpty()) return Taken.NO;
        List<? extends String> keep = Config.KEEP_BOOKS.get();
        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        if (keep.contains(itemId)) return Taken.NO;
        ResourceLocation id = Books.bookOf(stack);
        if (id != null) {
            if (keep.contains(id.toString())) return Taken.NO;
            Enderpedia.LOGGER.info("Took join book {} ({}) from {}", id, itemId, player.getGameProfile().getName());
            return Taken.REMOVED;
        }
        if (Config.TAKE_WRITTEN_BOOKS.get() && stack.is(Items.WRITTEN_BOOK) || isExtra(stack, itemId)) {
            Shelves.keep(player, stack);
            Enderpedia.LOGGER.info("Shelved join item {} x{} for {}", itemId, stack.getCount(), player.getGameProfile().getName());
            return Taken.SHELVED;
        }
        return Taken.NO;
    }

    private static boolean isExtra(ItemStack stack, String itemId) {
        if (stack.is(JOIN_ITEMS)) return true;
        for (String entry : Config.EXTRA_JOIN_ITEMS.get()) {
            if (entry.startsWith("#")) {
                ResourceLocation tag = ResourceLocation.tryParse(entry.substring(1));
                if (tag != null && stack.is(TagKey.create(Registries.ITEM, tag))) return true;
            } else if (entry.equals(itemId)) {
                return true;
            }
        }
        return false;
    }
}
