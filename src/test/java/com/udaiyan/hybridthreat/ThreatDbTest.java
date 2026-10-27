package com.udaiyan.hybridthreat;

import com.udaiyan.hybridthreat.model.Threat;
import com.udaiyan.hybridthreat.model.ThreatType;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ThreatDbTest extends BaseApiTest {

    private final ObjectMapper mapper = new ObjectMapper();

    // ---------- seed data is really in Postgres ----------

    @Test
    void seedData_existsInPostgres() throws SQLException {
        try (Connection conn = openDbConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT name FROM threats ORDER BY id");
             ResultSet rs = ps.executeQuery()) {

            List<String> names = new ArrayList<>();
            while (rs.next()) names.add(rs.getString("name"));

            assertEquals(SEED_COUNT, names.size());
            assertTrue(names.contains("Operation Cobalt Drift"));
            assertTrue(names.contains("Coordinated disinformation + DDoS"));
        }
    }

    // ---------- API writes are visible via JDBC ----------

    @Test
    void postThreat_persistsRowInPostgres() throws Exception {
        long id = postThreat("DB check", ThreatType.CYBER, "MEDIUM", "OSINT", "OPEN");

        try (Connection conn = openDbConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT name, type, severity, source, status FROM threats WHERE id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next(), "Row not found for id " + id);
                assertEquals("DB check", rs.getString("name"));
                assertEquals("CYBER",    rs.getString("type"));
                assertEquals("MEDIUM",   rs.getString("severity"));
                assertEquals("OSINT",    rs.getString("source"));
                assertEquals("OPEN",     rs.getString("status"));
            }
        }
    }

    @Test
    void deleteThreat_removesRowFromPostgres() throws Exception {
        long id = postThreat("To vanish", ThreatType.HYBRID, "HIGH", "HUMINT", "OPEN");
        assertTrue(rowExists(id), "Row should exist before delete");

        requestSpec
        .when()
            .delete("/api/threats/{id}", id)
        .then()
            .statusCode(204);

        assertFalse(rowExists(id), "Row should be gone after delete");
    }

    @Test
    void updateThreat_modifiesRowInPostgres() throws Exception {
        long id = postThreat("Before DB", ThreatType.CYBER, "LOW", "OSINT", "OPEN");

        Threat update = new Threat(null, "After DB", ThreatType.HYBRID,
            "CRITICAL", "SIGINT", Instant.now(), "MITIGATED");

        requestSpec
            .body(mapper.writeValueAsString(update))
        .when()
            .put("/api/threats/{id}", id)
        .then()
            .statusCode(200);

        try (Connection conn = openDbConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT name, type, severity, status FROM threats WHERE id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertEquals("After DB", rs.getString("name"));
                assertEquals("HYBRID",   rs.getString("type"));
                assertEquals("CRITICAL", rs.getString("severity"));
                assertEquals("MITIGATED",rs.getString("status"));
            }
        }
    }

    @Test
    void updateSeedThreatViaApi_isReflectedInPostgres() throws Exception {
        // Seed row 1 is "Operation Cobalt Drift" (CYBER / CRITICAL / ACTIVE).
        Threat update = new Threat(null, "Operation Cobalt Drift (reassessed)",
            ThreatType.HYBRID, "CRITICAL", "SIGINT", Instant.now(), "MITIGATED");

        requestSpec
            .body(mapper.writeValueAsString(update))
        .when()
            .put("/api/threats/{id}", 1L)
        .then()
            .statusCode(200);

        try (Connection conn = openDbConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT name, type, status FROM threats WHERE id = 1")) {
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next(), "Seed row 1 should still exist");
                assertEquals("Operation Cobalt Drift (reassessed)", rs.getString("name"));
                assertEquals("HYBRID",    rs.getString("type"));
                assertEquals("MITIGATED", rs.getString("status"));
            }
        }
    }

    // ---------- helpers ----------

    private long postThreat(String name, ThreatType type,
                            String severity, String source, String status) throws Exception {
        Threat payload = new Threat(null, name, type, severity, source, Instant.now(), status);
        return requestSpec
            .body(mapper.writeValueAsString(payload))
        .when()
            .post("/api/threats")
        .then()
            .statusCode(201)
            .extract().jsonPath().getLong("id");
    }

    private boolean rowExists(long id) throws SQLException {
        try (Connection conn = openDbConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM threats WHERE id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
