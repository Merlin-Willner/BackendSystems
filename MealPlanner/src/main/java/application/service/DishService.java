package application.service;

import application.exception.NotFoundException;
import application.port.in.DishAPI;
import application.port.in.DishCreationCommand;
import application.port.out.DishRepository;
import application.port.out.FoodItemRepository;
import domain.entity.Dish;
import domain.entity.FoodItem;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DishService implements DishAPI {

    private final DishRepository dishRepository;
    private final FoodItemRepository foodItemRepository;

    public DishService(DishRepository dishRepository, FoodItemRepository foodItemRepository) {
        this.dishRepository = dishRepository;
        this.foodItemRepository = foodItemRepository;
    }

    @Override
    public Dish create(DishCreationCommand command) {
        if (command == null) throw new IllegalArgumentException("Anfrage darf nicht null sein");
        if (command.name() == null || command.name().isBlank()) {
            throw new IllegalArgumentException("Name darf nicht leer sein");
        }
        if (command.userId() == null) {
            throw new IllegalArgumentException("userId darf nicht null sein");
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
            if (ingredient.weight() <= 0) {
                throw new IllegalArgumentException("Gewicht muss größer als 0 sein für foodItemId " + foodItemId);
            }
            if (!seenFoodItemIds.add(foodItemId)) {
                throw new IllegalArgumentException("foodItemId " + foodItemId + " ist doppelt und daher nicht erlaubt");
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

    @Override
    public Dish update(Long id, DishCreationCommand command) {
        if (command == null) throw new IllegalArgumentException("Anfrage darf nicht null sein");
        if (command.name() == null || command.name().isBlank()) {
            throw new IllegalArgumentException("Name darf nicht leer sein");
        }
        if (command.userId() == null) {
            throw new IllegalArgumentException("userId darf nicht null sein");
        }
        if (command.servingWeight() <= 0) {
            throw new IllegalArgumentException("servingWeight muss größer als 0 sein");
        }
        if (command.ingredients() == null || command.ingredients().isEmpty()) {
            throw new IllegalArgumentException("Mindestens eine Zutat ist erforderlich");
        }

        Dish dish = dishRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Dish mit ID " + id + " nicht gefunden"));

        dish.setUserId(command.userId());
        dish.setName(command.name());
        dish.setCategory(command.category());
        dish.setServingWeight(command.servingWeight());
        dish.setPreparationTime(command.preparationTime());
        dish.setImageUrl(command.imageUrl());

        dish.getIngredients().clear();
        Set<Long> seenFoodItemIds = new HashSet<>();
        for (DishCreationCommand.IngredientCommand ingredient : command.ingredients()) {
            Long foodItemId = ingredient.foodItemId();
            if (foodItemId == null) {
                throw new IllegalArgumentException("foodItemId darf nicht null sein");
            }
            if (ingredient.weight() <= 0) {
                throw new IllegalArgumentException("Gewicht muss größer als 0 sein für foodItemId " + foodItemId);
            }
            if (!seenFoodItemIds.add(foodItemId)) {
                throw new IllegalArgumentException("foodItemId " + foodItemId + " ist doppelt und daher nicht erlaubt");
            }
            FoodItem foodItem = foodItemRepository.findById(foodItemId)
                    .orElseThrow(() -> new NotFoundException("FoodItem mit ID " + foodItemId + " nicht gefunden"));
            dish.addIngredient(foodItem, ingredient.weight());
        }

        return dishRepository.save(dish);
    }

    @Override
    public boolean delete(Long id) {
        Dish dish = dishRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Dish mit ID " + id + " nicht gefunden"));
        dishRepository.delete(dish);
        return true;
    }

    @Override
    public Dish addIngredient(Long dishId, Long foodItemId, double weight) {
        if (dishId == null) throw new IllegalArgumentException("dishId darf nicht null sein");
        if (foodItemId == null) throw new IllegalArgumentException("foodItemId darf nicht null sein");
        if (weight <= 0) throw new IllegalArgumentException("Gewicht muss größer als 0 sein");

        Dish dish = dishRepository.findById(dishId)
                .orElseThrow(() -> new NotFoundException("Dish mit ID " + dishId + " nicht gefunden"));
        FoodItem foodItem = foodItemRepository.findById(foodItemId)
                .orElseThrow(() -> new NotFoundException("FoodItem mit ID " + foodItemId + " nicht gefunden"));

        dish.addIngredient(foodItem, weight);
        return dishRepository.save(dish);
    }

    @Override
    public Dish updateIngredientWeight(Long dishId, Long foodItemId, double weight) {
        if (dishId == null) throw new IllegalArgumentException("dishId darf nicht null sein");
        if (foodItemId == null) throw new IllegalArgumentException("foodItemId darf nicht null sein");
        if (weight <= 0) throw new IllegalArgumentException("Gewicht muss größer als 0 sein");

        Dish dish = dishRepository.findById(dishId)
                .orElseThrow(() -> new NotFoundException("Dish mit ID " + dishId + " nicht gefunden"));

        dish.updateIngredientWeight(foodItemId, weight);
        return dishRepository.save(dish);
    }

    @Override
    public Dish removeIngredient(Long dishId, Long foodItemId) {
        if (dishId == null) throw new IllegalArgumentException("dishId darf nicht null sein");
        if (foodItemId == null) throw new IllegalArgumentException("foodItemId darf nicht null sein");

        Dish dish = dishRepository.findById(dishId)
                .orElseThrow(() -> new NotFoundException("Dish mit ID " + dishId + " nicht gefunden"));

        if (dish.getIngredients() == null || dish.getIngredients().isEmpty()) {
            throw new IllegalArgumentException("Dish muss mindestens eine Zutat enthalten");
        }

        if (dish.getIngredients().size() == 1) {
            throw new IllegalArgumentException("Dish muss mindestens eine Zutat enthalten");
        }

        dish.removeIngredient(foodItemId);
        return dishRepository.save(dish);
    }
}
