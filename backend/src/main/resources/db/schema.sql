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

CREATE TABLE IF NOT EXISTS permissions (
    id UUID PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by UUID
);

CREATE TABLE IF NOT EXISTS role_permissions (
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by UUID,
    PRIMARY KEY (role_id, permission_id)
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
CREATE INDEX IF NOT EXISTS idx_role_permissions_permission_id ON role_permissions(permission_id);

INSERT INTO roles (id, code, name)
VALUES
    ('11111111-1111-1111-1111-111111111101', 'ADMINISTRATOR', 'Administrator'),
    ('11111111-1111-1111-1111-111111111102', 'RECEPTION', 'Reception / Front Desk'),
    ('11111111-1111-1111-1111-111111111103', 'RESIDENT', 'Resident'),
    ('11111111-1111-1111-1111-111111111104', 'OWNER', 'Owner'),
    ('11111111-1111-1111-1111-111111111105', 'PROVIDER', 'Provider')
ON CONFLICT (code) DO NOTHING;


INSERT INTO permissions (id, code, name, description)
VALUES
    ('22222222-2222-2222-2222-222222220001', 'BUILDINGS_VIEW', 'View buildings', 'View building records and building-level information.'),
    ('22222222-2222-2222-2222-222222220002', 'BUILDINGS_CREATE', 'Create buildings', 'Create new building records.'),
    ('22222222-2222-2222-2222-222222220003', 'BUILDINGS_UPDATE', 'Update buildings', 'Modify building records.'),
    ('22222222-2222-2222-2222-222222220004', 'UNITS_VIEW', 'View units', 'View unit records.'),
    ('22222222-2222-2222-2222-222222220005', 'UNITS_VIEW_OWN', 'View own units', 'View units associated with the authenticated user.'),
    ('22222222-2222-2222-2222-222222220006', 'UNITS_CREATE', 'Create units', 'Create unit records.'),
    ('22222222-2222-2222-2222-222222220007', 'UNITS_UPDATE', 'Update units', 'Modify unit records.'),
    ('22222222-2222-2222-2222-222222220008', 'UNITS_UPDATE_OWN', 'Update own units', 'Modify unit information within the user-owned scope.'),
    ('22222222-2222-2222-2222-222222220009', 'RESIDENTS_VIEW', 'View residents', 'View resident records.'),
    ('22222222-2222-2222-2222-222222220010', 'RESIDENTS_VIEW_OWN', 'View own resident data', 'View resident data within the user-owned scope.'),
    ('22222222-2222-2222-2222-222222220011', 'RESIDENTS_CREATE', 'Create residents', 'Create resident records.'),
    ('22222222-2222-2222-2222-222222220012', 'RESIDENTS_UPDATE', 'Update residents', 'Modify resident records.'),
    ('22222222-2222-2222-2222-222222220013', 'RESIDENTS_UPDATE_OWN', 'Update own resident data', 'Modify the authenticated user profile or own resident data.'),
    ('22222222-2222-2222-2222-222222220014', 'ACCESS_VIEW', 'View access', 'View access and visitor records.'),
    ('22222222-2222-2222-2222-222222220015', 'ACCESS_VIEW_OWN', 'View own access', 'View access records within the user-owned scope.'),
    ('22222222-2222-2222-2222-222222220016', 'ACCESS_CREATE', 'Create access records', 'Create visitor authorizations or access records.'),
    ('22222222-2222-2222-2222-222222220017', 'ACCESS_CREATE_OWN', 'Create own access authorizations', 'Create visitor authorizations for the authenticated user scope.'),
    ('22222222-2222-2222-2222-222222220018', 'ACCESS_UPDATE', 'Update access records', 'Modify access and visitor records.'),
    ('22222222-2222-2222-2222-222222220019', 'ACCESS_UPDATE_OWN', 'Update own access records', 'Modify access records within the user-owned scope.'),
    ('22222222-2222-2222-2222-222222220020', 'DELIVERIES_VIEW', 'View deliveries', 'View delivery and mail records.'),
    ('22222222-2222-2222-2222-222222220021', 'DELIVERIES_VIEW_OWN', 'View own deliveries', 'View deliveries assigned to the authenticated user scope.'),
    ('22222222-2222-2222-2222-222222220022', 'DELIVERIES_CREATE', 'Create deliveries', 'Register received deliveries or mail.'),
    ('22222222-2222-2222-2222-222222220023', 'DELIVERIES_UPDATE', 'Update deliveries', 'Update delivery status and operational information.'),
    ('22222222-2222-2222-2222-222222220024', 'DELIVERIES_UPDATE_OWN', 'Update own deliveries', 'Update deliveries within the authenticated user scope.'),
    ('22222222-2222-2222-2222-222222220025', 'BOOKINGS_VIEW', 'View bookings', 'View common-area bookings.'),
    ('22222222-2222-2222-2222-222222220026', 'BOOKINGS_VIEW_OWN', 'View own bookings', 'View bookings created by or associated with the authenticated user.'),
    ('22222222-2222-2222-2222-222222220027', 'BOOKINGS_CREATE', 'Create bookings', 'Create common-area bookings.'),
    ('22222222-2222-2222-2222-222222220028', 'BOOKINGS_CREATE_OWN', 'Create own bookings', 'Create bookings within the authenticated user scope.'),
    ('22222222-2222-2222-2222-222222220029', 'BOOKINGS_UPDATE', 'Update bookings', 'Modify bookings and booking rules.'),
    ('22222222-2222-2222-2222-222222220030', 'BOOKINGS_UPDATE_OWN', 'Update own bookings', 'Modify bookings within the authenticated user scope.'),
    ('22222222-2222-2222-2222-222222220031', 'BOOKINGS_CANCEL_OWN', 'Cancel own bookings', 'Cancel bookings created by or associated with the authenticated user.'),
    ('22222222-2222-2222-2222-222222220032', 'INCIDENTS_VIEW', 'View incidents', 'View incident records.'),
    ('22222222-2222-2222-2222-222222220033', 'INCIDENTS_VIEW_OWN', 'View own incidents', 'View incidents created by or associated with the authenticated user.'),
    ('22222222-2222-2222-2222-222222220034', 'INCIDENTS_VIEW_ASSIGNED', 'View assigned incidents', 'View incidents assigned to the authenticated provider or staff user.'),
    ('22222222-2222-2222-2222-222222220035', 'INCIDENTS_CREATE', 'Create incidents', 'Create incident records.'),
    ('22222222-2222-2222-2222-222222220036', 'INCIDENTS_CREATE_OWN', 'Create own incidents', 'Create incidents within the authenticated user scope.'),
    ('22222222-2222-2222-2222-222222220037', 'INCIDENTS_UPDATE', 'Update incidents', 'Modify incident records.'),
    ('22222222-2222-2222-2222-222222220038', 'INCIDENTS_UPDATE_OWN', 'Update own incidents', 'Modify incidents within the authenticated user scope.'),
    ('22222222-2222-2222-2222-222222220039', 'INCIDENTS_UPDATE_ASSIGNED', 'Update assigned incidents', 'Update incidents assigned to the authenticated user.'),
    ('22222222-2222-2222-2222-222222220040', 'INCIDENTS_ASSIGN', 'Assign incidents', 'Assign incidents to staff or providers.'),
    ('22222222-2222-2222-2222-222222220041', 'MAINTENANCE_VIEW', 'View maintenance', 'View maintenance requests.'),
    ('22222222-2222-2222-2222-222222220042', 'MAINTENANCE_VIEW_OWN', 'View own maintenance', 'View maintenance requests created by or associated with the authenticated user.'),
    ('22222222-2222-2222-2222-222222220043', 'MAINTENANCE_VIEW_ASSIGNED', 'View assigned maintenance', 'View maintenance requests assigned to the authenticated provider or staff user.'),
    ('22222222-2222-2222-2222-222222220044', 'MAINTENANCE_CREATE', 'Create maintenance', 'Create maintenance requests.'),
    ('22222222-2222-2222-2222-222222220045', 'MAINTENANCE_CREATE_OWN', 'Create own maintenance', 'Create maintenance requests within the authenticated user scope.'),
    ('22222222-2222-2222-2222-222222220046', 'MAINTENANCE_UPDATE', 'Update maintenance', 'Modify maintenance requests.'),
    ('22222222-2222-2222-2222-222222220047', 'MAINTENANCE_UPDATE_OWN', 'Update own maintenance', 'Modify maintenance requests within the authenticated user scope.'),
    ('22222222-2222-2222-2222-222222220048', 'MAINTENANCE_UPDATE_ASSIGNED', 'Update assigned maintenance', 'Update maintenance requests assigned to the authenticated user.'),
    ('22222222-2222-2222-2222-222222220049', 'MAINTENANCE_ASSIGN', 'Assign maintenance', 'Assign maintenance requests to staff or providers.'),
    ('22222222-2222-2222-2222-222222220050', 'MOVES_VIEW', 'View move requests', 'View move requests.'),
    ('22222222-2222-2222-2222-222222220051', 'MOVES_VIEW_OWN', 'View own move requests', 'View move requests within the authenticated user scope.'),
    ('22222222-2222-2222-2222-222222220052', 'MOVES_CREATE', 'Create move requests', 'Create move requests.'),
    ('22222222-2222-2222-2222-222222220053', 'MOVES_CREATE_OWN', 'Create own move requests', 'Create move requests within the authenticated user scope.'),
    ('22222222-2222-2222-2222-222222220054', 'MOVES_APPROVE', 'Approve move requests', 'Approve or authorize move requests.'),
    ('22222222-2222-2222-2222-222222220055', 'REPORTS_VIEW', 'View reports', 'View operational reports and KPIs.'),
    ('22222222-2222-2222-2222-222222220056', 'BUILDING_CONFIG_VIEW', 'View building configuration', 'View building configuration.'),
    ('22222222-2222-2222-2222-222222220057', 'BUILDING_CONFIG_UPDATE', 'Update building configuration', 'Modify building configuration and policies.'),
    ('22222222-2222-2222-2222-222222220058', 'USER_MANAGEMENT_VIEW', 'View users', 'View application users and staff records.'),
    ('22222222-2222-2222-2222-222222220059', 'USER_MANAGEMENT_CREATE', 'Create users', 'Create application users and staff.'),
    ('22222222-2222-2222-2222-222222220060', 'USER_MANAGEMENT_UPDATE', 'Update users', 'Modify users, roles and account state.'),
    ('22222222-2222-2222-2222-222222220061', 'COMMUNICATIONS_VIEW', 'View communications', 'Receive and view announcements and notifications.'),
    ('22222222-2222-2222-2222-222222220062', 'COMMUNICATIONS_CREATE', 'Create communications', 'Create and send announcements and communications.')
ON CONFLICT (code) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'ADMINISTRATOR'
ON CONFLICT (role_id, permission_id) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN (
    'UNITS_VIEW',
    'RESIDENTS_VIEW',
    'ACCESS_VIEW', 'ACCESS_CREATE', 'ACCESS_UPDATE',
    'DELIVERIES_VIEW', 'DELIVERIES_CREATE', 'DELIVERIES_UPDATE',
    'BOOKINGS_VIEW',
    'INCIDENTS_VIEW', 'INCIDENTS_CREATE',
    'MAINTENANCE_VIEW', 'MAINTENANCE_CREATE',
    'MOVES_VIEW'
)
WHERE r.code = 'RECEPTION'
ON CONFLICT (role_id, permission_id) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN (
    'UNITS_VIEW_OWN',
    'RESIDENTS_VIEW_OWN', 'RESIDENTS_UPDATE_OWN',
    'ACCESS_VIEW_OWN', 'ACCESS_CREATE_OWN', 'ACCESS_UPDATE_OWN',
    'DELIVERIES_VIEW_OWN',
    'BOOKINGS_VIEW_OWN', 'BOOKINGS_CREATE_OWN', 'BOOKINGS_UPDATE_OWN', 'BOOKINGS_CANCEL_OWN',
    'INCIDENTS_VIEW_OWN', 'INCIDENTS_CREATE_OWN', 'INCIDENTS_UPDATE_OWN',
    'MAINTENANCE_VIEW_OWN', 'MAINTENANCE_CREATE_OWN', 'MAINTENANCE_UPDATE_OWN',
    'MOVES_VIEW_OWN', 'MOVES_CREATE_OWN',
    'COMMUNICATIONS_VIEW'
)
WHERE r.code = 'RESIDENT'
ON CONFLICT (role_id, permission_id) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN (
    'UNITS_VIEW_OWN',
    'RESIDENTS_VIEW_OWN', 'RESIDENTS_UPDATE_OWN',
    'INCIDENTS_VIEW',
    'MAINTENANCE_VIEW',
    'MOVES_VIEW', 'MOVES_APPROVE',
    'COMMUNICATIONS_VIEW'
)
WHERE r.code = 'OWNER'
ON CONFLICT (role_id, permission_id) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN (
    'INCIDENTS_VIEW_ASSIGNED',
    'INCIDENTS_UPDATE_ASSIGNED',
    'MAINTENANCE_VIEW_ASSIGNED',
    'MAINTENANCE_UPDATE_ASSIGNED'
)
WHERE r.code = 'PROVIDER'
ON CONFLICT (role_id, permission_id) DO NOTHING;

CREATE UNIQUE INDEX IF NOT EXISTS uq_active_resident_user_unit
    ON residents(user_id, unit_id)
    WHERE active = TRUE;