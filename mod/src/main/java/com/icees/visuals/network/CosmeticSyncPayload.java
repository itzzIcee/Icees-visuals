package com.icees.visuals.network;

import com.icees.visuals.cosmetics.CosmeticState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.UUID;

public record CosmeticSyncPayload(UUID playerId, CosmeticState state) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<CosmeticSyncPayload> TYPE =
		new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("icees-visuals", "cosmetic_sync"));

	public static final StreamCodec<FriendlyByteBuf, CosmeticSyncPayload> CODEC =
		CustomPacketPayload.codec(CosmeticSyncPayload::write, CosmeticSyncPayload::new);

	private CosmeticSyncPayload(FriendlyByteBuf buf) {
		this(buf.readUUID(), CosmeticState.read(buf));
	}

	private void write(FriendlyByteBuf buf) {
		buf.writeUUID(playerId);
		state.write(buf);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
