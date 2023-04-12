package chase.minecraft.architectury.warpmod.networking;

import chase.minecraft.architectury.warpmod.WarpMod;
import net.minecraft.resources.ResourceLocation;

public abstract class WarpNetworking
{
	
	public static final ResourceLocation LIST = WarpMod.id("list_packet");
	public static final ResourceLocation GET = WarpMod.id("get_packet");
	public static final ResourceLocation EDIT = WarpMod.id("edit_packet");
	public static final ResourceLocation TELEPORT = WarpMod.id("teleport_packet");
	public static final ResourceLocation CREATE = WarpMod.id("create_packet");
	public static final ResourceLocation REMOVE = WarpMod.id("remove_packet");
	public static final ResourceLocation DIMENSIONS = WarpMod.id("dimensions_packet");
	public static final ResourceLocation PING = WarpMod.id("ping_packet");
	
	public static void init()
	{
	
	}
}
