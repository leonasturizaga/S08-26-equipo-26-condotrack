CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(50),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by UUID
);

CREATE TABLE IF NOT EXISTS roles (
    id UUID PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by UUID
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by UUID,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE IF NOT EXISTS buildings (
    id UUID PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    address_line_1 VARCHAR(255) NOT NULL,
    address_line_2 VARCHAR(255),
    city VARCHAR(120) NOT NULL,
    state_province VARCHAR(120),
    postal_code VARCHAR(30),
    country VARCHAR(120) NOT NULL,
    timezone VARCHAR(80) NOT NULL DEFAULT 'UTC',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by UUID
);

CREATE TABLE IF NOT EXISTS units (
    id UUID PRIMARY KEY,
    building_id UUID NOT NULL REFERENCES buildings(id) ON DELETE CASCADE,
    unit_number VARCHAR(50) NOT NULL,
    floor_number INTEGER,
    unit_type VARCHAR(30) NOT NULL DEFAULT 'RESIDENTIAL',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by UUID,
    CONSTRAINT uq_units_building_number UNIQUE (building_id, unit_number),
    CONSTRAINT ck_units_type CHECK (unit_type IN ('RESIDENTIAL', 'COMMERCIAL', 'PARKING', 'STORAGE', 'OTHER'))
);

CREATE TABLE IF NOT EXISTS residents (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    unit_id UUID NOT NULL REFERENCES units(id),
    resident_type VARCHAR(30) NOT NULL,
    move_in_date DATE,
    move_out_date DATE,
    primary_contact BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by UUID,
    CONSTRAINT ck_resident_type CHECK (resident_type IN ('OWNER', 'TENANT', 'OCCUPANT', 'AUTHORIZED_RESIDENT')),
    CONSTRAINT ck_resident_dates CHECK (move_out_date IS NULL OR move_in_date IS NULL OR move_out_date >= move_in_date)
);

CREATE TABLE IF NOT EXISTS staff (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    building_id UUID NOT NULL REFERENCES buildings(id),
    staff_type VARCHAR(30) NOT NULL,
    employee_code VARCHAR(80),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by UUID,
    CONSTRAINT ck_staff_type CHECK (staff_type IN ('ADMINISTRATOR', 'RECEPTION', 'MAINTENANCE', 'SECURITY', 'OTHER')),
    CONSTRAINT uq_staff_building_employee UNIQUE (building_id, employee_code)
);

CREATE TABLE IF NOT EXISTS visitors (
    id UUID PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    document_type VARCHAR(40),
    document_number VARCHAR(100),
    phone VARCHAR(50),
    company_name VARCHAR(150),
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by UUID
);

CREATE TABLE IF NOT EXISTS visitor_authorizations (
    id UUID PRIMARY KEY,
    building_id UUID NOT NULL REFERENCES buildings(id),
    unit_id UUID NOT NULL REFERENCES units(id),
    resident_id UUID REFERENCES residents(id),
    visitor_id UUID NOT NULL REFERENCES visitors(id),
    qr_token VARCHAR(255) UNIQUE,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    valid_from TIMESTAMPTZ NOT NULL,
    valid_until TIMESTAMPTZ NOT NULL,
    purpose VARCHAR(255),
    approved_by UUID REFERENCES users(id),
    approved_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by UUID,
    CONSTRAINT ck_visitor_auth_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'EXPIRED', 'CANCELLED', 'USED')),
    CONSTRAINT ck_visitor_auth_dates CHECK (valid_until > valid_from)
);

CREATE TABLE IF NOT EXISTS access_logs (
    id UUID PRIMARY KEY,
    building_id UUID NOT NULL REFERENCES buildings(id),
    unit_id UUID NOT NULL REFERENCES units(id),
    visitor_id UUID REFERENCES visitors(id),
    authorization_id UUID REFERENCES visitor_authorizations(id),
    handled_by_staff_id UUID REFERENCES staff(id),
    direction VARCHAR(10) NOT NULL,
    access_method VARCHAR(30) NOT NULL DEFAULT 'MANUAL',
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by UUID,
    CONSTRAINT ck_access_direction CHECK (direction IN ('IN', 'OUT')),
    CONSTRAINT ck_access_method CHECK (access_method IN ('MANUAL', 'QR', 'CARD', 'BIOMETRIC', 'OTHER'))
);

CREATE TABLE IF NOT EXISTS deliveries (
    id UUID PRIMARY KEY,
    building_id UUID NOT NULL REFERENCES buildings(id),
    unit_id UUID NOT NULL REFERENCES units(id),
    resident_id UUID REFERENCES residents(id),
    received_by_staff_id UUID REFERENCES staff(id),
    carrier_name VARCHAR(150),
    tracking_number VARCHAR(150),
    delivery_type VARCHAR(30) NOT NULL DEFAULT 'PACKAGE',
    status VARCHAR(30) NOT NULL DEFAULT 'RECEIVED',
    received_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notified_at TIMESTAMPTZ,
    collected_at TIMESTAMPTZ,
    collected_by_user_id UUID REFERENCES users(id),
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by UUID,
    CONSTRAINT ck_delivery_status CHECK (status IN ('RECEIVED', 'NOTIFIED', 'COLLECTED', 'RETURNED', 'CANCELLED')),
    CONSTRAINT ck_delivery_type CHECK (delivery_type IN ('PACKAGE', 'MAIL', 'FOOD', 'DOCUMENT', 'OTHER'))
);

