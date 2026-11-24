package com.icees.visuals.cosmetics;

import com.icees.visuals.config.CosmeticTint;
import com.icees.visuals.config.IceesConfig;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Snapshot of one player's cosmetics — local config or synced remote.
 * Protocol v2 — both clients need the same mod build.
 */
public final class CosmeticState {
	public static final int PROTOCOL = 2;

	public boolean wings = true;
	public WingStyle wingStyle = WingStyle.PETAL;
	public int wingSize = 5;
	public CosmeticTint wingTint = new CosmeticTint(true, 330, 70, 100);

	public boolean halo = true;
	public HaloStyle haloStyle = HaloStyle.BLOSSOM;
	public int haloSize = 5;
	public CosmeticTint haloTint = new CosmeticTint(true, 320, 65, 100);

	public boolean hat = true;
	public HatStyle hatStyle = HatStyle.STRAW_RED;
	public int hatSize = 5;
	public CosmeticTint hatTint = new CosmeticTint(true, 40, 55, 90);

	public boolean cape = true;
	public CapeStyle capeStyle = CapeStyle.SAKURA;
	public int capeSize = 5;
	public CosmeticTint capeTint = new CosmeticTint(true, 330, 70, 100);

	public boolean aura = true;
	public AuraStyle auraStyle = AuraStyle.ORBIT;
	public int auraSize = 5;
	public CosmeticTint auraTint = new CosmeticTint(true, 310, 70, 100);

	public boolean jumpCircles = true;
	public JumpCircleStyle jumpCircleStyle = JumpCircleStyle.RIPPLE;
	public int jumpCircleSize = 5;
	public int jumpCircleSpeed = 5;
	public int jumpCircleCount = 4;
	public CosmeticTint jumpTint = new CosmeticTint(true, 310, 70, 100);

	public int brightness = 7;

	public static CosmeticState fromLocal() {
		CosmeticState s = new CosmeticState();
		s.wings = IceesConfig.wings;
		s.wingStyle = IceesConfig.wingStyle;
		s.wingSize = IceesConfig.wingSize;
		s.wingTint = IceesConfig.wingTint.copy();
		s.halo = IceesConfig.halo;
		s.haloStyle = IceesConfig.haloStyle;
		s.haloSize = IceesConfig.haloSize;
		s.haloTint = IceesConfig.haloTint.copy();
		s.hat = IceesConfig.hat;
		s.hatStyle = IceesConfig.hatStyle;
		s.hatSize = IceesConfig.hatSize;
		s.hatTint = IceesConfig.hatTint.copy();
		s.cape = IceesConfig.cape;
		s.capeStyle = IceesConfig.capeStyle;
		s.capeSize = IceesConfig.capeSize;
		s.capeTint = IceesConfig.capeTint.copy();
		s.aura = IceesConfig.aura;
		s.auraStyle = IceesConfig.auraStyle;
		s.auraSize = IceesConfig.auraSize;
		s.auraTint = IceesConfig.auraTint.copy();
		s.jumpCircles = IceesConfig.jumpCircles;
		s.jumpCircleStyle = IceesConfig.jumpCircleStyle;
		s.jumpCircleSize = IceesConfig.jumpCircleSize;
		s.jumpCircleSpeed = IceesConfig.jumpCircleSpeed;
		s.jumpCircleCount = IceesConfig.jumpCircleCount;
		s.jumpTint = IceesConfig.jumpTint.copy();
		s.brightness = IceesConfig.cosmeticBrightness;
		return s;
	}

	/** All cosmetics off — used when a player leaves so remotes clear their look. */
	public static CosmeticState cleared() {
		CosmeticState s = new CosmeticState();
		s.wings = false;
		s.halo = false;
		s.hat = false;
		s.cape = false;
		s.aura = false;
		s.jumpCircles = false;
		return s;
	}

	public boolean anyVisible() {
		return wings || halo || hat || cape || aura || jumpCircles;
	}

	public float brightnessFactor() {
		return 0.5f + IceesConfig.clamp10(brightness) * 0.15f;
	}

