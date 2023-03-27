package chase.minecraft.architectury.warpmod;

import dev.architectury.event.events.common.CommandRegistrationEvent;

public class WarpMod {
    public static final String MOD_ID = "warpmod";

    public static void init() {
        CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) -> {
            WarpCommand.register(dispatcher);
        });
    }
}