CREATE TABLE IF NOT EXISTS common_areas (
    id UUID PRIMARY KEY,
    building_id UUID NOT NULL REFERENCES buildings(id) ON DELETE CASCADE,
    name VARCHAR(150) NOT NULL,
    area_type VARCHAR(50) NOT NULL,
    capacity INTEGER,
    booking_required BOOLEAN NOT NULL DEFAULT TRUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    booking_duration_minutes INTEGER,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by UUID,
    CONSTRAINT uq_common_area_name UNIQUE (building_id, name),
    CONSTRAINT ck_common_area_capacity CHECK (capacity IS NULL OR capacity > 0),
    CONSTRAINT ck_common_area_duration CHECK (booking_duration_minutes IS NULL OR booking_duration_minutes > 0)
);

CREATE TABLE IF NOT EXISTS bookings (
    id UUID PRIMARY KEY,
    building_id UUID NOT NULL REFERENCES buildings(id),
    common_area_id UUID NOT NULL REFERENCES common_areas(id),
    unit_id UUID NOT NULL REFERENCES units(id),
    resident_id UUID REFERENCES residents(id),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    start_at TIMESTAMPTZ NOT NULL,
    end_at TIMESTAMPTZ NOT NULL,
    purpose VARCHAR(255),
    approved_by_staff_id UUID REFERENCES staff(id),
    approved_at TIMESTAMPTZ,
    cancellation_reason TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by UUID,

    CONSTRAINT ck_booking_status
        CHECK (status IN (
            'PENDING',
            'APPROVED',
            'REJECTED',
            'CANCELLED',
            'COMPLETED',
            'NO_SHOW'
        )),

    CONSTRAINT ck_booking_dates
        CHECK (end_at > start_at),

    CONSTRAINT ex_bookings_no_overlap
        EXCLUDE USING gist (
            common_area_id WITH =,
            tstzrange(start_at, end_at, '[)') WITH &&
        )
        WHERE (status IN ('PENDING', 'APPROVED'))
);

