/********************************************************************************
 * Copyright (c) 2022, 2023 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 * Copyright (c) 2022, 2023 ZF Friedrichshafen AG
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.traceability.infrastructure.jpa.notification;

import org.eclipse.tractusx.traceability.assets.infrastructure.adapters.jpa.asset.AssetEntity;
import org.eclipse.tractusx.traceability.infrastructure.jpa.investigation.InvestigationEntity;
import org.eclipse.tractusx.traceability.investigations.domain.model.Severity;
import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "notification")
public class NotificationEntity {

	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "uuid2")
	private String id;

	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name = "investigation_id")
	private InvestigationEntity investigation;

	@ManyToMany(cascade = CascadeType.ALL)
	@JoinTable(
		name = "assets_notifications",
		joinColumns = @JoinColumn(name = "notification_id"),
		inverseJoinColumns = @JoinColumn(name = "asset_id")
	)
	private List<AssetEntity> assets;

	private String senderBpnNumber;
	private String receiverBpnNumber;
	private String edcUrl;
	private String contractAgreementId;
	private String notificationReferenceId;
	private Instant targetDate;
	private Severity severity;

	public NotificationEntity() {
	}

	public NotificationEntity(InvestigationEntity investigation, String senderBpnNumber,
							  String receiverBpnNumber, List<AssetEntity> assets, String notificationReferenceId,
							  Instant targetDate, Severity severity) {
		this.investigation = investigation;
		this.senderBpnNumber = senderBpnNumber;
		this.receiverBpnNumber = receiverBpnNumber;
		this.assets = assets;
		this.notificationReferenceId = notificationReferenceId;
		this.targetDate = targetDate;
		this.severity = severity;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public InvestigationEntity getInvestigation() {
		return investigation;
	}

	public void setInvestigation(InvestigationEntity investigationsId) {
		this.investigation = investigationsId;
	}

	public String getSenderBpnNumber() {
		return senderBpnNumber;
	}

	public void setSenderBpnNumber(String senderBpnNumber) {
		this.senderBpnNumber = senderBpnNumber;
	}

	public String getReceiverBpnNumber() {
		return receiverBpnNumber;
	}

	public void setReceiverBpnNumber(String bpnNumber) {
		this.receiverBpnNumber = bpnNumber;
	}

	public String getEdcUrl() {
		return edcUrl;
	}

	public void setEdcUrl(String edcUrl) {
		this.edcUrl = edcUrl;
	}

	public String getContractAgreementId() {
		return contractAgreementId;
	}

	public void setContractAgreementId(String contractAgreementId) {
		this.contractAgreementId = contractAgreementId;
	}

	public String getNotificationReferenceId() {
		return notificationReferenceId;
	}

	public void setNotificationReferenceId(String notificationReferenceId) {
		this.notificationReferenceId = notificationReferenceId;
	}

	public List<AssetEntity> getAssets() {
		return assets;
	}

	public void setAssets(List<AssetEntity> assets) {
		this.assets = assets;
	}

	public Instant getTargetDate() {
		return this.targetDate;
	}

	public void setTargetDate(Instant targetDate) {
		this.targetDate = targetDate;
	}

	public Severity getSeverity() {
		return severity;
	}

	public void setSeverity(Severity severity) {
		this.severity = severity;
	}
}