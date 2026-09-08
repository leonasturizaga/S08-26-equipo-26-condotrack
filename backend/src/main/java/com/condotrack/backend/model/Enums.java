package com.condotrack.backend.model;

public final class Enums {
    private Enums() {}

    public enum UnitType { RESIDENTIAL, COMMERCIAL, PARKING, STORAGE, OTHER }
    public enum ResidentType { OWNER, TENANT, OCCUPANT, AUTHORIZED_RESIDENT }
    public enum StaffType { ADMINISTRATOR, RECEPTION, MAINTENANCE, SECURITY, OTHER }
    public enum VisitorAuthorizationStatus { PENDING, APPROVED, REJECTED, EXPIRED, CANCELLED, USED }
    public enum AccessDirection { IN, OUT }
    public enum AccessMethod { MANUAL, QR, CARD, BIOMETRIC, OTHER }
    public enum DeliveryStatus { RECEIVED, NOTIFIED, COLLECTED, RETURNED, CANCELLED }
    public enum DeliveryType { PACKAGE, MAIL, FOOD, DOCUMENT, OTHER }
    public enum BookingStatus { PENDING, APPROVED, REJECTED, CANCELLED, COMPLETED, NO_SHOW }
    public enum MoveRequestType { MOVE_IN, MOVE_OUT }
    public enum MoveRequestStatus { REQUESTED, PENDING_APPROVAL, APPROVED, REJECTED, SCHEDULED, COMPLETED, CANCELLED }
    public enum IncidentStatus { CREATED, ASSIGNED, IN_PROGRESS, RESOLVED, CLOSED, CANCELLED }
    public enum Severity { LOW, MEDIUM, HIGH, CRITICAL }
    public enum MaintenanceStatus { CREATED, ASSIGNED, IN_PROGRESS, ON_HOLD, RESOLVED, CLOSED, CANCELLED }
    public enum Priority { LOW, MEDIUM, HIGH, URGENT }
    public enum NotificationStatus { PENDING, SENT, DELIVERED, READ, FAILED, CANCELLED }
    public enum NotificationChannel { IN_APP, EMAIL, SMS, PUSH, OTHER }
}
