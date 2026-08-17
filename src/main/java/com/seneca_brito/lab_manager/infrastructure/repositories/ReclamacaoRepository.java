package com.seneca_brito.lab_manager.infrastructure.repositories;

import com.seneca_brito.lab_manager.domain.Reclamacao;
import com.seneca_brito.lab_manager.shared.ENUM.StatusReclamacao;
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

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReclamacaoRepository extends JpaRepository<Reclamacao, UUID>,
        JpaSpecificationExecutor<Reclamacao> {

    @Override
    @EntityGraph(attributePaths = {"usuario", "laboratorio"})
    Page<Reclamacao> findAll(org.springframework.data.jpa.domain.Specification<Reclamacao> spec,
                             Pageable pageable);

    @EntityGraph(attributePaths = {"usuario", "laboratorio"})
    @Query("select r from Reclamacao r where r.id = :id")
    Optional<Reclamacao> findDetailedById(@Param("id") UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"usuario", "laboratorio"})
    @Query("select r from Reclamacao r where r.id = :id")
    Optional<Reclamacao> findByIdForUpdate(@Param("id") UUID id);

    long countByStatusIn(Collection<StatusReclamacao> status);

    @Query("""
            select r.status as status, count(r) as quantidade
            from Reclamacao r group by r.status order by r.status
            """)
    List<ReclamacaoStatusQuantidadeProjection> countGroupedByStatus();

    @Query("""
            select r.categoriaProblema as categoria, count(r) as quantidade
            from Reclamacao r group by r.categoriaProblema order by r.categoriaProblema
            """)
    List<ReclamacaoCategoriaQuantidadeProjection> countGroupedByCategoria();

    @Query("""
            select r.status as status, count(r) as quantidade
            from Reclamacao r
            where r.DataReclamacao >= :inicio and r.DataReclamacao < :fimExclusivo
            group by r.status order by r.status
            """)
    List<ReclamacaoStatusQuantidadeProjection> countGroupedByStatusAndPeriod(
            @Param("inicio") LocalDateTime inicio,
            @Param("fimExclusivo") LocalDateTime fimExclusivo);

}
