package chase.minecraft.architectury.warpmod.networking;

import chase.minecraft.architectury.warpmod.data.Warp;
import chase.minecraft.architectury.warpmod.data.Warps;
import io.netty.buffer.Unpooled;
import lol.bai.badpackets.api.C2SPacketReceiver;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.nio.charset.Charset;

public class ServerNetworking extends WarpNetworking
{
	public static void init()
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
}
