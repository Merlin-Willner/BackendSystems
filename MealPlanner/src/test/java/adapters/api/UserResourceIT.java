package adapters.api;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserResourceIT {

    private static class CreatedUser {
        private String username;
        private String email;
        private String password;

        private CreatedUser(String username, String email, String password) {
            this.username = username;
            this.email = email;
            this.password = password;
        }
    }

    private final List<CreatedUser> createdUsers = new ArrayList<>();

    @BeforeAll
    static void enableLogging() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @AfterEach
    void cleanup() {
        for (int i = createdUsers.size() - 1; i >= 0; i--) {
            CreatedUser user = createdUsers.get(i);
            try {
                String token = login(user.username, user.password);
                if (token == null) {
                    continue;
                }
                String etag = getUserEtag(token);
                if (etag != null) {
                    given()
                            .header("Authorization", "Bearer " + token)
                            .header("If-Match", etag)
                    .when()
                            .delete("/user")
                    .then()
                            .statusCode(anyOf(equalTo(204), equalTo(404))); // 204 No Content or 404 if already deleted
                }
            } catch (Exception e) {
                // ignore cleanup errors
            }
        }
        createdUsers.clear();
    }

    private CreatedUser registerUser(String username) {
        String email = username + "@test.com";
        String password = "secret123";

        given()
                .contentType("application/json")
                .body(Map.of(
                        "username", username,
                        "email", email,
                        "password", password
                ))
        .when()
                .post("/auth/registration")
        .then()
                .statusCode(201);

        CreatedUser created = new CreatedUser(username, email, password);
        createdUsers.add(created);
        return created;
    }

    private String login(String username, String password) {
        Response response = given()
                .contentType("application/json")
                .body(Map.of(
                        "username", username,
                        "password", password
                ))
        .when()
                .post("/auth/login");
        if (response.statusCode() != 200) {
            return null;
        }
        return response.jsonPath().getString("token");
    }

    private String getUserEtag(String token) {
        Response response = given()
                .header("Authorization", "Bearer " + token)
        .when()
                .get("/user");
        if (response.statusCode() != 200) {
            return null;
        }
        return response.getHeader("ETag");
    }

    @Test
    @Order(1)
    @DisplayName("GET /user without auth returns 401")
    void getUserWithoutAuthReturns401() {
        given()
        .when()
                .get("/user")
        .then()
                .statusCode(401);
    }

    @Test
    @Order(2)
    @DisplayName("GET /user returns current authenticated user")
    void getCurrentUserReturnsUser() {
        String token = login("alice", "alice-secret");

        given()
                .header("Authorization", "Bearer " + token)
        .when()
                .get("/user")
        .then()
                .statusCode(200)
                .body("data.username", equalTo("alice"))
                .body("data.email", equalTo("alice@example.com"))
                .body("_links.self", containsString("/user"));
    }

    @Test
    @Order(10)
    @DisplayName("POST /auth/registration registers a new user with 201 and hypermedia links")
    void registerUserReturns201() {
        String uniqueUsername = "user-" + UUID.randomUUID().toString().substring(0, 8);
        String uniqueEmail = uniqueUsername + "@test.com";

        given()
                .contentType("application/json")
                .body(Map.of(
                        "username", uniqueUsername,
                        "email", uniqueEmail,
                        "password", "secret123"
                ))
        .when()
                .post("/auth/registration")
        .then()
                .statusCode(201)
                .header("Location", notNullValue())
                .body("data.username", equalTo(uniqueUsername))
                .body("data.email", equalTo(uniqueEmail))
                .body("_links.self", notNullValue())
                .body("_links.login", notNullValue())
                .body("_links.user", notNullValue());

        createdUsers.add(new CreatedUser(uniqueUsername, uniqueEmail, "secret123"));
    }

    @Test
    @Order(11)
    @DisplayName("POST /auth/registration with duplicate username returns 409 (conflict)")
    void duplicateUsernameReturnsError() {
        given()
                .contentType("application/json")
                .body(Map.of(
                        "username", "alice",
                        "email", "unique-" + UUID.randomUUID() + "@test.com",
                        "password", "secret"
                ))
        .when()
                .post("/auth/registration")
        .then()
                .statusCode(409);
    }

    @Test
    @Order(12)
    @DisplayName("POST /auth/registration with duplicate email returns 409 (conflict)")
    void duplicateEmailReturnsError() {
        given()
                .contentType("application/json")
                .body(Map.of(
                        "username", "unique-" + UUID.randomUUID().toString().substring(0, 8),
                        "email", "alice@example.com",
                        "password", "secret"
                ))
        .when()
                .post("/auth/registration")
        .then()
                .statusCode(409);
    }

    @Test
    @Order(20)
    @DisplayName("PUT /user updates current user and returns 204 No Content")
    void updateCurrentUserReturns204() {
        String uniqueUsername = "upd-" + UUID.randomUUID().toString().substring(0, 8);
        CreatedUser user = registerUser(uniqueUsername);
        String token = login(user.username, user.password);
        String etag = getUserEtag(token);

        String newUsername = "new-" + UUID.randomUUID().toString().substring(0, 8);
        String newEmail = newUsername + "@test.com";

        given()
                .header("Authorization", "Bearer " + token)
                .header("If-Match", etag)
                .contentType("application/json")
                .body(Map.of(
                        "username", newUsername,
                        "email", newEmail,
                        "password", "updated"
                ))
        .when()
                .put("/user")
        .then()
                .statusCode(204);

        user.username = newUsername;
        user.email = newEmail;
        user.password = "updated";
    }

    @Test
    @Order(30)
    @DisplayName("DELETE /user removes current user and returns 204 No Content")
    void deleteCurrentUserReturns204() {
        String uniqueUsername = "del-" + UUID.randomUUID().toString().substring(0, 8);
        CreatedUser user = registerUser(uniqueUsername);
        String token = login(user.username, user.password);
        String etag = getUserEtag(token);
        createdUsers.remove(user);

        given()
                .header("Authorization", "Bearer " + token)
                .header("If-Match", etag)
        .when()
                .delete("/user")
        .then()
                .statusCode(204);

        given()
                .header("Authorization", "Bearer " + token)
        .when()
                .get("/user")
        .then()
                .statusCode(404);
    }
}
