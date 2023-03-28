package chase.minecraft.architectury.warpmod.mixin;

import chase.minecraft.architectury.warpmod.data.Warps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class PlayerMixin {

    @Inject(at = @At("RETURN"), method = "addAdditionalSaveData")
    public void addAdditionalSaveData(CompoundTag compoundTag, CallbackInfo cbi) {
        ServerPlayer player = (ServerPlayer) ((Object) this);
        compoundTag.put("warps", Warps.fromPlayer(player).toNBT());
    }

    @Inject(at = @At("RETURN"), method = "readAdditionalSaveData")
    public void readAdditionalSaveData(CompoundTag compoundTag, CallbackInfo cbi) {
        ServerPlayer player = (ServerPlayer) ((Object) this);
        Warps.fromPlayer(player).fromNBT(compoundTag);
    }

}
