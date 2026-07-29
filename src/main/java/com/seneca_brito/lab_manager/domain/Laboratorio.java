package com.seneca_brito.lab_manager.domain;

import com.seneca_brito.lab_manager.shared.ENUM.StatusLaboratorio;
import com.seneca_brito.lab_manager.shared.ENUM.TipoLaboratorio;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "laboratorio")
@Data
public class Laboratorio {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "id_laboratorio")
    private UUID id;

    private String nome;

    private Integer capacidade;

    private Integer quantidadeComputadores;

    private StatusLaboratorio status;

    @Column(name = "tipo_laboratorio")
    private TipoLaboratorio tipoLaboratorio;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Laboratorio that = (Laboratorio) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
