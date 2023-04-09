package chase.minecraft.architectury.warpmod.mixin;

import chase.minecraft.architectury.warpmod.data.Warp;
import chase.minecraft.architectury.warpmod.data.Warps;
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
		compoundTag.put("warps", Warps.fromPlayer(player).toNBT());
	}
	
	@Inject(at = @At("RETURN"), method = "readAdditionalSaveData")
	public void readAdditionalSaveData(CompoundTag compoundTag, CallbackInfo cbi)
	{
		ServerPlayer player = (ServerPlayer) ((Object) this);
		Warps.fromPlayer(player).fromNBT(compoundTag);
	}
	
	@Inject(at = @At("HEAD"), method = "changeDimension")
	public void changeDimension(ServerLevel serverLevel, CallbackInfoReturnable<Entity> cbi)
	{
		ServerPlayer player = (ServerPlayer) ((Object) this);
		Warp.removeTravelBar(player);
	}
	
	@Inject(at = @At("HEAD"), method = "disconnect")
	public void disconnect(CallbackInfo cbli)
	{
		ServerPlayer player = (ServerPlayer) ((Object) this);
		Warp.removeTravelBar(player);
	}
	
	@Inject(at = @At("HEAD"), method = "die")
	public void die(DamageSource damageSource, CallbackInfo info)
	{
		ServerPlayer player = (ServerPlayer) ((Object) this);
		Warp.createBack(player);
		Warp.removeTravelBar(player);
	}
	
}
