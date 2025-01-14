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
package org.eclipse.tractusx.traceability.notification.domain.base.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.traceability.assets.domain.asbuilt.repository.AssetAsBuiltRepository;
import org.eclipse.tractusx.traceability.assets.domain.base.model.AssetBase;
import org.eclipse.tractusx.traceability.bpn.domain.model.BpnEdcMapping;
import org.eclipse.tractusx.traceability.bpn.infrastructure.repository.BpnRepository;
import org.eclipse.tractusx.traceability.common.model.BPN;
import org.eclipse.tractusx.traceability.common.model.PageResult;
import org.eclipse.tractusx.traceability.common.model.SearchCriteria;
import org.eclipse.tractusx.traceability.common.properties.TraceabilityProperties;
import org.eclipse.tractusx.traceability.notification.application.notification.service.NotificationService;
import org.eclipse.tractusx.traceability.notification.domain.base.exception.SendNotificationException;
import org.eclipse.tractusx.traceability.notification.domain.base.model.Notification;
import org.eclipse.tractusx.traceability.notification.domain.base.model.NotificationId;
import org.eclipse.tractusx.traceability.notification.domain.base.model.NotificationMessage;
import org.eclipse.tractusx.traceability.notification.domain.base.model.NotificationSeverity;
import org.eclipse.tractusx.traceability.notification.domain.base.model.NotificationSide;
import org.eclipse.tractusx.traceability.notification.domain.base.model.NotificationStatus;
import org.eclipse.tractusx.traceability.notification.domain.base.model.NotificationType;
import org.eclipse.tractusx.traceability.notification.domain.notification.exception.NotificationSenderAndReceiverBPNEqualException;
import org.eclipse.tractusx.traceability.notification.domain.notification.model.EditNotification;
import org.eclipse.tractusx.traceability.notification.domain.notification.model.StartNotification;
import org.eclipse.tractusx.traceability.notification.domain.notification.repository.NotificationRepository;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
public abstract class AbstractNotificationService implements NotificationService {

    private final TraceabilityProperties traceabilityProperties;
    private final NotificationPublisherService notificationPublisherService;
    private final AssetAsBuiltRepository assetAsBuiltRepository;
    private final BpnRepository bpnRepository;

    private static final List<String> SUPPORTED_ENUM_FIELDS = List.of("status", "side", "messages_severity", "type");

    protected abstract NotificationRepository getNotificationRepository();


    protected abstract RuntimeException getNotFoundException(String message);

    @Override
    public PageResult<Notification> getNotifications(Pageable pageable, SearchCriteria searchCriteria) {
        return getNotificationRepository().getNotifications(pageable, searchCriteria);
    }

    @Override
    public NotificationId start(StartNotification startNotification) {
        validateReceiverIsNotOwnBpn(startNotification.getReceiverBpn(), null);
        Notification notification = notificationPublisherService.startNotification(startNotification);
        NotificationId createdAlertId = getNotificationRepository().saveNotification(notification);
        log.info("Start Quality Notification {}", notification);
        return createdAlertId;
    }

    @Override
    public void updateStatusTransition(Long notificationId, NotificationStatus notificationStatus, String reason) {
        Notification notification = loadOrNotFoundException(new NotificationId(notificationId));

        List<NotificationMessage> messages = notification.getNotifications();
        NotificationStatus previousStatus = NotificationStatus.getPreviousStatus(notificationStatus, messages);

        /* Create a copy of the latest notifications.
        As per asset there will be a notification created on start
        it is possible that several elements with the same previous state are returned.*/
        messages.stream()
                .filter(notificationMessage -> notificationMessage.getNotificationStatus().equals(previousStatus))
                .forEach(notificationMessage -> {
                    NotificationMessage notificationMessageSwitchedSenderAndReceiver = notificationMessage.copyAndSwitchSenderAndReceiver(traceabilityProperties.getBpn());
                    notificationMessageSwitchedSenderAndReceiver.setId(UUID.randomUUID().toString());
                    notificationMessageSwitchedSenderAndReceiver.changeStatusTo(notificationStatus);
                    notificationMessageSwitchedSenderAndReceiver.setDescription(reason);
                    notification.addNotificationMessage(notificationMessageSwitchedSenderAndReceiver);
                });

        Notification updatedNotification;
        try {
            updatedNotification = notificationPublisherService.updateNotificationPublisher(notification, notificationStatus, reason);
        } catch (SendNotificationException exception) {
            log.info("Notification status rollback", exception);
            throw new SendNotificationException(exception.getMessage());
        }

        getNotificationRepository().updateNotification(updatedNotification);
    }

