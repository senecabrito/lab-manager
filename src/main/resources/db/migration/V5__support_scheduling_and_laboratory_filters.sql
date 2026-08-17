ALTER TABLE Laboratorio
    ADD COLUMN localizacao VARCHAR(120) NULL;

CREATE TABLE laboratorio_recursos (
    laboratorio_id BINARY(16) NOT NULL,
    recurso VARCHAR(100) NOT NULL,
    PRIMARY KEY (laboratorio_id, recurso),
    CONSTRAINT fk_laboratorio_recursos_laboratorio
        FOREIGN KEY (laboratorio_id) REFERENCES Laboratorio (id_laboratorio)
);

ALTER TABLE Reserva
    ADD COLUMN quantidade_alunos INT NULL,
    ADD COLUMN observacao TEXT NULL,
    ADD COLUMN status ENUM('PENDENTE', 'APROVADA', 'REJEITADA', 'CANCELADA') NULL;

UPDATE Reserva
SET status = 'APROVADA'
WHERE status IS NULL;

ALTER TABLE Reserva
    MODIFY COLUMN status ENUM('PENDENTE', 'APROVADA', 'REJEITADA', 'CANCELADA') NOT NULL,
    ADD CONSTRAINT ck_reserva_quantidade_alunos
        CHECK (quantidade_alunos IS NULL OR quantidade_alunos > 0);

CREATE INDEX idx_reserva_agenda
    ON Reserva (fk_id_laboratorio, data_reserva, status, horario_inicio, horario_fim);

CREATE INDEX idx_reserva_usuario_data
    ON Reserva (fk_id_usuario, data_reserva);

CREATE INDEX idx_reclamacao_status_data
    ON Reclamacao (status, data_reclamacao);

CREATE INDEX idx_reclamacao_laboratorio_categoria
    ON Reclamacao (fk_id_laboratorio, categoria_problema);
