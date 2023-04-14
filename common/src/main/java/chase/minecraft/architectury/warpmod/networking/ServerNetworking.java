package chase.minecraft.architectury.warpmod.networking;

import chase.minecraft.architectury.warpmod.WarpMod;
import chase.minecraft.architectury.warpmod.data.Warp;
import chase.minecraft.architectury.warpmod.data.Warps;
import dev.architectury.platform.Platform;
import io.netty.buffer.Unpooled;
import lol.bai.badpackets.api.C2SPacketReceiver;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.nio.charset.Charset;

public class ServerNetworking extends WarpNetworking
{
	/**
	 * Initializes the server network packet receiver
	 */
	public static void init()
	{
		C2SPacketReceiver.register(LIST, (server, player, handler, buf, responseSender) ->
		{
			server.execute(() ->
			{
				CompoundTag data = new CompoundTag();
				data.put("warps", Warps.fromPlayer(player).toNbt());
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
					warps.get(name).teleport(true);
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
			int length = buf.readInt();
			String ogName = buf.readCharSequence(length, Charset.defaultCharset()).toString();
			CompoundTag tag = buf.readNbt();
			assert tag != null;
			Warp warp = Warp.fromTag(tag, player);
			Warps warps = Warps.fromPlayer(player);
			if (!ogName.isEmpty() && !warp.getName().equals(ogName))
			{
				if (warps.exists(ogName))
				{
					warps.rename(ogName, warp.getName());
				}
			} else
			{
				warps.createAddOrUpdate(warp);
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
		
		C2SPacketReceiver.register(PING, ((server, player, handler, buf, responseSender) ->
		{
			String version = Platform.getMod(WarpMod.MOD_ID).getVersion();
			boolean isOP = player.hasPermissions(4);
			FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer());
			
			data.writeInt(version.length());
			data.writeCharSequence(version, Charset.defaultCharset());
			data.writeBoolean(isOP);
			try
			{
				if (buf.readBoolean())
				{
					player.sendSystemMessage(Component.literal("%sThe Warp Mod%s version %s%s%s is installed on the server!".formatted(ChatFormatting.GOLD, ChatFormatting.GREEN, ChatFormatting.GOLD, version, ChatFormatting.GREEN)));
				}
			} catch (Exception e)
			{
			}
			server.execute(() ->
			{
				responseSender.send(PING, data);
			});
		}));
		
	}
}
