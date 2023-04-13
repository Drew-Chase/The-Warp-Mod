package chase.minecraft.architectury.warpmod.data;

import chase.minecraft.architectury.warpmod.WarpMod;
import com.google.common.collect.ImmutableList;
import net.minecraft.resources.ResourceLocation;

/**
 * The WaypointIcons class contains static ResourceLocation objects representing different icons and a method to return an immutable list of these icons.
 */
public class WaypointIcons
{
	public static final int SIZE = 32;
	public static final ResourceLocation DEFAULT = id("default");
	public static final ResourceLocation HOME = id("home");
	public static final ResourceLocation BOOKMARK = id("bookmark");
	public static final ResourceLocation STORAGE = id("storage");
	public static final ResourceLocation DEATH = id("death");
	public static final ResourceLocation CROP = id("crop");
	public static final ResourceLocation MOUNTAINS = id("mountains");
	public static final ResourceLocation TELEPORT = id("teleport");
	
	
	/**
	 * This function returns a ResourceLocation object for a given file path in a specific directory.
	 *
	 * @param name The parameter "name" is a string that represents the name of an icon file in the "textures/gui/icons" directory. The method "id" then formats this string into a file path and returns a ResourceLocation object that can be used to load the icon image.
	 * @return The method is returning a ResourceLocation object that represents the file path of a PNG image located in the "textures/gui/icons/" directory of the mod's resources. The file name is determined by the "name" parameter passed to the method.
	 */
	private static ResourceLocation id(String name)
	{
		return WarpMod.id("textures/gui/icons/%s.png".formatted(name));
	}
	
	/**
	 * The function returns an immutable list of resource locations representing icons.
	 *
	 * @return An immutable list of ResourceLocation objects. The list contains six ResourceLocation objects representing different icons.
	 */
	public static ImmutableList<ResourceLocation> icons()
	{
		return ImmutableList.of(
				DEFAULT,
				HOME,
				BOOKMARK,
				STORAGE,
				DEATH,
				TELEPORT
		);
	}
	
}
