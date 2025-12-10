package domain.service;


import application.port.in.ShoppingCartAPI;
import application.port.out.DishRepository;
import application.port.out.FoodItemRepository;
import application.port.out.ShoppingCartRepository;
import domain.entity.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;

@ApplicationScoped
public class ShoppingCartService implements ShoppingCartAPI {

    @Inject
    ShoppingCartRepository cartRepository;

    @Inject
    DishRepository dishRepository;

    @Override
    @Transactional
    public ShoppingCart addDishToCart(Long cartId, Long dishId, int servingsMultiplier) {
        if(servingsMultiplier <= 0){
            throw new IllegalArgumentException("servingsMultiplier muss positiv sein");
        }

        ShoppingCart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new NotFoundException("Shopping cart not found: " + cartId));

        Dish dish = dishRepository.findById(dishId)
                .orElseThrow(() -> new NotFoundException("Dish not found: " + dishId));

        if(dish.getIngredients() == null || dish.getIngredients().isEmpty()){
            throw new WebApplicationException("Dish hat keine Ingredients", 422);
        }

        for(DishIngredient dishIngredient : dish.getIngredients()){
            FoodItem foodItem = dishIngredient.getFoodItem();
            if(foodItem == null || foodItem.getPackPrice() <= 0 || foodItem.getPackSize() <= 0){ // soll null erlaubt sein ja/nein
                throw new WebApplicationException("Fooditem hat keine gültigen Werte", 422);
            }

            double totalWeight = dishIngredient.getWeight() * servingsMultiplier;
            double packs = totalWeight / foodItem.getPackSize();
            int requiredPacks = (int) Math.ceil(packs); // Rundet immer auf
            if(requiredPacks <= 0) {requiredPacks = 1;}

            CartItem item = new CartItem();
            item.setShoppingCartId(cart.getShoppingCartId());
            item.setFoodItemId(foodItem.getFoodItemId());
            item.setQuantity(requiredPacks);

            cart.getItems().add(item);
        } // ende for-loop

        return cartRepository.save(cart);
    }
}
