package com.seneca_brito.lab_manager.shared.ENUM;

import java.util.EnumSet;
import java.util.Set;

public enum StatusReserva {
    PENDENTE,
    APROVADA,
    REJEITADA,
    CANCELADA;

    private static final Set<StatusReserva> ESTADOS_QUE_BLOQUEIAM =
            EnumSet.of(PENDENTE, APROVADA);
    private static final Set<StatusReserva> ESTADOS_QUE_CONTAM_COMO_UTILIZACAO =
            EnumSet.of(APROVADA);

    public boolean bloqueiaHorario() {
        return ESTADOS_QUE_BLOQUEIAM.contains(this);
    }

    public static Set<StatusReserva> estadosQueBloqueiam() {
        return EnumSet.copyOf(ESTADOS_QUE_BLOQUEIAM);
    }

    public boolean contaComoUtilizacao() {
        return ESTADOS_QUE_CONTAM_COMO_UTILIZACAO.contains(this);
    }

    public static Set<StatusReserva> estadosQueContamComoUtilizacao() {
        return EnumSet.copyOf(ESTADOS_QUE_CONTAM_COMO_UTILIZACAO);
    }
}
