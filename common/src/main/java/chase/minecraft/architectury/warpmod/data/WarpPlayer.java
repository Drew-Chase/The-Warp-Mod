package chase.minecraft.architectury.warpmod.data;

import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class WarpPlayer extends ServerPlayer {
    public WarpPlayer(MinecraftServer minecraftServer, ServerLevel serverLevel, GameProfile gameProfile) {
        super(minecraftServer, serverLevel, gameProfile);
    }
}
