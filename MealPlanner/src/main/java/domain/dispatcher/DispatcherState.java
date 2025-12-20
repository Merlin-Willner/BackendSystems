package domain.dispatcher;

public enum DispatcherState {
    FOOD_BASE,           // Grundzustand alle FoodItems vorrüber gehend
    FOOD_SINGLE_SELECTED, // Ein einzelnes FoodItem
    FOOD_SEARCH_ACTIVE      // Filter/Search aktiv
}