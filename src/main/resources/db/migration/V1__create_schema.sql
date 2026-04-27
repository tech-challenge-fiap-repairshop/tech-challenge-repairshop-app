-- ============================================================
-- Repair Shop — Initial Schema
-- ============================================================

-- Enums
CREATE TYPE status_os AS ENUM (
    'RECEIVED',
    'IN_DIAGNOSIS',
    'WAITING_APPROVAL',
    'APPROVED',
    'REFUSED',
    'IN_EXECUTION',
    'FINALIZED',
    'PAID',
    'CANCELED'
);

CREATE TYPE status_service AS ENUM (
    'INITIATED',
    'PENDING',
    'FINALIZED'
);

-- Customer
CREATE TABLE tb_customer (
    id_tb_customer UUID PRIMARY KEY,
    name           VARCHAR(150) NOT NULL,
    document       VARCHAR(14)  NOT NULL UNIQUE,
    email          VARCHAR(255) UNIQUE,
    phone          VARCHAR(20),
    birth_date     DATE,
    created        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Vehicle
CREATE TABLE tb_vehicle (
    id_tb_vehicle      UUID PRIMARY KEY,
    customer_id        UUID         NOT NULL REFERENCES tb_customer(id_tb_customer),
    plate              VARCHAR(7)   NOT NULL UNIQUE,
    brand              VARCHAR(50)  NOT NULL,
    model              VARCHAR(80)  NOT NULL,
    color              VARCHAR(30),
    manufacturing_date DATE,
    last_maintenance   TIMESTAMP,
    created            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Insume (inventory)
CREATE TABLE tb_insume (
    id_tb_insume UUID PRIMARY KEY,
    name         VARCHAR(150)   NOT NULL,
    brand        VARCHAR(100),
    sku_id       VARCHAR(50),
    quantity     INTEGER        NOT NULL DEFAULT 0,
    price        DECIMAL(10,2)  NOT NULL,
    unity_price  DECIMAL(10,2)  NOT NULL
);

-- User
CREATE TABLE tb_user (
    id_tb_user UUID PRIMARY KEY,
    name       VARCHAR(150) NOT NULL,
    function   VARCHAR(50)  NOT NULL,
    email      VARCHAR(255) NOT NULL UNIQUE,
    phone      VARCHAR(20),
    password   VARCHAR(255) NOT NULL
);

-- Service Order
CREATE TABLE tb_service_order (
    id_tb_service_order UUID PRIMARY KEY,
    customer_id         UUID           NOT NULL REFERENCES tb_customer(id_tb_customer),
    vehicle_id          UUID           NOT NULL REFERENCES tb_vehicle(id_tb_vehicle),
    status              VARCHAR(30)    NOT NULL DEFAULT 'RECEIVED',
    total_price         DECIMAL(12,2)  NOT NULL DEFAULT 0.00,
    enter_time          TIMESTAMP,
    end_time            TIMESTAMP,
    valid_date          DATE,
    created             TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated             TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Service Order History
CREATE TABLE tb_service_order_history (
    id_tb_service_order_history UUID PRIMARY KEY,
    service_order_id            UUID        NOT NULL REFERENCES tb_service_order(id_tb_service_order),
    status                      VARCHAR(30) NOT NULL,
    register_time               TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    interval_time               BIGINT
);

-- Execution
CREATE TABLE tb_execution (
    id_tb_execution UUID PRIMARY KEY,
    service_order   UUID          NOT NULL REFERENCES tb_service_order(id_tb_service_order),
    basic_description VARCHAR(50) NOT NULL,
    full_description  TEXT,
    price           DECIMAL(10,2) NOT NULL,
    estimated_time  DECIMAL(5,2),
    status          VARCHAR(20)   NOT NULL DEFAULT 'INITIATED',
    created         TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated         TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Execution-Insume junction
CREATE TABLE tb_execution_insume (
    id_tb_execution UUID NOT NULL REFERENCES tb_execution(id_tb_execution),
    id_tb_insume    UUID NOT NULL REFERENCES tb_insume(id_tb_insume),
    quantity_used   INTEGER NOT NULL DEFAULT 1,
    PRIMARY KEY (id_tb_execution, id_tb_insume)
);

-- Execution History
CREATE TABLE tb_execution_history (
    id_tb_execution_history UUID PRIMARY KEY,
    execution_id            UUID        NOT NULL REFERENCES tb_execution(id_tb_execution),
    status                  VARCHAR(20) NOT NULL,
    register_time           TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    interval_time           BIGINT
);

-- Indexes
CREATE INDEX idx_vehicle_customer_id ON tb_vehicle(customer_id);
CREATE INDEX idx_service_order_customer_id ON tb_service_order(customer_id);
CREATE INDEX idx_service_order_vehicle_id ON tb_service_order(vehicle_id);
CREATE INDEX idx_service_order_status ON tb_service_order(status);
CREATE INDEX idx_so_history_service_order_id ON tb_service_order_history(service_order_id);
CREATE INDEX idx_execution_service_order ON tb_execution(service_order);
CREATE INDEX idx_execution_status ON tb_execution(status);
CREATE INDEX idx_execution_insume_insume_id ON tb_execution_insume(id_tb_insume);
CREATE INDEX idx_execution_history_execution_id ON tb_execution_history(execution_id);
CREATE INDEX idx_customer_document ON tb_customer(document);
CREATE INDEX idx_user_email ON tb_user(email);

-- Invoice
CREATE TABLE tb_invoice (
    id_tb_invoice    UUID PRIMARY KEY,
    customer_id      UUID           NOT NULL REFERENCES tb_customer(id_tb_customer),
    service_order_id UUID           NOT NULL UNIQUE REFERENCES tb_service_order(id_tb_service_order),
    price            DECIMAL(12,2)  NOT NULL,
    invoice_number   VARCHAR(50)    NOT NULL UNIQUE,
    emission_date    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created          TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated          TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_invoice_customer_id ON tb_invoice(customer_id);
CREATE INDEX idx_invoice_service_order_id ON tb_invoice(service_order_id);
