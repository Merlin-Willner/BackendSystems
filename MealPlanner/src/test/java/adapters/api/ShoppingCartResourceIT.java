package adapters.api;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ShoppingCartResourceIT {

    private String aliceToken;
    private String bobToken;

    @BeforeAll
    static void enableLogging() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @BeforeEach
    void obtainTokens() {
        aliceToken = given()
                .contentType("application/json")
                .body(Map.of("username", "alice", "password", "alice-secret"))
                .post("/auth/login")
                .then()
                .extract()
                .jsonPath()
                .getString("token");

        bobToken = given()
                .contentType("application/json")
                .body(Map.of("username", "bob", "password", "bob-secret"))
                .post("/auth/login")
                .then()
                .extract()
                .jsonPath()
                .getString("token");
    }

    private String getCartEtag(String token) {
        Response response = given()
                .header("Authorization", "Bearer " + token)
                .get("/shopping-carts");
        if (response.statusCode() != 200) {
            return null;
        }
        return response.getHeader("ETag");
    }

    private Long getFirstFoodItemId(String token) {
        Response response = given()
                .header("Authorization", "Bearer " + token)
                .get("/shopping-carts");
        if (response.statusCode() != 200) {
            return null;
        }
        return response.jsonPath().getLong("data.items[0].foodItemId");
    }

    // ==================== Authentication Tests ====================

    @Test
    @Order(1)
    @DisplayName("GET /shopping-carts without token returns 401")
    void getCartWithoutTokenReturns401() {
        given()
        .when()
                .get("/shopping-carts")
        .then()
                .statusCode(401);
    }

    @Test
    @Order(2)
    @DisplayName("GET /shopping-carts returns current user's cart")
    void getCartReturns200() {
        given()
                .header("Authorization", "Bearer " + aliceToken)
        .when()
                .get("/shopping-carts")
        .then()
                .statusCode(200)
                .body("data.userId", equalTo(1))
                .body("_links.self", containsString("/shopping-carts"));
    }

    // ==================== UC05: Add Dish to Shopping Cart ====================

    @Test
    @Order(10)
    @DisplayName("UC05: POST /shopping-carts/items adds dish ingredients to cart")
    void addDishToCartReturns200() {
        given()
                .header("Authorization", "Bearer " + bobToken)
                .contentType("application/json")
                .body(Map.of("dishId", 1, "servingsMultiplier", 2))
        .when()
                .post("/shopping-carts/items")
        .then()
                .statusCode(200)
                .body("data.items.size()", greaterThanOrEqualTo(1));
    }

    @Test
    @Order(11)
    @DisplayName("POST /shopping-carts/items/food-items adds single food item")
    void addFoodItemToCartReturns200() {
        given()
                .header("Authorization", "Bearer " + aliceToken)
                .contentType("application/json")
                .body(Map.of("foodItemId", 1, "quantity", 2))
        .when()
                .post("/shopping-carts/items/food-items")
        .then()
                .statusCode(200)
                .body("data.items.find { it.foodItemId == 1 }.quantity", greaterThanOrEqualTo(2));
    }

    @Test
    @Order(12)
    @DisplayName("UC05: POST /shopping-carts/items with invalid dish returns 404")
    void addInvalidDishToCartReturns404() {
        given()
                .header("Authorization", "Bearer " + aliceToken)
                .contentType("application/json")
                .body(Map.of("dishId", 99999, "servingsMultiplier", 1))
        .when()
                .post("/shopping-carts/items")
        .then()
                .statusCode(404);
    }

    @Test
    @Order(13)
    @DisplayName("PATCH /shopping-carts/items/{foodItemId} updates quantity")
    void updateCartItemQuantityReturns200() {
        given()
                .header("Authorization", "Bearer " + aliceToken)
                .contentType("application/json")
                .body(Map.of("dishId", 1, "servingsMultiplier", 1))
        .when()
                .post("/shopping-carts/items")
        .then()
                .statusCode(200);

        Long foodItemId = getFirstFoodItemId(aliceToken);
        given()
                .header("Authorization", "Bearer " + aliceToken)
                .contentType("application/json")
                .body(Map.of("quantity", 5))
        .when()
                .patch("/shopping-carts/items/{foodItemId}", foodItemId)
        .then()
                .statusCode(200);

        given()
                .header("Authorization", "Bearer " + aliceToken)
        .when()
                .get("/shopping-carts")
        .then()
                .statusCode(200)
                .body("data.items.find { it.foodItemId == " + foodItemId + " }.quantity", equalTo(5));
    }

    @Test
    @Order(14)
    @DisplayName("DELETE /shopping-carts/items/{foodItemId} removes item")
    void removeCartItemReturns200() {
        given()
                .header("Authorization", "Bearer " + bobToken)
                .contentType("application/json")
                .body(Map.of("dishId", 1, "servingsMultiplier", 1))
        .when()
                .post("/shopping-carts/items")
        .then()
                .statusCode(200);

        Long foodItemId = getFirstFoodItemId(bobToken);
        given()
                .header("Authorization", "Bearer " + bobToken)
        .when()
                .delete("/shopping-carts/items/{foodItemId}", foodItemId)
        .then()
                .statusCode(200)
                .body("data.items.foodItemId", not(hasItem(foodItemId.intValue())));
    }

    // ==================== UC06: Cart Summary ====================

    @Test
    @Order(20)
    @DisplayName("UC06: GET /shopping-carts/summary returns aggregated totals")
    void getCartSummaryReturnsAggregatedTotals() {
        given()
                .header("Authorization", "Bearer " + aliceToken)
                .contentType("application/json")
                .body(Map.of("dishId", 1, "servingsMultiplier", 1))
        .when()
                .post("/shopping-carts/items")
        .then()
                .statusCode(200);

        given()
                .header("Authorization", "Bearer " + aliceToken)
        .when()
                .get("/shopping-carts/summary")
        .then()
                .statusCode(200)
                .body("data.cartId", notNullValue())
                .body("data.items", notNullValue())
                .body("data.totalCost", notNullValue())
                .body("_links.self", containsString("/summary"));
    }

    @Test
    @Order(21)
    @DisplayName("UC06: Cart summary without auth returns 401")
    void getCartSummaryWithoutAuthReturns401() {
        given()
        .when()
                .get("/shopping-carts/summary")
        .then()
                .statusCode(401);
    }

    @Test
    @Order(30)
    @DisplayName("DELETE /shopping-carts clears current user's cart")
    void clearCartReturns200() {
        String etag = getCartEtag(aliceToken);

        given()
                .header("Authorization", "Bearer " + aliceToken)
                .header("If-Match", etag)
        .when()
                .delete("/shopping-carts")
        .then()
                .statusCode(200);

        given()
                .header("Authorization", "Bearer " + aliceToken)
        .when()
                .get("/shopping-carts")
        .then()
                .statusCode(200)
                .body("data.items.size()", equalTo(0));
    }
}
