package domain.entity;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ShoppingCart {

    private UUID shoppingCartId;   // Eindeutige ID
    private UUID userId;           // Besitzer des Carts
    private List<CartItem> items;  // enthaltene Items
    private double totalPrice;     // berechneter Gesamtpreis

    public ShoppingCart(UUID userId) {
        this.shoppingCartId = UUID.randomUUID();
        this.userId = userId;
        this.items = new ArrayList<>();
        this.totalPrice = 0;
    }


    // Getter & Setter
    public UUID getShoppingCartId() { return shoppingCartId; }
    public void setShoppingCartId(UUID shoppingCartId) { this.shoppingCartId = shoppingCartId; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public List<CartItem> getItems() { return items; }

    public double getTotalPrice() { return totalPrice; }

    // Item hinzufügen
    public void addItem(CartItem item) {
        if(item == null) throw new IllegalArgumentException("Item darf nicht null sein");
        items.add(item);
        recalculateTotal();
    }

    // Item entfernen
    public void removeItem(CartItem item) {
        items.remove(item);
        recalculateTotal();
    }

    // Gesamtpreis berechnen
    private void recalculateTotal() {
        totalPrice = 0;
        for(CartItem item : items) {
            totalPrice += item.getTotalPrice();
        }
    }
}