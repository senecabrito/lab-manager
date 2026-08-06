package com.seneca_brito.lab_manager.domain;

import com.seneca_brito.lab_manager.shared.ENUM.StatusLaboratorio;
import com.seneca_brito.lab_manager.shared.ENUM.TipoLaboratorio;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "Laboratorio")
@Data
public class Laboratorio {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "id_laboratorio", nullable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "nome", nullable = false, length = 50)
    private String nome;

    @Column(name = "capacidade", nullable = false)
    private Integer capacidade;

    @Column(name = "quantidade_computadores", nullable = false)
    private Integer quantidadeComputadores;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "ENUM('DISPONIVEL','EM_USO','RESERVADO','EM_MANUTENCAO','INDISPONIVEL')")
    private StatusLaboratorio status;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_laboratorio", nullable = false, columnDefinition = "ENUM('INFORMATICA','QUIMICA','FISICA','BIOLOGIA','ROBOTICA','ELETRONICA','MULTIDISCIPLINAR','IDIOMAS','OUTRO')")
    private TipoLaboratorio tipoLaboratorio;

    @OneToMany(mappedBy = "laboratorio",fetch = FetchType.LAZY)
    private List<Reserva> reservas;

    @OneToMany(mappedBy = "laboratorio",fetch = FetchType.LAZY)
    private List<Reclamacao> reclamacoes;

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
