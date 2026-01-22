package domain.entity;


import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "shoppingcart") // anpassen genauer Name
public class ShoppingCart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long shoppingCartId;

    @Version
    private Long version;

    /*  Aktuell keine JPA Relation, wir müssten dafür den kompletten User speichern, also:
        @OneToOne
        @JoinColumn(name = "USERID", unique = true, nullable = false)
        private User user;
        Je nachdem wie Braun es will, vielleicht mal abklären
        Müsste dann auch in der Klasse User angepasst werden
     */
    @Column(name = "userid", unique = true, nullable = false)
    private Long userId;

    @OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "shoppingcartid")
    private List<CartItem> items;
    private double totalPrice;

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
    public void setShoppingCartId(Long shoppingCartId) { this.shoppingCartId = shoppingCartId; } // darf existieren aber nicht benutzt werden wegen Line 14

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public List<CartItem> getItems() { return items; }

    public double getTotalPrice() { return totalPrice; }


    public void addItem(CartItem item) {
        if(item == null) throw new IllegalArgumentException("Item darf nicht null sein");
        for (CartItem existing : items) {
            if (existing.getFoodItemId() != null && existing.getFoodItemId().equals(item.getFoodItemId())) {
                double unitPrice = existing.getQuantity() > 0 ? existing.getTotalPrice() / existing.getQuantity() : 0;
                if (unitPrice <= 0 && item.getQuantity() > 0) {
                    unitPrice = item.getTotalPrice() / item.getQuantity();
                }
                existing.setQuantity(existing.getQuantity() + item.getQuantity());
                existing.setTotalPrice(existing.getQuantity() * unitPrice);
                recalculateTotal();
                return;
            }
        }
        item.setShoppingCart(this);
        items.add(item);
        recalculateTotal();
    }


    public void removeItem(CartItem item) {
        items.remove(item);
        recalculateTotal();
    }

    public void clearItems() {
        items.clear();
        recalculateTotal();
    }

    public boolean removeItemByFoodItemId(Long foodItemId) {
        if (foodItemId == null) {
            return false;
        }
        for (int i = 0; i < items.size(); i++) {
            CartItem item = items.get(i);
            if (foodItemId.equals(item.getFoodItemId())) {
                items.remove(i);
                recalculateTotal();
                return true;
            }
        }
        return false;
    }

    public boolean updateItemQuantity(Long foodItemId, int quantity) {
        if (foodItemId == null) {
            return false;
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Menge muss größer als 0 sein");
        }
        for (CartItem item : items) {
            if (foodItemId.equals(item.getFoodItemId())) {
                double unitPrice = item.getQuantity() > 0 ? item.getTotalPrice() / item.getQuantity() : 0;
                item.setQuantity(quantity);
                item.setTotalPrice(quantity * unitPrice);
                recalculateTotal();
                return true;
            }
        }
        return false;
    }


    private void recalculateTotal() {
        totalPrice = 0;
        for(CartItem item : items) {
            totalPrice += item.getTotalPrice();
        }
    }
}
