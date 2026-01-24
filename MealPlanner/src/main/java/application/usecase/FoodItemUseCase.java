package application.usecase;

import application.port.in.FoodItemAPI;
import application.port.out.FoodItemRepository;
import application.service.FoodItemService;
import domain.entity.FoodItem;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class FoodItemUseCase implements FoodItemAPI {

    private final FoodItemService service;

    @Inject
    public FoodItemUseCase(FoodItemRepository foodItemRepository) {
        this.service = new FoodItemService(foodItemRepository);
    }

    @Override
    @Transactional
    public FoodItem create(FoodItem foodItem) {
        return service.create(foodItem);
    }

    @Override
    public List<FoodItem> findAll() {
        return service.findAll();
    }

    @Override
    public FoodItem findById(Long id) {
        return service.findById(id);
    }

    @Override
    @Transactional
    public FoodItem update(Long id, FoodItem foodItem) {
        return service.update(id, foodItem);
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        return service.delete(id);
    }

    @Override
    public boolean existsByName(String name) {
        return service.existsByName(name);
    }

    @Override
    public List<FoodItem> filterAndRank(Double minProtein,
                                        Double maxProtein,
                                        Double minCalories,
                                        Double maxCalories,
                                        Double minFat,
                                        Double maxFat,
                                        String sortBy) {
        return service.filterAndRank(minProtein, maxProtein, minCalories, maxCalories, minFat, maxFat, sortBy);
    }
}
