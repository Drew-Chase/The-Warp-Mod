package tk.dccraft.warp_mod.commands.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;
/**
 * A Custom interdimensional teleporting class
 * @author Drew Chase
 *
 */
public class Teleport extends Teleporter {
	private final WorldServer world;
	private double x, y, z;
	private float yaw, pitch;

	/**
	 * Gathers information for teleportation
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 * @param yaw
	 * @param pitch
	 * @author Drew Chase
	 */
	public Teleport(WorldServer world, double x, double y, double z, float yaw, float pitch) {
		super(world);
		this.world = world;
		this.y = y;
		this.x = x;
		this.z = z;
		this.yaw = yaw;
		this.pitch = pitch;
	}

	@Override
	public void placeInPortal(Entity entity, float rotationYaw) {
		this.world.getBlockState(new BlockPos((int) this.x, (int) this.y, (int) this.z));
		// entity.setPosition(this.x, this.y, this.z);
		entity.setPositionAndRotation(this.x, this.y, this.z, this.yaw, this.pitch);
		entity.motionX = 0.0f;
		entity.motionY = 0.0f;
		entity.motionZ = 0.0f;
	}

	/**
	 * Teleports a player to a specific position in a specific dimension
	 * @param player
	 * @param dimension
	 * @param x
	 * @param y
	 * @param z
	 * @param yaw
	 * @param pitch
	 * @author Drew Chase
	 */
	public static void teleportToDimension(EntityPlayer player, int dimension, double x, double y, double z, float yaw, float pitch) {
		int oldDimension = player.getEntityWorld().provider.getDimension();
		EntityPlayerMP entityPlayerMP = (EntityPlayerMP) player;
		MinecraftServer server = player.getEntityWorld().getMinecraftServer();
		WorldServer worldServer = server.getWorld(dimension);

		if (oldDimension == dimension) {
			player.setPositionAndUpdate(x, y, z);
			player.setPositionAndRotation(x, y, z, yaw, pitch);
			return;
		}

		if (worldServer == null || server == null)
			throw new IllegalArgumentException("Dimension: " + dimension + " not found");
		worldServer.getMinecraftServer().getPlayerList().transferPlayerToDimension(entityPlayerMP, dimension, new Teleport(worldServer, x, y, z, yaw, pitch));
		player.setPositionAndUpdate(x, y, z);
		player.setPositionAndRotation(x, y, z, yaw, pitch);
	}

}
