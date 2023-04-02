package chase.minecraft.architectury.warpmod.data;

import chase.minecraft.architectury.warpmod.WarpMod;
import chase.minecraft.architectury.warpmod.data.enums.WarpCreationResponseType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
@SuppressWarnings("unused")
public class Warps {
    private static HashMap<ServerPlayer, Warps> _instance;
    private final HashMap<String, Warp> _warps;
    private final ServerPlayer _player;

    private Warps(ServerPlayer player) {
        _player = player;
        if (_instance == null) {
            _instance = new HashMap<>();
        }
        if (_instance.containsKey(player)) {
            _warps = _instance.get(player)._warps;
        } else {
            _warps = new HashMap<>();
            _instance.put(player, this);
        }
    }

    /**
     * This function returns a new Warps object with the player's UUID.
     *
     * @param player The player who is warping.
     * @return A new instance of the Warps class.
     */
    public static Warps fromPlayer(ServerPlayer player) {
        return new Warps(player);
    }

    /**
     * If the warp exists, overwrite it, otherwise add it
     *
     * @param warp The warp to add or update.
     * @return A WarpCreationResponseType enum.
     */
    public WarpCreationResponseType createAddOrUpdate(Warp warp) {
        if (_warps.containsKey(warp.getName())) {
            _warps.put(warp.getName(), warp);
            return WarpCreationResponseType.Overwritten;
        }
        _warps.put(warp.getName(), warp);
        return WarpCreationResponseType.Success;
    }

    /**
     * If the warp exists, remove it from the list
     *
     * @param name The name of the warp to remove.
     * @return A boolean value.
     */
    public boolean Remove(String name) {
        if (Exists(name)) {
            _warps.remove(name);
            return true;
        }
        return false;
    }

    /**
     * Get all the warps in the plugin and return them as an array.
     *
     * @return An array of Warp objects.
     */
    public Warp[] GetWarps() {
        return _warps.values().toArray(new Warp[0]);
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
    public CompletableFuture<Suggestions> GetSuggestions(SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(_warps.keySet(), builder);
    }

    /**
     * It gets a warp by name
     *
     * @param name The name of the warp.
     * @return A warp object.
     */
    public Warp Get(String name) {
        return _warps.get(name);
    }

    /**
     * Returns true if the warp exists, false if it doesn't.
     *
     * @param name The name of the warp.
     * @return A boolean value.
     */
    public boolean Exists(String name) {
        return _warps.containsKey(name);
    }

    /**
     * Converts all loaded warps to NBTData that can be saved!
     *
     * @return NBTData
     */
    public ListTag toNBT() {
        ListTag listTag = new ListTag();
        for (Warp warp : _warps.values()) {
            listTag.add(warp.toNBT());
        }
        return listTag;
    }

    /**
     * Gets warps from PlayerNBT
     *
     * @param tag the players nbt
     */
    public void fromNBT(CompoundTag tag) {
        _warps.clear();
        try {
            ListTag listTag = tag.getList("warps", CompoundTag.TAG_COMPOUND);
            WarpMod.log.info(String.format("Loading NBT for player: %s - %d", _player.getDisplayName().getString(), listTag.size()));
            for (int i = 0; i < listTag.size(); i++) {
                CompoundTag ct = listTag.getCompound(i);
                Warp warp = Warp.fromTag(ct, _player);
                _warps.put(warp.getName(), warp);
            }
        } catch (Exception e) {
            WarpMod.log.error(String.format("Unable to load Player Warp NBT: %s", e.getMessage()));
        }
        WarpMod.log.info(String.format("%d Warps found for %s", _warps.size(), _player.getDisplayName().getString()));
    }

}
