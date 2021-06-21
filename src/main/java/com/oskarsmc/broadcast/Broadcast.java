package com.oskarsmc.broadcast;

import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.velocity.CloudInjectionModule;
import cloud.commandframework.velocity.VelocityCommandManager;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.oskarsmc.broadcast.command.BroadcastBrigadier;
import com.oskarsmc.broadcast.command.BroadcastCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import org.bstats.velocity.Metrics;
import org.slf4j.Logger;

import java.util.function.Function;

public class Broadcast {

    @Inject
    public Logger logger;

    @Inject
    public ProxyServer proxyServer;

    @Inject
    private Metrics.Factory metricsFactory;

    @Inject
    private Injector injector;


    public VelocityCommandManager<CommandSource> commandManager;
    public BroadcastCommand broadcastCommand;
    public Metrics metrics;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        final Injector childInjector = injector.createChildInjector(
                new CloudInjectionModule<>(
                        CommandSource.class,
                        CommandExecutionCoordinator.simpleCoordinator(),
                        Function.identity(),
                        Function.identity()
                )
        );

        this.commandManager = childInjector.getInstance(
                Key.get(new TypeLiteral<VelocityCommandManager<CommandSource>>() {
                })
        );

        this.metrics = metricsFactory.make(this, 11783);

        this.broadcastCommand = new BroadcastCommand(this);
    }
}
