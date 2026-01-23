package adapters.usecase;

import application.port.in.DishAPI;
import application.port.in.DishCreationCommand;
import application.port.out.DishRepository;
import application.port.out.FoodItemRepository;
import application.service.DishService;
import domain.entity.Dish;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class DishUseCase implements DishAPI {

    private final DishService service;

    @Inject
    public DishUseCase(DishRepository dishRepository, FoodItemRepository foodItemRepository) {
        this.service = new DishService(dishRepository, foodItemRepository);
    }

    @Override
    @Transactional
    public Dish create(DishCreationCommand command) {
        return service.create(command);
    }

    @Override
    public List<Dish> findAll() {
        return service.findAll();
    }

    @Override
    public Dish findById(Long id) {
        return service.findById(id);
    }

    @Override
    @Transactional
    public Dish update(Long id, DishCreationCommand command) {
        return service.update(id, command);
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        return service.delete(id);
    }

    @Override
    @Transactional
    public Dish addIngredient(Long dishId, Long foodItemId, double weight) {
        return service.addIngredient(dishId, foodItemId, weight);
    }

    @Override
    @Transactional
    public Dish updateIngredientWeight(Long dishId, Long foodItemId, double weight) {
        return service.updateIngredientWeight(dishId, foodItemId, weight);
    }

    @Override
    @Transactional
    public Dish removeIngredient(Long dishId, Long foodItemId) {
        return service.removeIngredient(dishId, foodItemId);
    }
}
