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
    @Column(name = "id_reclamacao")
    private UUID id;

    private String descricao;

    @Column(name = "categoria_problema")
    private CategoriaProblema categoriaProblema;

    @Column(name = "data_reclamacao")
    private LocalDateTime DataReclamacao;

    private StatusReclamacao status;
    

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
