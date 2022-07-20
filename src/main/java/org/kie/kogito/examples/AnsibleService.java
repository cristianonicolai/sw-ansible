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

import java.util.Base64;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.kie.kogito.internal.process.runtime.KogitoProcessContext;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@ApplicationScoped
public class AnsibleService {

    @Inject
    @RestClient
    AnsibleApiClient client;

    @Inject
    ObjectMapper mapper;

    public JsonNode triggerJob(String jobId, KogitoProcessContext context) {
        System.out.println("context = " + context);
        System.out.println(" context.getProcessInstance().getStringId() = " + context.getProcessInstance().getStringId());
        System.out.println("jobId = " + jobId);
        JsonNode data = mapper.createObjectNode().set("extra_vars", mapper.createObjectNode().put("kogito_id", context.getProcessInstance().getStringId()));
        String auth = "Basic " + Base64.getEncoder().encodeToString("awx:awx".getBytes());
        return client.jobTemplateLaunch(auth, jobId, data);
    }

}