    @Override
    public void editNotification(EditNotification editNotification) {
        validateReceiverIsNotOwnBpn(editNotification.getReceiverBpn(), editNotification.getId());
        Notification notification = loadOrNotFoundException(new NotificationId(editNotification.getId()));
        List<AssetBase> affectedParts = assetAsBuiltRepository.getAssetsById(editNotification.getAffectedPartIds());
        List<BpnEdcMapping> bpnMappings = bpnRepository.findAllByIdIn(affectedParts.stream().map(AssetBase::getManufacturerId).toList());

        List<String> oldMessageIds =
                notification.getNotifications().stream().map(NotificationMessage::getId).toList();

        getNotificationRepository().deleteByIdIn(oldMessageIds);
        notification.clearNotifications();
        notification.createInitialNotifications(affectedParts, traceabilityProperties.getBpn(), editNotification, bpnMappings);
        if (editNotification.getReceiverBpn() != null) {
            notification.setBpn(BPN.of(editNotification.getReceiverBpn()));
        }

        notification.setTitle(editNotification.getTitle());

        if (editNotification.getDescription() != null) {
            notification.setDescription(editNotification.getDescription());
        }
        if (editNotification.getAffectedPartIds() != null) {
            notification.setAffectedPartIds(editNotification.getAffectedPartIds());
        }


        getNotificationRepository().updateNotificationAndMessage(notification, editNotification.getSeverity());
    }

    @Override
    public Notification find(Long id) {
        NotificationId investigationId = new NotificationId(id);
        return loadOrNotFoundException(investigationId);
    }

    @Override
    public void approve(Long notificationId) {
        Notification notification = loadOrNotFoundException(new NotificationId(notificationId));
        List<NotificationMessage> createdNotifications = notification
                .getNotifications()
                .stream()
                .filter(notificationMessage -> notificationMessage.getNotificationStatus().equals(NotificationStatus.CREATED))
                .map(notificationMessage -> notificationMessage.toBuilder().build())
                .toList();

        log.info("Found {} notification messages in status CREATED", createdNotifications.size());
        List<NotificationMessage> approvedNotifications = new ArrayList<>(createdNotifications);
        approvedNotifications.forEach(notificationMessage -> {
            notificationMessage.setId(UUID.randomUUID().toString());
            notificationMessage.changeStatusTo(NotificationStatus.SENT);
        });
        log.info("Found {} notification messages in status SENT", approvedNotifications.size());

        notification.addNotificationMessages(approvedNotifications);
        log.info("Found {} notification messages at all", notification.getNotifications().size());
        notification.getNotifications().stream().map(notificationMessage -> notificationMessage.getNotificationStatus().name()).forEach(s -> log.info("Notification Status {} ", s));

        final Notification approvedInvestigation;
        try {
            approvedInvestigation = notificationPublisherService.approveNotification(notification);
        } catch (SendNotificationException exception) {
            log.info("Notification status rollback", exception);
            throw new SendNotificationException(exception.getMessage());
        }
        getNotificationRepository().updateNotification(approvedInvestigation);
    }

    @Override
    public void cancel(Long notificationId) {
        Notification notification = loadOrNotFoundException(new NotificationId(notificationId));
        Notification canceledNotification = notificationPublisherService.cancelNotification(notification);

        getNotificationRepository().updateNotification(canceledNotification);
    }

    @Override
    public List<String> getDistinctFilterValues(String fieldName, String startWith, Integer size, NotificationSide side) {
        final Integer resultSize = Objects.isNull(size) ? Integer.MAX_VALUE : size;

        if (isSupportedEnumType(fieldName)) {
            return getAssetEnumFieldValues(fieldName);
        }
        return getNotificationRepository().getDistinctFieldValues(fieldName, startWith, resultSize, side);
    }

    @Override
    public Notification loadOrNotFoundException(NotificationId investigationId) {
        return getNotificationRepository().findOptionalNotificationById(investigationId)
                .orElseThrow(() -> getNotFoundException(investigationId.value().toString()));
    }

    @Override
    public Notification loadByEdcNotificationIdOrNotFoundException(String edcNotificationId) {
        return getNotificationRepository().findByEdcNotificationId(edcNotificationId)
                .orElseThrow(() -> getNotFoundException(edcNotificationId));
    }

    private boolean isSupportedEnumType(String fieldName) {
        return SUPPORTED_ENUM_FIELDS.contains(fieldName);
    }

    private List<String> getAssetEnumFieldValues(String fieldName) {
        return switch (fieldName) {
            case "status" -> Arrays.stream(NotificationStatus.values()).map(Enum::name).toList();
            case "side" -> Arrays.stream(NotificationSide.values()).map(Enum::name).toList();
            case "messages_severity" -> Arrays.stream(NotificationSeverity.values()).map(Enum::name).toList();
            case "type" -> Arrays.stream(NotificationType.values()).map(Enum::name).toList();
            default -> null;
        };
    }

    private void validateReceiverIsNotOwnBpn(String bpn, Long notificationId) {
        if (traceabilityProperties.getBpn().value().equals(bpn)) {
            if (notificationId != null) {
                throw new NotificationSenderAndReceiverBPNEqualException(bpn, notificationId);
            } else {
                throw new NotificationSenderAndReceiverBPNEqualException(bpn);
            }
        }

    }

}
