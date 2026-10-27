package com.udaiyan.hybridthreat;

import org.junit.jupiter.api.Test;

class ThreatValidationTest extends BaseApiTest {

    @Test
    void createThreat_missingName_returns400() {
        String json = """
            {
              "type": "HYBRID",
              "severity": "HIGH",
              "source": "SIGINT",
              "detectedAt": "2024-01-01T00:00:00Z",
              "status": "OPEN"
            }
            """;

        requestSpec
            .body(json)
        .when()
            .post("/api/threats")
        .then()
            .statusCode(400);
    }

    @Test
    void createThreat_unknownType_returns400() {
        String json = """
            {
              "name": "Bad type",
              "type": "BOGUS",
              "severity": "HIGH",
              "source": "SIGINT",
              "detectedAt": "2024-01-01T00:00:00Z",
              "status": "OPEN"
            }
            """;

        requestSpec
            .body(json)
        .when()
            .post("/api/threats")
        .then()
            .statusCode(400);
    }

    @Test
    void getThreat_unknownId_returns404() {
        requestSpec
        .when()
            .get("/api/threats/{id}", 999_999L)
        .then()
            .statusCode(404);
    }

    @Test
    void updateThreat_unknownId_returns404() {
        String json = """
            {
              "name": "Ghost",
              "type": "CYBER",
              "severity": "LOW",
              "source": "OSINT",
              "detectedAt": "2024-01-01T00:00:00Z",
              "status": "OPEN"
            }
            """;

        requestSpec
            .body(json)
        .when()
            .put("/api/threats/{id}", 999_999L)
        .then()
            .statusCode(404);
    }

    @Test
    void deleteThreat_unknownId_returns404() {
        requestSpec
        .when()
            .delete("/api/threats/{id}", 999_999L)
        .then()
            .statusCode(404);
    }
}
