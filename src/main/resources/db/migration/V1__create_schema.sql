-- ============================================================
-- Repair Shop — Initial Schema
-- ============================================================

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


--Initial INSERTS

INSERT INTO tb_insume (id_tb_insume, name, brand, sku_id, quantity, price, unity_price) VALUES
('0b42ed55-22ea-4116-8a86-9d0e7dc03d4f', 'Filtro de oleo motor 1.0/1.4 flex', 'Tecfil', 'FLT-OLE-001', 45, 18.90, 18.90),
('6ff595dd-8bcd-450f-9167-d746f69b1333', 'Filtro de oleo motor 1.6/1.8 flex', 'Mann Filter', 'FLT-OLE-002', 30, 22.50, 22.50),
('f98612ee-84cf-42e1-9e17-1bf1c370ec14', 'Filtro de oleo motor 2.0 flex', 'Fram', 'FLT-OLE-003', 25, 25.00, 25.00),
('effdcb5c-9733-4239-8086-0e5677088e51', 'Filtro de oleo motor diesel', 'Tecfil', 'FLT-OLE-004', 18, 35.00, 35.00),
('9c895a26-ef06-4a5b-9099-1e176580dae5', 'Filtro de ar motor 1.0/1.4', 'Tecfil', 'FLT-ARM-001', 35, 32.00, 32.00),
('b0766379-73bc-4483-996a-ddadb05c0d44', 'Filtro de ar motor 1.6/2.0', 'Mann Filter', 'FLT-ARM-002', 28, 38.00, 38.00);