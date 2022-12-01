/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

package org.kie.kogito.jitexecutor.efesto.api;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.kie.kogito.jitexecutor.efesto.JITEfestoService;
import org.kie.kogito.jitexecutor.efesto.requests.MultipleResourcesPayload;
import org.kie.kogito.jitexecutor.efesto.responses.JITEfestoResult;

@Path("/jitefesto")
public class JITEfestoResource {

    @Inject
    JITEfestoService jitEfestoService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response jitefesto(MultipleResourcesPayload modelsPayload, Map<String, Object> inputData) {
        JITEfestoResult evaluateAll =  jitEfestoService.evaluateModel(modelsPayload, inputData);
        Map<String, Object> restResulk = new HashMap<>();
        restResulk.put(modelsPayload.getMainURI().fullPath(), evaluateAll.getEfestoResult());
        return Response.ok(restResulk).build();
    }

}