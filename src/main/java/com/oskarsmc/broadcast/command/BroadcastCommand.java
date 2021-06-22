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
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bstats.charts.SingleLineChart;
import org.bstats.velocity.Metrics;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

public class BroadcastCommand {
    private Broadcast plugin;
    private AtomicInteger broadcasts = new AtomicInteger(0);

    enum BroadcastType {
        RAW,
        MINIMESSAGE,
        TEXT
    }

    public BroadcastCommand(Broadcast plugin) {
        this.plugin = plugin;

        Command.Builder<CommandSource> builder = plugin.commandManager.commandBuilder("broadcast");

        CommandArgument.Builder<CommandSource, String> minimessageBuilder = StringArgument.<CommandSource>newBuilder("minimessage")
                .greedy()
/*                .withParser((context, inputQueue) -> {
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
                })*/;

        plugin.commandManager.command(builder.literal("minimessage", ArgumentDescription.of("Broadcast a minimessage string."))
                .permission("osmc.broadcast.minimessage")
                .argument(minimessageBuilder.build(), ArgumentDescription.of("MiniMessage String"))
                .handler(context -> {
                    broadcastComponent(MiniMessage.get().parse(context.get("minimessage")), BroadcastType.MINIMESSAGE);
                })
        );

        CommandArgument.Builder<CommandSource, String> rawBuilder = StringArgument.<CommandSource>newBuilder("json")
                .greedy()
/*                .withParser((commandContext, inputQueue) -> {
                    String input = inputQueue.peek();
                    System.out.println("hello?");
                    System.out.println(inputQueue);
                    System.out.println(input);

                    if (input == null) {
                        return ArgumentParseResult.failure(new IllegalArgumentException("The JSON you provided was null."));
                    }

                    try {
                        GsonComponentSerializer.gson().deserialize(input);
                        return ArgumentParseResult.success(input);
                    } catch (Exception e) {
                        return ArgumentParseResult.failure(e);
                    }
                })*/;

        plugin.commandManager.command(builder.literal("raw", ArgumentDescription.of("Broadcast raw JSON"))
                .argument(rawBuilder.build(), ArgumentDescription.of("The JSON to broadcast."))
                .permission("osmc.broadcast.raw")
                .handler(context -> {
                    try {
                        broadcastComponent(GsonComponentSerializer.gson().deserialize(context.get("json")), BroadcastType.RAW);
                    } catch (Exception e) { //TODO: Don't use this workaround!
                        context.getSender().sendMessage(Component.text("Error while sending your broadcast (likely a JSON formatting issue!): " + e.getMessage(), NamedTextColor.RED));
                    }
                })
        );

        plugin.commandManager.command(builder.literal("text", ArgumentDescription.of("Broadcast text"))
                .argument(StringArgument.greedy("text"), ArgumentDescription.of("The text to broadcast."))
                .permission("osmc.broadcast.text")
                .handler(context -> {
                    broadcastComponent(Component.text((String) context.get("text")), BroadcastType.TEXT);
                })
        );

        metrics(plugin.metrics);
    }

    public void broadcastComponent(Component component, BroadcastType type) { // Broadcast type can be useful for future!
        plugin.proxyServer.sendMessage(component);

        broadcasts.incrementAndGet();
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
    }
}
