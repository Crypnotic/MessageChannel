package me.crypnotic.messagechannel.core.impl.pipeline;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import me.crypnotic.messagechannel.api.pipeline.IPipeline;
import me.crypnotic.messagechannel.api.pipeline.PipelineMessage;
import me.crypnotic.messagechannel.core.MessageChannelCore;
import me.crypnotic.messagechannel.core.impl.PipelineRegistryImpl;

public abstract class AbstractPipeline implements IPipeline {

    protected MessageChannelCore core;
    protected PipelineRegistryImpl registry;
    protected String channel;
    protected Set<Consumer<PipelineMessage>> listeners;

    public AbstractPipeline(MessageChannelCore core, PipelineRegistryImpl registry, String channel) {
        this.core = core;
        this.registry = registry;
        this.channel = channel;
        this.listeners = new HashSet<Consumer<PipelineMessage>>();
    }

    public void onReceive(Consumer<PipelineMessage> listener) {
        if (!this.listeners.contains(listener)) {
            synchronized (listeners) {
                if (!this.listeners.contains(listener)) {
                    this.listeners.add(listener);
                }
            }
        }
    }

    public void send(PipelineMessage message) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            ObjectOutputStream output = new ObjectOutputStream(bytes);

            output.writeUTF(channel);
            output.writeObject(message.getTarget());
            output.writeObject(message.getContents());

            output.close();
            bytes.close();

            core.getPlatform().send(message, bytes.toByteArray());
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public void post(PipelineMessage message) {
        synchronized (listeners) {
            listeners.forEach(listener -> listener.accept(message));
        }
    }

    @Override
    public void broadcast(PipelineMessage message) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            ObjectOutputStream output = new ObjectOutputStream(bytes);

            output.writeUTF(channel);
            output.writeObject(message.getTarget());
            output.writeObject(message.getContents());

            output.close();
            bytes.close();

            core.getPlatform().broadcast(message, bytes.toByteArray());
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
