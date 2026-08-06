package com.seneca_brito.lab_manager.domain;

import com.seneca_brito.lab_manager.shared.ENUM.CategoriaProblema;
import com.seneca_brito.lab_manager.shared.ENUM.StatusReclamacao;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "Reclamacao")
@Data
public class Reclamacao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "id_reclamacao", nullable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "descricao", nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria_problema", nullable = false, columnDefinition = "ENUM('EQUIPAMENTO','SOFTWARE','REDE_INTERNET','INFRAESTRUTURA','LIMPEZA','SEGURANCA','ACESSO','RESERVA','OUTRO')")
    private CategoriaProblema categoriaProblema;

    @Column(name = "data_reclamacao", nullable = false, columnDefinition = "DATETIME")
    private LocalDateTime DataReclamacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "ENUM('PENDENTE','EM_ANALISE','CONCLUIDA','CANCELADA','IMPROCEDENTE')")
    private StatusReclamacao status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_id_laboratorio", nullable = false)
    private Laboratorio laboratorio;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Reclamacao that = (Reclamacao) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