	public void write(FriendlyByteBuf buf) {
		buf.writeByte(PROTOCOL);
		int flags = 0;
		if (wings) flags |= 1;
		if (halo) flags |= 2;
		if (hat) flags |= 4;
		if (cape) flags |= 8;
		if (aura) flags |= 16;
		if (jumpCircles) flags |= 32;
		buf.writeByte(flags);
		buf.writeByte(wingStyle.ordinal());
		buf.writeByte(haloStyle.ordinal());
		buf.writeByte(hatStyle.ordinal());
		buf.writeByte(capeStyle.ordinal());
		buf.writeByte(auraStyle.ordinal());
		buf.writeByte(jumpCircleStyle.ordinal());
		buf.writeByte(IceesConfig.clamp10(wingSize));
		buf.writeByte(IceesConfig.clamp10(haloSize));
		buf.writeByte(IceesConfig.clamp10(hatSize));
		buf.writeByte(IceesConfig.clamp10(capeSize));
		buf.writeByte(IceesConfig.clamp10(auraSize));
		buf.writeByte(IceesConfig.clamp10(jumpCircleSize));
		buf.writeByte(IceesConfig.clamp10(jumpCircleSpeed));
		buf.writeByte(Math.max(1, Math.min(8, jumpCircleCount)));
		buf.writeByte(IceesConfig.clamp10(brightness));
		writeTint(buf, wingTint);
		writeTint(buf, haloTint);
		writeTint(buf, hatTint);
		writeTint(buf, capeTint);
		writeTint(buf, auraTint);
		writeTint(buf, jumpTint);
	}

	public static CosmeticState read(FriendlyByteBuf buf) {
		int protocol = buf.readUnsignedByte();
		if (protocol != PROTOCOL) {
			// Drain unknown payloads so the networking codec stays aligned.
			buf.readerIndex(buf.writerIndex());
			return cleared();
		}
		CosmeticState s = new CosmeticState();
		int flags = buf.readUnsignedByte();
		s.wings = (flags & 1) != 0;
		s.halo = (flags & 2) != 0;
		s.hat = (flags & 4) != 0;
		s.cape = (flags & 8) != 0;
		s.aura = (flags & 16) != 0;
		s.jumpCircles = (flags & 32) != 0;
		s.wingStyle = enumOr(WingStyle.values(), buf.readUnsignedByte(), WingStyle.PETAL);
		s.haloStyle = enumOr(HaloStyle.values(), buf.readUnsignedByte(), HaloStyle.BLOSSOM);
		s.hatStyle = enumOr(HatStyle.values(), buf.readUnsignedByte(), HatStyle.STRAW_RED);
		s.capeStyle = enumOr(CapeStyle.values(), buf.readUnsignedByte(), CapeStyle.SAKURA);
		s.auraStyle = enumOr(AuraStyle.values(), buf.readUnsignedByte(), AuraStyle.ORBIT);
		s.jumpCircleStyle = enumOr(JumpCircleStyle.values(), buf.readUnsignedByte(), JumpCircleStyle.RIPPLE);
		s.wingSize = IceesConfig.clamp10(buf.readUnsignedByte());
		s.haloSize = IceesConfig.clamp10(buf.readUnsignedByte());
		s.hatSize = IceesConfig.clamp10(buf.readUnsignedByte());
		s.capeSize = IceesConfig.clamp10(buf.readUnsignedByte());
		s.auraSize = IceesConfig.clamp10(buf.readUnsignedByte());
		s.jumpCircleSize = IceesConfig.clamp10(buf.readUnsignedByte());
		s.jumpCircleSpeed = IceesConfig.clamp10(buf.readUnsignedByte());
		s.jumpCircleCount = Math.max(1, Math.min(8, buf.readUnsignedByte()));
		s.brightness = IceesConfig.clamp10(buf.readUnsignedByte());
		s.wingTint = readTint(buf);
		s.haloTint = readTint(buf);
		s.hatTint = readTint(buf);
		s.capeTint = readTint(buf);
		s.auraTint = readTint(buf);
		s.jumpTint = readTint(buf);
		return s;
	}

	private static void writeTint(FriendlyByteBuf buf, CosmeticTint tint) {
		buf.writeBoolean(tint.enabled);
		buf.writeShort(tint.hue);
		buf.writeByte(tint.sat);
		buf.writeByte(tint.val);
		buf.writeBoolean(tint.gradient);
		buf.writeShort(tint.hue2);
		buf.writeByte(tint.sat2);
		buf.writeByte(tint.val2);
		buf.writeUtf(tint.gradientName != null ? tint.gradientName : "OFF", 32);
	}

	private static CosmeticTint readTint(FriendlyByteBuf buf) {
		CosmeticTint t = new CosmeticTint(
			buf.readBoolean(),
			buf.readShort() & 0xFFFF,
			buf.readUnsignedByte(),
			buf.readUnsignedByte()
		);
		t.gradient = buf.readBoolean();
		t.hue2 = buf.readShort() & 0xFFFF;
		t.sat2 = buf.readUnsignedByte();
		t.val2 = buf.readUnsignedByte();
		t.gradientName = buf.readUtf(32);
		return t;
	}

	private static <T extends Enum<T>> T enumOr(T[] values, int ordinal, T fallback) {
		if (ordinal < 0 || ordinal >= values.length) return fallback;
		return values[ordinal];
	}
}
