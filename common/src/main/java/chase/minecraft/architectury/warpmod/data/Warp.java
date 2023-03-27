package chase.minecraft.architectury.warpmod.data;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

public class Warp {

    private String _name;
    private float _x, _y, _z;
    private ServerPlayer _player;

    private Warp(String name, float x, float y, float z, ServerPlayer player) {
        _x = x;
        _y = y;
        _z = z;
        _name = name;
        _player = player;
    }

    public String getName() {
        return _name;
    }

    public float getX() {
        return _x;
    }

    public float getY() {
        return _y;
    }

    public float getZ() {
        return _z;
    }

    public BlockPos getPos() {
        return new BlockPos((int) _x, (int) _y, (int) _z);
    }

    public static Warp create(String name, float x, float y, float z, ServerPlayer player) {
        Warp warp = new Warp(name, x, y, z, player);
        Warps playersWarps = Warps.fromPlayer(player);
        return warp;
    }

}
