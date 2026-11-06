package dev.enderpedia;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.ArrayList;
import java.util.List;

/**
 * What Enderpedia remembers per player: whether they have joined before, and the shelf of items
 * taken from them on join that are not guide books from a book system (written books, manuals from
 * any mod). Kept through death.
 */
public record PlayerBooks(boolean seen, List<ItemStack> shelf) {
    public static final PlayerBooks EMPTY = new PlayerBooks(false, List.of());
    public static final Codec<PlayerBooks> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.BOOL.optionalFieldOf("seen", false).forGetter(PlayerBooks::seen),
            ItemStack.CODEC.listOf().optionalFieldOf("shelf", List.of()).forGetter(PlayerBooks::shelf)
    ).apply(i, PlayerBooks::new));

    private static final DeferredRegister<AttachmentType<?>> TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Enderpedia.MODID);
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerBooks>> TYPE = TYPES.register("books",
            () -> AttachmentType.builder(() -> EMPTY).serialize(CODEC).copyOnDeath().build());

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
    }

    public PlayerBooks withSeen(boolean value) {
        return new PlayerBooks(value, shelf);
    }

    public PlayerBooks withAdded(ItemStack stack) {
        List<ItemStack> list = new ArrayList<>(shelf);
        list.add(stack.copy());
        return new PlayerBooks(seen, List.copyOf(list));
    }

    public PlayerBooks withRemoved(int index) {
        List<ItemStack> list = new ArrayList<>(shelf);
        list.remove(index);
        return new PlayerBooks(seen, List.copyOf(list));
    }

    public PlayerBooks cleared() {
        return new PlayerBooks(seen, List.of());
    }
}
