package com.seneca_brito.lab_manager.infrastructure.config;

import com.seneca_brito.lab_manager.application.services.ReservaSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class TimeConfig {

    @Bean
    public Clock labManagerClock(ReservaSettings settings) {
        return Clock.system(settings.zoneId());
    }
}
