//-------------------- M23 ----------------------
package com.condotrack.backend.service;

import com.condotrack.backend.dto.UnitDashboardResponse;
import com.condotrack.backend.model.Enums.DeliveryStatus;
import com.condotrack.backend.model.Enums.MoveRequestStatus;
import com.condotrack.backend.repository.AccessLogRepository;
import com.condotrack.backend.repository.BookingRepository;
import com.condotrack.backend.repository.DeliveryRepository;
import com.condotrack.backend.repository.IncidentRepository;
import com.condotrack.backend.repository.MaintenanceRequestRepository;
import com.condotrack.backend.repository.MoveRequestRepository;
import com.condotrack.backend.repository.ResidentRepository;
import com.condotrack.backend.repository.UnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UnitDashboardService {
    private final UnitRepository unitRepository;
    private final ResidentRepository residentRepository;
    private final AccessLogRepository accessLogRepository;
    private final DeliveryRepository deliveryRepository;
    private final BookingRepository bookingRepository;
    private final MoveRequestRepository moveRequestRepository;
    private final IncidentRepository incidentRepository;
    private final MaintenanceRequestRepository maintenanceRequestRepository;

    @Transactional(readOnly = true)
    public UnitDashboardResponse getUnitDashboard(UUID unitId) {
        var unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new IllegalArgumentException("Unit not found: " + unitId));

        var residents = residentRepository.findByUnitIdAndActiveTrue(unitId).stream()
                .map(r -> new UnitDashboardResponse.ResidentSummary(
                        r.getId(), r.getUser().getId(), r.getUser().getFirstName(), r.getUser().getLastName(),
                        r.getResidentType().name(), r.isPrimaryContact()))
                .toList();

        var accessHistory = accessLogRepository.findTop50ByUnitIdOrderByOccurredAtDesc(unitId).stream()
                .map(a -> new UnitDashboardResponse.AccessLogSummary(
                        a.getId(), a.getDirection().name(), a.getAccessMethod().name(), a.getOccurredAt(),
                        a.getVisitor() == null ? null : a.getVisitor().getId(),
                        a.getAuthorization() == null ? null : a.getAuthorization().getId()))
                .toList();

        var pendingDeliveries = deliveryRepository
                .findByUnitIdAndStatusInOrderByReceivedAtDesc(unitId, List.of(DeliveryStatus.RECEIVED, DeliveryStatus.NOTIFIED))
                .stream()
                .map(d -> new UnitDashboardResponse.DeliverySummary(
                        d.getId(), d.getDeliveryType().name(), d.getStatus().name(), d.getCarrierName(),
                        d.getTrackingNumber(), d.getReceivedAt(), d.getNotifiedAt()))
                .toList();

        var bookings = bookingRepository.findByUnitIdOrderByStartAtDesc(unitId).stream()
                .map(b -> new UnitDashboardResponse.BookingSummary(
                        b.getId(), b.getCommonArea().getId(), b.getCommonArea().getName(), b.getStatus().name(), b.getStartAt(), b.getEndAt(), b.getPurpose()))
                .toList();

        var activeMoveRequests = moveRequestRepository.findByUnitIdAndStatusNotInOrderByRequestedAtDesc(
                        unitId, List.of(MoveRequestStatus.COMPLETED, MoveRequestStatus.CANCELLED, MoveRequestStatus.REJECTED))
                .stream()
                .map(m -> new UnitDashboardResponse.MoveRequestSummary(
                        m.getId(), m.getRequestType().name(), m.getStatus().name(), m.getRequestedAt(),
                        m.getScheduledStart(), m.getScheduledEnd()))
                .toList();

        var incidents = incidentRepository.findByUnitIdOrderByCreatedAtDesc(unitId).stream()
                .map(i -> new UnitDashboardResponse.IncidentSummary(
                        i.getId(), i.getStatus().name(), i.getSeverity().name(), i.getTitle(),
                        i.getOccurredAt(), i.getResolvedAt(), i.getResolution()))
                .toList();

        var maintenanceRequests = maintenanceRequestRepository.findByUnitIdOrderByCreatedAtDesc(unitId).stream()
                .map(m -> new UnitDashboardResponse.MaintenanceSummary(
                        m.getId(), m.getIncident() == null ? null : m.getIncident().getId(), m.getStatus().name(),
                        m.getPriority().name(), m.getCategory(), m.getScheduledAt(), m.getCompletedAt(),
                        m.getDescription(), m.getResolution()))
                .toList();

        return new UnitDashboardResponse(
                new UnitDashboardResponse.UnitSummary(
                        unit.getId(), unit.getBuilding().getId(), unit.getBuilding().getName(), unit.getUnitNumber(), unit.getFloorNumber(), unit.getUnitType().name()),
                residents, accessHistory, pendingDeliveries, bookings, activeMoveRequests, incidents, maintenanceRequests);
    }
}
