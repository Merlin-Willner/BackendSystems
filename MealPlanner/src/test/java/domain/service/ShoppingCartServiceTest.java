package domain.service;

import application.port.out.DishRepository;
import application.port.out.ShoppingCartRepository;
import domain.entity.CartItem;
import domain.entity.Dish;
import domain.entity.DishCategory;
import domain.entity.DishIngredient;
import domain.entity.FoodItem;
import domain.entity.ShoppingCart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShoppingCartServiceTest {

    @Mock
    ShoppingCartRepository cartRepository;

    @Mock
    DishRepository dishRepository;

    ShoppingCartService service;

    @BeforeEach
    void setUp() {
        service = new ShoppingCartService();
        // Inject mocks (fields are package-private for CDI; set via reflection)
        service.cartRepository = cartRepository;
        service.dishRepository = dishRepository;
    }

    private Dish buildDishWithIngredient(long dishId, FoodItem item, double weight) {
        Dish dish = new Dish(1L, "Dish", DishCategory.LUNCH, 300, 10, null);
        dish.setDishId(dishId);
        dish.addIngredient(item, weight);
        return dish;
    }

    @Test
    @DisplayName("addDishToCart calculates required packs and saves cart")
    void addDishToCartCalculatesQuantity() {
        ShoppingCart cart = new ShoppingCart(99L);
        cart.setShoppingCartId(50L);

        FoodItem rice = new FoodItem("Rice", "B", 500, 2.0, 7, 78, 1, 360);
        rice.setFoodItemId(10L);
        Dish dish = buildDishWithIngredient(5L, rice, 600); // needs ceil(600/500)=2 packs

        when(cartRepository.findById(50L)).thenReturn(Optional.of(cart));
        when(dishRepository.findById(5L)).thenReturn(Optional.of(dish));
        when(cartRepository.save(org.mockito.ArgumentMatchers.any(ShoppingCart.class)))
                .thenAnswer(inv -> inv.getArgument(0, ShoppingCart.class));

        ShoppingCart updated = service.addDishToCart(50L, 5L, 1);

        ArgumentCaptor<ShoppingCart> captor = ArgumentCaptor.forClass(ShoppingCart.class);
        verify(cartRepository).save(captor.capture());
        ShoppingCart saved = captor.getValue();

        assertEquals(1, saved.getItems().size());
        CartItem item = saved.getItems().get(0);
        assertEquals(2, item.getQuantity(), "Quantity should be ceil(600/500) = 2");
        assertEquals(updated, saved);
    }

    @Test
    @DisplayName("addDishToCart multiplies quantity by servingsMultiplier")
    void addDishToCartMultiplier() {
        ShoppingCart cart = new ShoppingCart(99L);
        cart.setShoppingCartId(50L);

        FoodItem rice = new FoodItem("Rice", "B", 1000, 2.0, 7, 78, 1, 360);
        rice.setFoodItemId(10L);
        Dish dish = buildDishWithIngredient(5L, rice, 400); // base packs ceil(400/1000)=1

        when(cartRepository.findById(50L)).thenReturn(Optional.of(cart));
        when(dishRepository.findById(5L)).thenReturn(Optional.of(dish));
        when(cartRepository.save(org.mockito.ArgumentMatchers.any(ShoppingCart.class)))
                .thenAnswer(inv -> inv.getArgument(0, ShoppingCart.class));

        ShoppingCart updated = service.addDishToCart(50L, 5L, 3);

        CartItem item = updated.getItems().get(0);
        // weight * 3 = 1200; ceil(1200/1000) = 2
        assertEquals(2, item.getQuantity());
    }

    @Test
    @DisplayName("addDishToCart rejects non-positive multiplier")
    void addDishToCartRejectsNonPositive() {
        ShoppingCart cart = new ShoppingCart(1L);
        cart.setShoppingCartId(1L);
        FoodItem rice = new FoodItem("Rice", "B", 1000, 2.0, 7, 78, 1, 360);
        rice.setFoodItemId(10L);
        Dish dish = buildDishWithIngredient(1L, rice, 100);

        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
        assertThrows(IllegalArgumentException.class, () -> service.addDishToCart(1L, 1L, 0));
    }

    @Test
    @DisplayName("addDishToCart fails on missing dish or cart")
    void addDishToCartMissing() {
        when(cartRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(jakarta.ws.rs.NotFoundException.class, () -> service.addDishToCart(1L, 2L, 1));

        ShoppingCart cart = new ShoppingCart(1L);
        cart.setShoppingCartId(1L);
        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(dishRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(jakarta.ws.rs.NotFoundException.class, () -> service.addDishToCart(1L, 2L, 1));
    }

    @Test
    @DisplayName("addDishToCart rejects dish without ingredients")
    void addDishToCartDishWithoutIngredients() {
        ShoppingCart cart = new ShoppingCart(1L);
        cart.setShoppingCartId(1L);
        Dish dish = new Dish(1L, "Empty", DishCategory.LUNCH, 100, 10, null);
        dish.setDishId(2L);

        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(dishRepository.findById(2L)).thenReturn(Optional.of(dish));

        assertThrows(jakarta.ws.rs.WebApplicationException.class, () -> service.addDishToCart(1L, 2L, 1));
    }

    @Test
    @DisplayName("getOrCreateCart returns existing or creates new")
    void getOrCreateCartCreatesWhenMissing() {
        when(cartRepository.findByUserId(7L)).thenReturn(Optional.empty());
        when(cartRepository.save(org.mockito.ArgumentMatchers.any(ShoppingCart.class)))
                .thenAnswer(inv -> {
                    ShoppingCart c = inv.getArgument(0, ShoppingCart.class);
                    c.setShoppingCartId(123L);
                    return c;
                });

        ShoppingCart created = service.getOrCreateCart(7L);
        assertEquals(7L, created.getUserId());

        ShoppingCart existing = new ShoppingCart(7L);
        existing.setShoppingCartId(99L);
        when(cartRepository.findByUserId(7L)).thenReturn(Optional.of(existing));

        ShoppingCart reused = service.getOrCreateCart(7L);
        assertEquals(existing, reused);
    }

    @Test
    @DisplayName("addDishToCart rejects invalid food item data")
    void addDishToCartRejectsBadFoodItemValues() {
        ShoppingCart cart = new ShoppingCart(1L);
        cart.setShoppingCartId(1L);
        FoodItem item = new FoodItem("Bad", "B", 0, 0, 1, 1, 1, 1); // packSize/packPrice invalid
        item.setFoodItemId(10L);
        Dish dish = buildDishWithIngredient(2L, item, 100);

        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(dishRepository.findById(2L)).thenReturn(Optional.of(dish));

        assertThrows(jakarta.ws.rs.WebApplicationException.class, () -> service.addDishToCart(1L, 2L, 1));
    }
}
