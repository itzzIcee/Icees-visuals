package com.icees.visuals.cosmetics;

import com.icees.visuals.config.CosmeticTint;
import com.icees.visuals.config.IceesConfig;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Snapshot of one player's cosmetics — local config or synced remote.
 */
public final class CosmeticState {
	public boolean wings = true;
	public WingStyle wingStyle = WingStyle.PETAL;
	public boolean halo = true;
	public HaloStyle haloStyle = HaloStyle.BLOSSOM;
	public boolean hat = true;
	public HatStyle hatStyle = HatStyle.STRAW_RED;
	public boolean cape = true;
	public CapeStyle capeStyle = CapeStyle.SAKURA;
	public boolean aura = true;
	public AuraStyle auraStyle = AuraStyle.ORBIT;
	public boolean jumpCircles = true;
	public boolean shield = false;
	public ShieldStyle shieldStyle = ShieldStyle.SAKURA;
	public int brightness = 5;
	public int size = 4;
	public CosmeticTint wingTint = new CosmeticTint(true, 330, 70, 100);
	public CosmeticTint haloTint = new CosmeticTint(true, 320, 65, 100);
	public CosmeticTint capeTint = new CosmeticTint(true, 330, 70, 100);
	public CosmeticTint auraTint = new CosmeticTint(true, 310, 70, 100);
	public CosmeticTint shieldTint = new CosmeticTint(true, 330, 55, 100);

	public static CosmeticState fromLocal() {
		CosmeticState s = new CosmeticState();
		s.wings = IceesConfig.wings;
		s.wingStyle = IceesConfig.wingStyle;
		s.halo = IceesConfig.halo;
		s.haloStyle = IceesConfig.haloStyle;
		s.hat = IceesConfig.hat;
		s.hatStyle = IceesConfig.hatStyle;
		s.cape = IceesConfig.cape;
		s.capeStyle = IceesConfig.capeStyle;
		s.aura = IceesConfig.aura;
		s.auraStyle = IceesConfig.auraStyle;
		s.jumpCircles = IceesConfig.jumpCircles;
		s.shield = IceesConfig.shield;
		s.shieldStyle = IceesConfig.shieldStyle;
		s.brightness = IceesConfig.cosmeticBrightness;
		s.size = IceesConfig.cosmeticSize;
		s.wingTint = IceesConfig.wingTint.copy();
		s.haloTint = IceesConfig.haloTint.copy();
		s.capeTint = IceesConfig.capeTint.copy();
		s.auraTint = IceesConfig.auraTint.copy();
		s.shieldTint = IceesConfig.shieldTint.copy();
		return s;
	}

	/** Brighter curve so cosmetics read clearly outdoors and at distance. */
	public float brightnessFactor() {
		return 0.65f + clamp(brightness, 1, 5) * 0.28f;
	}

	public float sizeFactor() {
		return 0.85f + clamp(size, 1, 5) * 0.30f;
	}

	public void write(FriendlyByteBuf buf) {
		int flags = 0;
		if (wings) flags |= 1;
		if (halo) flags |= 2;
		if (hat) flags |= 4;
		if (cape) flags |= 8;
		if (aura) flags |= 16;
		if (jumpCircles) flags |= 32;
		if (shield) flags |= 64;
		buf.writeByte(flags);
		buf.writeByte(wingStyle.ordinal());
		buf.writeByte(haloStyle.ordinal());
		buf.writeByte(hatStyle.ordinal());
		buf.writeByte(capeStyle.ordinal());
		buf.writeByte(auraStyle.ordinal());
		buf.writeByte(shieldStyle.ordinal());
		buf.writeByte(clamp(brightness, 1, 5));
		buf.writeByte(clamp(size, 1, 5));
		writeTint(buf, wingTint);
		writeTint(buf, haloTint);
		writeTint(buf, capeTint);
		writeTint(buf, auraTint);
		writeTint(buf, shieldTint);
	}

	public static CosmeticState read(FriendlyByteBuf buf) {
		CosmeticState s = new CosmeticState();
		int flags = buf.readUnsignedByte();
		s.wings = (flags & 1) != 0;
		s.halo = (flags & 2) != 0;
		s.hat = (flags & 4) != 0;
		s.cape = (flags & 8) != 0;
		s.aura = (flags & 16) != 0;
		s.jumpCircles = (flags & 32) != 0;
		s.shield = (flags & 64) != 0;
		s.wingStyle = enumOr(WingStyle.values(), buf.readUnsignedByte(), WingStyle.PETAL);
		s.haloStyle = enumOr(HaloStyle.values(), buf.readUnsignedByte(), HaloStyle.BLOSSOM);
		s.hatStyle = migrateHat(enumOr(HatStyle.values(), buf.readUnsignedByte(), HatStyle.STRAW_RED));
		s.capeStyle = enumOr(CapeStyle.values(), buf.readUnsignedByte(), CapeStyle.SAKURA);
		s.auraStyle = enumOr(AuraStyle.values(), buf.readUnsignedByte(), AuraStyle.ORBIT);
		s.shieldStyle = enumOr(ShieldStyle.values(), buf.readUnsignedByte(), ShieldStyle.SAKURA);
		s.brightness = clamp(buf.readUnsignedByte(), 1, 5);
		s.size = clamp(buf.readUnsignedByte(), 1, 5);
		s.wingTint = readTint(buf);
		s.haloTint = readTint(buf);
		s.capeTint = readTint(buf);
		s.auraTint = readTint(buf);
		s.shieldTint = readTint(buf);
		return s;
	}

	private static void writeTint(FriendlyByteBuf buf, CosmeticTint tint) {
		buf.writeBoolean(tint.enabled);
		buf.writeShort(tint.hue);
		buf.writeByte(tint.sat);
		buf.writeByte(tint.val);
	}

	private static CosmeticTint readTint(FriendlyByteBuf buf) {
		return new CosmeticTint(buf.readBoolean(), buf.readShort() & 0xFFFF, buf.readUnsignedByte(), buf.readUnsignedByte());
	}

	private static HatStyle migrateHat(HatStyle style) {
		return style == null ? HatStyle.STRAW_RED : style;
	}

	private static <T extends Enum<T>> T enumOr(T[] values, int ordinal, T fallback) {
		if (ordinal < 0 || ordinal >= values.length) return fallback;
		return values[ordinal];
	}

	private static int clamp(int v, int lo, int hi) {
		return Math.max(lo, Math.min(hi, v));
	}
}
