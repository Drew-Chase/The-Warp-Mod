package chase.minecraft.architectury.warpmod;

import dev.architectury.event.events.common.CommandRegistrationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
@SuppressWarnings("all")
public class WarpMod {
    public static final String MOD_ID = "warpmod";
    public static Logger log = LogManager.getLogger("WarpMod");

    public static void init() {
        CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) -> {
            WarpCommand.register(dispatcher);
        });
    }
}