package chase.minecraft.architectury.warpmod.client;

import chase.minecraft.architectury.warpmod.data.Warps;
import chase.minecraft.architectury.warpmod.networking.ClientNetworking;
import chase.minecraft.architectury.warpmod.networking.WarpNetworking;
import chase.minecraft.architectury.warpmod.utils.WorldUtils;
import com.google.common.collect.ImmutableList;
import dev.architectury.platform.Platform;
import io.netty.buffer.Unpooled;
import lol.bai.badpackets.api.PacketSender;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;

@Environment(EnvType.CLIENT)
public abstract class WarpModClient
{
	
	@NotNull
	public static List<String> dimensions = ImmutableList.of("minecraft:overworld", "minecraft:the_nether", "minecraft:the_end");
	public static boolean onServer = false;
	public static boolean isOP = false;
	public static String remoteVersion = "";
	public static final Path WARP_DIRECTORY = Path.of(Platform.getGameFolder().toString(), "The Warp Mod");
	
	/**
	 * Initializes the client
	 */
	public static void init()
	{
		WARP_DIRECTORY.toFile().mkdirs();
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
	 */
	protected static void onServerLogin(Connection connection)
	{
		FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer());
		PacketSender.c2s().send(WarpNetworking.LIST, new FriendlyByteBuf(Unpooled.buffer()));
		PacketSender.c2s().send(WarpNetworking.DIMENSIONS, new FriendlyByteBuf(Unpooled.buffer()));
		data.writeBoolean(true);
		PacketSender.c2s().send(WarpNetworking.PING, data);
		Warps.loadClient();
		
	}
	
	protected static void onServerLogout(Connection connection)
	{
		Warps.saveClient();
	}
	
	public static void changeDimension(ServerPlayer player, ResourceLocation dimension)
	{
		WorldUtils.removeTravelBar(player);
		if (!WarpModClient.dimensions.contains(dimension.toString()))
		{
			WarpModClient.dimensions.add(dimension.toString());
		}
	}
	
}
