package com.seneca_brito.lab_manager.infrastructure.repositories;

import com.seneca_brito.lab_manager.domain.Reserva;
import com.seneca_brito.lab_manager.shared.ENUM.StatusReserva;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, UUID>, JpaSpecificationExecutor<Reserva> {

    @Override
    @EntityGraph(attributePaths = {"usuario", "laboratorio"})
    Page<Reserva> findAll(org.springframework.data.jpa.domain.Specification<Reserva> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"usuario", "laboratorio"})
    @Query("select r from Reserva r where r.id = :id")
    Optional<Reserva> findDetailedById(@Param("id") UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"usuario", "laboratorio"})
    @Query("select r from Reserva r where r.id = :id")
    Optional<Reserva> findByIdForUpdate(@Param("id") UUID id);

    @Query("""
            select (count(r) > 0) from Reserva r
            where r.laboratorio.id = :laboratorioId
              and r.dataReserva = :data
              and r.status in :status
              and (:reservaId is null or r.id <> :reservaId)
              and r.horarioInicio < :fim
              and r.horarioFim > :inicio
            """)
    boolean existsConflito(@Param("laboratorioId") UUID laboratorioId,
                           @Param("data") LocalDate data,
                           @Param("inicio") LocalTime inicio,
                           @Param("fim") LocalTime fim,
                           @Param("status") Collection<StatusReserva> status,
                           @Param("reservaId") UUID reservaId);

    @Query("""
            select r from Reserva r
            where r.laboratorio.id = :laboratorioId
              and r.dataReserva = :data
              and r.status in :status
            order by r.horarioInicio, r.id
            """)
    List<Reserva> findAgenda(@Param("laboratorioId") UUID laboratorioId,
                             @Param("data") LocalDate data,
                             @Param("status") Collection<StatusReserva> status);

    @Query("""
            select r.status as status, count(r) as quantidade
            from Reserva r
            group by r.status
            order by r.status
            """)
    List<ReservaStatusQuantidadeProjection> countGroupedByStatus();

    @Query("""
            select r.status as status, count(r) as quantidade
            from Reserva r
            where r.dataReserva between :inicio and :fim
            group by r.status
            order by r.status
            """)
    List<ReservaStatusQuantidadeProjection> countGroupedByStatusAndPeriod(
            @Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query(value = """
            SELECT HEX(fk_id_laboratorio) AS laboratorioHex,
                   COALESCE(SUM(TIME_TO_SEC(TIMEDIFF(horario_fim, horario_inicio)) / 60), 0)
                       AS minutosOcupados
            FROM Reserva
            WHERE data_reserva BETWEEN :inicio AND :fim
              AND status IN (:status)
            GROUP BY fk_id_laboratorio
            """, nativeQuery = true)
    List<LaboratorioOcupacaoProjection> sumOccupiedMinutes(
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim,
            @Param("status") Collection<String> status);
}
