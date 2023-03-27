package chase.minecraft.architectury.warpmod.forge;

import dev.architectury.platform.forge.EventBuses;
import chase.minecraft.architectury.warpmod.WarpMod;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(WarpMod.MOD_ID)
public class WarpModForge {
    public WarpModForge() {
        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(WarpMod.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        WarpMod.init();
    }
}