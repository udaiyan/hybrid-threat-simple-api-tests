package com.udaiyan.hybridthreat;

import com.udaiyan.hybridthreat.model.Threat;
import com.udaiyan.hybridthreat.model.ThreatType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

class ThreatCrudTest extends BaseApiTest {

    private final ObjectMapper mapper = new ObjectMapper();

    // ---------- reads against seed data ----------

    @Test
    void listThreats_returnsAllSeedRows() {
        List<Threat> threats = given()
                .spec(requestSpec)
                .when()
                .get("/api/threats")
                .then()
                .statusCode(200)
                .extract().jsonPath().getList("", Threat.class);

        assertEquals(SEED_COUNT, threats.size());

        List<String> names = threats.stream().map(Threat::getName).toList();
        assertTrue(names.contains("Operation Cobalt Drift"));
        assertTrue(names.contains("Combined cyber-kinetic exercise"));
    }

    @Test
    void getSeedThreatById_returnsSeedRow() {
        Threat threat = given()
                .spec(requestSpec)
                .pathParam("id", 1L)
                .when()
                .get("/api/threats/{id}")
                .then()
                .statusCode(200)
                .extract().as(Threat.class);

        assertEquals(1L, threat.getId());
        assertEquals("Operation Cobalt Drift", threat.getName());
        assertEquals(ThreatType.CYBER, threat.getType());
        assertEquals("CRITICAL", threat.getSeverity());
    }

    // ---------- creates against seed baseline ----------

    @Test
    void createThreat_returns201WithLocationAndBody() { // Removed throws Exception
        Threat payload = new Threat(null, "Hybrid op X", ThreatType.HYBRID,
                "HIGH", "SIGINT", Instant.now(), "OPEN");

        Response response = given()
                .spec(requestSpec)
                .body(mapper.writeValueAsString(payload))
                .when()
                .post("/api/threats")
                .then()
                .statusCode(201)
                .extract().response();

        assertTrue(response.header("Location").matches(".*/api/threats/\\d+"));

        Threat created = response.as(Threat.class);
        assertNotNull(created.getId());
        assertEquals("Hybrid op X", created.getName());
        assertEquals(ThreatType.HYBRID, created.getType());
        assertEquals("HIGH", created.getSeverity());
        assertEquals("OPEN", created.getStatus());
    }

    @Test
    void listThreats_returnsSeedPlusCreatedThreats() { // Removed throws Exception
        createThreat("Cyber intrusion", ThreatType.CYBER);
        createThreat("Physical breach", ThreatType.PHYSICAL);

        List<Threat> threats = given()
                .spec(requestSpec)
                .when()
                .get("/api/threats")
                .then()
                .statusCode(200)
                .extract().jsonPath().getList("", Threat.class);

        assertEquals(SEED_COUNT + 2, threats.size());

        List<String> names = threats.stream().map(Threat::getName).toList();
        assertTrue(names.contains("Cyber intrusion"));
        assertTrue(names.contains("Physical breach"));
        assertTrue(names.contains("Operation Cobalt Drift"));
    }

    // ---------- update / delete ----------

    @Test
    void updateThreat_replacesFields() { // Removed throws Exception
        long id = createThreat("Before", ThreatType.CYBER).getId();

        Threat update = new Threat(null, "After", ThreatType.HYBRID,
                "CRITICAL", "HUMINT", Instant.now(), "MITIGATED");

        Threat updated = given()
                .spec(requestSpec)
                .pathParam("id", id)
                .body(mapper.writeValueAsString(update))
                .when()
                .put("/api/threats/{id}")
                .then()
                .statusCode(200)
                .extract().as(Threat.class);

        assertEquals(id, updated.getId());
        assertEquals("After", updated.getName());
        assertEquals(ThreatType.HYBRID, updated.getType());
        assertEquals("CRITICAL", updated.getSeverity());
        assertEquals("MITIGATED", updated.getStatus());
    }

    @Test
    void deleteThreat_returns204() { // Removed throws Exception
        long id = createThreat("To be deleted", ThreatType.PHYSICAL).getId();

        given()
                .spec(requestSpec)
                .pathParam("id", id)
                .when()
                .delete("/api/threats/{id}")
                .then()
                .statusCode(204);

        given()
                .spec(requestSpec)
                .pathParam("id", id)
                .when()
                .get("/api/threats/{id}")
                .then()
                .statusCode(404);
    }

    @Test
    void deleteSeedThreat_returns204AndRemovesIt() {
        given()
                .spec(requestSpec)
                .pathParam("id", 1L)
                .when()
                .delete("/api/threats/{id}")
                .then()
                .statusCode(204);

        given()
                .spec(requestSpec)
                .pathParam("id", 1L)
                .when()
                .get("/api/threats/{id}")
                .then()
                .statusCode(404);
    }

    // ---------- filtering ----------

    @Test
    void filterByType_returnsOnlyMatching() { // Removed throws Exception
        createThreat("extra hybrid", ThreatType.HYBRID);

        List<Threat> threats = given()
                .spec(requestSpec)
                .queryParam("type", "HYBRID")
                .when()
                .get("/api/threats")
                .then()
                .statusCode(200)
                .extract().jsonPath().getList("", Threat.class);

        assertEquals(3, threats.size());
        assertTrue(threats.stream().allMatch(t -> t.getType() == ThreatType.HYBRID));

        List<String> names = threats.stream().map(Threat::getName).toList();
        assertTrue(names.contains("Combined cyber-kinetic exercise"));
        assertTrue(names.contains("extra hybrid"));
    }

    // ---------- helper ----------

    private Threat createThreat(String name, ThreatType type) { // Removed throws Exception
        Threat payload = new Threat(null, name, type, "MEDIUM",
                "OSINT", Instant.now(), "OPEN");
        return given()
                .spec(requestSpec)
                .body(mapper.writeValueAsString(payload))
                .when()
                .post("/api/threats")
                .then()
                .statusCode(201)
                .extract().as(Threat.class);
    }
}