package com.oskarsmc.broadcast.command;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.standard.StringArgument;
import com.oskarsmc.broadcast.Broadcast;
import com.oskarsmc.broadcast.util.JSONUtils;
import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bstats.charts.AdvancedPie;
import org.bstats.charts.SingleLineChart;
import org.bstats.velocity.Metrics;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class BroadcastCommand {
    private Broadcast plugin;
    private AtomicInteger broadcasts = new AtomicInteger(0);
    private ConcurrentHashMap<String, Integer> broadcastTypes = new ConcurrentHashMap<String, Integer>();

    enum BroadcastType {
        RAW,
        MINIMESSAGE,
        TEXT
    }

    public BroadcastCommand(Broadcast plugin) {
        this.plugin = plugin;

        Command.Builder<CommandSource> builder = plugin.commandManager.commandBuilder("broadcast");

        CommandArgument.Builder<CommandSource, String> minimessageBuilder = StringArgument.<CommandSource>newBuilder("minimessage")
                .withParser((context, inputQueue) -> {
                    String input = inputQueue.peek();

                    if (input == null) {
                        return ArgumentParseResult.failure(new IllegalArgumentException("Your MiniMessage string was null."));
                    }

                    try {
                        Component component = MiniMessage.get().parse(input);
                        return ArgumentParseResult.success(input);
                    } catch (Exception e) {
                        return ArgumentParseResult.failure(e);
                    }
                });

        plugin.commandManager.command(builder.literal("minimessage", ArgumentDescription.of("Broadcast a minimessage string."))
                .permission("osmc.broadcast.minimessage")
                .argument(minimessageBuilder, ArgumentDescription.of("MiniMessage String"))
                .handler(context -> {
                    broadcastComponent(MiniMessage.get().parse(context.get("minimessage")), BroadcastType.MINIMESSAGE);
                })
        );

        CommandArgument.Builder<CommandSource, String> rawBuilder = StringArgument.<CommandSource>newBuilder("json")
                .withParser((context, inputQueue) -> {
                    String input = inputQueue.peek();

                    if (input == null) {
                        return ArgumentParseResult.failure(new IllegalArgumentException("The JSON you provided was null."));
                    }

                    if (JSONUtils.isJSONValid(input)) {
                        return ArgumentParseResult.success(input);
                    }

                    return ArgumentParseResult.failure(new IllegalArgumentException("The JSON you provided was not valid."));
                });

        plugin.commandManager.command(builder.literal("raw", ArgumentDescription.of("Broadcast raw JSON"))
                .argument(rawBuilder, ArgumentDescription.of("The JSON to broadcast."))
                .permission("osmc.broadcast.raw")
                .handler(context -> {
                    broadcastComponent(GsonComponentSerializer.gson().deserialize(context.get("json")), BroadcastType.RAW);
                })
        );

        plugin.commandManager.command(builder.literal("text", ArgumentDescription.of("Broadcast text"))
                .argument(StringArgument.greedy("text"), ArgumentDescription.of("The text to broadcast."))
                .permission("osmc.broadcast.text")
                .handler(context -> {
                    broadcastComponent(Component.text((String) context.get("text")), BroadcastType.TEXT);
                })
        );
    }

    public void broadcastComponent(Component component, BroadcastType type) {
        plugin.proxyServer.sendMessage(component);
    }

    private void metrics(Metrics metrics) {

        metrics.addCustomChart(new SingleLineChart("broadcasts", new Callable<Integer>() {
            @Override
            public Integer call() {
                int broadcasts = BroadcastCommand.this.broadcasts.get();
                BroadcastCommand.this.broadcasts.set(0);
                return broadcasts;
            }
        }));

        metrics.addCustomChart(new AdvancedPie("broadcast_type", new Callable<Map<String, Integer>>() {
            @Override
            public Map<String, Integer> call() {
                Map<String, Integer> map = BroadcastCommand.this.broadcastTypes;
                BroadcastCommand.this.broadcastTypes.clear();
                return map;
            }
        }));
    }
}
