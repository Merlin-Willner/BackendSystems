package adapters.API;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class AuthResourceIT {

    @BeforeAll
    static void enableLogging() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test
    @DisplayName("POST /auth/login with valid credentials returns JWT token")
    void loginWithValidCredentialsReturnsToken() {
        // alice/alice-secret is seeded in import.sql
        given()
                .contentType("application/json")
                .body(Map.of(
                        "username", "alice",
                        "password", "alice-secret"
                ))
        .when()
                .post("/auth/login")
        .then()
                .statusCode(200)
                .body("token", notNullValue())
                .body("tokenType", equalTo("Bearer"))
                .body("expiresInMinutes", notNullValue())
                .body("userId", equalTo(1))
                .body("_links.self", notNullValue())
                .body("_links.user", notNullValue());
    }

    @Test
    @DisplayName("POST /auth/login with wrong password returns 401")
    void loginWithWrongPasswordReturns401() {
        given()
                .contentType("application/json")
                .body(Map.of(
                        "username", "alice",
                        "password", "wrong-password"
                ))
        .when()
                .post("/auth/login")
        .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("POST /auth/login with unknown username returns 401")
    void loginWithUnknownUserReturns401() {
        given()
                .contentType("application/json")
                .body(Map.of(
                        "username", "nonexistent-user",
                        "password", "any"
                ))
        .when()
                .post("/auth/login")
        .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("POST /auth/login with missing username returns 400")
    void loginWithMissingUsernameReturns400() {
        given()
                .contentType("application/json")
                .body(Map.of(
                        "password", "any"
                ))
        .when()
                .post("/auth/login")
        .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("POST /auth/login with missing password returns 400")
    void loginWithMissingPasswordReturns400() {
        given()
                .contentType("application/json")
                .body(Map.of(
                        "username", "alice"
                ))
        .when()
                .post("/auth/login")
        .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("POST /auth/login with empty body returns 400")
    void loginWithEmptyBodyReturns400() {
        given()
                .contentType("application/json")
                .body("{}")
        .when()
                .post("/auth/login")
        .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("POST /auth/login returns token that can be used for authentication")
    void loginTokenCanBeUsedForAuth() {
        // Get token
        String token =
                given()
                        .contentType("application/json")
                        .body(Map.of(
                                "username", "alice",
                                "password", "alice-secret"
                        ))
                .when()
                        .post("/auth/login")
                .then()
                        .statusCode(200)
                        .extract()
                        .jsonPath()
                        .getString("token");

        // Use token to access protected resource
        given()
                .header("Authorization", "Bearer " + token)
        .when()
                .get("/shopping-carts")
        .then()
                .statusCode(200);
    }

    @Test
    @DisplayName("POST /auth/registration with empty body returns 400")
    void registrationWithEmptyBodyReturns400() {
        given()
                .contentType("application/json")
                .body("{}")
        .when()
                .post("/auth/registration")
        .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("POST /auth/registration with missing fields returns 400")
    void registrationWithMissingFieldsReturns400() {
        given()
                .contentType("application/json")
                .body(Map.of(
                        "username", "only-username"
                ))
        .when()
                .post("/auth/registration")
        .then()
                .statusCode(400);
    }
}
