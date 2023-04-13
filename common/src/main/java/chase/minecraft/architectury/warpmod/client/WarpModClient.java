package chase.minecraft.architectury.warpmod.client;

import chase.minecraft.architectury.warpmod.networking.ClientNetworking;
import chase.minecraft.architectury.warpmod.networking.WarpNetworking;
import io.netty.buffer.Unpooled;
import lol.bai.badpackets.api.PacketSender;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public abstract class WarpModClient
{
	
	@NotNull
	public static String[] dimensions = {"ERROR"};
	public static boolean onServer = false;
	public static boolean isOP = false;
	public static String remoteVersion = "";
	
	/**
	 * Initializes the client
	 */
	public static void init()
	{
		initKeyBindings();
		ClientNetworking.init();
	}
	
	/**
	 * The function initializes key bindings for warp key mappings.
	 */
	private static void initKeyBindings()
	{
		for (WarpKeyMappings mapping : WarpKeyMappings.values())
		{
			mapping.register();
		}
	}
	
	/**
	 * This function iterates through all the values of the WarpKeyMappings enum and calls their tick() method.
	 */
	protected static void onClientTick()
	{
		for (WarpKeyMappings mapping : WarpKeyMappings.values())
		{
			mapping.tick();
		}
	}
	
	/**
	 * Runs when logging in to a server.
	 *
	 * @param connection
	 */
	protected static void onServerLogin(Connection connection)
	{
		FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer());
		PacketSender.c2s().send(WarpNetworking.LIST, new FriendlyByteBuf(Unpooled.buffer()));
		PacketSender.c2s().send(WarpNetworking.DIMENSIONS, new FriendlyByteBuf(Unpooled.buffer()));
		data.writeBoolean(true);
		PacketSender.c2s().send(WarpNetworking.PING, data);
	}
	
	protected static void onServerLogout(Connection connection)
	{
	
	}
	
	
}
