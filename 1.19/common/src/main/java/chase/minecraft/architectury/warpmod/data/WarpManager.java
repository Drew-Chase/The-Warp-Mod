
package chase.minecraft.architectury.warpmod.data;

import chase.minecraft.architectury.warpmod.WarpMod;
import chase.minecraft.architectury.warpmod.client.gui.waypoint.WaypointColor;
import chase.minecraft.architectury.warpmod.enums.ClientServerWarpShareMethod;
import chase.minecraft.architectury.warpmod.enums.WarpCreationResponseType;
import chase.minecraft.architectury.warpmod.utils.WorldUtils;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The WarpManager class is a utility class that manages warps for players in a Minecraft server, allowing
 * them to create, update, remove, and retrieve warp locations.
 */
public class WarpManager
{
	private static ConcurrentHashMap<Player, WarpManager> _instance;
	private final ConcurrentHashMap<String, Warp> warps;
	private final Player player;
	private ClientServerWarpShareMethod shareMethod;
	
	/**
	 * This is a constructor for the WarpManager class that takes a Player object as a parameter.
	 * It initializes the _player field with the passed-in player object. It also checks if the _instance HashMap
	 * is null, and if it is, it creates a new HashMap. If the _instance HashMap already contains the player object,
	 * it sets the _warps field to the _warps field of the existing WarpManager object associated with the player.
	 * Otherwise, it creates a new HashMap for the _warps field and adds the new WarpManager object to the _instance
	 * HashMap with the player object as the key.
	 *
	 * @param player The player who is warping.
	 */
	private WarpManager(Player player)
	{
		this.player = player;
		if (_instance == null)
		{
			_instance = new ConcurrentHashMap<>();
		}
		if (_instance.containsKey(player))
		{
			warps = _instance.get(player).warps;
		} else
		{
			warps = new ConcurrentHashMap<>();
			_instance.put(player, this);
		}
	}
	
	/**
	 * This function returns a new WarpManager object with the player's UUID.
	 *
	 * @param player The player who is warping.
	 * @return A new instance of the WarpManager class.
	 */
	public static WarpManager fromPlayer(Player player)
	{
		return new WarpManager(player);
	}
	
	/**
	 * If the warp exists, overwrite it, otherwise add it
	 *
	 * @param warp The warp to add or update.
	 * @return A WarpCreationResponseType enum.
	 */
	public WarpCreationResponseType createOrUpdate(Warp warp)
	{
		if (warps.containsKey(warp.getName()))
		{
			warps.put(warp.getName(), warp);
			saveClient();
			return WarpCreationResponseType.Overwritten;
		}
		warps.put(warp.getName(), warp);
		saveClient();
		return WarpCreationResponseType.Success;
	}
	
	/**
	 * Renames a warp with the given name to the new name.
	 *
	 * @param name original name of the warp to be renamed
	 * @param new_name new name for the warp
	 */
	public void rename(String name, String new_name) {
		Warp old = get(name);
		remove(name);
		old.update(new_name, old.getX(), old.getY(), old.getZ(), old.getPitch(), old.getYaw(), old.getDimension(), old.getColor(), old.getIcon());
		warps.put(new_name, old);
		saveClient();
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
			saveClient();
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
		return warps.values().toArray(Warp[]::new);
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
	 * Updates the share method used in the Client-Server warp communication.
	 * @param method The new share method to be used.
	 */
	public void updateShareMethod(ClientServerWarpShareMethod method){
		shareMethod = method;
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
			try
			{
				
				this.shareMethod = Enum.valueOf(ClientServerWarpShareMethod.class, tag.getString("share_method"));
			} catch (IllegalArgumentException ignored)
			{
				this.shareMethod = ClientServerWarpShareMethod.MIRROR_CLIENT;
			}
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
		WarpMod.log.info(String.format("%d WarpManager found for %s", warps.size(), player.getDisplayName().getString()));
	}
	
	/**
	 * Creates a back warp
	 */
	public void createBack()
	{
//		createAddOrUpdate(new Warp("back", player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot(), player.level.dimension().location(), player, false, WaypointIcons.TELEPORT, WaypointColor.WHITE, false));
	}
	
	/**
	 * Creates a death warp
	 */
	public void createDeath()
	{
		if (player.getLastDeathLocation().isPresent())
		{
			GlobalPos globalPos = player.getLastDeathLocation().get();
			BlockPos deathPos = globalPos.pos();
			ResourceLocation dimension = globalPos.dimension().location();
			int deaths = getDeathpoints().length;
			if (player instanceof ServerPlayer player)
			{
				deaths = player.getStats().getValue(Stats.CUSTOM.get(Stats.DEATHS));
			}
			
			createOrUpdate(new Warp("death.%d".formatted(deaths), deathPos.getX(), deathPos.getY(), deathPos.getZ(), player.getYRot(), player.getXRot(), dimension, player, true, WaypointIcons.DEATH, WaypointColor.RED, true, true));
		}
	}
	
	/**
	 * Returns an array of all deathpoints in the list of warps.
	 *
	 * @return an array of all deathpoints in the list of warps.
	 */
	public Warp[] getDeathpoints() {
		List<Warp> deathpoints = new ArrayList<>();
		
		for (Warp warp : getWarps()) {
			if (warp.isDeathpoint())
				deathpoints.add(warp);
		}
		
		return deathpoints.toArray(Warp[]::new);
	}
	
	
	/**
	 * Saves the client warp data to a file.
	 *
	 * @param data The current server data.
	 */
	public void saveClient(ServerData data)
	{
		// Create a new compound tag to hold the warp data.
		CompoundTag tag = new CompoundTag();
		// Add the share method to the tag.
		tag.putString("share_method", shareMethod.name());
		// Add the warps to the tag.
		tag.put("warps", toNbt());
		
		try
		{
			// Get the file to write the data to.
			File file = WorldUtils.getWarpDataFile();
			// If the file doesn't exist, create a new one.
			if (!file.exists())
			{
				if (!file.createNewFile())
					return;
			}
			// Write the compressed tag to the file.
			NbtIo.writeCompressed(tag, file);
		} catch (IOException e)
		{
			// Log any errors that occur.
			WarpMod.log.error("Unable to write warp save file: {}", e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * Loads the client warp data from a file.
	 *
	 * @param data The current server data.
	 */
	public void loadClient(ServerData data)
	{
		try
		{
			// Get the file to read the data from.
			File file = WorldUtils.getWarpDataFile();
			if (file.exists())
			{
				// Load the warp data from the file.
				fromNbt(NbtIo.readCompressed(file));
			} else
			{
				// If the file doesn't exist, create a new one and save the default warps.
				saveClient(data);
			}
		} catch (IOException e)
		{
			// Log any errors that occur.
			WarpMod.log.error("Unable to read warp save file: {}", e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * Saves the client warp data.
	 */
	public static void saveClient()
	{
		// Get the current client, server data, and local player.
		Minecraft client = Minecraft.getInstance();
		ServerData serverData = client.getCurrentServer();
		LocalPlayer player = client.player;
		// Save the player's warps.
		WarpManager.fromPlayer(player).saveClient(serverData);
	}
	
	/**
	 * Loads the client warp data.
	 */
	public static void loadClient()
	{
		// Get the current client, server data, and local player.
		Minecraft client = Minecraft.getInstance();
		ServerData serverData = client.getCurrentServer();
		LocalPlayer player = client.player;
		// Load the player's warps.
		WarpManager.fromPlayer(player).loadClient(serverData);
	}
	
}
