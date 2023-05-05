# WarpManager Class

The `WarpManager` class is a utility class that manages warps for players in a Minecraft server, allowing them to create, update, remove, and retrieve warp locations.

## Constructor

The `WarpManager` class constructor takes a `Player` object as a parameter. It initializes the `_player` field with the passed-in player object. If the `_instance` `HashMap` is null, it creates a new `HashMap`. If the `_instance` `HashMap` already contains the player object, it sets the `_warps` field to the `_warps` field of the existing `WarpManager` object associated with the player. Otherwise, it creates a new `HashMap` for the `_warps` field and adds the new `WarpManager` object to the `_instance` `HashMap` with the player object as the key.

## Methods

- `createOrUpdate(Warp warp)`: If the warp exists, overwrite it, otherwise add it. Returns a `WarpCreationResponseType` enum.
- `rename(String name, String new_name)`: Renames a warp with the given name to the new name.
- `remove(String name)`: If the warp exists, remove it from the list. Returns a boolean value.
- `getWarps()`: Get all the warps in the plugin and return them as an array.
- `getWarpNames()`: Returns an array of all the warp names in the plugin.
- `suggestions(SuggestionsBuilder builder)`: Returns a CompletableFuture that completes with a Suggestions object containing all the warps in the server.
- `get(String name)`: It gets a warp by name.
- `exists(String name)`: Returns true if the warp exists, false if it doesn't.
- `updateShareMethod(ClientServerWarpShareMethod method)`: Updates the share method used in the Client-Server warp communication.
- `toNbt()`: Converts all loaded warps to NBTData that can be saved! Returns NBTData.
- `fromNbt(CompoundTag tag)`: Gets warps from PlayerNBT.
- `createBack()`: Creates a back warp.
- `createDeath()`: Creates a death warp.
- `getDeathpoints()`: Returns an array of all deathpoints in the list of warps.
- `saveClient(ServerData data)`: Saves the client warp data to a file.
- `loadClient(ServerData data)`: Loads the client warp data from a file.
- `saveClient()`: Saves the client warp data.
- `loadClient()`: Loads the client warp data.

The WarpManager class is a utility class that manages warps for players in a Minecraft server. It allows players to create, update, remove, and retrieve warp locations. To use this class, you can follow the steps below:

1. Create a new instance of the class by calling the static method `fromPlayer(Player player)` and passing in the player object. This will return a WarpManager object for the player.

2. To create a new warp, create an instance of the Warp class with the necessary parameters and call the `createOrUpdate(Warp warp)` method of the WarpManager object. This method will return a WarpCreationResponseType enum indicating whether the warp was created successfully, overwritten, or failed to create.

3. You can update an existing warp by calling the `createOrUpdate(Warp warp)` method with the updated warp object.

4. To remove a warp, call the `remove(String name)` method of the WarpManager object and pass in the name of the warp. This method will return a boolean value indicating whether the warp was successfully removed.

5. To retrieve a warp, call the `get(String name)` method of the WarpManager object and pass in the name of the warp. This method will return a Warp object.

6. You can check whether a warp exists by calling the `exists(String name)` method of the WarpManager object and passing in the name of the warp. This method will return a boolean value.

7. To rename a warp, call the `rename(String name, String new_name)` method of the WarpManager object and pass in the original name of the warp and the new name.

8. To get a list of all the warps for a player, call the `getWarps()` method of the WarpManager object. This method will return an array of Warp objects.

9. To get the names of all the warps for a player, call the `getWarpNames()` method of the WarpManager object. This method will return an array of strings.

10. To get a suggestion list of all the warps for a player, call the `suggestions(SuggestionsBuilder builder)` method of the WarpManager object and pass in a SuggestionsBuilder object. This method will return a CompletableFuture<Suggestions> object containing the suggestion list.

11. To update the share method used in the Client-Server warp communication, call the `updateShareMethod(ClientServerWarpShareMethod method)` method of the WarpManager object and pass in the new share method.

12. To save the client warp data to a file, call the `saveClient(ServerData data)` method of the WarpManager object and pass in the current server data. This method will write

here's an example of how to use the WarpManager class to create, update, and remove player warps:

```java
// Get the current player
Player player = ...;

// Create a new WarpManager instance for the player
WarpManager warpManager = WarpManager.fromPlayer(player);

// Create a new warp
Warp warp = new Warp("My Warp", 123, 45, 678, 0, 0, "my_dimension", player, false, WaypointIcons.DEFAULT, WaypointColor.RED, false);

// Add the warp to the WarpManager
warpManager.createOrUpdate(warp);

// Update an existing warp
warp.update("My Updated Warp", 321, 54, 876, 0, 0, "my_dimension", WaypointColor.GREEN);

// Remove a warp
warpManager.remove("My Warp");
```

This code creates a new WarpManager instance for a player, creates a new warp with some default values, adds the warp to the WarpManager, updates an existing warp with new values, and finally removes a previously added warp.   
**Note:** *that this is just an example and should be tailored to suit your specific use case.*