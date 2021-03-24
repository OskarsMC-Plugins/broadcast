package com.oskarsmc.broadcast.command;

import com.google.gson.stream.MalformedJsonException;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.oskarsmc.broadcast.util.JSONUtils;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

public class BroadcastBrigadier {
    public Component invalidMessage = Component.text("Invalid input.", NamedTextColor.RED, TextDecoration.BOLD);

    public BroadcastBrigadier(ProxyServer proxyServer) {
        LiteralCommandNode<CommandSource> sendCommand = LiteralArgumentBuilder
                .<CommandSource>literal("broadcast")
                .executes(context -> {
                    return 1;
                })
                .build();

        ArgumentCommandNode<CommandSource, String> serverNode = RequiredArgumentBuilder
                .<CommandSource, String>argument("content",  StringArgumentType.greedyString())
                .executes(context -> {
                    Component component;
                    if (context.getSource().hasPermission("osmc.broadcast.send.raw") && context.getArgument("type", String.class).equals("raw")) {
                        if (JSONUtils.isJSONValid(context.getArgument("content", String.class))) {
                            component = GsonComponentSerializer.gson().deserialize(context.getArgument("content", String.class));
                        } else {
                            context.getSource().sendMessage(invalidMessage);
                            return 0;
                        }
                    } else if (context.getSource().hasPermission("osmc.broadcast.send.minimessage") && context.getArgument("type", String.class).equals("minimessage")) {
                            component = MiniMessage.get().parse(context.getArgument("content", String.class));
                    } else {
                        return 0;
                    }

                    proxyServer.sendMessage(component);

                    return 1;
                }).build();

        ArgumentCommandNode<CommandSource, String> typeSelectionNode = RequiredArgumentBuilder
                .<CommandSource, String>argument("type", StringArgumentType.word())
                .suggests((context, builder) -> {
                    builder.suggest("raw");
                    builder.suggest("minimessage");
                    builder.suggest("text");

                    return builder.buildFuture();
                })
                .build();

        typeSelectionNode.addChild(serverNode);
        sendCommand.addChild(typeSelectionNode);

        BrigadierCommand sendBrigadier = new BrigadierCommand(sendCommand);


        CommandMeta meta = proxyServer.getCommandManager().metaBuilder(sendBrigadier)
                .build();

        proxyServer.getCommandManager().register(meta, sendBrigadier);
    }
}
