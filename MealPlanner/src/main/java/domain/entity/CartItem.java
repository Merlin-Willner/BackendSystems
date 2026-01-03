package domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cartItemId;

    @ManyToOne
    @JoinColumn(name = "shoppingcartid") // stimmt mit ShoppingCart @JoinColumn überein
    @JsonIgnore
    private ShoppingCart shoppingCart; /*    Kann laut Chat rausgenommen werden (Du brauchst kein shoppingCartId mehr,
                                        wenn du in ShoppingCart @JoinColumn(name = "shopping_cart_id")
                                        verwendest – dann verwaltet JPA die FK-Spalte.) */
    private Long foodItemId;
    private int quantity;
    private double totalPrice;

    public CartItem(Long foodItemId, int quantity, double totalPrice) {
        this.foodItemId = foodItemId;
        setQuantity(quantity);
        setTotalPrice(totalPrice);
    }

    public CartItem() {

    }


    public Long getCartItemId() { return cartItemId; }
    public void setCartItemId(Long cartItemId) { this.cartItemId = cartItemId; }

    public ShoppingCart getShoppingCart() { return shoppingCart; }
    public void setShoppingCart(ShoppingCart shoppingCart) { this.shoppingCart = shoppingCart; }

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
