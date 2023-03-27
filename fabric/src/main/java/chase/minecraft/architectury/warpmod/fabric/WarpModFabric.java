package chase.minecraft.architectury.warpmod.fabric;

import chase.minecraft.architectury.warpmod.WarpMod;
import net.fabricmc.api.ModInitializer;

public class WarpModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        WarpMod.init();
    }
}