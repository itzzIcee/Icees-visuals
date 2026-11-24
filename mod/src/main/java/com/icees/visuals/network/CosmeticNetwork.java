package com.icees.visuals.network;

import com.icees.visuals.cosmetics.CosmeticState;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Common + server: register codecs and relay cosmetics between mod users. */
public final class CosmeticNetwork {
	private static final Map<UUID, CosmeticSyncPayload> CACHE = new ConcurrentHashMap<>();

	private CosmeticNetwork() {}

	public static void registerCommon() {
		PayloadTypeRegistry.playC2S().register(CosmeticSyncPayload.TYPE, CosmeticSyncPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(CosmeticSyncPayload.TYPE, CosmeticSyncPayload.CODEC);
	}

	public static void registerServer() {
		ServerPlayNetworking.registerGlobalReceiver(CosmeticSyncPayload.TYPE, (payload, context) -> {
			ServerPlayer sender = context.player();
			CosmeticSyncPayload stamped = new CosmeticSyncPayload(sender.getUUID(), payload.state());
			CACHE.put(sender.getUUID(), stamped);
			relay(sender.level().getServer(), stamped, sender.getUUID());
		});

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayer joined = handler.getPlayer();
			server.execute(() -> {
				for (CosmeticSyncPayload cached : CACHE.values()) {
					if (cached.playerId().equals(joined.getUUID())) continue;
					if (ServerPlayNetworking.canSend(joined, CosmeticSyncPayload.TYPE)) {
						ServerPlayNetworking.send(joined, cached);
					}
				}
			});
		});

		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			UUID id = handler.getPlayer().getUUID();
			CACHE.remove(id);
			relay(server, new CosmeticSyncPayload(id, CosmeticState.cleared()), null);
		});
	}

	/** Send to every mod client except {@code except} (null = everyone). */
	private static void relay(MinecraftServer server, CosmeticSyncPayload payload, UUID except) {
		if (server == null) return;
		for (ServerPlayer other : server.getPlayerList().getPlayers()) {
			if (except != null && other.getUUID().equals(except)) continue;
			if (ServerPlayNetworking.canSend(other, CosmeticSyncPayload.TYPE)) {
				ServerPlayNetworking.send(other, payload);
			}
		}
	}

	public static void clearCache() {
		CACHE.clear();
	}
}
