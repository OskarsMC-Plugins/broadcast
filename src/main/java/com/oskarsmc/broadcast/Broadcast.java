package com.oskarsmc.broadcast;

import com.google.inject.Inject;
import com.oskarsmc.broadcast.command.BroadcastBrigadier;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

@Plugin(
        id = "broadcast",
        name = "Broadcast",
        version = "0.1.0",
        description = "Broadcasting Plugin For Velocity",
        url = "https://software.oskarsmc.com/",
        authors = {"OskarZyg", "OskarsMC"}
)
public class Broadcast {

    @Inject
    private Logger logger;

    @Inject
    private ProxyServer proxyServer;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        new BroadcastBrigadier(proxyServer);
    }
}
