package dev.enderpedia;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Server asks a client to open the book index (used by /enderpedia open). */
public record OpenIndexPayload() implements CustomPacketPayload {
    public static final OpenIndexPayload INSTANCE = new OpenIndexPayload();
    public static final Type<OpenIndexPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Enderpedia.MODID, "open_index"));
    public static final StreamCodec<ByteBuf, OpenIndexPayload> CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").optional().playToClient(TYPE, CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> dev.enderpedia.client.EnderpediaClient.openIndex()));
    }
}
