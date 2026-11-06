package dev.enderpedia;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import java.util.List;

/** All Enderpedia packets. Optional, so vanilla clients and servers without Enderpedia still connect. */
public final class Network {
    private Network() {}

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Enderpedia.MODID, path);
    }

    /** Server asks a client to open the book index (used by /enderpedia open). */
    public record OpenIndex() implements CustomPacketPayload {
        public static final OpenIndex INSTANCE = new OpenIndex();
        public static final Type<OpenIndex> TYPE = new Type<>(id("open_index"));
        public static final StreamCodec<ByteBuf, OpenIndex> CODEC = StreamCodec.unit(INSTANCE);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** The player's shelf of kept items, sent on login and whenever it changes. */
    public record Shelf(List<ItemStack> items) implements CustomPacketPayload {
        public static final Type<Shelf> TYPE = new Type<>(id("shelf"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Shelf> CODEC =
                ItemStack.OPTIONAL_LIST_STREAM_CODEC.map(Shelf::new, Shelf::items);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /**
     * Client asks for an item: shelf slot >= 0 hands back that kept item, otherwise a copy of the
     * guide book with that id.
     */
    public record GetBook(ResourceLocation book, int shelfSlot) implements CustomPacketPayload {
        public static final Type<GetBook> TYPE = new Type<>(id("get_book"));
        public static final StreamCodec<ByteBuf, GetBook> CODEC = StreamCodec.composite(
                ResourceLocation.STREAM_CODEC, GetBook::book, ByteBufCodecs.VAR_INT, GetBook::shelfSlot, GetBook::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        var r = event.registrar("1").optional();
        r.playToClient(OpenIndex.TYPE, OpenIndex.CODEC,
                (p, ctx) -> ctx.enqueueWork(() -> dev.enderpedia.client.EnderpediaClient.openIndex()));
        r.playToClient(Shelf.TYPE, Shelf.CODEC,
                (p, ctx) -> ctx.enqueueWork(() -> dev.enderpedia.client.EnderpediaClient.setShelf(p.items())));
        r.playToServer(GetBook.TYPE, GetBook.CODEC,
                (p, ctx) -> ctx.enqueueWork(() -> {
                    if (ctx.player() instanceof ServerPlayer player) Shelves.handOut(player, p.book(), p.shelfSlot());
                }));
    }

    public static void sendShelf(ServerPlayer player) {
        if (player.connection != null && player.connection.hasChannel(Shelf.TYPE)) {
            PacketDistributor.sendToPlayer(player, new Shelf(player.getData(PlayerBooks.TYPE).shelf()));
        }
    }

    public static void openIndex(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, OpenIndex.INSTANCE);
    }
}
