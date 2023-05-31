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

package org.kie.kogito.swf.tools.dataindex;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import static org.kie.kogito.swf.tools.dataindex.DataIndexClient.DATA_INDEX_CONFIG_KEY;

@Path("/graphql")
@RegisterRestClient(configKey = DATA_INDEX_CONFIG_KEY)
@ApplicationScoped
public interface DataIndexClient {

    String DATA_INDEX_CONFIG_KEY = "kogito.data-index.url";

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    String query(String query);

}
