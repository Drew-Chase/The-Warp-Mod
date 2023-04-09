package chase.minecraft.architectury.warpmod.networking;

import chase.minecraft.architectury.warpmod.WarpMod;
import chase.minecraft.architectury.warpmod.client.ClientWarps;
import chase.minecraft.architectury.warpmod.client.WarpModClient;
import chase.minecraft.architectury.warpmod.client.gui.WarpScreen;
import chase.minecraft.architectury.warpmod.data.Warp;
import chase.minecraft.architectury.warpmod.data.Warps;
import io.netty.buffer.Unpooled;
import lol.bai.badpackets.api.C2SPacketReceiver;
import lol.bai.badpackets.api.S2CPacketReceiver;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.nio.charset.Charset;

public class WarpNetworking
{
	
	public static final ResourceLocation LIST = WarpMod.id("list_packet"), GET = WarpMod.id("get_packet"), EDIT = WarpMod.id("edit_packet"), TELEPORT = WarpMod.id("teleport_packet"), CREATE = WarpMod.id("create_packet"), REMOVE = WarpMod.id("remove_packet"), DIMENSIONS = WarpMod.id("dimensions_packet");
	
	public static void initServer()
	{
		C2SPacketReceiver.register(LIST, (server, player, handler, buf, responseSender) ->
		{
			server.execute(() ->
			{
				CompoundTag data = new CompoundTag();
				data.put("warps", Warps.fromPlayer(player).toNBT());
				FriendlyByteBuf dataBuf = new FriendlyByteBuf(Unpooled.buffer());
				dataBuf.writeNbt(data);
				responseSender.send(LIST, dataBuf);
			});
		});
		C2SPacketReceiver.register(TELEPORT, (server, player, handler, buf, responseSender) ->
		{
			if (player.hasPermissions(4))
			{
				int length = buf.readInt();
				String name = buf.readCharSequence(length, Charset.defaultCharset()).toString();
				Warps warps = Warps.fromPlayer(player);
				if (warps.exists(name))
				{
					Warp warp = warps.get(name);
					warp.teleportTo();
				}
			}
		});
		
		C2SPacketReceiver.register(REMOVE, (server, player, handler, buf, responseSender) ->
		{
			int length = buf.readInt();
			String name = buf.readCharSequence(length, Charset.defaultCharset()).toString();
			Warps.fromPlayer(player).remove(name);
		});
		
		C2SPacketReceiver.register(CREATE, (server, player, handler, buf, responseSender) ->
		{
			CompoundTag tag = buf.readNbt();
			assert tag != null;
			String name = tag.getString("name");
			Warps warps = Warps.fromPlayer(player);
			String ogName = tag.getString("ogName");
			if (!ogName.isEmpty() && !name.equals(ogName))
			{
				if (warps.exists(ogName))
				{
					warps.rename(ogName, name);
				}
			} else
			{
				Warp.create(name, tag.getDouble("x"), tag.getDouble("y"), tag.getDouble("z"), tag.getFloat("pitch"), tag.getFloat("yaw"), player, new ResourceLocation(tag.getString("dim")), true);
			}
			
//			server.execute(() ->
//			{
//				responseSender.send(LIST, new FriendlyByteBuf(Unpooled.buffer()));
//			});
		});
		C2SPacketReceiver.register(DIMENSIONS, (server, player, handler, buf, responseSender) ->
		{
			CompoundTag tag = new CompoundTag();
			int index = 0;
			for (ResourceKey<Level> levelKey : server.levelKeys())
			{
				tag.putString(levelKey.location().toString(), levelKey.location().toString());
				index++;
			}
			server.execute(() ->
			{
				FriendlyByteBuf dataBuf = new FriendlyByteBuf(Unpooled.buffer());
				dataBuf.writeNbt(tag);
				responseSender.send(DIMENSIONS, dataBuf);
			});
		});
		
	}
	
	public static void initClient()
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
	}
	
	
}
