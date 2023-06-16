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

package org.eclipse.tractusx.traceability.assets.infrastructure.asplanned.repository;

import lombok.RequiredArgsConstructor;
import org.eclipse.tractusx.traceability.assets.domain.exception.AssetNotFoundException;
import org.eclipse.tractusx.traceability.assets.domain.model.Asset;
import org.eclipse.tractusx.traceability.assets.domain.model.Owner;
import org.eclipse.tractusx.traceability.assets.domain.service.repository.AssetAsPlannedRepository;
import org.eclipse.tractusx.traceability.assets.infrastructure.asplanned.model.AssetAsPlannedEntity;
import org.eclipse.tractusx.traceability.common.model.PageResult;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Component
public class JpaAssetAsPlannedRepositoryImpl implements AssetAsPlannedRepository {

    private final JpaAssetAsPlannedRepository assetsRepository;

    @Override
    @Transactional
    public Asset getAssetById(String assetId) {
        return assetsRepository.findById(assetId).map(AssetAsPlannedEntity::toDomain)
                .orElseThrow(() -> new AssetNotFoundException("Asset with id %s was not found.".formatted(assetId)));
    }

    @Override
    public boolean existsById(String globalAssetId) {
        return assetsRepository.existsById(globalAssetId);
    }

    @Override
    public List<Asset> getAssetsById(List<String> assetIds) {
        return assetsRepository.findByIdIn(assetIds).stream().map(AssetAsPlannedEntity::toDomain)
                .toList();
    }

    @Override
    public Asset getAssetByChildId(String assetId, String childId) {
        return assetsRepository.findById(childId).map(AssetAsPlannedEntity::toDomain)
                .orElseThrow(() -> new AssetNotFoundException("Child Asset Not Found"));
    }

    @Override
    public PageResult<Asset> getAssets(Pageable pageable, Owner owner) {
        if (owner != null) {
            return new PageResult<>(assetsRepository.findByOwner(pageable, owner), AssetAsPlannedEntity::toDomain);
        }
        return new PageResult<>(assetsRepository.findAll(pageable), AssetAsPlannedEntity::toDomain);
    }

    @Override
    @Transactional
    public List<Asset> getAssets() {
        return AssetAsPlannedEntity.toDomainList(assetsRepository.findAll());
    }

    @Override
    public Asset save(Asset asset) {
        return AssetAsPlannedEntity.toDomain(assetsRepository.save(AssetAsPlannedEntity.from(asset)));
    }

    @Override
    @Transactional
    public List<Asset> saveAll(List<Asset> assets) {
        return AssetAsPlannedEntity.toDomainList(assetsRepository.saveAll(AssetAsPlannedEntity.fromList(assets)));
    }

    @Transactional
    @Override
    public void updateParentDescriptionsAndOwner(final Asset asset) {
        Asset assetById = this.getAssetById(asset.getId());
        if (assetById.getOwner().equals(Owner.UNKNOWN)) {
            assetById.setOwner(asset.getOwner());
        }
        assetById.setParentRelations(asset.getParentRelations());
        save(assetById);
    }

    @Transactional
    @Override
    public long countAssets() {
        return assetsRepository.count();
    }

    @Override
    public long countAssetsByOwner(Owner owner) {
        return assetsRepository.countAssetsByOwner(owner);
    }
}
