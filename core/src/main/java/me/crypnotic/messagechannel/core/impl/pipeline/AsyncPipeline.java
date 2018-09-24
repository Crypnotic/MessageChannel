package me.crypnotic.messagechannel.core.impl.pipeline;

import me.crypnotic.messagechannel.api.pipeline.PipelineMessage;
import me.crypnotic.messagechannel.core.MessageChannelCore;
import me.crypnotic.messagechannel.core.impl.PipelineRegistryImpl;

public class AsyncPipeline extends AbstractPipeline {

    public AsyncPipeline(MessageChannelCore core, PipelineRegistryImpl registry, String channel) {
        super(core, registry, channel);
    }

    @Override
    public void send(PipelineMessage message) {
        registry.getExecutor().submit(() -> super.send(message));
    }

    @Override
    public final void post(PipelineMessage message) {
        registry.getExecutor().submit(() -> super.post(message));
    }

    @Override
    public void broadcast(PipelineMessage message) {
        registry.getExecutor().submit(() -> super.broadcast(message));
    }

    @Override
    public boolean isAsync() {
        return true;
    }
}
