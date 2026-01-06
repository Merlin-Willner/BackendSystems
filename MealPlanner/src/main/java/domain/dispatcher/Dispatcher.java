package domain.dispatcher;


import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class Dispatcher {

    private DispatcherState currentState;


    public Dispatcher() {
        // Startzustand
        this.currentState = DispatcherState.MEALPLANNER;
    }

    public DispatcherState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(DispatcherState state) {
        this.currentState = state;
    }

    /*Prüft welche Aktionen im aktuellen State erlaubt sind*/
    public List<AllowedAction> getAllowedActions() {
        List<AllowedAction> actions = new ArrayList<>();

        switch (currentState) {
            case MEALPLANNER:
                actions.add(AllowedAction.MEALPLANNER_ENTRY);
                actions.add(AllowedAction.GET_ALL_FOOD);
                actions.add(AllowedAction.CREATE_FOOD);
                actions.add(AllowedAction.SEARCH_FOOD);
                addNonFoodActions(actions);
                break;

            case FOOD_BASE:
                actions.add(AllowedAction.GET_ALL_FOOD);
                actions.add(AllowedAction.GET_SINGLE_FOOD);
                actions.add(AllowedAction.CREATE_FOOD);
                actions.add(AllowedAction.SEARCH_FOOD);
                addNonFoodActions(actions);
                break;

            case FOOD_SINGLE_SELECTED:
                actions.add(AllowedAction.GET_SINGLE_FOOD);
                actions.add(AllowedAction.GET_ALL_FOOD);
                actions.add(AllowedAction.SEARCH_FOOD);
                addNonFoodActions(actions);
                break;

            case FOOD_SEARCH_ACTIVE:
                actions.add(AllowedAction.GET_ALL_FOOD);
                actions.add(AllowedAction.GET_SINGLE_FOOD);
                actions.add(AllowedAction.SEARCH_FOOD);
                // CREATE_FOOD ist hier nicht erlaubt
                addNonFoodActions(actions);
                break;
        }

        return actions;
    }

    /* Metode um zu prüfen ob eine bestimmte Aktion erlaubt ist*/
    public boolean isActionAllowed(AllowedAction action) {
        return getAllowedActions().contains(action);
    }

    private void addNonFoodActions(List<AllowedAction> actions) {
        actions.add(AllowedAction.DISH_CREATE);
        actions.add(AllowedAction.DISH_GET_ALL);
        actions.add(AllowedAction.DISH_GET_SINGLE);
        actions.add(AllowedAction.DISH_INGREDIENT_ADD);
        actions.add(AllowedAction.DISH_INGREDIENT_UPDATE);
        actions.add(AllowedAction.DISH_INGREDIENT_REMOVE);
        actions.add(AllowedAction.USER_CREATE);
        actions.add(AllowedAction.USER_GET_ALL);
        actions.add(AllowedAction.USER_GET_SINGLE);
        actions.add(AllowedAction.USER_GET_BY_USERNAME);
        actions.add(AllowedAction.USER_GET_BY_EMAIL);
        actions.add(AllowedAction.USER_UPDATE);
        actions.add(AllowedAction.CART_CREATE);
        actions.add(AllowedAction.CART_GET);
        actions.add(AllowedAction.CART_ADD_DISH);
        actions.add(AllowedAction.CART_ADD_DISH_BY_USER);
        actions.add(AllowedAction.CART_SUMMARY);
    }
}
