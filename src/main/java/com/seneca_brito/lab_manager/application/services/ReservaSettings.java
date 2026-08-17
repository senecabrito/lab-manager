package com.seneca_brito.lab_manager.application.services;

import com.seneca_brito.lab_manager.shared.exceptions.RegraNegocioException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;

@Component
public class ReservaSettings {

    private final ZoneId zoneId;
    private final LocalTime openingTime;
    private final LocalTime closingTime;
    private final int slotMinutes;
    private final int minimumAdvanceHours;

    public ReservaSettings(
            @Value("${lab-manager.scheduling.zone-id:America/Sao_Paulo}") String zoneId,
            @Value("${lab-manager.scheduling.opening-time:07:30}") LocalTime openingTime,
            @Value("${lab-manager.scheduling.closing-time:18:00}") LocalTime closingTime,
            @Value("${lab-manager.scheduling.slot-minutes:30}") int slotMinutes,
            @Value("${lab-manager.scheduling.minimum-advance-hours:72}") int minimumAdvanceHours) {
        this.zoneId = ZoneId.of(zoneId);
        this.openingTime = openingTime;
        this.closingTime = closingTime;
        this.slotMinutes = slotMinutes;
        this.minimumAdvanceHours = minimumAdvanceHours;
        if (!openingTime.isBefore(closingTime) || slotMinutes <= 0 || minimumAdvanceHours < 0
                || Duration.between(openingTime, closingTime).toMinutes() % slotMinutes != 0) {
            throw new RegraNegocioException("Configuracao de agenda invalida");
        }
    }

    public ZoneId zoneId() {
        return zoneId;
    }

    public LocalTime openingTime() {
        return openingTime;
    }

    public LocalTime closingTime() {
        return closingTime;
    }

    public int slotMinutes() {
        return slotMinutes;
    }

    public int minimumAdvanceHours() {
        return minimumAdvanceHours;
    }
}
