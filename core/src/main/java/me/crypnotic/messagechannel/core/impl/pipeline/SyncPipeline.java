package me.crypnotic.messagechannel.core.impl.pipeline;

import me.crypnotic.messagechannel.core.MessageChannelCore;
import me.crypnotic.messagechannel.core.impl.PipelineRegistryImpl;

public class SyncPipeline extends AbstractPipeline {

    public SyncPipeline(MessageChannelCore core, PipelineRegistryImpl registry, String channel) {
        super(core, registry, channel);
    }

    @Override
    public boolean isAsync() {
        return false;
    }
}
