/*
 * This file is part of MessageChannel, licensed under the MIT License (MIT).
 *
 * Copyright (c) Crypnotic <https://www.crypnotic.me>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package me.crypnotic.messagechannel.velocity;

import java.util.Optional;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent.ForwardResult;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import me.crypnotic.messagechannel.api.MessageChannelAPI;
import me.crypnotic.messagechannel.api.access.IMessageChannel;
import me.crypnotic.messagechannel.api.access.IPlatform;
import me.crypnotic.messagechannel.api.exception.MessageChannelException;
import me.crypnotic.messagechannel.api.pipeline.PipelineMessage;
import me.crypnotic.messagechannel.core.MessageChannelCore;

@Plugin(id = "${project.artifactId}", name = "${project.name}", version = "${project.version}")
public class MessageChannelVelocity implements IPlatform {

    private IMessageChannel core;
    private ProxyServer proxy;

    private MinecraftChannelIdentifier INCOMING;
    private MinecraftChannelIdentifier OUTGOING;

    @Inject
    public MessageChannelVelocity(ProxyServer proxy) {
        this.proxy = proxy;

        this.core = new MessageChannelCore(this);

        try {
            MessageChannelAPI.setCore(core);
        } catch (MessageChannelException exception) {
            exception.printStackTrace();
        }
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        proxy.getChannelRegistrar().register(INCOMING = MinecraftChannelIdentifier.create("messagechannel", "proxy"));
        proxy.getChannelRegistrar().register(OUTGOING = MinecraftChannelIdentifier.create("messagechannel", "server"));

        proxy.getEventManager().register(this, this);
    }

    @Override
    public boolean send(PipelineMessage message, byte[] data) {
        Optional<Player> player = proxy.getPlayer(message.getTarget());
        if (player.isPresent()) {
            Optional<ServerConnection> server = player.get().getCurrentServer();
            if (server.isPresent()) {
                return server.get().sendPluginMessage(OUTGOING, data);
            }
        }
        return false;
    }

    @Override
    public boolean broadcast(PipelineMessage message, byte[] data) {
        for (RegisteredServer subserver : proxy.getAllServers()) {
            subserver.sendPluginMessage(OUTGOING, data);
        }
        return true;
    }

    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {
        if (event.getIdentifier().equals(INCOMING)) {
            core.getPipelineRegistry().receive(event.getData());

            event.setResult(ForwardResult.handled());
        }
    }

    @Override
    public boolean isProxy() {
        return true;
    }
}
