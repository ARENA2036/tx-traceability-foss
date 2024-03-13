/********************************************************************************
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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

package org.eclipse.tractusx.traceability.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.edc.client.asset.EdcAssetService;
import org.eclipse.tractusx.irs.edc.client.contract.service.EdcContractDefinitionService;
import org.eclipse.tractusx.irs.edc.client.policy.service.EdcPolicyDefinitionService;
import org.eclipse.tractusx.irs.edc.client.transformer.EdcTransformer;
import org.eclipse.tractusx.irs.registryclient.decentral.DigitalTwinRegistryCreateShellService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@ConfigurationPropertiesScan(basePackages = "org.eclipse.tractusx.traceability.*")
@EnableWebMvc
@EnableAsync(proxyTargetClass = true)
@EnableConfigurationProperties
@RequiredArgsConstructor
@Slf4j
@EnableJpaRepositories(basePackages = "org.eclipse.tractusx.traceability.*")
public class EdcConfiguration {

    @Value("${registry.urlWithPath}")
    String registryUrlWithPath;
    @Value("${registry.shellDescriptorUrl}")
    String shellDescriptorUrl;

    @Bean
    public EdcAssetService edcNotificationAssetService(org.eclipse.tractusx.irs.edc.client.EdcConfiguration edcConfiguration, EdcTransformer edcTransformer, RestTemplate edcNotificationAssetRestTemplate) {
        return new EdcAssetService(edcTransformer, edcConfiguration, edcNotificationAssetRestTemplate);
    }

    @Bean
    public EdcPolicyDefinitionService edcPolicyDefinitionService(org.eclipse.tractusx.irs.edc.client.EdcConfiguration edcConfiguration, RestTemplate edcNotificationAssetRestTemplate) {
        return new EdcPolicyDefinitionService(edcConfiguration, edcNotificationAssetRestTemplate);
    }

    @Bean
    public EdcContractDefinitionService edcContractDefinitionService(org.eclipse.tractusx.irs.edc.client.EdcConfiguration edcConfiguration, RestTemplate edcNotificationAssetRestTemplate) {
        return new EdcContractDefinitionService(edcConfiguration, edcNotificationAssetRestTemplate);
    }

    @Bean
    public EdcAssetService edcDtrAssetService(org.eclipse.tractusx.irs.edc.client.EdcConfiguration edcConfiguration, EdcTransformer edcTransformer, RestTemplate edcDtrAssetRestTemplate) {
        return new EdcAssetService(edcTransformer, edcConfiguration, edcDtrAssetRestTemplate);
    }

    @Bean
    public EdcPolicyDefinitionService edcDtrPolicyDefinitionService(org.eclipse.tractusx.irs.edc.client.EdcConfiguration edcConfiguration, RestTemplate edcDtrAssetRestTemplate) {
        return new EdcPolicyDefinitionService(edcConfiguration, edcDtrAssetRestTemplate);
    }

    @Bean
    public EdcContractDefinitionService edcDtrContractDefinitionService(org.eclipse.tractusx.irs.edc.client.EdcConfiguration edcConfiguration, RestTemplate edcDtrAssetRestTemplate) {
        return new EdcContractDefinitionService(edcConfiguration, edcDtrAssetRestTemplate);
    }

    @Bean
    public DigitalTwinRegistryCreateShellService dtrCreateShellService(RestTemplate digitalTwinRegistryCreateShellRestTemplate) {
        return new DigitalTwinRegistryCreateShellService(digitalTwinRegistryCreateShellRestTemplate, registryUrlWithPath + shellDescriptorUrl);
    }
}
