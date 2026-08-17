package com.seneca_brito.lab_manager.infrastructure.repositories;

import com.seneca_brito.lab_manager.domain.InventarioItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface InventarioRepository extends JpaRepository<InventarioItem, UUID> {

    @EntityGraph(attributePaths = "laboratorio")
    @Query("select i from InventarioItem i where (:laboratorioId is null or i.laboratorio.id = :laboratorioId)")
    Page<InventarioItem> findAllDetailed(@Param("laboratorioId") UUID laboratorioId, Pageable pageable);

    @EntityGraph(attributePaths = "laboratorio")
    @Query("select i from InventarioItem i where i.id = :id")
    Optional<InventarioItem> findDetailedById(@Param("id") UUID id);
}
