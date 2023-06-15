package chase.minecraft.architectury.warpmod.mixin;

import chase.minecraft.architectury.warpmod.data.Warp;
import chase.minecraft.architectury.warpmod.data.WarpManager;
import chase.minecraft.architectury.warpmod.utils.WorldUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
@SuppressWarnings("all")
public class PlayerMixin
{
	
	@Inject(at = @At("RETURN"), method = "addAdditionalSaveData")
	public void addAdditionalSaveData(CompoundTag compoundTag, CallbackInfo cbi)
	{
		ServerPlayer player = (ServerPlayer) ((Object) this);
		compoundTag.put("warps", WarpManager.fromPlayer(player).toNbt().getList("warps", ListTag.TAG_COMPOUND));
	}
	
	@Inject(at = @At("RETURN"), method = "readAdditionalSaveData")
	public void readAdditionalSaveData(CompoundTag compoundTag, CallbackInfo cbi)
	{
		ServerPlayer player = (ServerPlayer) ((Object) this);
		ListTag warps = compoundTag.getList("warps", ListTag.TAG_COMPOUND);
		if (warps.size() != 0)
		{
			WarpManager.fromPlayer(player).fromNbt(compoundTag);
		}
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
		WarpManager.fromPlayer(player).createDeath();
		WorldUtils.removeTravelBar(player);
	}
	
	@Inject(at = @At("RETURN"), method = "tick")
	public void tick(CallbackInfo cb)
	{
		ServerPlayer player = (ServerPlayer) ((Object) this);
		WarpManager warpManager = WarpManager.fromPlayer(player);
		for (Warp warp : warpManager.getWarps())
		{
			if (warp.temporary() && warp.distance() < 10)
			{
				warpManager.remove(warp.getName());
			}
		}
	}
	
}
