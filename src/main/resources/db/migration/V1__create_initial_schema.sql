CREATE TABLE Usuario (
    id_usuario binary(16) PRIMARY KEY,
    nome varchar(50) not null,
    email varchar(100) not null UNIQUE,
    senha varchar(60) not null,
    tipo_usuario enum('ADMIN', 'PROF') default 'PROF' not null,
    curso varchar(120) not null
);

CREATE TABLE Laboratorio (
    id_laboratorio binary(16) PRIMARY KEY,
    nome varchar(50) not null,
    capacidade int not null,
    quantidade_computadores int not null,
    status enum('DISPONIVEL', 'EM_USO', 'RESERVADO', 'EM_MANUTENCAO', 'INDISPONIVEL') not null,
    tipo_laboratorio enum('INFORMATICA', 'QUIMICA', 'FISICA', 'BIOLOGIA', 'ROBOTICA', 'ELETRONICA', 'MULTIDISCIPLINAR', 'IDIOMAS', 'OUTRO') not null
);

CREATE TABLE Reserva (
    id_reserva binary(16) PRIMARY KEY,
    fk_id_usuario binary(16) not null,
    fk_id_laboratorio binary(16) not null,
    data_reserva date not null,
    horario_inicio time not null,
    horario_fim time not null
);

CREATE TABLE Reclamacao (
    id_reclamacao binary(16) PRIMARY KEY,
    fk_id_usuario binary(16) not null,
    fk_id_laboratorio binary(16) not null,
    descricao text not null,
    categoria_problema enum('EQUIPAMENTO', 'SOFTWARE', 'REDE_INTERNET', 'INFRAESTRUTURA', 'LIMPEZA', 'SEGURANCA', 'ACESSO', 'RESERVA', 'OUTRO') not null,
    data_reclamacao datetime default current_timestamp,
    status enum('PENDENTE', 'EM_ANALISE', 'CONCLUIDA', 'CANCELADA', 'IMPROCEDENTE') default 'PENDENTE' not null
);

ALTER TABLE Reclamacao ADD CONSTRAINT fk_reclamacao_usuario
    FOREIGN KEY (fk_id_usuario)
    REFERENCES Usuario (id_usuario);

ALTER TABLE Reclamacao ADD CONSTRAINT fk_reclamacao_laboratorio
    FOREIGN KEY (fk_id_laboratorio)
    REFERENCES Laboratorio (id_laboratorio);

ALTER TABLE Reserva ADD CONSTRAINT fk_reserva_usuario
    FOREIGN KEY (fk_id_usuario)
    REFERENCES Usuario (id_usuario);

ALTER TABLE Reserva ADD CONSTRAINT fk_reserva_laboratorio
    FOREIGN KEY (fk_id_laboratorio)
    REFERENCES Laboratorio (id_laboratorio);
