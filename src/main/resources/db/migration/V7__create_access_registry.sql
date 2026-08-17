CREATE TABLE RegistroAcesso (
    id_acesso BINARY(16) PRIMARY KEY,
    fk_id_reserva BINARY(16) NOT NULL,
    check_in TIMESTAMP(6) NOT NULL,
    check_out TIMESTAMP(6) NULL,
    status ENUM('EM_ANDAMENTO', 'FINALIZADO') NOT NULL,
    CONSTRAINT uk_acesso_reserva UNIQUE (fk_id_reserva),
    CONSTRAINT fk_acesso_reserva
        FOREIGN KEY (fk_id_reserva) REFERENCES Reserva (id_reserva)
);

CREATE INDEX idx_acesso_check_in
    ON RegistroAcesso (check_in);
