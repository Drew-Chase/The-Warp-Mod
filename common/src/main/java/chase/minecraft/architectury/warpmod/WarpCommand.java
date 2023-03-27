package chase.minecraft.architectury.warpmod;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class WarpCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("foo")
                .then(
                        argument("bar", integer())
                                .executes(c -> {
                                    c.getSource().sendSuccess(Component.literal(String.format("bar: %d", getInteger(c, "bar"))), true);
                                    return 1;
                                })
                )
                .executes(c -> {
                    c.getSource().sendFailure(Component.literal("Called with no arguments"));
                    return 0;
                }));
    }
}
