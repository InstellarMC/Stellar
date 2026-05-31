package dev.instellar.stellar.plugin.messaging;

import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.DiscardedPayload;
import net.minecraft.resources.ResourceLocation;

// Based from IzzelAliz/Arclight
// see https://github.com/IzzelAliz/Arclight/blob/0769551b8755f0a0452e95b069e314be81f0f29b/arclight-common/src/main/java/io/izzel/arclight/common/mod/plugin/messaging/RawPayload.java
/**
 * Read from channel: native/heap retained buffer;
 * Write to channel: heap slice buffer;
 * The payload may be serialized more than once, but is guaranteed to be read only once
 */
public interface RawPayload {

    ByteBuf getData();

    void setData(ByteBuf data);

    default byte[] readBytes() {
        final var buf = getData();
        byte[] allocate = new byte[buf.readableBytes()];
        buf.readBytes(allocate);
        ReferenceCountUtil.release(buf);
        setData(null);
        return allocate;
    }

    default ByteBuf getSlicedData() {
        return getData().slice();
    }

    default byte[] leak() {
        final var buf = getData();
        byte[] allocate = new byte[buf.readableBytes()];
        buf.readBytes(allocate);
        ReferenceCountUtil.release(buf);
        setData(null);
        return allocate;
    }

    static <B extends FriendlyByteBuf> StreamCodec<B, CustomPacketPayload> discardedCodec(ResourceLocation location, int max) {
        return new StreamCodec<B, CustomPacketPayload>() {
            @Override
            public DiscardedPayload decode(B buf) {
                int j = buf.readableBytes();
                if (j >= 0 && j <= max) {
                    var data = buf.readRetainedSlice(j);
                    return new DiscardedPayload(location, data);
                } else {
                    throw new IllegalArgumentException("Payload may not be larger than " + max + " bytes");
                }
            }

            @Override
            public void encode(B buf, CustomPacketPayload obj) {
                if (obj instanceof RawPayload raw) {
                    buf.writeBytes(raw.getSlicedData());
                }
            }
        };
    }

}
