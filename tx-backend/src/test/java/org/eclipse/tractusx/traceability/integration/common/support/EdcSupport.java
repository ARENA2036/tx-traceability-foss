/********************************************************************************
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

package org.eclipse.tractusx.traceability.integration.common.support;

import com.xebialabs.restito.semantics.Condition;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp;
import static com.xebialabs.restito.builder.verify.VerifyHttp.verifyHttp;
import static com.xebialabs.restito.semantics.Action.noContent;
import static com.xebialabs.restito.semantics.Action.status;
import static com.xebialabs.restito.semantics.Condition.composite;
import static com.xebialabs.restito.semantics.Condition.get;
import static com.xebialabs.restito.semantics.Condition.matchesUri;
import static com.xebialabs.restito.semantics.Condition.method;
import static com.xebialabs.restito.semantics.Condition.post;
import static com.xebialabs.restito.semantics.Condition.startsWithUri;
import static com.xebialabs.restito.semantics.Condition.withHeader;
import static org.glassfish.grizzly.http.Method.DELETE;

@Component
public class EdcSupport {

    @Autowired
    RestitoProvider restitoProvider;

    private static final Condition EDC_API_KEY_HEADER = withHeader("X-Api-Key", "integration-tests");

    public void edcWillApproveInvestigations() {
        whenHttp(restitoProvider.stubServer()).match(
                matchesUri(Pattern.compile(
                        "/api/investigations/[\\w]+/approve")),
                withHeader("Content-Type", "application/json")
        ).then(
                status(HttpStatus.NO_CONTENT_204)
        );
    }

    public void edcWillApproveAlerts() {
        whenHttp(restitoProvider.stubServer()).match(
                matchesUri(Pattern.compile("/api/alerts/[\\w\\-]+/approve")),
                withHeader("Content-Type", "application/json")
        ).then(
                status(HttpStatus.NO_CONTENT_204)
        );
    }

    public void edcWillCreateNotificationAsset() {
        whenHttp(restitoProvider.stubServer()).match(
                post("/management/v2/assets"),
                EDC_API_KEY_HEADER
        ).then(
                status(HttpStatus.OK_200)
        );
    }

    public void edcWillCreateAsset() {
        whenHttp(restitoProvider.stubServer()).match(
                post("/management/v3/assets"),
                EDC_API_KEY_HEADER
        ).then(
                status(HttpStatus.OK_200)
        );
    }

    public void edcWillFailToCreateAsset() {
        whenHttp(restitoProvider.stubServer()).match(
                post("/management/v3/assets"),
                EDC_API_KEY_HEADER
        ).then(
                status(HttpStatus.SERVICE_UNAVAILABLE_503)
        );
    }

    public void edcWillRemoveNotificationAsset() {
        whenHttp(restitoProvider.stubServer()).match(
                method(DELETE),
                startsWithUri("/management/v2/assets/"),
                EDC_API_KEY_HEADER
        ).then(
                noContent()
        );
    }

    public void edcWillReturnCatalog() {
        whenHttp(restitoProvider.stubServer()).match(
                post("/management/v2/catalog/request"),
                EDC_API_KEY_HEADER
        ).then(
                status(HttpStatus.OK_200),
                restitoProvider.jsonResponseFromFile("stubs/edc/post/data/contractagreements/catalog_response_200.json")
        );
    }

    public void edcWillReturnCatalogDupl() {
        whenHttp(restitoProvider.stubServer()).match(
                post("/management/v2/catalog/request"),
                EDC_API_KEY_HEADER
        ).then(
                status(HttpStatus.OK_200),
                restitoProvider.jsonResponseFromFile("stubs/edc/post/data/contractagreements/catalog_dupl_response_200.json")
        );
    }

    public void edcWillReturnContractAgreements() {
        whenHttp(restitoProvider.stubServer()).match(
                post("/management/v2/contractagreements/request"),
                EDC_API_KEY_HEADER
        ).then(
                status(HttpStatus.OK_200),
                restitoProvider.jsonResponseFromFile("stubs/edc/post/data/contractagreements/all_contractagreements_response_200.json")
        );
    }

    public void edcWillReturnOnlyOneContractAgreement() {
        whenHttp(restitoProvider.stubServer()).match(
                post("/management/v2/contractagreements/request"),
                EDC_API_KEY_HEADER
        ).then(
                status(HttpStatus.OK_200),
                restitoProvider.jsonResponseFromFile("stubs/edc/post/data/contractagreements/one_contractagreement_response_200.json")
        );
    }

    public void edcWillReturnPaginatedContractAgreements() {
        whenHttp(restitoProvider.stubServer()).match(
                post("/management/v2/contractagreements/request"),
                EDC_API_KEY_HEADER
        ).then(
                status(HttpStatus.OK_200)
        ).withSequence(
                restitoProvider.jsonResponseFromFile("stubs/edc/post/data/contractagreements/first_page_contractagreements_response_200.json"),
                restitoProvider.jsonResponseFromFile("stubs/edc/post/data/contractagreements/second_page_contractagreements_response_200.json"));
    }

    public void edcWillReturnContractAgreementNegotiation() {
        whenHttp(restitoProvider.stubServer()).match(
                matchesUri(Pattern.compile("/management/v2/contractagreements/[\\w]+/negotiation")),
                EDC_API_KEY_HEADER
        ).then(
                status(HttpStatus.OK_200),
                restitoProvider.jsonResponseFromFile("stubs/edc/post/data/contractagreements/contractagreement_negotiation_response_200.json")
        );
    }

    public void edcWillFailToCreateNotificationAsset() {
        whenHttp(restitoProvider.stubServer()).match(
                post("/management/v2/assets"),
                EDC_API_KEY_HEADER
        ).then(
                status(HttpStatus.INTERNAL_SERVER_ERROR_500)
        );
    }

    void edcNotificationAssetAlreadyExist() {
        whenHttp(restitoProvider.stubServer()).match(
                post("/management/v2/assets"),
                EDC_API_KEY_HEADER
        ).then(
                status(HttpStatus.CONFLICT_409),
                restitoProvider.jsonResponseFromFile("./stubs/edc/post/management/v2/assets/response_409.json")
        );
    }

    public void edcWillCreatePolicyDefinition() {
        whenHttp(restitoProvider.stubServer()).match(
                post("/management/v2/policydefinitions"),
                EDC_API_KEY_HEADER
        ).then(
                status(HttpStatus.OK_200)
        );
    }

    public void edcWillReturnConflictWhenCreatePolicyDefinition() {
        whenHttp(restitoProvider.stubServer()).match(
                post("/management/v2/policydefinitions"),
                EDC_API_KEY_HEADER
        ).then(
                status(HttpStatus.CONFLICT_409)
        );
    }

    public void edcWillRemovePolicyDefinition() {
        whenHttp(restitoProvider.stubServer()).match(
                composite(
                        method(DELETE),
                        startsWithUri("/management/v2/policydefinitions/")
                ),
                EDC_API_KEY_HEADER
        ).then(
                noContent()
        );
    }

    public void edcWillFailToCreatePolicyDefinition() {
        whenHttp(restitoProvider.stubServer()).match(
                post("/management/v2/policydefinitions"),
                EDC_API_KEY_HEADER
        ).then(
                status(HttpStatus.SERVICE_UNAVAILABLE_503)
        );
    }

    public void edcWillCreateContractDefinition() {
        whenHttp(restitoProvider.stubServer()).match(
                post("/management/v2/contractdefinitions"),
                EDC_API_KEY_HEADER
        ).then(
                status(HttpStatus.OK_200)
        );
    }

    public void edcWillFailToCreateContractDefinition() {
        whenHttp(restitoProvider.stubServer()).match(
                post("/management/v2/contractdefinitions"),
                EDC_API_KEY_HEADER
        ).then(
                status(HttpStatus.INTERNAL_SERVER_ERROR_500)
        );
    }

    public void edcWillCreateContractNegotiation() {
        whenHttp(restitoProvider.stubServer()).match(
                post("/management/v2/contractnegotiations"),
                EDC_API_KEY_HEADER
        ).then(
                status(HttpStatus.OK_200),
                restitoProvider.jsonResponseFromFile("stubs/edc/post/data/contractagreements/contractnegotiation_response_200.json")
        );
    }

    public void edcWillReturnContractNegotiationOnlyState() {
        whenHttp(restitoProvider.stubServer()).match(
                get("/management/v2/contractnegotiations/cfc7f1e9-fb04-499b-a444-f5f5d41dd789/state"),
                EDC_API_KEY_HEADER
        ).then(
                status(HttpStatus.OK_200),
                restitoProvider.jsonResponseFromFile("stubs/edc/post/data/contractagreements/contractnegotiationonlystate_response_200.json")
        );
    }

    public void edcWillReturnContractNegotiationState() {
        whenHttp(restitoProvider.stubServer()).match(
                get("/management/v2/contractnegotiations/cfc7f1e9-fb04-499b-a444-f5f5d41dd789"),
                EDC_API_KEY_HEADER
        ).then(
                status(HttpStatus.OK_200),
                restitoProvider.jsonResponseFromFile("stubs/edc/post/data/contractagreements/contractnegotiationstate_response_200.json")
        );
    }

    public void edcWillCreateTransferprocesses() {
        whenHttp(restitoProvider.stubServer()).match(
                post("/management/v2/transferprocesses"),
                EDC_API_KEY_HEADER
        ).then(
                status(HttpStatus.OK_200),
                restitoProvider.jsonResponseFromFile("stubs/edc/post/data/contractagreements/transferprocesses_response_200.json")
        );
    }

    public void edcWillReturnTransferprocessesOnlyState() {
        whenHttp(restitoProvider.stubServer()).match(
                get("/management/v2/transferprocesses/8a157c93-2dfb-440b-9218-ee456ce5ba10/state"),
                EDC_API_KEY_HEADER
        ).then(
                status(HttpStatus.OK_200),
                restitoProvider.jsonResponseFromFile("stubs/edc/post/data/contractagreements/transferprocessesonlystate_response_200.json")
        );
    }

    public void edcWillReturnTransferprocessesState() {
        whenHttp(restitoProvider.stubServer()).match(
                get("/management/v2/transferprocesses/8a157c93-2dfb-440b-9218-ee456ce5ba10"),
                EDC_API_KEY_HEADER
        ).then(
                status(HttpStatus.OK_200),
                restitoProvider.jsonResponseFromFile("stubs/edc/post/data/contractagreements/transferprocessesstate_response_200.json")
        );
    }
    public void verifyCreateNotificationAssetEndpointCalledTimes(int times) {
        verifyHttp(restitoProvider.stubServer()).times(times,
                post("/management/v2/assets")
        );
    }

    public void verifyDeleteNotificationAssetEndpointCalledTimes(int times) {
        verifyHttp(restitoProvider.stubServer()).times(times,
                method(DELETE),
                startsWithUri("/management/v2/assets")
        );
    }

    public void verifyCreatePolicyDefinitionEndpointCalledTimes(int times) {
        verifyHttp(restitoProvider.stubServer()).times(times,
                post("/management/v2/policydefinitions")
        );
    }

    public void verifyDeletePolicyDefinitionEndpointCalledTimes(int times) {
        verifyHttp(restitoProvider.stubServer()).times(times,
                method(DELETE),
                startsWithUri("/management/v2/policydefinitions")
        );
    }

    public void verifyCreateContractDefinitionEndpointCalledTimes(int times) {
        verifyHttp(restitoProvider.stubServer()).times(times,
                post("/management/v2/contractdefinitions")
        );
    }

    public void verifyDeleteContractDefinitionEndpointCalledTimes(int times) {
        verifyHttp(restitoProvider.stubServer()).times(times,
                method(DELETE),
                startsWithUri("/management/v2/contractdefinitions")
        );
    }


}
