package com.deathdim.eyelid;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** action: 0 = mulai orbit, 1 = selesai orbit. ticks = durasi easing. */
public record CameraPayload(int action, int ticks) implements CustomPayload {
    public static final CustomPayload.Id<CameraPayload> ID =
            new CustomPayload.Id<>(Identifier.of("deathdim", "camera"));

    public static final PacketCodec<PacketByteBuf, CameraPayload> CODEC = PacketCodec.of(
            (v, buf) -> {
                buf.writeByte(v.action());
                buf.writeInt(v.ticks());
            },
            buf -> new CameraPayload(buf.readByte(), buf.readInt())
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
