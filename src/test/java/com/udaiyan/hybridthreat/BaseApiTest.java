package com.udaiyan.hybridthreat;

import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static io.restassured.RestAssured.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Sql(scripts = "/data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public abstract class BaseApiTest {

    /** Must match the number of INSERTs in src/main/resources/data.sql. */
    protected static final int SEED_COUNT = 6;

    @Value("${local.server.port}")
    protected int port;

    @Value("${spring.datasource.url}")
    protected String jdbcUrl;

    @Value("${spring.datasource.username}")
    protected String jdbcUser;

    @Value("${spring.datasource.password}")
    protected String jdbcPassword;

    protected RequestSpecification requestSpec;

    @BeforeEach
    void setUpBase() {
        requestSpec = given()
            .baseUri("http://localhost")
            .port(port)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON);
    }

    /** Plain DriverManager connection — deliberately no pooling. */
    protected Connection openDbConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPassword);
    }
}
