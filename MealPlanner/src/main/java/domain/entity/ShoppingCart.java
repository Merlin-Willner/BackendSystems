package domain.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import java.util.ArrayList;
import java.util.List;

@Entity
public class ShoppingCart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long shoppingCartId;   // Eindeutige ID
    private Long userId;           // Besitzer des Carts
    @OneToMany
    private List<CartItem> items;  // enthaltene Items
    private double totalPrice;     // berechneter Gesamtpreis

    public ShoppingCart(Long userId) {
        this.userId = userId;
        this.items = new ArrayList<>();
        this.totalPrice = 0;
    }

    public ShoppingCart() {
        this.items = new ArrayList<>();
    }


    // Getter & Setter
    public Long getShoppingCartId() { return shoppingCartId; }
    public void setShoppingCartId(Long shoppingCartId) { this.shoppingCartId = shoppingCartId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

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
