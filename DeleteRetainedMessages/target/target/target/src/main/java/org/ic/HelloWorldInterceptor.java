
/*
 * Copyright 2018-present HiveMQ GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ic;

import com.codahale.metrics.Counter;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.interceptor.publish.PublishInboundInterceptor;
import com.hivemq.extension.sdk.api.interceptor.publish.parameter.PublishInboundInput;
import com.hivemq.extension.sdk.api.interceptor.publish.parameter.PublishInboundOutput;
import com.hivemq.extension.sdk.api.packets.publish.ModifiablePublishPacket;
import com.hivemq.extension.sdk.api.services.Services;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * This is a very simple {@link PublishInboundInterceptor},
 * it changes the payload of every incoming PUBLISH with the topic 'hello/world' to 'Hello World!'.
 *
 * @author Yannick Weber
 * @since 4.3.1
 */
public class HelloWorldInterceptor implements PublishInboundInterceptor {

    @Override
    public void onInboundPublish(final @NotNull PublishInboundInput publishInboundInput, final @NotNull PublishInboundOutput publishInboundOutput) {
        final ModifiablePublishPacket publishPacket = publishInboundOutput.getPublishPacket();

        if ("server/actions".equals(publishPacket.getTopic())) {
            String command = getPayloadAsString(publishPacket.getPayload());
            if("clearRetainedMessages".equals(command)){
                Services.retainedMessageStore().clear();
                publishPacket.setPayload(StringToByteBuffer("Services.retainedMessageStore().clear() called"));
            }
            else{
                final ByteBuffer payload = ByteBuffer.wrap("UnKnown command".getBytes(StandardCharsets.UTF_8));
                publishPacket.setPayload(payload);
            }
        }
        else if("server/metrics".equals(publishPacket.getTopic())){
            String command = getPayloadAsString(publishPacket.getPayload());
            if("connectionsCount".equals(command)){
                Counter currentConnectionsCounter = Services.metricRegistry().counter("com.hivemq.networking.connections.current");
                var count = currentConnectionsCounter.getCount()+"";

                publishPacket.setPayload(StringToByteBuffer(count));
            }
            else{
                final ByteBuffer payload = ByteBuffer.wrap("UnKnown command".getBytes(StandardCharsets.UTF_8));
                publishPacket.setPayload(payload);
            }
        }
    }

    private @NotNull ByteBuffer StringToByteBuffer(String payload) {
        return ByteBuffer.wrap(payload.getBytes(StandardCharsets.UTF_8));
    }

    private String getPayloadAsString(Optional<ByteBuffer> payload) {
        return StandardCharsets.UTF_8.decode(payload.get()).toString();
    }

// if ("server/actions".equals(publishPacket.getTopic())) {
//        var payloadAsString = publishPacket.getPayload();
//        Services.retainedMessageStore().clear();
//        final ByteBuffer payload = ByteBuffer.wrap("Services.retainedMessageStore().clear() called".getBytes(StandardCharsets.UTF_8));
//        publishPacket.setPayload(payload);
//        Counter currentConnectionsCounter = Services.metricRegistry().counter("com.hivemq.networking.connections.current");
//        var c = currentConnectionsCounter.getCount();
//    }
//        else if("server/metrics".equals(publishPacket.getTopic())){
//        string command = getPayloadAsString(publishPacket.getPayload());
//
//    }
}