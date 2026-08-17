CREATE TABLE Usuario (
    id_usuario BINARY(16) PRIMARY KEY,
    nome VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    senha VARCHAR(60) NOT NULL,
    tipo_usuario ENUM('ADMIN', 'PROF') NOT NULL,
    curso VARCHAR(120) NOT NULL,
    matricula VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE Laboratorio (
    id_laboratorio BINARY(16) PRIMARY KEY,
    nome VARCHAR(50) NOT NULL,
    capacidade INT NOT NULL,
    quantidade_computadores INT NOT NULL,
    status ENUM('DISPONIVEL', 'EM_USO', 'RESERVADO', 'EM_MANUTENCAO', 'INDISPONIVEL') NOT NULL,
    tipo_laboratorio ENUM('INFORMATICA', 'QUIMICA', 'FISICA', 'BIOLOGIA', 'ROBOTICA',
        'ELETRONICA', 'MULTIDISCIPLINAR', 'IDIOMAS', 'OUTRO') NOT NULL,
    localizacao VARCHAR(120) NULL
);

CREATE TABLE Reserva (
    id_reserva BINARY(16) PRIMARY KEY,
    fk_id_usuario BINARY(16) NOT NULL,
    fk_id_laboratorio BINARY(16) NOT NULL,
    data_reserva DATE NOT NULL,
    horario_inicio TIME NOT NULL,
    horario_fim TIME NOT NULL,
    quantidade_alunos INT NULL,
    observacao TEXT NULL,
    status ENUM('PENDENTE', 'APROVADA', 'REJEITADA', 'CANCELADA') NOT NULL,
    CONSTRAINT fk_reserva_usuario FOREIGN KEY (fk_id_usuario) REFERENCES Usuario (id_usuario),
    CONSTRAINT fk_reserva_laboratorio FOREIGN KEY (fk_id_laboratorio)
        REFERENCES Laboratorio (id_laboratorio)
);

CREATE TABLE Reclamacao (
    id_reclamacao BINARY(16) PRIMARY KEY,
    fk_id_usuario BINARY(16) NOT NULL,
    fk_id_laboratorio BINARY(16) NOT NULL,
    descricao TEXT NOT NULL,
    categoria_problema ENUM('EQUIPAMENTO', 'SOFTWARE', 'REDE_INTERNET', 'INFRAESTRUTURA',
        'LIMPEZA', 'SEGURANCA', 'ACESSO', 'RESERVA', 'OUTRO') NOT NULL,
    data_reclamacao DATETIME NOT NULL,
    status ENUM('PENDENTE', 'EM_ANALISE', 'CONCLUIDA', 'CANCELADA', 'IMPROCEDENTE') NOT NULL,
    CONSTRAINT fk_reclamacao_usuario FOREIGN KEY (fk_id_usuario) REFERENCES Usuario (id_usuario),
    CONSTRAINT fk_reclamacao_laboratorio FOREIGN KEY (fk_id_laboratorio)
        REFERENCES Laboratorio (id_laboratorio)
);

CREATE TABLE roles (
    id BINARY(16) PRIMARY KEY,
    nome VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE usuario_roles (
    usuario_id BINARY(16) NOT NULL,
    role_id BINARY(16) NOT NULL,
    PRIMARY KEY (usuario_id, role_id),
    CONSTRAINT fk_usuario_roles_usuario FOREIGN KEY (usuario_id) REFERENCES Usuario (id_usuario),
    CONSTRAINT fk_usuario_roles_role FOREIGN KEY (role_id) REFERENCES roles (id)
);

CREATE TABLE laboratorio_recursos (
    laboratorio_id BINARY(16) NOT NULL,
    recurso VARCHAR(100) NOT NULL,
    PRIMARY KEY (laboratorio_id, recurso),
    CONSTRAINT fk_laboratorio_recursos_laboratorio
        FOREIGN KEY (laboratorio_id) REFERENCES Laboratorio (id_laboratorio)
);
