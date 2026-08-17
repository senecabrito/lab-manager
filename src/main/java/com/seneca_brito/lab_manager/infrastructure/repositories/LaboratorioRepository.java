package com.seneca_brito.lab_manager.infrastructure.repositories;

import com.seneca_brito.lab_manager.domain.Laboratorio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface LaboratorioRepository extends JpaRepository<Laboratorio, UUID>,
        JpaSpecificationExecutor<Laboratorio> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from Laboratorio l where l.id in :ids order by l.id")
    List<Laboratorio> findAllByIdForUpdate(@Param("ids") Collection<UUID> ids);

    @Query("select l.id as id, l.nome as nome from Laboratorio l order by l.nome, l.id")
    List<LaboratorioBasicoProjection> findAllBasic();
}
