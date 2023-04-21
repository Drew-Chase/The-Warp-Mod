package chase.minecraft.architectury.warpmod.mixin;

import chase.minecraft.architectury.warpmod.data.Warp;
import chase.minecraft.architectury.warpmod.data.Warps;
import chase.minecraft.architectury.warpmod.utils.WorldUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
@SuppressWarnings("all")
public class PlayerMixin
{
	
	@Inject(at = @At("RETURN"), method = "addAdditionalSaveData")
	public void addAdditionalSaveData(CompoundTag compoundTag, CallbackInfo cbi)
	{
		ServerPlayer player = (ServerPlayer) ((Object) this);
		compoundTag.put("warps", Warps.fromPlayer(player).toNbt());
	}
	
	@Inject(at = @At("RETURN"), method = "readAdditionalSaveData")
	public void readAdditionalSaveData(CompoundTag compoundTag, CallbackInfo cbi)
	{
		ServerPlayer player = (ServerPlayer) ((Object) this);
		Warps.fromPlayer(player).fromNbt(compoundTag);
	}
	
	@Inject(at = @At("HEAD"), method = "changeDimension")
	public void changeDimension(ServerLevel serverLevel, CallbackInfoReturnable<Entity> cbi)
	{
		ServerPlayer player = (ServerPlayer) ((Object) this);
		WorldUtils.removeTravelBar(player);
	}
	
	@Inject(at = @At("HEAD"), method = "disconnect")
	public void disconnect(CallbackInfo cbli)
	{
		ServerPlayer player = (ServerPlayer) ((Object) this);
		WorldUtils.removeTravelBar(player);
	}
	
	@Inject(at = @At("HEAD"), method = "die", cancellable = true)
	public void die(DamageSource damageSource, CallbackInfo info)
	{
		info.cancel();
		ServerPlayer player = (ServerPlayer) ((Object) this);
		Warps.fromPlayer(player).createDeath();
		WorldUtils.removeTravelBar(player);
	}
	
	@Inject(at = @At("RETURN"), method = "tick")
	public void tick(CallbackInfo cb)
	{
		ServerPlayer player = (ServerPlayer) ((Object) this);
		Warps warps = Warps.fromPlayer(player);
		for (Warp warp : warps.getWarps())
		{
			if (warp.temporary() && warp.distance() < 10)
			{
				warps.remove(warp.getName());
			}
		}
	}
	
}
