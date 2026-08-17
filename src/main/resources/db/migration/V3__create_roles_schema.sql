CREATE TABLE roles (
    id BINARY(16) PRIMARY KEY,
    nome VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE usuario_roles (
    usuario_id BINARY(16) NOT NULL,
    role_id BINARY(16) NOT NULL,
    PRIMARY KEY (usuario_id, role_id),
    CONSTRAINT fk_usuario_roles_usuario
        FOREIGN KEY (usuario_id) REFERENCES Usuario (id_usuario),
    CONSTRAINT fk_usuario_roles_role
        FOREIGN KEY (role_id) REFERENCES roles (id)
);
