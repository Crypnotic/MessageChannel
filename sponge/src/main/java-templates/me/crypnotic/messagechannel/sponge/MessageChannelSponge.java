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
package me.crypnotic.messagechannel.sponge;

import org.spongepowered.api.Game;
import org.spongepowered.api.Platform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameConstructionEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.network.ChannelBinding.RawDataChannel;
import org.spongepowered.api.plugin.Plugin;

import com.google.inject.Inject;

import me.crypnotic.messagechannel.api.MessageChannelAPI;
import me.crypnotic.messagechannel.api.access.IMessageChannel;
import me.crypnotic.messagechannel.api.access.IRelay;
import me.crypnotic.messagechannel.api.exception.MessageChannelException;
import me.crypnotic.messagechannel.api.pipeline.PipelineMessage;
import me.crypnotic.messagechannel.core.MessageChannelCore;

@Plugin(id = "${project.artifactId}", name = "${project.name}", version = "${project.version}")
public class MessageChannelSponge implements IRelay {

    @Inject
    private Game game;
    private IMessageChannel core;
    private RawDataChannel outgoing;

    @Listener
    public void onGameConstruction(GameConstructionEvent event) {
        this.core = new MessageChannelCore(this);

        try {
            MessageChannelAPI.setCore(core);
        } catch (MessageChannelException exception) {
            exception.printStackTrace();
        }
    }

    @Listener
    public void onGamePreInitialization(GamePreInitializationEvent event) {
        this.outgoing = game.getChannelRegistrar().createRawChannel(this, "messagechannel:proxy");
        game.getChannelRegistrar().createRawChannel(this, "messagechannel:server").addListener(Platform.Type.SERVER,
                (buffer, connection, side) -> {
                    try {
                        core.getPipelineRegistry().receive(buffer.readBytes(buffer.available()));
                    } catch (UnsupportedOperationException exception) {
                        exception.printStackTrace();
                    }
                });
    }

    @Override
    public boolean send(PipelineMessage message, byte[] data) {
        if (game.getServer().getOnlinePlayers().size() > 0) {
            Player player = (Player) game.getServer().getOnlinePlayers().toArray()[0];
            if (player != null) {
                outgoing.sendTo(player, (buffer) -> {
                    buffer.writeBytes(data);
                });
            }
        }
        return false;
    }

    @Override
    public boolean broadcast(PipelineMessage message, byte[] data) {
        return false;
    }

    @Override
    public boolean isProxy() {
        return false;
    }
}