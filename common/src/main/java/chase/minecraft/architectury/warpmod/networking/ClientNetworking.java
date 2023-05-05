package chase.minecraft.architectury.warpmod.networking;

import chase.minecraft.architectury.warpmod.client.WarpModClient;
import chase.minecraft.architectury.warpmod.client.gui.screen.WarpListScreen;
import chase.minecraft.architectury.warpmod.data.WarpManager;
import lol.bai.badpackets.api.S2CPacketReceiver;
import net.minecraft.nbt.CompoundTag;

import java.nio.charset.Charset;

/**
 * Handles client networking
 */
public class ClientNetworking extends WarpNetworking
{
	/**
	 * Initializes the clients network packet receiver
	 */
	public static void init()
	{
		S2CPacketReceiver.register(LIST, (client, handler, buf, responseSender) ->
		{
			CompoundTag data = buf.readNbt();
			assert data != null;
			WarpManager warpManager = WarpManager.fromPlayer(client.player);
			warpManager.fromNbt(data);
			if (client.screen instanceof WarpListScreen screen)
			{
				screen.refresh();
			}
		});
		
		S2CPacketReceiver.register(DIMENSIONS, (client, handler, buf, responseSender) ->
		{
			CompoundTag data = buf.readNbt();
			assert data != null;
			WarpModClient.dimensions = data.getAllKeys().stream().toList();
		});
		S2CPacketReceiver.register(PING, (client, handler, buf, responseSender) ->
		{
			WarpModClient.onServer = true;
			int length = buf.readInt();
			WarpModClient.remoteVersion = buf.readCharSequence(length, Charset.defaultCharset()).toString();
			WarpModClient.isOP = buf.readBoolean();
		});
		S2CPacketReceiver.register(MIRROR, (client, handler, buf, responseSender) -> {
		
		});
	}
}
