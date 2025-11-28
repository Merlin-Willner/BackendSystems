package domain.entity;


import java.util.UUID;

public class CartItem {

    private UUID cartItemId;      // Eindeutige ID
    private UUID shoppingCartId;  // Referenz auf den Einkaufswagen
    private UUID foodItemId;      // Referenz auf FoodItem
    private int quantity;         // Anzahl der Packungen
    private double totalPrice;    // berechneter Gesamtpreis (€)

    public CartItem(UUID shoppingCartId, UUID foodItemId, int quantity, double totalPrice) {
        this.cartItemId = UUID.randomUUID();
        this.shoppingCartId = shoppingCartId;
        this.foodItemId = foodItemId;
        setQuantity(quantity);
        setTotalPrice(totalPrice);
    }

    public CartItem() {
        this.cartItemId = UUID.randomUUID();
    }

    // Getter & Setter
    public UUID getCartItemId() { return cartItemId; }
    public void setCartItemId(UUID cartItemId) { this.cartItemId = cartItemId; }

    public UUID getShoppingCartId() { return shoppingCartId; }
    public void setShoppingCartId(UUID shoppingCartId) { this.shoppingCartId = shoppingCartId; }

    public UUID getFoodItemId() { return foodItemId; }
    public void setFoodItemId(UUID foodItemId) { this.foodItemId = foodItemId; }

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