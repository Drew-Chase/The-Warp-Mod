# The Warp Mod

## About
The warp mod is a forge port of my bucket plugin.<br>
The mod allows the creation of warp points or waypoints that you can teleport to.<br>
warps are created and maintained with commands.

## Commands
### Warp Command
```/warp set <name> [-p]``` Creates a warp at your current position.  Add ```-p``` to create a public warp.<br>
```/warp remove <name>``` Deletes the warp <name>.<br>
```/warp list [-p]``` Lists all warp names.   Add ```-p``` to only list public warps<br>
```/warp map [-p]``` Lists all warp names with coordinates. Add ```-p``` to only show public warps<br>
```/warp invite <player> <warp name>``` Shares a warp from your private collection to another player<br>
```/warp me <player>```  Teleports you to a player.<br>
```/warp <player> me``` Teleports a player to you.<br>
```/warp back```  When you teleport using warp or when you die a "warp back" is automatically created.<br>
```/warp random [max-distance]```  Warps a random distance away, if a max distance is set it wont go beyond that.  Default Max Distance is 600 blocks.  **!!!WARNING - Could kill your server!!!<br>**

### Warp-Config Command
```/warp-config add <config-setting> <player>```  Adds a Permission to a Player.<br>
```/warp-config set <config-setting> <true/false>```  Sets a boolean (true/false) value.<br>
```/warp-config get <config-setting>```  Gets the Current Status of the Setting.<br>
EX: ```/warp-config get allowed-players```   This will return all players allowed to use warp mod or if no players then "No Players Are Allowed!" will appear.<br>
EX #2: ```/warp-config set public-allowed true``` This will set if public warps are allowed.<br>

## Config File
``` ini
#All Configurations can be controlled with-in the game using the "/warp-config" Command
#You can add your username below under "players allowed to change config:" section or in server console with the "/warp-config add config-editors LittleBilly101"

#Sets if public warps are allowed.
#Public warps are created by the allowed player typing /warp set NAME_OF_WARP -p
public-warps-allowed:false

#Sets players that are allowed to create public warps.
#Ex: (allowed-players-public:["LittleBilly101", "LittleBilly102"])
#You can also use * to signify all players are allowed
#Ex (allowed-players:["*"])
allowed-players-public:[]

#Sets if the Mod verbosly states all of its moves
debug-mode:true

#Are All Players Allowed to use the Mod
#Ex: (allowed-players:["LittleBilly101", "LittleBilly102"]
#You can also use * to signify all players allowed
#Ex (allowed-players:["*"])
allowed-players:["*"]

#Players allowed to change the config within game using /warp-config command
#The * Wildcard can NOT be used in this situation
#Ex (players allowed to change config:["LittleBilly101]"
players allowed to change config:[]

```
