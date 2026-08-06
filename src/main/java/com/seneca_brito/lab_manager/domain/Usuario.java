package com.seneca_brito.lab_manager.domain;

import com.seneca_brito.lab_manager.shared.ENUM.TipoDeUsuarios;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "Usuario")
@Data
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "id_usuario", nullable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "nome", nullable = false, length = 50)
    private String nome;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "senha", nullable = false, length = 60)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_usuario", nullable = false, columnDefinition = "ENUM('ADMIN','PROF')")
    private TipoDeUsuarios tipoDeUsuarios;

    @Column(name = "curso", nullable = false, length = 120)
    private String curso;

    @OneToMany(mappedBy = "usuario",fetch = FetchType.LAZY)
    private List<Reserva> reservas;

    @OneToMany(mappedBy = "usuario",fetch = FetchType.LAZY)
    private List<Reclamacao> reclamacoes;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(id, usuario.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
