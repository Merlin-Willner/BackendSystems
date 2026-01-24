package domain.entity;

public class CartItem {
    private Long cartItemId;

    private ShoppingCart shoppingCart;
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
