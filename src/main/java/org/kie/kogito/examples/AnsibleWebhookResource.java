/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
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

package org.kie.kogito.examples;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.kie.kogito.event.cloudevents.CloudEventExtensionConstants;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;

@Path("/webhook")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class AnsibleWebhookResource {

    @Channel("tower-send")
    Emitter<String> emitter;

    @Inject
    ObjectMapper mapper;

    @POST
    public void add(AnsiblePayload payload) {
        System.out.println("received payload = \n" + payload);
        try {
            if (payload.getExtraVars() != null) {
                System.out.println("payload.getExtraVars() = " + payload.getExtraVars());
                JsonNode extraVars = mapper.readTree(payload.getExtraVars());
                System.out.println("extraVars isObject = " + extraVars.isObject());
                extraVars.fieldNames().forEachRemaining(f -> System.out.println("field = " + f));
                String kogitoId = extraVars.get("kogito_id").asText();
                System.out.println("kogitoId = " + kogitoId);
                CloudEvent ce = CloudEventBuilder.v1()
                        .withDataContentType(MediaType.APPLICATION_JSON)
                        .withId(UUID.randomUUID().toString())
                        .withSource(URI.create(payload.getUrl()))
                        .withTime(OffsetDateTime.now())
                        .withType("ansible-tower-job-" + (payload.getFinished() == null ? "started" : "completed"))
                        .withExtension(CloudEventExtensionConstants.PROCESS_REFERENCE_ID, kogitoId)
                        .withData(mapper.writeValueAsBytes(payload))
                        .build();
                String json = mapper.writeValueAsString(ce);
                System.out.println("json = " + json);
                emitter.send(json);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

}
