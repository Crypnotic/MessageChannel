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
package me.crypnotic.messagechannel.bungeecord;

import me.crypnotic.messagechannel.api.MessageChannelAPI;
import me.crypnotic.messagechannel.api.access.IMessageChannel;
import me.crypnotic.messagechannel.api.access.IRelay;
import me.crypnotic.messagechannel.api.exception.MessageChannelException;
import me.crypnotic.messagechannel.api.pipeline.PipelineMessage;
import me.crypnotic.messagechannel.core.MessageChannelCore;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

public class MessageChannelBungeecord extends Plugin implements IRelay, Listener {

    private IMessageChannel core;

    @Override
    public void onLoad() {
        this.core = new MessageChannelCore(this);

        try {
            MessageChannelAPI.setCore(core);
        } catch (MessageChannelException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void onEnable() {
        getProxy().registerChannel("messagechannel:proxy");
        getProxy().registerChannel("messagechannel:server");

        getProxy().getPluginManager().registerListener(this, this);
    }

    @Override
    public boolean send(PipelineMessage message, byte[] data) {
        ProxiedPlayer player = getProxy().getPlayer(message.getTarget());
        if (player != null) {
            player.getServer().sendData("messagechannel:server", data);
            return true;
        }
        return false;
    }

    @Override
    public boolean broadcast(PipelineMessage message, byte[] data) {
        for (ServerInfo info : getProxy().getServers().values()) {
            info.sendData("messagechannel:server", data);
        }
        return true;
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent event) {
        if (event.getTag().equals("messagechannel:proxy")) {
            core.getPipelineRegistry().receive(event.getData());
        }
    }

    @Override
    public boolean isProxy() {
        return true;
    }
}