CREATE TABLE IF NOT EXISTS move_requests (
    id UUID PRIMARY KEY,
    building_id UUID NOT NULL REFERENCES buildings(id),
    unit_id UUID NOT NULL REFERENCES units(id),
    resident_id UUID REFERENCES residents(id),
    request_type VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'REQUESTED',
    requested_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    scheduled_start TIMESTAMPTZ,
    scheduled_end TIMESTAMPTZ,
    approved_by_staff_id UUID REFERENCES staff(id),
    approved_at TIMESTAMPTZ,
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by UUID,
    CONSTRAINT ck_move_request_type CHECK (request_type IN ('MOVE_IN', 'MOVE_OUT')),
    CONSTRAINT ck_move_request_status CHECK (status IN ('REQUESTED', 'PENDING_APPROVAL', 'APPROVED', 'REJECTED', 'SCHEDULED', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT ck_move_request_dates CHECK (scheduled_end IS NULL OR scheduled_start IS NULL OR scheduled_end > scheduled_start)
);

CREATE TABLE IF NOT EXISTS incidents (
    id UUID PRIMARY KEY,
    building_id UUID NOT NULL REFERENCES buildings(id),
    unit_id UUID NOT NULL REFERENCES units(id),
    reported_by_user_id UUID NOT NULL REFERENCES users(id),
    assigned_to_staff_id UUID REFERENCES staff(id),
    status VARCHAR(30) NOT NULL DEFAULT 'CREATED',
    severity VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    occurred_at TIMESTAMPTZ,
    resolved_at TIMESTAMPTZ,
    resolution TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by UUID,
    CONSTRAINT ck_incident_status CHECK (status IN ('CREATED', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'CLOSED', 'CANCELLED')),
    CONSTRAINT ck_incident_severity CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL'))
);

CREATE TABLE IF NOT EXISTS maintenance_requests (
    id UUID PRIMARY KEY,
    building_id UUID NOT NULL REFERENCES buildings(id),
    unit_id UUID NOT NULL REFERENCES units(id),
    incident_id UUID REFERENCES incidents(id),
    requested_by_user_id UUID NOT NULL REFERENCES users(id),
    assigned_to_staff_id UUID REFERENCES staff(id),
    status VARCHAR(30) NOT NULL DEFAULT 'CREATED',
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    category VARCHAR(60) NOT NULL,
    description TEXT NOT NULL,
    scheduled_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    resolution TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by UUID,
    CONSTRAINT ck_maintenance_status CHECK (status IN ('CREATED', 'ASSIGNED', 'IN_PROGRESS', 'ON_HOLD', 'RESOLVED', 'CLOSED', 'CANCELLED')),
    CONSTRAINT ck_maintenance_priority CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT'))
);

CREATE TABLE IF NOT EXISTS notifications (
    id UUID PRIMARY KEY,
    building_id UUID REFERENCES buildings(id),
    recipient_user_id UUID NOT NULL REFERENCES users(id),
    notification_type VARCHAR(40) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    channel VARCHAR(20) NOT NULL DEFAULT 'IN_APP',
    subject VARCHAR(255),
    message TEXT NOT NULL,
    related_entity_type VARCHAR(80),
    related_entity_id UUID,
    sent_at TIMESTAMPTZ,
    read_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by UUID,
    CONSTRAINT ck_notification_status CHECK (status IN ('PENDING', 'SENT', 'DELIVERED', 'READ', 'FAILED', 'CANCELLED')),
    CONSTRAINT ck_notification_channel CHECK (channel IN ('IN_APP', 'EMAIL', 'SMS', 'PUSH', 'OTHER'))
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id UUID PRIMARY KEY,
    building_id UUID REFERENCES buildings(id),
    entity_type VARCHAR(80) NOT NULL,
    entity_id UUID NOT NULL,
    action VARCHAR(80) NOT NULL,
    actor_user_id UUID REFERENCES users(id),
    previous_status VARCHAR(50),
    new_status VARCHAR(50),
    resolution TEXT,
    details JSONB,
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by UUID
);

CREATE INDEX IF NOT EXISTS idx_units_building_id ON units(building_id);
CREATE INDEX IF NOT EXISTS idx_residents_unit_id ON residents(unit_id);
CREATE INDEX IF NOT EXISTS idx_residents_user_id ON residents(user_id);
CREATE INDEX IF NOT EXISTS idx_staff_building_id ON staff(building_id);
CREATE INDEX IF NOT EXISTS idx_staff_user_id ON staff(user_id);
CREATE INDEX IF NOT EXISTS idx_visitor_auth_unit_id ON visitor_authorizations(unit_id);
CREATE INDEX IF NOT EXISTS idx_visitor_auth_visitor_id ON visitor_authorizations(visitor_id);
CREATE INDEX IF NOT EXISTS idx_visitor_auth_validity ON visitor_authorizations(valid_from, valid_until);
CREATE INDEX IF NOT EXISTS idx_access_logs_unit_id_occurred_at ON access_logs(unit_id, occurred_at DESC);
CREATE INDEX IF NOT EXISTS idx_access_logs_authorization_id ON access_logs(authorization_id);
CREATE INDEX IF NOT EXISTS idx_access_logs_building_id_occurred_at ON access_logs(building_id, occurred_at DESC);
CREATE INDEX IF NOT EXISTS idx_deliveries_unit_id_status ON deliveries(unit_id, status);
CREATE INDEX IF NOT EXISTS idx_deliveries_unit_id_received_at ON deliveries(unit_id, received_at DESC);
CREATE INDEX IF NOT EXISTS idx_deliveries_building_id_status ON deliveries(building_id, status);
CREATE INDEX IF NOT EXISTS idx_common_areas_building_id ON common_areas(building_id);
CREATE INDEX IF NOT EXISTS idx_bookings_unit_id_start_at ON bookings(unit_id, start_at DESC);
CREATE INDEX IF NOT EXISTS idx_bookings_common_area_time ON bookings(common_area_id, start_at, end_at);
CREATE INDEX IF NOT EXISTS idx_bookings_building_id_status ON bookings(building_id, status);
CREATE INDEX IF NOT EXISTS idx_move_requests_unit_id_status ON move_requests(unit_id, status);
CREATE INDEX IF NOT EXISTS idx_incidents_unit_id_status ON incidents(unit_id, status);
CREATE INDEX IF NOT EXISTS idx_incidents_building_id_status ON incidents(building_id, status);
CREATE INDEX IF NOT EXISTS idx_maintenance_unit_id_status ON maintenance_requests(unit_id, status);
CREATE INDEX IF NOT EXISTS idx_maintenance_building_id_status ON maintenance_requests(building_id, status);
CREATE INDEX IF NOT EXISTS idx_notifications_recipient_status ON notifications(recipient_user_id, status);
CREATE INDEX IF NOT EXISTS idx_audit_logs_entity ON audit_logs(entity_type, entity_id, occurred_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_logs_building ON audit_logs(building_id, occurred_at DESC);

INSERT INTO roles (id, code, name)
VALUES
    ('11111111-1111-1111-1111-111111111101', 'ADMINISTRATOR', 'Administrator'),
    ('11111111-1111-1111-1111-111111111102', 'RECEPTION', 'Reception / Front Desk'),
    ('11111111-1111-1111-1111-111111111103', 'RESIDENT', 'Resident')
ON CONFLICT (code) DO NOTHING;
