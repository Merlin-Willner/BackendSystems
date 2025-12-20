package domain.dispatcher;

import java.util.ArrayList;
import java.util.List;

public class Dispatcher {

    private DispatcherState currentState;

    public Dispatcher() {
        // Startzustand
        this.currentState = DispatcherState.FOOD_BASE;
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
            case FOOD_BASE:
                actions.add(AllowedAction.GET_ALL_FOOD);
                actions.add(AllowedAction.GET_SINGLE_FOOD);
                actions.add(AllowedAction.CREATE_FOOD);
                actions.add(AllowedAction.SEARCH_FOOD);
                break;

            case FOOD_SINGLE_SELECTED:
                actions.add(AllowedAction.GET_SINGLE_FOOD);
                break;

            case FOOD_SEARCH_ACTIVE:
                actions.add(AllowedAction.GET_ALL_FOOD);
                actions.add(AllowedAction.GET_SINGLE_FOOD);
                actions.add(AllowedAction.SEARCH_FOOD);
                // CREATE_FOOD ist hier nicht erlaubt
                break;
        }

        return actions;
    }

    /* Metode um zu prüfen ob eine bestimmte Aktion erlaubt ist*/
    public boolean isActionAllowed(AllowedAction action) {
        return getAllowedActions().contains(action);
    }
}