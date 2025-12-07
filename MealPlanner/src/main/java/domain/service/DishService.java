package domain.service;

import application.port.in.DishAPI;
import application.port.in.DishCreationCommand;
import application.port.out.DishRepository;
import application.port.out.FoodItemRepository;
import domain.entity.Dish;
import domain.entity.FoodItem;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class DishService implements DishAPI {

    private final DishRepository dishRepository;
    private final FoodItemRepository foodItemRepository;

    @Inject
    public DishService(DishRepository dishRepository, FoodItemRepository foodItemRepository) {
        this.dishRepository = dishRepository;
        this.foodItemRepository = foodItemRepository;
    }

    @Override
    @Transactional
    public Dish create(DishCreationCommand command) {
        if (command == null) throw new IllegalArgumentException("Anfrage darf nicht null sein");
        if (command.name() == null || command.name().isBlank()) {
            throw new IllegalArgumentException("Name darf nicht leer sein");
        }
        if (command.servingWeight() <= 0) {
            throw new IllegalArgumentException("servingWeight muss größer als 0 sein");
        }
        if (command.ingredients() == null || command.ingredients().isEmpty()) {
            throw new IllegalArgumentException("Mindestens eine Zutat ist erforderlich");
        }

        Dish dish = new Dish(
                command.userId(),
                command.name(),
                command.category(),
                command.servingWeight(),
                command.preparationTime(),
                command.imageUrl()
        );

        Set<Long> seenFoodItemIds = new HashSet<>();
        for (DishCreationCommand.IngredientCommand ingredient : command.ingredients()) {
            Long foodItemId = ingredient.foodItemId();
            if (foodItemId == null) {
                throw new IllegalArgumentException("foodItemId darf nicht null sein");
            }
            if (!seenFoodItemIds.add(foodItemId)) {
                throw new IllegalArgumentException("foodItemId " + foodItemId + " ist doppelt; Gewichte bitte zusammenfassen");
            }

            FoodItem foodItem = foodItemRepository.findById(foodItemId)
                    .orElseThrow(() -> new NotFoundException("FoodItem mit ID " + foodItemId + " nicht gefunden"));
            dish.addIngredient(foodItem, ingredient.weight());
        }

        return dishRepository.save(dish);
    }

    @Override
    public List<Dish> findAll() {
        return dishRepository.findAll();
    }

    @Override
    public Dish findById(Long id) {
        return dishRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Dish mit ID " + id + " nicht gefunden"));
    }
}
