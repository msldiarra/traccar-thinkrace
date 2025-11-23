package org.traccar.protocol;

import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import org.traccar.BaseProtocol;
import org.traccar.PipelineBuilder;
import org.traccar.TrackerServer;
import org.traccar.config.Config;

import jakarta.inject.Inject;

public class ThinkRaceV22Protocol extends BaseProtocol {

    @Inject
    public ThinkRaceV22Protocol(Config config) {
        addServer(new TrackerServer(config, getName(), false) {
            @Override
            protected void addProtocolHandlers(PipelineBuilder pipeline, Config config) {
                // MT4 sends ASCII IW...# lines, possibly long because of AP lists
                pipeline.addLast(new LineBasedFrameDecoder(16 * 1024));
                pipeline.addLast(new StringDecoder());
                pipeline.addLast(new ThinkRaceV22ProtocolDecoder(ThinkRaceV22Protocol.this));
            }
        });
    }

}
