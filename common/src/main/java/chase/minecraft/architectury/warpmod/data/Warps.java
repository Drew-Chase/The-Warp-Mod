package chase.minecraft.architectury.warpmod.data;

import chase.minecraft.architectury.warpmod.WarpMod;
import chase.minecraft.architectury.warpmod.enums.WarpCreationResponseType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
/**
 * The Warps class is a utility class that manages warps for players in a Minecraft server, allowing them to create, update, remove, and retrieve warp locations.
 */
public class Warps
{
	private static HashMap<Player, Warps> _instance;
	private final HashMap<String, Warp> warps;
	private final Player player;
	
	// This is a constructor for the Warps class that takes a Player object as a parameter. It initializes the _player field with the passed-in player object. It also checks if the _instance HashMap is null, and if it is, it creates a new HashMap. If the _instance HashMap already contains the player object, it sets the _warps field to the _warps field of the existing Warps object associated with the player. Otherwise, it creates a new HashMap for the _warps field and adds the new Warps object to
	// the _instance HashMap with the player object as the key.
	private Warps(Player player)
	{
		this.player = player;
		if (_instance == null)
		{
			_instance = new HashMap<>();
		}
		if (_instance.containsKey(player))
		{
			warps = _instance.get(player).warps;
		} else
		{
			warps = new HashMap<>();
			_instance.put(player, this);
		}
	}
	
	/**
	 * This function returns a new Warps object with the player's UUID.
	 *
	 * @param player The player who is warping.
	 * @return A new instance of the Warps class.
	 */
	public static Warps fromPlayer(Player player)
	{
		return new Warps(player);
	}
	
	/**
	 * If the warp exists, overwrite it, otherwise add it
	 *
	 * @param warp The warp to add or update.
	 * @return A WarpCreationResponseType enum.
	 */
	public WarpCreationResponseType createAddOrUpdate(Warp warp)
	{
		if (warps.containsKey(warp.getName()))
		{
			warps.put(warp.getName(), warp);
			return WarpCreationResponseType.Overwritten;
		}
		warps.put(warp.getName(), warp);
		return WarpCreationResponseType.Success;
	}
	
	public void rename(String name, String new_name)
	{
		Warp old = get(name);
		remove(name);
		old.update(new_name, old.getX(), old.getY(), old.getZ(), old.getPitch(), old.getYaw(), old.getDimension(), old.getColor(), old.getIcon());
		warps.put(new_name, old);
	}
	
	/**
	 * If the warp exists, remove it from the list
	 *
	 * @param name The name of the warp to remove.
	 * @return A boolean value.
	 */
	public boolean remove(String name)
	{
		if (exists(name))
		{
			warps.remove(name);
			return true;
		}
		return false;
	}
	
	/**
	 * Get all the warps in the plugin and return them as an array.
	 *
	 * @return An array of Warp objects.
	 */
	public Warp[] getWarps()
	{
		return warps.values().toArray(new Warp[0]);
	}
	
	public String[] getWarpNames()
	{
		return warps.keySet().toArray(new String[0]);
	}
	
	/**
	 * "Return a CompletableFuture that completes with a Suggestions object containing all the warps in the server."
	 * <p>
	 * The first line of the function is the return statement. It returns a CompletableFuture that completes with a
	 * Suggestions object
	 *
	 * @param builder The SuggestionsBuilder object that you can use to add suggestions to.
	 * @return A list of all the warps that are registered.
	 */
	public CompletableFuture<Suggestions> suggestions(SuggestionsBuilder builder)
	{
		return SharedSuggestionProvider.suggest(warps.keySet(), builder);
	}
	
	/**
	 * It gets a warp by name
	 *
	 * @param name The name of the warp.
	 * @return A warp object.
	 */
	public Warp get(String name)
	{
		return warps.get(name);
	}
	
	/**
	 * Returns true if the warp exists, false if it doesn't.
	 *
	 * @param name The name of the warp.
	 * @return A boolean value.
	 */
	public boolean exists(String name)
	{
		return warps.containsKey(name);
	}
	
	/**
	 * Converts all loaded warps to NBTData that can be saved!
	 *
	 * @return NBTData
	 */
	public ListTag toNbt()
	{
		ListTag listTag = new ListTag();
		for (Warp warp : warps.values())
		{
			listTag.add(warp.toNbt());
		}
		return listTag;
	}
	
	/**
	 * Gets warps from PlayerNBT
	 *
	 * @param tag the players nbt
	 */
	public void fromNbt(CompoundTag tag)
	{
		warps.clear();
		try
		{
			ListTag listTag = tag.getList("warps", CompoundTag.TAG_COMPOUND);
			WarpMod.log.info(String.format("Loading NBT for player: %s - %d", player.getDisplayName().getString(), listTag.size()));
			for (int i = 0; i < listTag.size(); i++)
			{
				CompoundTag item = listTag.getCompound(i);
				Warp warp = Warp.fromTag(item, player);
				warps.put(warp.getName(), warp);
			}
		} catch (Exception e)
		{
			WarpMod.log.error(String.format("Unable to load Player Warp NBT: %s", e.getMessage()));
		}
		WarpMod.log.info(String.format("%d Warps found for %s", warps.size(), player.getDisplayName().getString()));
	}
	
	/**
	 * Creates a back warp
	 */
	public void createBack()
	{
		createAddOrUpdate(new Warp("back", player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot(), player.level.dimension().location(), player, false, WaypointIcons.TELEPORT, 0xFF_FF_FF));
	}
	
	/**
	 * Creates a death warp
	 */
	public void createDeath()
	{
		createAddOrUpdate(new Warp("death.%d".formatted(System.currentTimeMillis()), player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot(), player.level.dimension().location(), player, true, WaypointIcons.DEATH, 0xFF_00_00));
	}
	
}
