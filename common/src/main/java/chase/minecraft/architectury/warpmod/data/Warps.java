package chase.minecraft.architectury.warpmod.data;

import chase.minecraft.architectury.warpmod.data.enums.WarpCreationResponseType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public class Warps {
    private List<Warp> _warps;

    private Warps(ServerPlayer player) {
        _warps = new ArrayList<Warp>();
    }

    public static Warps fromPlayer(ServerPlayer player) {
        return new Warps(player);
    }

    public WarpCreationResponseType createAndAdd(Warp warp) {
        for (Warp w : _warps) {
            if (w.getName().equals(warp.getName())) {
                return WarpCreationResponseType.FailureDueToDuplicate;
            }
        }
        _warps.add(warp);
        return WarpCreationResponseType.Success;
    }

    public Warp[] GetWarps() {
        return _warps.toArray(new Warp[0]);
    }


    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        return tag;
    }

}
