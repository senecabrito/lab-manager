package com.seneca_brito.lab_manager.infrastructure.security.config;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;

@Entity
@Table(name = "roles")
@NoArgsConstructor
@RequiredArgsConstructor
@Data
public class RolesEntity implements GrantedAuthority {

    @Id
    private Integer id;

    private String nome;


    @Override
    public @Nullable String getAuthority() {
        return nome;
    }
}
