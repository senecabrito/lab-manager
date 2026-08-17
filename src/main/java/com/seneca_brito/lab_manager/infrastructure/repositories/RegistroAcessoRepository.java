package com.seneca_brito.lab_manager.infrastructure.repositories;

import com.seneca_brito.lab_manager.domain.RegistroAcesso;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface RegistroAcessoRepository extends JpaRepository<RegistroAcesso, UUID>,
        JpaSpecificationExecutor<RegistroAcesso> {

    @Override
    @EntityGraph(attributePaths = {"reserva", "reserva.usuario", "reserva.laboratorio"})
    Page<RegistroAcesso> findAll(Specification<RegistroAcesso> specification, Pageable pageable);

    @EntityGraph(attributePaths = {"reserva", "reserva.usuario", "reserva.laboratorio"})
    @Query("select a from RegistroAcesso a where a.id = :id")
    Optional<RegistroAcesso> findDetailedById(@Param("id") UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"reserva", "reserva.usuario", "reserva.laboratorio"})
    @Query("select a from RegistroAcesso a where a.reserva.id = :reservaId")
    Optional<RegistroAcesso> findByReservaIdForUpdate(@Param("reservaId") UUID reservaId);

    boolean existsByReservaId(UUID reservaId);
}
