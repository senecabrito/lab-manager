package com.seneca_brito.lab_manager.infrastructure.repositories;

import com.seneca_brito.lab_manager.infrastructure.security.RolesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RolesRepository extends JpaRepository<RolesEntity,Integer> {
}
