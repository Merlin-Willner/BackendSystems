package adapters.API;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for UserResource - covers User Management.
 * 
 * All tests use unique names (UUID) and clean up created entities after each test
 * to ensure database state is unchanged after test execution.
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserResourceIT {

    /** Track created user IDs for cleanup */
    private final List<Long> createdUserIds = new ArrayList<>();

    @BeforeAll
    static void enableLogging() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @AfterEach
    void cleanup() {
        // Delete all created users in reverse order
        for (int i = createdUserIds.size() - 1; i >= 0; i--) {
            Long id = createdUserIds.get(i);
            try {
                given()
                    .delete("/user/{id}", id)
                    .then()
                    .statusCode(anyOf(equalTo(200), equalTo(404)));
            } catch (Exception e) {
                // Ignore cleanup errors
            }
        }
        createdUserIds.clear();
    }

    /**
     * Helper: Create a user and track for cleanup.
     */
    private Long createUser(String username) {
        Long id = given()
                .contentType("application/json")
                .body(Map.of(
                        "username", username,
                        "email", username + "@test.com",
                        "password", "secret123"
                ))
            .when()
                .post("/user")
            .then()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getLong("data.userId");
        
        createdUserIds.add(id);
        return id;
    }

    // ==================== Read Tests (use seeded data) ====================

    @Test
    @Order(1)
    @DisplayName("GET /user/{id} returns seeded user")
    void getUserByIdReturnsUser() {
        // User id=1 (alice) is seeded in import.sql
        given()
        .when()
                .get("/user/1")
        .then()
                .statusCode(200)
                .body("data.username", equalTo("alice"))
                .body("data.email", equalTo("alice@example.com"))
                .body("_links.self", containsString("/user/1"));
    }

    @Test
    @Order(2)
    @DisplayName("GET /user/{id} returns 404 for missing user")
    void getUserByIdReturns404() {
        given()
        .when()
                .get("/user/99999")
        .then()
                .statusCode(404);
    }

    @Test
    @Order(3)
    @DisplayName("GET /user/username/{username} returns user")
    void getUserByUsernameReturnsUser() {
        given()
        .when()
                .get("/user/username/alice")
        .then()
                .statusCode(200)
                .body("data.username", equalTo("alice"));
    }

    @Test
    @Order(4)
    @DisplayName("GET /user/username/{username} returns 404 for unknown")
    void getUserByUsernameReturns404() {
        given()
        .when()
                .get("/user/username/nonexistent-user-xyz-" + UUID.randomUUID())
        .then()
                .statusCode(404);
    }

    @Test
    @Order(5)
    @DisplayName("GET /user/email/{email} returns user")
    void getUserByEmailReturnsUser() {
        given()
        .when()
                .get("/user/email/alice@example.com")
        .then()
                .statusCode(200)
                .body("data.email", equalTo("alice@example.com"));
    }

    @Test
    @Order(6)
    @DisplayName("GET /user returns all users with pagination")
    void getAllUsersWithPagination() {
        given()
                .queryParam("page", 0)
                .queryParam("size", 10)
        .when()
                .get("/user")
        .then()
                .statusCode(200)
                .body("page", equalTo(0))
                .body("size", equalTo(10))
                .body("total", greaterThanOrEqualTo(2)) // alice and bob are seeded
                .body("_links.self", notNullValue());
    }

    // ==================== Create Tests ====================

    @Test
    @Order(10)
    @DisplayName("POST /user registers a new user with 201 and hypermedia links")
    void registerUserReturns201() {
        String uniqueUsername = "user-" + UUID.randomUUID().toString().substring(0, 8);
        String uniqueEmail = uniqueUsername + "@test.com";

        Long id = given()
                .contentType("application/json")
                .body(Map.of(
                        "username", uniqueUsername,
                        "email", uniqueEmail,
                        "password", "secret123"
                ))
        .when()
                .post("/user")
        .then()
                .statusCode(201)
                .header("Location", notNullValue())
                .body("data.username", equalTo(uniqueUsername))
                .body("data.email", equalTo(uniqueEmail))
                .body("_links.self", notNullValue())
                .body("_links.update", notNullValue())
                .body("_links.delete", notNullValue())
                .extract()
                .jsonPath()
                .getLong("data.userId");
        
        createdUserIds.add(id);
    }

    @Test
    @Order(11)
    @DisplayName("POST /user with duplicate username returns 500 (conflict)")
    void duplicateUsernameReturnsError() {
        // alice is seeded in import.sql
        given()
                .contentType("application/json")
                .body(Map.of(
                        "username", "alice",
                        "email", "unique-" + UUID.randomUUID() + "@test.com",
                        "password", "secret"
                ))
        .when()
                .post("/user")
        .then()
                .statusCode(500); // IllegalArgumentException results in 500
    }

    @Test
    @Order(12)
    @DisplayName("POST /user with duplicate email returns 500 (conflict)")
    void duplicateEmailReturnsError() {
        // alice@example.com is seeded in import.sql
        given()
                .contentType("application/json")
                .body(Map.of(
                        "username", "unique-" + UUID.randomUUID().toString().substring(0, 8),
                        "email", "alice@example.com",
                        "password", "secret"
                ))
        .when()
                .post("/user")
        .then()
                .statusCode(500); // IllegalArgumentException results in 500
    }

    // ==================== Update Tests ====================

    @Test
    @Order(20)
    @DisplayName("PUT /user/{id} updates existing user")
    void updateUserReturns200() {
        String uniqueUsername = "upd-" + UUID.randomUUID().toString().substring(0, 8);
        Long userId = createUser(uniqueUsername);

        // Update the user
        String newUsername = "new-" + UUID.randomUUID().toString().substring(0, 8);
        String newEmail = newUsername + "@test.com";

        given()
                .contentType("application/json")
                .body(Map.of(
                        "username", newUsername,
                        "email", newEmail,
                        "password", "updated"
                ))
        .when()
                .put("/user/{id}", userId)
        .then()
                .statusCode(200)
                .body("data.username", equalTo(newUsername))
                .body("data.email", equalTo(newEmail));
    }

    @Test
    @Order(21)
    @DisplayName("PUT /user/{id} returns 409 for missing user")
    void updateMissingUserReturns409() {
        given()
                .contentType("application/json")
                .body(Map.of(
                        "username", "any-" + UUID.randomUUID().toString().substring(0, 8),
                        "email", "any@test.com",
                        "password", "any"
                ))
        .when()
                .put("/user/99999")
        .then()
                .statusCode(409); // API returns 409 CONFLICT for failed updates
    }

    // ==================== Delete Tests ====================

    @Test
    @Order(30)
    @DisplayName("DELETE /user/{id} removes user")
    void deleteUserReturns200() {
        String uniqueUsername = "del-" + UUID.randomUUID().toString().substring(0, 8);
        Long userId = createUser(uniqueUsername);
        createdUserIds.remove(userId); // Don't double-delete in cleanup

        // Delete the user
        given()
        .when()
                .delete("/user/{id}", userId)
        .then()
                .statusCode(200);

        // Verify it's gone
        given()
        .when()
                .get("/user/{id}", userId)
        .then()
                .statusCode(404);
    }

    @Test
    @Order(31)
    @DisplayName("DELETE /user/{id} returns 404 for missing user")
    void deleteMissingUserReturns404() {
        given()
        .when()
                .delete("/user/99999")
        .then()
                .statusCode(404);
    }
}
