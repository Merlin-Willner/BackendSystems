package domain.service;

import application.port.in.FoodItemAPI;
import application.port.out.FoodItemRepository;
import domain.entity.FoodItem;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class FoodItemService implements FoodItemAPI {

    private final FoodItemRepository foodItemRepository;

    @Inject
    public FoodItemService(FoodItemRepository foodItemRepository) {
        this.foodItemRepository = foodItemRepository;
    }

    @Override
    @Transactional
    public FoodItem create(FoodItem foodItem) {
        return foodItemRepository.save(foodItem);
    }

    @Override
    public java.util.List<FoodItem> findAll() {
        return foodItemRepository.findAll();
    }

    //
    @Override
    public boolean existsByName(String name) { return foodItemRepository.findByName(name).isPresent(); }
}
