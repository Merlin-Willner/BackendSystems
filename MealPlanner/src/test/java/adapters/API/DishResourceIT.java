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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class DishResourceIT {

    @BeforeAll
    static void enableLogging() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test
    @DisplayName("GET /dishes/{id} returns seeded dish")
    void getDishByIdReturnsSeeded() {
        given()
        .when()
                .get("/dishes/1")
        .then()
                .statusCode(200)
                .body("name", equalTo("Chicken Rice Bowl"));
    }

    @Test
    @DisplayName("POST /dishes creates a dish with ingredients and returns 201")
    void createDishReturns201() {
        String name = "IT-Dish-" + UUID.randomUUID();

        Long id =
                given()
                        .contentType("application/json")
                        .body(Map.of(
                                "name", name,
                                "category", "DINNER",
                                "servingWeight", 400,
                                "preparationTime", 10,
                                "imageUrl", "",
                                "userId", 1,
                                "ingredients", List.of(
                                        Map.of("foodItemId", 1, "weight", 150),
                                        Map.of("foodItemId", 2, "weight", 200)
                                )
                        ))
                .when()
                        .post("/dishes")
                .then()
                        .statusCode(201)
                        .body("dishId", notNullValue())
                        .extract()
                        .jsonPath()
                        .getLong("dishId");

        // verify it can be fetched
        given()
        .when()
                .get("/dishes/{id}", id)
        .then()
                .statusCode(200)
                .body("name", equalTo(name));
    }

    @Test
    @DisplayName("POST /dishes/{id}/ingredients adds an ingredient")
    void addIngredientToDish() {
        // create a dish first
        Long dishId =
                given()
                        .contentType("application/json")
                        .body(Map.of(
                                "name", "IT-Dish-" + UUID.randomUUID(),
                                "category", "DINNER",
                                "servingWeight", 300,
                                "preparationTime", 5,
                                "imageUrl", "",
                                "userId", 1,
                                "ingredients", List.of(
                                        Map.of("foodItemId", 1, "weight", 100)
                                )
                        ))
                .when()
                        .post("/dishes")
                .then()
                        .statusCode(201)
                        .extract()
                        .jsonPath()
                        .getLong("dishId");

        var ingredients =
                given()
                        .contentType("application/json")
                        .body(Map.of("foodItemId", 2, "weight", 50))
                .when()
                        .post("/dishes/{id}/ingredients", dishId)
                .then()
                        .statusCode(200)
                        .extract()
                        .jsonPath()
                        .getList("ingredients.foodItemId", Integer.class);

        assertTrue(ingredients.contains(2));
        assertEquals(2, ingredients.size(), "Ingredient should be added to existing list");
    }
}
