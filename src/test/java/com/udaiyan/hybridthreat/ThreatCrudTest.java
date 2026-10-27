package com.udaiyan.hybridthreat;

import com.udaiyan.hybridthreat.model.Threat;
import com.udaiyan.hybridthreat.model.ThreatType;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class ThreatCrudTest extends BaseApiTest {

    private final ObjectMapper mapper = new ObjectMapper();

    // ---------- reads against seed data ----------

    @Test
    void listThreats_returnsAllSeedRows() {
        given()
                .spec(requestSpec)
                .when()
                .get("/api/threats")
                .then()
                .statusCode(200)
                .body("size()", is(SEED_COUNT))
                .body("name", hasItems(
                        "Operation Cobalt Drift",
                        "Port facility reconnaissance",
                        "Combined cyber-kinetic exercise",
                        "Phishing campaign against MOD staff",
                        "Suspicious drone activity near site",
                        "Coordinated disinformation + DDoS"));
    }

    @Test
    void getSeedThreatById_returnsSeedRow() {
        given()
                .spec(requestSpec)
                .pathParam("id", 1L)
                .when()
                .get("/api/threats/{id}")
                .then()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("name", equalTo("Operation Cobalt Drift"))
                .body("type", equalTo("CYBER"))
                .body("severity", equalTo("CRITICAL"));
    }

    // ---------- creates against seed baseline ----------

    @Test
    void createThreat_returns201WithLocationAndBody() throws Exception {
        Threat payload = new Threat(null, "Hybrid op X", ThreatType.HYBRID,
                "HIGH", "SIGINT", Instant.now(), "OPEN");

        given()
                .spec(requestSpec)
                .body(mapper.writeValueAsString(payload))
                .when()
                .post("/api/threats")
                .then()
                .statusCode(201)
                .header("Location", matchesPattern(".*/api/threats/\\d+"))
                .body("id", notNullValue())
                .body("name", equalTo("Hybrid op X"))
                .body("type", equalTo("HYBRID"))
                .body("severity", equalTo("HIGH"))
                .body("status", equalTo("OPEN"));
    }

    @Test
    void listThreats_returnsSeedPlusCreatedThreats() throws Exception {
        createThreat("Cyber intrusion", ThreatType.CYBER);
        createThreat("Physical breach", ThreatType.PHYSICAL);

        given()
                .spec(requestSpec)
                .when()
                .get("/api/threats")
                .then()
                .statusCode(200)
                .body("size()", is(SEED_COUNT + 2))
                .body("name", hasItems("Cyber intrusion", "Physical breach",
                        "Operation Cobalt Drift"));
    }

    // ---------- update / delete ----------

    @Test
    void updateThreat_replacesFields() throws Exception {
        long id = createThreat("Before", ThreatType.CYBER);

        Threat update = new Threat(null, "After", ThreatType.HYBRID,
                "CRITICAL", "HUMINT", Instant.now(), "MITIGATED");

        given()
                .spec(requestSpec)
                .pathParam("id", id)
                .body(mapper.writeValueAsString(update))
                .when()
                .put("/api/threats/{id}")
                .then()
                .statusCode(200)
                .body("id", equalTo((int) id))
                .body("name", equalTo("After"))
                .body("type", equalTo("HYBRID"))
                .body("severity", equalTo("CRITICAL"))
                .body("status", equalTo("MITIGATED"));
    }

    @Test
    void deleteThreat_returns204() throws Exception {
        long id = createThreat("To be deleted", ThreatType.PHYSICAL);

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
    void filterByType_returnsOnlyMatching() throws Exception {
        // Seed already has 2 HYBRID rows.
        createThreat("extra hybrid", ThreatType.HYBRID);

        given()
                .spec(requestSpec)
                .queryParam("type", "HYBRID")
                .when()
                .get("/api/threats")
                .then()
                .statusCode(200)
                .body("size()", is(3))
                .body("type", everyItem(equalTo("HYBRID")))
                .body("name", hasItems(
                        "Combined cyber-kinetic exercise",
                        "Coordinated disinformation + DDoS",
                        "extra hybrid"));
    }

    // ---------- helper ----------

    private long createThreat(String name, ThreatType type) throws Exception {
        Threat payload = new Threat(null, name, type, "MEDIUM",
                "OSINT", Instant.now(), "OPEN");
        return given()
                .spec(requestSpec)
                .body(mapper.writeValueAsString(payload))
                .when()
                .post("/api/threats")
                .then()
                .statusCode(201)
                .extract().jsonPath().getLong("id");
    }
}