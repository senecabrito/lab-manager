CREATE TABLE InventarioItem (
    id_item BINARY(16) PRIMARY KEY,
    fk_id_laboratorio BINARY(16) NOT NULL,
    nome VARCHAR(100) NOT NULL,
    quantidade_disponivel INT NOT NULL,
    quantidade_indisponivel INT NOT NULL,
    CONSTRAINT ck_inventario_quantidade_disponivel
        CHECK (quantidade_disponivel >= 0),
    CONSTRAINT ck_inventario_quantidade_indisponivel
        CHECK (quantidade_indisponivel >= 0),
    CONSTRAINT fk_inventario_laboratorio
        FOREIGN KEY (fk_id_laboratorio) REFERENCES Laboratorio (id_laboratorio)
);

CREATE INDEX idx_inventario_laboratorio_nome
    ON InventarioItem (fk_id_laboratorio, nome);
