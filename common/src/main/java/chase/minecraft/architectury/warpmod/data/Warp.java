package chase.minecraft.architectury.warpmod.data;

import chase.minecraft.architectury.warpmod.WarpMod;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;

/**
 * Warp object holds name, position, and player, and add it to the player's warps.
 */
public class Warp {

    private final String _name;
    private final float _x, _y, _z, _yaw, _pitch;
    private final ServerPlayer _player;
    private ServerLevel _dimension;

    private Warp(String name, float x, float y, float z, float yaw, float pitch, ServerPlayer player) {
        _x = x;
        _y = y;
        _z = z;
        _yaw = yaw;
        _pitch = pitch;
        _name = name;
        _player = player;
        _dimension = player.getLevel();
    }

    private Warp(String name, float x, float y, float z, float yaw, float pitch, ServerPlayer player, ServerLevel level) {
        this(name, x, y, z, yaw, pitch, player);
        _dimension = level;
    }

    private Warp(String name, float x, float y, float z, float yaw, float pitch, ServerPlayer player, ResourceLocation level) {
        this(name, x, y, z, yaw, pitch, player);
        for (ServerLevel l : Objects.requireNonNull(player.getServer()).getAllLevels()) {
            if (l.dimension().location().equals(level)) {
                _dimension = l;
            }
        }
    }

    /**
     * Returns the dimension that the warp resides in.
     *
     * @return The dimension
     */
    public ServerLevel getLevel() {
        return _dimension;
    }

    /**
     * Returns the resource location of the dimension
     *
     * @return The location of the dimension.
     */
    public ResourceLocation getLevelResourceLocation() {
        WarpMod.log.info(String.format("LOCATION IS: %s", _dimension.dimension().location()));
        return _dimension.dimension().location();
    }

    /**
     * Gets the name of the warp
     *
     * @return the warp name
     */
    public String getName() {
        return _name;
    }

    /**
     * Gets the X coordinate of the warp
     *
     * @return the x coordinate
     */
    public float getX() {
        return _x;
    }

    /**
     * Gets the Y coordinate of the warp.
     *
     * @return the y coordinate
     */
    public float getY() {
        return _y;
    }

    /**
     * Gets the z coordinate of the warp
     *
     * @return the z coordinate
     */
    public float getZ() {
        return _z;
    }

    /**
     * Gets the players head yaw.
     *
     * @return yaw
     */
    public float getYaw() {
        return _yaw;
    }

    /**
     * Gets the players head pitch
     *
     * @return pitch
     */
    public float getPitch() {
        return _pitch;
    }


    /**
     * Gets the <b>BlockPos</b> of the warp
     *
     * @return the <b>BlockPos</b>
     */
    public BlockPos getPos() {
        return new BlockPos((int) _x, (int) _y, (int) _z);
    }

    /**
     * Gets the <b>ServerPlayer</b> that owns the warp.
     *
     * @return the <b>ServerPlayer</b>
     */
    public ServerPlayer getPlayer() {
        return _player;
    }

    /**
     * It returns a CompoundTag that contains the name, x, y, z, and dimension of the entity
     *
     * @return A CompoundTag
     */
    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("name", getName());
        tag.putFloat("x", ((int) getX()) + 0.5f);
        tag.putFloat("y", (int) getY());
        tag.putFloat("z", (getZ() + 0.5f));
        tag.putFloat("yaw", getYaw());
        tag.putFloat("pitch", getPitch());
        tag.putString("dim", getLevelResourceLocation().toString());
        return tag;
    }

    /**
     * Teleports the player to the warp location.
     */
    public void teleportTo() {
        _player.teleportTo(getLevel(), getX(), getY(), getZ(), getPitch(), getYaw());
        Warps.fromPlayer(_player).createAddOrUpdate(createBack(_player));
    }

    public static Warp createBack(ServerPlayer player) {
        return Warp.create("back", player.getBlockX() + .5f, player.getBlockY(), player.getBlockZ() + .5f, player.getYRot(), player.getXRot(), player);
    }

    /**
     * It takes a CompoundTag, and a ServerPlayer, and returns a Warp
     *
     * @param tag    The tag that was sent from the client.
     * @param player The player who is warping.
     * @return A Warp object
     */
    public static Warp fromTag(CompoundTag tag, ServerPlayer player) {
        String name = tag.getString("name");
        float x = tag.getFloat("x");
        float y = tag.getFloat("y");
        float z = tag.getFloat("z");
        float yaw = tag.getFloat("yaw");
        float pitch = tag.getFloat("pitch");
        ResourceLocation dim = new ResourceLocation(tag.getString("dim")); // minecraft:the_nether
        return new Warp(name, x, y, z, yaw, pitch, player, dim);
    }

    /**
     * Create a new warp with the given name, position, and player, and add it to the player's warps.
     *
     * @param name   The name of the warp.
     * @param x      The x coordinate of the warp
     * @param y      The y coordinate of the warp.
     * @param z      The z coordinate of the warp.
     * @param player The player who created the warp
     * @return A new warp object
     */
    public static Warp create(String name, float x, float y, float z, float yaw, float pitch, ServerPlayer player) {
        Warp warp = new Warp(name, x, y, z, yaw, pitch, player);
        Warps playersWarps = Warps.fromPlayer(player);
        playersWarps.createAddOrUpdate(warp);
        return warp;
    }

    /**
     * Create a new warp with the given name, position, and player, and add it to the player's warps
     *
     * @param name   The name of the warp.
     * @param x      The x coordinate of the warp
     * @param y      The y coordinate of the warp.
     * @param z      The z coordinate of the warp
     * @param player The player who created the warp
     * @param level  The level the warp is in.
     * @return A new Warp object
     */
    public static Warp create(String name, float x, float y, float z, float yaw, float pitch, ServerPlayer player, ResourceLocation level) {
        Warp warp = new Warp(name, x, y, z, yaw, pitch, player, level);
        Warps playersWarps = Warps.fromPlayer(player);
        playersWarps.createAddOrUpdate(warp);
        return warp;
    }

    /**
     * "Create a new warp with the given name, position, and player, and add it to the player's warps."
     * <p>
     * The first thing we do is create a new Warp object with the given name, position, and player
     *
     * @param name   The name of the warp.
     * @param x      The x coordinate of the warp
     * @param y      The y coordinate of the warp
     * @param z      The z coordinate of the warp.
     * @param player The player who created the warp
     * @param level  The level the warp is in
     * @return A new Warp object.
     */
    public static Warp create(String name, float x, float y, float z, float yaw, float pitch, ServerPlayer player, ServerLevel level) {
        Warp warp = new Warp(name, x, y, z, yaw, pitch, player, level);
        Warps playersWarps = Warps.fromPlayer(player);
        playersWarps.createAddOrUpdate(warp);
        return warp;
    }

}
