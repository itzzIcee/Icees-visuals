package com.icees.visuals.network;

import com.icees.visuals.cosmetics.CosmeticState;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Client: send own cosmetics, store remotes for rendering. */
public final class CosmeticClientSync {
	private static final Map<UUID, CosmeticState> REMOTE = new ConcurrentHashMap<>();
	private static int ticksSinceSend;

	private CosmeticClientSync() {}

	public static void register() {
		ClientPlayNetworking.registerGlobalReceiver(CosmeticSyncPayload.TYPE, (payload, context) -> {
			Minecraft client = context.client();
			if (client.player != null && payload.playerId().equals(client.player.getUUID())) return;
			if (!payload.state().anyVisible()) {
				REMOTE.remove(payload.playerId());
				return;
			}
			REMOTE.put(payload.playerId(), payload.state());
		});

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			REMOTE.clear();
			ticksSinceSend = 999; // send ASAP after join
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			REMOTE.clear();
			ticksSinceSend = 0;
		});
	}

	public static CosmeticState getRemote(UUID id) {
		return REMOTE.get(id);
	}

	public static void tick(Minecraft client) {
		if (client.player == null || client.getConnection() == null) return;
		ticksSinceSend++;
		if (ticksSinceSend < 40) return;
		ticksSinceSend = 0;
		sendNow(client);
	}

	/** Push local cosmetics to the server relay (other mod clients). */
	public static void sendNow(Minecraft client) {
		if (client == null || client.player == null) return;
		if (!ClientPlayNetworking.canSend(CosmeticSyncPayload.TYPE)) return;
		CosmeticSyncPayload payload = new CosmeticSyncPayload(client.player.getUUID(), CosmeticState.fromLocal());
		ClientPlayNetworking.send(payload);
		ticksSinceSend = 0;
	}

	public static void onConfigChanged() {
		sendNow(Minecraft.getInstance());
	}
}
