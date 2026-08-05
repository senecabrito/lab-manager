package com.seneca_brito.lab_manager.infrastructure.repositories;

import com.seneca_brito.lab_manager.domain.Laboratorio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LaboratorioRepository extends JpaRepository<Laboratorio, UUID> {
}
