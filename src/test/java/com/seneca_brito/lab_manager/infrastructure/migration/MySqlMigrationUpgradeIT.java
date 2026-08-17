package com.seneca_brito.lab_manager.infrastructure.migration;

import com.seneca_brito.lab_manager.Application;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.sql.DriverManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MySqlMigrationUpgradeIT {

    @Test
    void upgradesPersistentV5DataToLatestAndPassesHibernateValidation() throws Exception {
        String url = required("mysql.url");
        String username = System.getProperty("mysql.username", "admin");
        String password = System.getProperty("mysql.password", "admin123");

        Flyway v5 = Flyway.configure().dataSource(url, username, password)
                .locations("classpath:db/migration")
                .target(MigrationVersion.fromVersion("5"))
                .load();
        v5.migrate();
        assertEquals("5", v5.info().current().getVersion().getVersion());
        insertV5Fixtures(url, username, password);

        Flyway latest = Flyway.configure().dataSource(url, username, password)
                .locations("classpath:db/migration").load();
        latest.migrate();
        assertEquals("7", latest.info().current().getVersion().getVersion());
        assertFixturesPreserved(url, username, password);

        try (var context = new SpringApplicationBuilder(Application.class)
                .web(WebApplicationType.SERVLET)
                .run("--spring.datasource.url=" + url,
                        "--spring.datasource.username=" + username,
                        "--spring.datasource.password=" + password,
                        "--spring.flyway.enabled=true",
                        "--spring.jpa.hibernate.ddl-auto=validate",
                        "--server.port=0")) {
            assertTrue(context.isActive());
        }
    }

    private void insertV5Fixtures(String url, String username, String password) throws Exception {
        try (var connection = DriverManager.getConnection(url, username, password);
             var statement = connection.createStatement()) {
            statement.executeUpdate("""
                    INSERT INTO Usuario
                        (id_usuario,nome,email,senha,tipo_usuario,curso,matricula)
                    VALUES
                        (UNHEX('11111111111111111111111111111111'),'Usuario V5',
                         'preservado@v5.test','hash','PROF','Computacao','MAT-V5')
                    """);
            statement.executeUpdate("""
                    INSERT INTO Laboratorio
                        (id_laboratorio,nome,capacidade,quantidade_computadores,status,
                         tipo_laboratorio,localizacao)
                    VALUES
                        (UNHEX('22222222222222222222222222222222'),'Laboratorio V5',40,30,
                         'DISPONIVEL','INFORMATICA','Bloco V5')
                    """);
            statement.executeUpdate("""
                    INSERT INTO Reserva
                        (id_reserva,fk_id_usuario,fk_id_laboratorio,data_reserva,horario_inicio,
                         horario_fim,quantidade_alunos,observacao,status)
                    VALUES
                        (UNHEX('33333333333333333333333333333333'),
                         UNHEX('11111111111111111111111111111111'),
                         UNHEX('22222222222222222222222222222222'),
                         '2026-09-10','10:00:00','11:00:00',20,'Reserva V5','APROVADA')
                    """);
            statement.executeUpdate("""
                    INSERT INTO Reclamacao
                        (id_reclamacao,fk_id_usuario,fk_id_laboratorio,descricao,
                         categoria_problema,data_reclamacao,status)
                    VALUES
                        (UNHEX('44444444444444444444444444444444'),
                         UNHEX('11111111111111111111111111111111'),
                         UNHEX('22222222222222222222222222222222'),
                         'Reclamacao V5','EQUIPAMENTO','2026-08-01 10:00:00','PENDENTE')
                    """);
        }
    }

    private void assertFixturesPreserved(String url, String username, String password) throws Exception {
        try (var connection = DriverManager.getConnection(url, username, password);
             var statement = connection.createStatement()) {
            try (var result = statement.executeQuery(
                    "SELECT email, matricula FROM Usuario WHERE matricula='MAT-V5'")) {
                assertTrue(result.next());
                assertEquals("preservado@v5.test", result.getString("email"));
                assertEquals("MAT-V5", result.getString("matricula"));
            }
            assertEquals(1, count(statement, "Laboratorio", "nome='Laboratorio V5'"));
            assertEquals(1, count(statement, "Reserva", "status='APROVADA'"));
            assertEquals(1, count(statement, "Reclamacao", "descricao='Reclamacao V5'"));
            assertEquals(0, count(statement, "InventarioItem", "1=1"));
            assertEquals(0, count(statement, "RegistroAcesso", "1=1"));
            assertEquals(2, count(statement, "flyway_schema_history",
                    "version IN ('6','7') AND success=1"));
        }
    }

    private int count(java.sql.Statement statement, String table, String condition) throws Exception {
        try (var result = statement.executeQuery("SELECT COUNT(*) FROM " + table + " WHERE " + condition)) {
            result.next();
            return result.getInt(1);
        }
    }

    private String required(String property) {
        String value = System.getProperty(property);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("System property obrigatoria: " + property);
        }
        return value;
    }
}
