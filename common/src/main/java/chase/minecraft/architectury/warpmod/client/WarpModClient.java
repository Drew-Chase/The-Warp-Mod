package chase.minecraft.architectury.warpmod.client;

import chase.minecraft.architectury.warpmod.data.WarpManager;
import chase.minecraft.architectury.warpmod.networking.ClientNetworking;
import chase.minecraft.architectury.warpmod.networking.WarpNetworking;
import com.google.common.collect.ImmutableList;
import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.platform.Platform;
import io.netty.buffer.Unpooled;
import lol.bai.badpackets.api.PacketSender;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
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
		ClientPlayerEvent.CLIENT_PLAYER_JOIN.register(player -> onServerLogin());
		ClientPlayerEvent.CLIENT_PLAYER_QUIT.register(player -> onServerLogout());
		ClientTickEvent.CLIENT_POST.register(instance -> onClientTick());
		
		PlayerEvent.CHANGE_DIMENSION.register((player, oldLevel, newLevel) ->
		{
			if (!WarpModClient.dimensions.contains(newLevel.location().toString()))
			{
				WarpModClient.dimensions.add(newLevel.location().toString());
			}
			if (!WarpModClient.dimensions.contains(oldLevel.location().toString()))
			{
				WarpModClient.dimensions.add(oldLevel.location().toString());
			}
		});
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
	protected static void onServerLogin()
	{
		FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer());
		data.writeBoolean(true);
		PacketSender.c2s().send(WarpNetworking.PING, data);
		WarpManager.loadClient();
//		WorldmapThread.getInstance().start();
		
	}
	
	protected static void onServerLogout()
	{
//		WorldmapThread.getInstance().interrupt();
	}
	
	
}
