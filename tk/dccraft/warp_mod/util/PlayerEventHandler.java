package tk.dccraft.warp_mod.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Handles various player events
 * 
 * @author Drew Chase
 *
 */
@SuppressWarnings("all")
@EventBusSubscriber
public class PlayerEventHandler extends WarpUtilities {

	/**
	 * Creates a back warp on player death
	 * 
	 * @param event
	 * @author Drew Chase
	 */
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onPlayerDeath(LivingDeathEvent event) {
		if (event.getEntity() instanceof EntityPlayer) {
			event.setCanceled(true);
			EntityPlayerMP player = (EntityPlayerMP) event.getEntity();
			setPlayer(player);
			forceImport(player);
			BlockPos pos = player.getPosition();
			int dimension = player.getEntityWorld().provider.getDimension();
			float yaw = player.cameraYaw;
			float pitch = player.cameraPitch;
			back(pos, yaw, pitch, dimension, player);
			getWarps(player);
			event.setCanceled(false);
		}
	}

}
