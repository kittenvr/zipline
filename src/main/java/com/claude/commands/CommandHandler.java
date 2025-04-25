package com.claude.commands;

import com.claude.Zipline;
import com.claude.pathfinding.BaritoneIntegration;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

public class CommandHandler {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("telepath")
                .then(ClientCommandManager.argument("x", StringArgumentType.word())
                        .then(ClientCommandManager.argument("y", StringArgumentType.word())
                                .then(ClientCommandManager.argument("z", StringArgumentType.word())
                                        .executes(context -> {
                                            int x = Integer.parseInt(StringArgumentType.getString(context, "x"));
                                            int y = Integer.parseInt(StringArgumentType.getString(context, "y"));
                                            int z = Integer.parseInt(StringArgumentType.getString(context, "z"));

                                            BlockPos target = new BlockPos(x, y, z);
                                            Zipline.LOGGER.info("Calculating path to " + target);

                                            BaritoneIntegration baritone = new BaritoneIntegration();
                                            Zipline.EXECUTOR.executePath(baritone.calculatePath(target));

                                            return 1;
                                        })))));
    }
}
