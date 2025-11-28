package domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cartItemId;      // Eindeutige ID
    private Long shoppingCartId;  // Referenz auf den Einkaufswagen
    private Long foodItemId;      // Referenz auf FoodItem
    private int quantity;         // Anzahl der Packungen
    private double totalPrice;    // berechneter Gesamtpreis (€)

    public CartItem(Long shoppingCartId, Long foodItemId, int quantity, double totalPrice) {
        this.shoppingCartId = shoppingCartId;
        this.foodItemId = foodItemId;
        setQuantity(quantity);
        setTotalPrice(totalPrice);
    }

    public CartItem() {
        // JPA requires a no-arg constructor
    }

    // Getter & Setter
    public Long getCartItemId() { return cartItemId; }
    public void setCartItemId(Long cartItemId) { this.cartItemId = cartItemId; }

    public Long getShoppingCartId() { return shoppingCartId; }
    public void setShoppingCartId(Long shoppingCartId) { this.shoppingCartId = shoppingCartId; }

    public Long getFoodItemId() { return foodItemId; }
    public void setFoodItemId(Long foodItemId) { this.foodItemId = foodItemId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) {
        if(quantity <= 0) throw new IllegalArgumentException("Menge muss größer als 0 sein");
        this.quantity = quantity;
    }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) {
        if(totalPrice < 0) throw new IllegalArgumentException("Gesamtpreis darf nicht negativ sein");
        this.totalPrice = totalPrice;
    }
}
