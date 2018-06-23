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
package me.crypnotic.messagechannel.core.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import me.crypnotic.messagechannel.api.pipeline.IPipeline;
import me.crypnotic.messagechannel.api.pipeline.PipelineMessage;
import me.crypnotic.messagechannel.core.MessageChannelCore;

public class PipelineImpl implements IPipeline {

    private MessageChannelCore core;
    private String channel;
    private Set<Consumer<PipelineMessage>> listeners;

    public PipelineImpl(MessageChannelCore core, String channel) {
        this.core = core;
        this.channel = channel;
        this.listeners = new HashSet<Consumer<PipelineMessage>>();
    }

    public boolean addListener(Consumer<PipelineMessage> listener) {
        synchronized (listeners) {
            return this.listeners.add(listener);
        }
    }

    public boolean send(PipelineMessage message) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            ObjectOutputStream output = new ObjectOutputStream(bytes);

            output.writeUTF(channel);
            output.writeObject(message.getTarget());
            output.writeObject(message.getContents());

            output.close();
            bytes.close();

            return core.getPlatform().send(message, bytes.toByteArray());
        } catch (IOException exception) {
            return false;
        }
    }

    public final void call(PipelineMessage message) {
        synchronized (listeners) {
            listeners.forEach(listener -> listener.accept(message));
        }
    }

    @Override
    public boolean broadcast(PipelineMessage message) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            ObjectOutputStream output = new ObjectOutputStream(bytes);

            output.writeUTF(channel);
            output.writeObject(message.getTarget());
            output.writeObject(message.getContents());

            output.close();
            bytes.close();

            return core.getPlatform().broadcast(message, bytes.toByteArray());
        } catch (IOException exception) {
            return false;
        }
    }
}