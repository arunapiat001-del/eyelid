package com.deathdim.eyelid;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** action: 0 = angkat kepala (tahan), 1 = kembalikan ke depan. pitch: derajat, negatif = menengadah. */
public record RebirthPayload(int action, int ticks, int pitch) implements CustomPayload {
    public static final CustomPayload.Id<RebirthPayload> ID =
            new CustomPayload.Id<>(Identifier.of("deathdim", "rebirth"));

    public static final PacketCodec<PacketByteBuf, RebirthPayload> CODEC = PacketCodec.of(
            (v, buf) -> {
                buf.writeByte(v.action());
                buf.writeInt(v.ticks());
                buf.writeInt(v.pitch());
            },
            buf -> new RebirthPayload(buf.readByte(), buf.readInt(), buf.readInt())
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
