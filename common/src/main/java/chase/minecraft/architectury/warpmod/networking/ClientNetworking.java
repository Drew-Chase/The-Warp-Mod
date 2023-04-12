package chase.minecraft.architectury.warpmod.networking;

import chase.minecraft.architectury.warpmod.WarpMod;
import chase.minecraft.architectury.warpmod.client.ClientWarps;
import chase.minecraft.architectury.warpmod.client.WarpModClient;
import chase.minecraft.architectury.warpmod.client.gui.WarpScreen;
import lol.bai.badpackets.api.S2CPacketReceiver;
import net.minecraft.nbt.CompoundTag;

public class ClientNetworking extends WarpNetworking
{
	public static void init()
	{
		S2CPacketReceiver.register(LIST, (client, handler, buf, responseSender) ->
		{
			CompoundTag data = buf.readNbt();
			assert data != null;
			ClientWarps.Instance.fromNBT(data);
			if (client.screen instanceof WarpScreen screen)
			{
				screen.refresh();
			}
		});
		
		S2CPacketReceiver.register(DIMENSIONS, (client, handler, buf, responseSender) ->
		{
			CompoundTag data = buf.readNbt();
			assert data != null;
			WarpModClient.dimensions = data.getAllKeys().toArray(new String[0]);
		});
		S2CPacketReceiver.register(PING, (client, handler, buf, responseSender)->{
			WarpMod.onServer = true;
		});
	}
}
