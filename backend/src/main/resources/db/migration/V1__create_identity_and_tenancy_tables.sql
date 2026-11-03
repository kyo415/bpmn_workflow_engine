
-- ==========================================
-- V1: Create Identity & Tenancy tables
-- ==========================================

-- 1. Tenants: logistics companies
CREATE TABLE tenants (
    id UUID PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    code VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT uk_tenants_code UNIQUE (code),
    CONSTRAINT ck_tenants_status
        CHECK (status IN ('ACTIVE', 'SUSPENDED'))
);

-- 2. Users: platform user accounts
CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    system_role VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT ck_users_system_role
        CHECK (system_role IN ('USER', 'SUPER_ADMIN')),
    CONSTRAINT ck_users_status
        CHECK (status IN ('ACTIVE', 'DISABLED'))
);

-- 3. Roles: shared system roles
CREATE TABLE roles (
    id UUID PRIMARY KEY,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,

    CONSTRAINT uk_roles_code UNIQUE (code)
);

-- 4. User-Tenant membership
CREATE TABLE user_tenants (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    role_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL,
    joined_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT uk_user_tenants_user_tenant
        UNIQUE (user_id, tenant_id),

    CONSTRAINT ck_user_tenants_status
        CHECK (status IN ('ACTIVE', 'SUSPENDED')),

    CONSTRAINT fk_user_tenants_user
        FOREIGN KEY (user_id)
        REFERENCES users (id),

    CONSTRAINT fk_user_tenants_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants (id),

    CONSTRAINT fk_user_tenants_role
        FOREIGN KEY (role_id)
        REFERENCES roles (id)
);

-- 5. Indexes for membership lookups
CREATE INDEX idx_user_tenants_user_id
    ON user_tenants (user_id);

CREATE INDEX idx_user_tenants_tenant_id
    ON user_tenants (tenant_id);

-- 6. Seed default roles
INSERT INTO roles (id, code, name, description)
VALUES
(
    gen_random_uuid(),
    'TENANT_ADMIN',
    'Tenant Administrator',
    'Manages users and operations within a logistics company.'
),
(
    gen_random_uuid(),
    'LOGISTICS_MANAGER',
    'Logistics Manager',
    'Manages logistics workflows, shipments and task assignments.'
),
(
    gen_random_uuid(),
    'OPERATOR',
    'Operator',
    'Handles assigned logistics tasks and shipment operations.'
);
