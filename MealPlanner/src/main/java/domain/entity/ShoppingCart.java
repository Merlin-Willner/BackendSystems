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
        item.setShoppingCart(this);
        items.add(item);
        recalculateTotal();
    }


    public void removeItem(CartItem item) {
        items.remove(item);
        recalculateTotal();
    }


    private void recalculateTotal() {
        totalPrice = 0;
        for(CartItem item : items) {
            totalPrice += item.getTotalPrice();
        }
    }
}
