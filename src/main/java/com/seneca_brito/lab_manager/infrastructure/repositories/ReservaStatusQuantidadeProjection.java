package com.seneca_brito.lab_manager.infrastructure.repositories;

import com.seneca_brito.lab_manager.shared.ENUM.StatusReserva;

public interface ReservaStatusQuantidadeProjection {
    StatusReserva getStatus();
    long getQuantidade();
}
