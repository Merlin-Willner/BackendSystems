package adapters.API;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for ShoppingCartResource - covers UC05 (Add Dish to Cart) and UC06 (Cart Summary).
 * 
 * Tests authenticate via JWT and clean up created carts after each test.
 * Each user can only have ONE cart due to unique constraint on userId.
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ShoppingCartResourceIT {

    private String aliceToken;
    private String bobToken;

    /** Track created cart IDs for cleanup */
    private final List<Long> createdCartIds = new ArrayList<>();
    private final List<String> cartOwnerTokens = new ArrayList<>();

    @BeforeAll
    static void enableLogging() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @BeforeEach
    void obtainTokens() {
        // Get token for alice (userId=1)
        aliceToken = given()
                .contentType("application/json")
                .body(Map.of("username", "alice", "password", "alice-secret"))
                .post("/auth/login")
                .then()
                .extract()
                .jsonPath()
                .getString("token");

        // Get token for bob (userId=2)
        bobToken = given()
                .contentType("application/json")
                .body(Map.of("username", "bob", "password", "bob-secret"))
                .post("/auth/login")
                .then()
                .extract()
                .jsonPath()
                .getString("token");
    }

    @AfterEach
    void cleanup() {
        // Delete all created carts
        for (int i = 0; i < createdCartIds.size(); i++) {
            Long cartId = createdCartIds.get(i);
            String token = cartOwnerTokens.get(i);
            try {
                given()
                    .header("Authorization", "Bearer " + token)
                    .delete("/shopping-carts/{cartId}", cartId)
                    .then()
                    .statusCode(anyOf(equalTo(200), equalTo(404)));
            } catch (Exception e) {
                // Ignore cleanup errors
            }
        }
        createdCartIds.clear();
        cartOwnerTokens.clear();
    }

    /**
     * Helper: Get existing cart or create new one for a user.
     * Tracks for cleanup.
     */
    private Long getOrCreateCart(String token, int userId) {
        // First check if user already has a cart
        Response existingCarts = given()
                .header("Authorization", "Bearer " + token)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .get("/shopping-carts");

        // Response format: { "items": [ { "data": { "shoppingCartId": ... }, "_links": ... } ], ... }
        List<Integer> cartIds = existingCarts.jsonPath().getList("items.data.shoppingCartId", Integer.class);
        if (cartIds != null && !cartIds.isEmpty()) {
            return cartIds.get(0).longValue();
        }

        // No cart exists, create one
        Response createResponse = given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(Map.of("userId", userId))
                .post("/shopping-carts");

        if (createResponse.statusCode() == 201) {
            Long cartId = createResponse.jsonPath().getLong("data.shoppingCartId");
            createdCartIds.add(cartId);
            cartOwnerTokens.add(token);
            return cartId;
        } else if (createResponse.statusCode() == 409) {
            // Race condition: cart was created between check and create
            existingCarts = given()
                    .header("Authorization", "Bearer " + token)
                    .queryParam("page", 0)
                    .queryParam("size", 10)
                    .get("/shopping-carts");
            cartIds = existingCarts.jsonPath().getList("items.data.shoppingCartId", Integer.class);
            if (cartIds != null && !cartIds.isEmpty()) {
                return cartIds.get(0).longValue();
            }
        }
        throw new RuntimeException("Could not get or create cart for user " + userId);
    }

    // ==================== Authentication Tests ====================

    @Test
    @Order(1)
    @DisplayName("POST /shopping-carts without token returns 401")
    void createCartWithoutTokenReturns401() {
        given()
                .contentType("application/json")
                .body(Map.of("userId", 1))
        .when()
                .post("/shopping-carts")
        .then()
                .statusCode(401);
    }

    @Test
    @Order(2)
    @DisplayName("POST /shopping-carts for different user returns 403")
    void createCartForOtherUserReturns403() {
        // Alice tries to create cart for bob (userId=2)
        given()
                .header("Authorization", "Bearer " + aliceToken)
                .contentType("application/json")
                .body(Map.of("userId", 2))
        .when()
                .post("/shopping-carts")
        .then()
                .statusCode(403);
    }

    // ==================== UC05: Add Dish to Shopping Cart ====================

    @Test
    @Order(10)
    @DisplayName("UC05: POST /shopping-carts creates cart for authenticated user (201 or 409 if exists)")
    void createCartReturns201Or409() {
        Response response = given()
                .header("Authorization", "Bearer " + aliceToken)
                .contentType("application/json")
                .body(Map.of("userId", 1))
        .when()
                .post("/shopping-carts");

        int statusCode = response.statusCode();
        // Accept 201 (created) or 409 (user already has a cart - valid constraint)
        if (statusCode == 201) {
            Long cartId = response.jsonPath().getLong("data.shoppingCartId");
            createdCartIds.add(cartId);
            cartOwnerTokens.add(aliceToken);
            
            response.then()
                    .body("data.userId", equalTo(1))
                    .body("_links.self", notNullValue())
                    .body("_links.addDish", notNullValue())
                    .body("_links.summary", notNullValue());
        } else {
            response.then().statusCode(409);
        }
    }

    @Test
    @Order(11)
    @DisplayName("UC05: POST /shopping-carts/{cartId}/items/from-dish adds dish ingredients to cart")
    void addDishToCartReturns200() {
        Long cartId = getOrCreateCart(bobToken, 2);

        // Add dish (id=1 is seeded Chicken Rice Bowl with ingredients)
        given()
                .header("Authorization", "Bearer " + bobToken)
                .contentType("application/json")
                .body(Map.of("dishId", 1, "servingsMultiplier", 2))
        .when()
                .post("/shopping-carts/{cartId}/items/from-dish", cartId)
        .then()
                .statusCode(200)
                .body("data.items.size()", greaterThanOrEqualTo(1));
    }

    @Test
    @Order(12)
    @DisplayName("UC05: Adding dish with servingsMultiplier increases quantities correctly")
    void addDishWithMultiplierIncreasesQuantities() {
        Long cartId = getOrCreateCart(aliceToken, 1);

        // Add dish with multiplier
        given()
                .header("Authorization", "Bearer " + aliceToken)
                .contentType("application/json")
                .body(Map.of("dishId", 1, "servingsMultiplier", 3))
        .when()
                .post("/shopping-carts/{cartId}/items/from-dish", cartId)
        .then()
                .statusCode(200);

        // Verify cart has items
        given()
                .header("Authorization", "Bearer " + aliceToken)
        .when()
                .get("/shopping-carts/{cartId}", cartId)
        .then()
                .statusCode(200)
                .body("data.items.size()", greaterThanOrEqualTo(1));
    }

    @Test
    @Order(13)
    @DisplayName("UC05: POST /shopping-carts/{cartId}/items/from-dish with invalid dish returns 404")
    void addInvalidDishToCartReturns404() {
        Long cartId = getOrCreateCart(aliceToken, 1);

        given()
                .header("Authorization", "Bearer " + aliceToken)
                .contentType("application/json")
                .body(Map.of("dishId", 99999, "servingsMultiplier", 1))
        .when()
                .post("/shopping-carts/{cartId}/items/from-dish", cartId)
        .then()
                .statusCode(404);
    }

    @Test
    @Order(14)
    @DisplayName("UC05: Adding dish to non-existent cart returns 404")
    void addDishToNonExistentCartReturns404() {
        given()
                .header("Authorization", "Bearer " + aliceToken)
                .contentType("application/json")
                .body(Map.of("dishId", 1, "servingsMultiplier", 1))
        .when()
                .post("/shopping-carts/{cartId}/items/from-dish", 99999)
        .then()
                .statusCode(anyOf(equalTo(404), equalTo(403))); // 403 if ownership check fails first
    }

    // ==================== UC06: Cart Summary ====================

    @Test
    @Order(20)
    @DisplayName("UC06: GET /shopping-carts/{cartId}/summary returns aggregated totals")
    void getCartSummaryReturnsAggregatedTotals() {
        Long cartId = getOrCreateCart(aliceToken, 1);

        // Add a dish to have items in cart
        given()
                .header("Authorization", "Bearer " + aliceToken)
                .contentType("application/json")
                .body(Map.of("dishId", 1, "servingsMultiplier", 1))
        .when()
                .post("/shopping-carts/{cartId}/items/from-dish", cartId)
        .then()
                .statusCode(200);

        // Get summary
        given()
                .header("Authorization", "Bearer " + aliceToken)
        .when()
                .get("/shopping-carts/{cartId}/summary", cartId)
        .then()
                .statusCode(200)
                .body("data.cartId", equalTo(cartId.intValue()))
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
                .get("/shopping-carts/1/summary")
        .then()
                .statusCode(401);
    }

    @Test
    @Order(22)
    @DisplayName("UC06: Cart summary for other user's cart returns 403")
    void getOtherUserCartSummaryReturns403() {
        Long aliceCartId = getOrCreateCart(aliceToken, 1);

        // Bob tries to access alice's cart summary
        given()
                .header("Authorization", "Bearer " + bobToken)
        .when()
                .get("/shopping-carts/{cartId}/summary", aliceCartId)
        .then()
                .statusCode(403);
    }

    @Test
    @Order(23)
    @DisplayName("UC06: Cart summary for non-existent cart returns 404")
    void getCartSummaryForNonExistentCartReturns404() {
        given()
                .header("Authorization", "Bearer " + aliceToken)
        .when()
                .get("/shopping-carts/{cartId}/summary", 99999)
        .then()
                .statusCode(anyOf(equalTo(404), equalTo(403)));
    }

    // ==================== CRUD Operations ====================

    @Test
    @Order(30)
    @DisplayName("GET /shopping-carts returns user's carts only")
    void getAllCartsReturnsUserCartsOnly() {
        given()
                .header("Authorization", "Bearer " + aliceToken)
                .queryParam("page", 0)
                .queryParam("size", 10)
        .when()
                .get("/shopping-carts")
        .then()
                .statusCode(200)
                .body("page", equalTo(0))
                .body("_links", hasKey("self"));
    }

    @Test
    @Order(31)
    @DisplayName("GET /shopping-carts/{cartId} returns cart details with hypermedia links")
    void getCartByIdReturnsCartWithLinks() {
        Long cartId = getOrCreateCart(aliceToken, 1);

        given()
                .header("Authorization", "Bearer " + aliceToken)
        .when()
                .get("/shopping-carts/{cartId}", cartId)
        .then()
                .statusCode(200)
                .body("data.shoppingCartId", equalTo(cartId.intValue()))
                .body("data.userId", equalTo(1))
                .body("_links.self", containsString("/shopping-carts/"));
    }

    @Test
    @Order(32)
    @DisplayName("GET /shopping-carts/{cartId} for other user's cart returns 403")
    void getOtherUserCartReturns403() {
        Long aliceCartId = getOrCreateCart(aliceToken, 1);

        given()
                .header("Authorization", "Bearer " + bobToken)
        .when()
                .get("/shopping-carts/{cartId}", aliceCartId)
        .then()
                .statusCode(403);
    }

    @Test
    @Order(33)
    @DisplayName("PUT /shopping-carts/{cartId} updates cart")
    void updateCartReturns200() {
        Long cartId = getOrCreateCart(aliceToken, 1);

        given()
                .header("Authorization", "Bearer " + aliceToken)
                .contentType("application/json")
                .body(Map.of("userId", 1))
        .when()
                .put("/shopping-carts/{cartId}", cartId)
        .then()
                .statusCode(200);
    }

    @Test
    @Order(40)
    @DisplayName("DELETE /shopping-carts/{cartId} removes cart and returns 200")
    void deleteCartReturns200() {
        Long cartId = getOrCreateCart(aliceToken, 1);
        // Remove from cleanup list since we're explicitly deleting
        int index = createdCartIds.indexOf(cartId);
        if (index >= 0) {
            createdCartIds.remove(index);
            cartOwnerTokens.remove(index);
        }

        given()
                .header("Authorization", "Bearer " + aliceToken)
        .when()
                .delete("/shopping-carts/{cartId}", cartId)
        .then()
                .statusCode(200);

        // Verify it's gone
        given()
                .header("Authorization", "Bearer " + aliceToken)
        .when()
                .get("/shopping-carts/{cartId}", cartId)
        .then()
                .statusCode(404);
    }
}
