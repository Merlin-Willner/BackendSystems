package adapters.API;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

@QuarkusTest
class ShoppingCartResourceIT {

    @BeforeAll
    static void enableLogging() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test
    @DisplayName("POST /shopping-carts/{cartId}/items/from-dish currently treats cartId as userId (known bug)")
    void addDishToCartPathUsesUserId() {
        given()
                .contentType("application/json")
                .body("""
                        {"dishId":1,"servingsMultiplier":1}
                        """)
        .when()
                .post("/shopping-carts/1/items/from-dish")
        .then()
                .statusCode(200)
                .body("items.size()", greaterThanOrEqualTo(1));
    }

    @Test
    @Disabled("UC06 summary endpoint not implemented")
    @DisplayName("GET /shopping-carts/{cartId}/summary should return aggregated totals (missing)")
    void cartSummaryNotImplemented() {
        given()
        .when()
                .get("/shopping-carts/1/summary")
        .then()
                .statusCode(200);
    }
}
