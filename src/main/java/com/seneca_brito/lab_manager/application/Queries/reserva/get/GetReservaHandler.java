package com.seneca_brito.lab_manager.application.Queries.reserva.get;

import com.seneca_brito.lab_manager.domain.Reserva;
import com.seneca_brito.lab_manager.infrastructure.repositories.ReservaRepository;
import com.seneca_brito.lab_manager.shared.ENUM.StatusReserva;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetReservaHandler {

    private final ReservaRepository reservaRepository;

    @Transactional(readOnly = true)
    public Page<Reserva> find(String ownerEmail, UUID laboratorioId, LocalDate data,
                              StatusReserva status, Pageable pageable) {
        Specification<Reserva> spec = (root, query, cb) -> cb.conjunction();
        if (ownerEmail != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("usuario").get("email"), ownerEmail));
        }
        if (laboratorioId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("laboratorio").get("id"), laboratorioId));
        }
        if (data != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("dataReserva"), data));
        }
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        return reservaRepository.findAll(spec, pageable);
    }
}
