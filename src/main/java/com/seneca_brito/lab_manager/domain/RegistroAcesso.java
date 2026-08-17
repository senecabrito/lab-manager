package com.seneca_brito.lab_manager.domain;

import com.seneca_brito.lab_manager.shared.ENUM.StatusAcesso;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "RegistroAcesso")
@Data
public class RegistroAcesso {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "id_acesso", nullable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_id_reserva", nullable = false, unique = true)
    private Reserva reserva;

    @Column(name = "check_in", nullable = false, columnDefinition = "TIMESTAMP(6)")
    private Instant checkIn;

    @Column(name = "check_out", columnDefinition = "TIMESTAMP(6)")
    private Instant checkOut;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false,
            columnDefinition = "ENUM('EM_ANDAMENTO','FINALIZADO')")
    private StatusAcesso status;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        RegistroAcesso that = (RegistroAcesso) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
