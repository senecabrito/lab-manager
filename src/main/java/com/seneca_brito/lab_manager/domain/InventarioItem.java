package com.seneca_brito.lab_manager.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "InventarioItem")
@Data
public class InventarioItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "id_item", nullable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "quantidade_disponivel", nullable = false)
    private Integer quantidadeDisponivel;

    @Column(name = "quantidade_indisponivel", nullable = false)
    private Integer quantidadeIndisponivel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_id_laboratorio", nullable = false)
    private Laboratorio laboratorio;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        InventarioItem that = (InventarioItem) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
