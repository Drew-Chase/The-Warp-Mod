package chase.minecraft.architectury.warpmod.fabric.mixin;

import chase.minecraft.architectury.warpmod.client.WarpModClient;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(ServerPlayer.class)
public class PlayerMixin
{
	
	@Inject(at = @At("HEAD"), method = "changeDimension")
	public void changeDimension(ServerLevel serverLevel, CallbackInfoReturnable<Entity> cbi)
	{
		ServerPlayer player = (ServerPlayer) ((Object) this);
		WarpModClient.changeDimension(player, serverLevel.dimension().location());
	}
}
