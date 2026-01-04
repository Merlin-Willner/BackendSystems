package adapters.API;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;

@QuarkusTest
class FoodItemResourceIT {

    @BeforeAll
    static void enableLogging() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test
    @DisplayName("POST /food-items creates a food item with 201 and Location")
    void createFoodItemReturns201() {
        String uniqueName = "IT-Food-" + UUID.randomUUID();

        given()
                .contentType("application/json")
                .body(Map.of(
                        "name", uniqueName,
                        "brand", "Brand",
                        "packSize", 500,
                        "packPrice", 3.5,
                        "proteinPer100g", 10,
                        "carbsPer100g", 20,
                        "fatPer100g", 5,
                        "caloriesPer100g", 180
                ))
        .when()
                .post("/food-items")
        .then()
                .statusCode(201)
                .header("Location", notNullValue())
                .body("foodItemId", notNullValue());
    }

    @Test
    @DisplayName("POST /food-items with duplicate name returns 409")
    void duplicateNameReturns409() {
        given()
                .contentType("application/json")
                .body(Map.of(
                        "name", "Chicken Breast", // seeded in import.sql
                        "brand", "Any",
                        "packSize", 100,
                        "packPrice", 1.0,
                        "proteinPer100g", 1,
                        "carbsPer100g", 1,
                        "fatPer100g", 1,
                        "caloriesPer100g", 10
                ))
        .when()
                .post("/food-items")
        .then()
                .statusCode(409);
    }

    @Test
    @DisplayName("GET /food-items/search filters by protein and sorts desc")
    void searchFiltersByProtein() {
        List<Float> proteins =
                given()
                        .queryParam("minProtein", 20)
                        .queryParam("sortBy", "protein")
                .when()
                        .get("/food-items/search")
                .then()
                        .statusCode(200)
                        .body("size()", greaterThanOrEqualTo(1))
                        .extract()
                        .jsonPath()
                        .getList("proteinPer100g", Float.class);

        assertFalse(proteins.isEmpty(), "Expected at least one result");
        assertFalse(proteins.stream().anyMatch(p -> p < 20), "All results should meet minProtein filter");
    }
}
