ALTER TABLE Usuario
    ADD COLUMN matricula VARCHAR(255) NULL;

UPDATE Usuario
SET matricula = LOWER(HEX(id_usuario))
WHERE matricula IS NULL;

ALTER TABLE Usuario
    MODIFY COLUMN matricula VARCHAR(255) NOT NULL,
    ADD CONSTRAINT uk_usuario_matricula UNIQUE (matricula);
