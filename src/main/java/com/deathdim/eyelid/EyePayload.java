package com.deathdim.eyelid;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record EyePayload(int action, int ticks) implements CustomPayload {
    public static final CustomPayload.Id<EyePayload> ID =
            new CustomPayload.Id<>(Identifier.of("deathdim", "eye"));

    public static final PacketCodec<PacketByteBuf, EyePayload> CODEC = PacketCodec.of(
            (value, buf) -> {
                buf.writeByte(value.action());
                buf.writeInt(value.ticks());
            },
            buf -> new EyePayload(buf.readByte(), buf.readInt())
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
