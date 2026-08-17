package com.seneca_brito.lab_manager.infrastructure.repositories;

import com.seneca_brito.lab_manager.infrastructure.security.RolesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RolesRepository extends JpaRepository<RolesEntity, UUID> {

    Optional<RolesEntity> findByNome(String Nome);
}
