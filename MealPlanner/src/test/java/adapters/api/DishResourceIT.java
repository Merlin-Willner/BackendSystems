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
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for DishResource - covers UC02 (Compose Dish) and UC04 (Adjust Dish Composition).
 * 
 * All tests use unique names (UUID) and clean up created entities after each test
 * to ensure database state is unchanged after test execution.
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DishResourceIT {

    /** Track created dish IDs for cleanup */
    private final List<Long> createdDishIds = new ArrayList<>();

    @BeforeAll
    static void enableLogging() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @AfterEach
    void cleanup() {
        // Delete all created dishes in reverse order
        for (int i = createdDishIds.size() - 1; i >= 0; i--) {
            Long id = createdDishIds.get(i);
            try {
                String etag = getDishEtag(id);
                if (etag != null) {
                    given()
                        .header("If-Match", etag)
                        .delete("/dishes/{id}", id)
                        .then()
                        .statusCode(anyOf(equalTo(204), equalTo(404))); // 204 No Content or 404 if already deleted
                }
            } catch (Exception e) {
                // Ignore cleanup errors
            }
        }
        createdDishIds.clear();
    }

    /**
     * Helper: Create a dish and track for cleanup.
     */
    private Long createDish(String name) {
        Long id = given()
                .contentType("application/json")
                .body(Map.of(
                        "name", name,
                        "category", "DINNER",
                        "servingWeight", 400,
                        "preparationTime", 15,
                        "imageUrl", "",
                        "userId", 1,
                        "ingredients", List.of(
                                Map.of("foodItemId", 1, "weight", 150), // Chicken Breast
                                Map.of("foodItemId", 2, "weight", 200)  // White Rice
                        )
                ))
            .when()
                .post("/dishes")
            .then()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getLong("data.dishId");
        
        createdDishIds.add(id);
        return id;
    }

    private String getDishEtag(Long id) {
        Response response = given()
                .get("/dishes/{id}", id);
        if (response.statusCode() != 200) {
            return null;
        }
        return response.getHeader("ETag");
    }

    // ==================== Read Tests (use seeded data) ====================

    @Test
    @Order(1)
    @DisplayName("GET /dishes/{id} returns seeded dish")
    void getDishByIdReturnsSeeded() {
        // Dish id=1 (Chicken Rice Bowl) is seeded in import.sql
        given()
        .when()
                .get("/dishes/1")
        .then()
                .statusCode(200)
                .body("data.name", equalTo("Chicken Rice Bowl"))
                .body("data.dishId", equalTo(1))
                .body("_links.self", containsString("/dishes/1"));
    }

    @Test
    @Order(2)
    @DisplayName("GET /dishes/{id} returns 404 for missing dish")
    void getMissingDishReturns404() {
        given()
        .when()
                .get("/dishes/99999")
        .then()
                .statusCode(404);
    }

    @Test
    @Order(3)
    @DisplayName("GET /dishes returns paginated list with hypermedia links")
    void getAllDishesWithPagination() {
        given()
                .queryParam("page", 0)
                .queryParam("size", 5)
        .when()
                .get("/dishes")
        .then()
                .statusCode(200)
                .body("page", equalTo(0))
                .body("size", equalTo(5))
                .body("total", greaterThanOrEqualTo(1))
                .body("_links", hasKey("self"))
                .body("_links", hasKey("create"));
    }

    @Test
    @Order(4)
    @DisplayName("GET /dishes with invalid page params returns 400")
    void getAllDishesInvalidPaginationReturns400() {
        given()
                .queryParam("page", -1)
                .queryParam("size", 10)
        .when()
                .get("/dishes")
        .then()
                .statusCode(400);
    }

    // ==================== UC02: Compose Dish ====================

    @Test
    @Order(10)
    @DisplayName("UC02: POST /dishes creates a dish with ingredients and computes totals")
    void createDishReturns201() {
        String name = "IT-Dish-" + UUID.randomUUID();

        Long id = given()
                .contentType("application/json")
                .body(Map.of(
                        "name", name,
                        "category", "DINNER",
                        "servingWeight", 400,
                        "preparationTime", 10,
                        "imageUrl", "",
                        "userId", 1,
                        "ingredients", List.of(
                                Map.of("foodItemId", 1, "weight", 150), // Chicken Breast: 31g protein/100g
                                Map.of("foodItemId", 2, "weight", 200)  // White Rice: 7g protein/100g
                        )
                ))
        .when()
                .post("/dishes")
        .then()
                .statusCode(201)
                .body("data.dishId", notNullValue())
                .body("data.name", equalTo(name))
                .body("data.totalProtein", notNullValue()) // Computed
                .body("data.totalCalories", notNullValue()) // Computed
                .body("_links.self", containsString("/dishes/"))
                .extract()
                .jsonPath()
                .getLong("data.dishId");
        
        createdDishIds.add(id);

        // Verify it can be fetched
        given()
        .when()
                .get("/dishes/{id}", id)
        .then()
                .statusCode(200)
                .body("data.name", equalTo(name));
    }

    @Test
    @Order(11)
    @DisplayName("UC02: POST /dishes with unknown foodItemId returns 404")
    void createDishWithUnknownFoodItemReturns404() {
        given()
                .contentType("application/json")
                .body(Map.of(
                        "name", "Invalid-Dish-" + UUID.randomUUID(),
                        "category", "LUNCH",
                        "servingWeight", 300,
                        "preparationTime", 10,
                        "imageUrl", "",
                        "userId", 1,
                        "ingredients", List.of(
                                Map.of("foodItemId", 99999, "weight", 100) // Non-existent
                        )
                ))
        .when()
                .post("/dishes")
        .then()
                .statusCode(404);
    }

    @Test
    @Order(12)
    @DisplayName("UC02: POST /dishes with empty ingredients returns 400")
    void createDishWithEmptyIngredientsReturns400() {
        given()
                .contentType("application/json")
                .body(Map.of(
                        "name", "Empty-Dish-" + UUID.randomUUID(),
                        "category", "LUNCH",
                        "servingWeight", 300,
                        "preparationTime", 10,
                        "imageUrl", "",
                        "userId", 1,
                        "ingredients", List.of() // Empty!
                ))
        .when()
                .post("/dishes")
        .then()
                .statusCode(400);
    }

    // ==================== UC04: Adjust Dish Composition ====================

    @Test
    @Order(20)
    @DisplayName("UC04: POST /dishes/{id}/ingredients adds an ingredient")
    void addIngredientToDish() {
        Long dishId = createDish("IT-AddIngr-" + UUID.randomUUID());

        // Add Broccoli (foodItemId=3) to the dish
        var ingredients = given()
                .contentType("application/json")
                .body(Map.of("foodItemId", 3, "weight", 100))
        .when()
                .post("/dishes/{id}/ingredients", dishId)
        .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList("data.ingredients.foodItemId", Integer.class);

        assertTrue(ingredients.contains(3), "Broccoli should be added");
        assertEquals(3, ingredients.size(), "Dish should now have 3 ingredients");
    }

    @Test
    @Order(21)
    @DisplayName("UC04: PATCH /dishes/{id}/ingredients/{foodItemId} updates ingredient weight")
    void updateIngredientWeight() {
        Long dishId = createDish("IT-Patch-" + UUID.randomUUID());
        String etag = getDishEtag(dishId);

        // Update weight of Chicken Breast (foodItemId=1) - PATCH gibt 200 zurück (teilweise Änderung)
        given()
                .header("If-Match", etag)
                .contentType("application/json")
                .body(Map.of("weight", 250))
        .when()
                .patch("/dishes/{id}/ingredients/{foodItemId}", dishId, 1)
        .then()
                .statusCode(200);

        // Verify the weight was updated
        var ingredients = given()
        .when()
                .get("/dishes/{id}", dishId)
        .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList("data.ingredients");

        boolean found = ingredients.stream()
                .filter(ing -> ((Map<?, ?>) ing).get("foodItemId").equals(1))
                .anyMatch(ing -> ((Number) ((Map<?, ?>) ing).get("weight")).doubleValue() == 250.0);
        assertTrue(found, "Ingredient weight should be updated to 250");
    }

    @Test
    @Order(22)
    @DisplayName("UC04: DELETE /dishes/{id}/ingredients/{foodItemId} removes ingredient and returns 204")
    void removeIngredientFromDish() {
        Long dishId = createDish("IT-Remove-" + UUID.randomUUID());
        String etag = getDishEtag(dishId);

        // Remove White Rice (foodItemId=2) from the dish
        given()
                .header("If-Match", etag)
        .when()
                .delete("/dishes/{id}/ingredients/{foodItemId}", dishId, 2)
        .then()
                .statusCode(204);

        // Verify it's removed
        var ingredients = given()
        .when()
                .get("/dishes/{id}", dishId)
        .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList("data.ingredients.foodItemId", Integer.class);

        assertFalse(ingredients.contains(2), "White Rice should be removed");
        assertEquals(1, ingredients.size(), "Dish should now have 1 ingredient");
    }

    @Test
    @Order(23)
    @DisplayName("UC04: Adding ingredient to non-existent dish returns 404")
    void addIngredientToNonExistentDishReturns404() {
        given()
                .contentType("application/json")
                .body(Map.of("foodItemId", 1, "weight", 100))
        .when()
                .post("/dishes/{id}/ingredients", 99999)
        .then()
                .statusCode(404);
    }

    // ==================== Update and Delete ====================

    @Test
    @Order(30)
    @DisplayName("PUT /dishes/{id} updates an existing dish and returns 204 No Content")
    void updateDishReturns204() {
        Long dishId = createDish("IT-Update-" + UUID.randomUUID());
        String etag = getDishEtag(dishId);

        String newName = "Updated-Dish-" + UUID.randomUUID();
        given()
                .header("If-Match", etag)
                .contentType("application/json")
                .body(Map.of(
                        "name", newName,
                        "category", "BREAKFAST",
                        "servingWeight", 500,
                        "preparationTime", 25,
                        "imageUrl", "http://example.com/new.jpg",
                        "userId", 1,
                        "ingredients", List.of(
                                Map.of("foodItemId", 3, "weight", 150) // Just Broccoli
                        )
                ))
        .when()
                .put("/dishes/{id}", dishId)
        .then()
                .statusCode(204);
    }

    @Test
    @Order(31)
    @DisplayName("PUT /dishes/{id} returns 404 for missing dish")
    void updateMissingDishReturns404() {
        given()
                .contentType("application/json")
                .body(Map.of(
                        "name", "Any",
                        "category", "LUNCH",
                        "servingWeight", 300,
                        "preparationTime", 10,
                        "imageUrl", "",
                        "userId", 1,
                        "ingredients", List.of(Map.of("foodItemId", 1, "weight", 100))
                ))
        .when()
                .put("/dishes/99999")
        .then()
                .statusCode(404);
    }

    @Test
    @Order(40)
    @DisplayName("DELETE /dishes/{id} removes dish and returns 204 No Content")
    void deleteDishReturns204() {
        Long dishId = createDish("IT-Delete-" + UUID.randomUUID());
        createdDishIds.remove(dishId); // Don't double-delete in cleanup
        String etag = getDishEtag(dishId);

        // Delete it - 204 No Content gemäß REST-Spezifikation
        given()
                .header("If-Match", etag)
        .when()
                .delete("/dishes/{id}", dishId)
        .then()
                .statusCode(204);

        // Verify it's gone
        given()
        .when()
                .get("/dishes/{id}", dishId)
        .then()
                .statusCode(404);
    }

    @Test
    @Order(41)
    @DisplayName("DELETE /dishes/{id} returns 404 for missing dish")
    void deleteMissingDishReturns404() {
        given()
        .when()
                .delete("/dishes/99999")
        .then()
                .statusCode(404);
    }
}
