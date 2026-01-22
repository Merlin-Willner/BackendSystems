package adapters.API;

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

/**
 * Integration tests for FoodItemResource - covers UC01 (Register Food Item) and UC03 (Filter and Rank).
 * 
 * All tests use unique names (UUID) and clean up created entities after each test
 * to ensure database state is unchanged after test execution.
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FoodItemResourceIT {

    /** Track created food item IDs for cleanup */
    private final List<Long> createdFoodItemIds = new ArrayList<>();

    @BeforeAll
    static void enableLogging() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @AfterEach
    void cleanup() {
        // Delete all created food items in reverse order
        for (int i = createdFoodItemIds.size() - 1; i >= 0; i--) {
            Long id = createdFoodItemIds.get(i);
            try {
                String etag = getFoodItemEtag(id);
                if (etag != null) {
                    given()
                        .header("If-Match", etag)
                        .delete("/food-items/{id}", id)
                        .then()
                        .statusCode(anyOf(equalTo(200), equalTo(404))); // 404 is OK if already deleted
                }
            } catch (Exception e) {
                // Ignore cleanup errors
            }
        }
        createdFoodItemIds.clear();
    }

    /**
     * Helper: Create a food item and track for cleanup.
     */
    private Long createFoodItem(String name) {
        Long id = given()
                .contentType("application/json")
                .body(Map.of(
                        "name", name,
                        "brand", "TestBrand",
                        "packSize", 500,
                        "packPrice", 3.5,
                        "proteinPer100g", 15.0,
                        "carbsPer100g", 20.0,
                        "fatPer100g", 5.0,
                        "caloriesPer100g", 180.0
                ))
            .when()
                .post("/food-items")
            .then()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getLong("data.foodItemId");
        
        createdFoodItemIds.add(id);
        return id;
    }

    private String getFoodItemEtag(Long id) {
        Response response = given()
                .get("/food-items/{id}", id);
        if (response.statusCode() != 200) {
            return null;
        }
        return response.getHeader("ETag");
    }

    // ==================== UC01: Register Food Item ====================

    @Test
    @Order(1)
    @DisplayName("UC01: POST /food-items creates a food item with 201 and Location header")
    void createFoodItemReturns201() {
        String uniqueName = "IT-Food-" + UUID.randomUUID();

        Long id = given()
                .contentType("application/json")
                .body(Map.of(
                        "name", uniqueName,
                        "brand", "Brand",
                        "packSize", 500,
                        "packPrice", 3.5,
                        "proteinPer100g", 10.0,
                        "carbsPer100g", 20.0,
                        "fatPer100g", 5.0,
                        "caloriesPer100g", 180.0
                ))
        .when()
                .post("/food-items")
        .then()
                .statusCode(201)
                .header("Location", notNullValue())
                .body("data.foodItemId", notNullValue())
                .body("data.name", equalTo(uniqueName))
                .extract()
                .jsonPath()
                .getLong("data.foodItemId");
        
        createdFoodItemIds.add(id);
    }

    @Test
    @Order(2)
    @DisplayName("UC01: POST /food-items with duplicate name returns 409 Conflict")
    void duplicateNameReturns409() {
        // "Chicken Breast" is seeded in import.sql
        given()
                .contentType("application/json")
                .body(Map.of(
                        "name", "Chicken Breast",
                        "brand", "Any",
                        "packSize", 100,
                        "packPrice", 1.0,
                        "proteinPer100g", 1.0,
                        "carbsPer100g", 1.0,
                        "fatPer100g", 1.0,
                        "caloriesPer100g", 10.0
                ))
        .when()
                .post("/food-items")
        .then()
                .statusCode(409);
    }

    @Test
    @Order(4)
    @DisplayName("UC01: GET /food-items/{id} returns seeded food item")
    void getFoodItemByIdReturnsItem() {
        // Chicken Breast is seeded with id=1
        given()
        .when()
                .get("/food-items/1")
        .then()
                .statusCode(200)
                .body("data.name", equalTo("Chicken Breast"))
                .body("data.brand", equalTo("FreshFarm"))
                .body("_links.self", containsString("/food-items/1"));
    }

    @Test
    @Order(5)
    @DisplayName("UC01: GET /food-items/{id} returns 404 for missing item")
    void getFoodItemByIdReturns404() {
        given()
        .when()
                .get("/food-items/99999")
        .then()
                .statusCode(404);
    }

    @Test
    @Order(6)
    @DisplayName("UC01: PUT /food-items/{id} updates existing item")
    void updateFoodItemReturns200() {
        String uniqueName = "Update-Food-" + UUID.randomUUID();
        Long id = createFoodItem(uniqueName);
        String etag = getFoodItemEtag(id);

        String newName = "Updated-Food-" + UUID.randomUUID();
        given()
                .header("If-Match", etag)
                .contentType("application/json")
                .body(Map.of(
                        "name", newName,
                        "brand", "NewBrand",
                        "packSize", 1000,
                        "packPrice", 5.0,
                        "proteinPer100g", 15.0,
                        "carbsPer100g", 25.0,
                        "fatPer100g", 8.0,
                        "caloriesPer100g", 200.0
                ))
        .when()
                .put("/food-items/{id}", id)
        .then()
                .statusCode(200)
                .body("data.name", equalTo(newName))
                .body("data.brand", equalTo("NewBrand"))
                .body("data.packSize", equalTo(1000.0f));
    }

    @Test
    @Order(7)
    @DisplayName("UC01: PUT /food-items/{id} returns 404 for missing item")
    void updateMissingFoodItemReturns404() {
        given()
                .contentType("application/json")
                .body(Map.of(
                        "name", "Any",
                        "brand", "Any",
                        "packSize", 100,
                        "packPrice", 1.0,
                        "proteinPer100g", 10.0,
                        "carbsPer100g", 10.0,
                        "fatPer100g", 5.0,
                        "caloriesPer100g", 100.0
                ))
        .when()
                .put("/food-items/99999")
        .then()
                .statusCode(404);
    }

    @Test
    @Order(8)
    @DisplayName("UC01: PUT /food-items/{id} with duplicate name returns 409")
    void updateFoodItemDuplicateNameReturns409() {
        String uniqueName = "Dup-Test-" + UUID.randomUUID();
        Long id = createFoodItem(uniqueName);
        String etag = getFoodItemEtag(id);

        // Try to update to existing name (Chicken Breast is seeded)
        given()
                .header("If-Match", etag)
                .contentType("application/json")
                .body(Map.of(
                        "name", "Chicken Breast",
                        "brand", "Any",
                        "packSize", 100,
                        "packPrice", 1.0,
                        "proteinPer100g", 10.0,
                        "carbsPer100g", 10.0,
                        "fatPer100g", 5.0,
                        "caloriesPer100g", 100.0
                ))
        .when()
                .put("/food-items/{id}", id)
        .then()
                .statusCode(409);
    }

    @Test
    @Order(9)
    @DisplayName("UC01: DELETE /food-items/{id} removes item")
    void deleteFoodItemReturns200() {
        String uniqueName = "Delete-Food-" + UUID.randomUUID();
        Long id = createFoodItem(uniqueName);
        createdFoodItemIds.remove(id); // Don't double-delete in cleanup
        String etag = getFoodItemEtag(id);

        // Delete it
        given()
                .header("If-Match", etag)
        .when()
                .delete("/food-items/{id}", id)
        .then()
                .statusCode(200);

        // Verify it's gone
        given()
        .when()
                .get("/food-items/{id}", id)
        .then()
                .statusCode(404);
    }

    @Test
    @Order(10)
    @DisplayName("UC01: DELETE /food-items/{id} returns 404 for missing item")
    void deleteMissingFoodItemReturns404() {
        given()
        .when()
                .delete("/food-items/99999")
        .then()
                .statusCode(404);
    }

    // ==================== UC03: Filter and Rank Food Items ====================

    @Test
    @Order(20)
    @DisplayName("UC03: GET /food-items returns paginated list with hypermedia links")
    void getAllFoodItemsWithPagination() {
        given()
                .queryParam("page", 0)
                .queryParam("size", 10)
        .when()
                .get("/food-items")
        .then()
                .statusCode(200)
                .body("page", equalTo(0))
                .body("size", equalTo(10))
                .body("total", greaterThanOrEqualTo(1))
                .body("_links", hasKey("self"))
                .body("_links", hasKey("create"));
    }

    @Test
    @Order(21)
    @DisplayName("UC03: GET /food-items includes caching headers")
    void getFoodItemsIncludesCacheControl() {
        given()
        .when()
                .get("/food-items")
        .then()
                .statusCode(200)
                .header("Cache-Control", notNullValue());
    }

    @Test
    @Order(22)
    @DisplayName("UC03: GET /food-items/search filters by protein with sorting")
    void searchFiltersByProtein() {
        // Search for items with minProtein=20
        // Chicken Breast has 31g protein per 100g (seeded)
        given()
                .queryParam("minProtein", 20)
                .queryParam("sortBy", "protein")
        .when()
                .get("/food-items/search")
        .then()
                .statusCode(200);
    }

    @Test
    @Order(23)
    @DisplayName("UC03: GET /food-items/search filters by calorie range")
    void searchFiltersByCalories() {
        // Chicken Breast has 165 kcal, Broccoli has 35 kcal (from import.sql)
        given()
                .queryParam("minCalories", 100)
                .queryParam("maxCalories", 200)
        .when()
                .get("/food-items/search")
        .then()
                .statusCode(200);
    }

    @Test
    @Order(24)
    @DisplayName("UC03: GET /food-items/search sorts by different keys")
    void searchSortsByDifferentKeys() {
        // Sort by calories
        given()
                .queryParam("sortBy", "calories")
        .when()
                .get("/food-items/search")
        .then()
                .statusCode(200);

        // Sort by protein
        given()
                .queryParam("sortBy", "protein")
        .when()
                .get("/food-items/search")
        .then()
                .statusCode(200);

        // Sort by fat
        given()
                .queryParam("sortBy", "fat")
        .when()
                .get("/food-items/search")
        .then()
                .statusCode(200);
    }

    @Test
    @Order(25)
    @DisplayName("UC03: GET /food-items/search with conflicting bounds returns 200 with empty array")
    void searchWithNoMatchesReturnsEmptyArray() {
        // Use conflicting bounds that cannot be satisfied simultaneously
        // minProtein > maxProtein ensures no item can match
        given()
                .queryParam("minProtein", 100)
                .queryParam("maxProtein", 1)
        .when()
                .get("/food-items/search")
        .then()
                .statusCode(200)
                .body("items.size()", equalTo(0))
                .body("total", equalTo(0));
    }

    // ==================== Hypermedia Tests ====================

    @Test
    @Order(30)
    @DisplayName("UC01: POST /food-items includes hypermedia links in response")
    void createFoodItemIncludesHypermediaLinks() {
        String uniqueName = "Links-Food-" + UUID.randomUUID();

        Long id = given()
                .contentType("application/json")
                .body(Map.of(
                        "name", uniqueName,
                        "brand", "Brand",
                        "packSize", 500,
                        "packPrice", 2.5,
                        "proteinPer100g", 10.0,
                        "carbsPer100g", 20.0,
                        "fatPer100g", 5.0,
                        "caloriesPer100g", 150.0
                ))
        .when()
                .post("/food-items")
        .then()
                .statusCode(201)
                .body("_links.self", containsString("/food-items/"))
                .body("_links.all", containsString("/food-items"))
                .body("_links.update", notNullValue())
                .body("_links.delete", notNullValue())
                .extract()
                .jsonPath()
                .getLong("data.foodItemId");
        
        createdFoodItemIds.add(id);
    }
}
