package domain.service;

import application.port.in.FoodItemAPI;
import application.port.out.FoodItemRepository;
import domain.entity.FoodItem;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

    @Override
    public FoodItem findById(Long id) {
        return foodItemRepository.findById(id).orElse(null);
    }

    //
    @Override
    public boolean existsByName(String name) { return foodItemRepository.findByName(name).isPresent(); }

    @Override
    public List<FoodItem> filterAndRank(Double minProtein,
                                        Double maxProtein,
                                        Double minCalories,
                                        Double maxCalories,
                                        Double minFat,
                                        Double maxFat,
                                        String sortBy){

        List<FoodItem> all = foodItemRepository.findAll();

        List<FoodItem> filtered = all.stream()
                .filter(f -> minProtein == null || f.getProteinPer100g() >= minProtein)
                .filter(f -> maxProtein == null || f.getProteinPer100g() <= maxProtein)
                .filter(f -> minCalories == null || f.getCaloriesPer100g() >= minCalories)
                .filter(f -> maxCalories == null || f.getCaloriesPer100g() <= maxCalories)
                .filter(f -> minFat == null || f.getFatPer100g() >= minFat)
                .filter(f -> maxFat == null || f.getFatPer100g() <= maxFat)
                .collect(Collectors.toList());

        if (sortBy != null) {
            SortBy sortEnum = SortBy.fromString(sortBy);

            Comparator<FoodItem> comparator = switch (sortEnum) {
                case TOTAL_PRICE -> Comparator.comparing(FoodItem::getPackPrice);
                case PROTEIN -> Comparator.comparing(FoodItem::getProteinPer100g).reversed();
                case CARBS -> Comparator.comparing(FoodItem::getCarbsPer100g).reversed();
                case FAT -> Comparator.comparing(FoodItem::getFatPer100g).reversed();
                case CALORIES -> Comparator.comparing(FoodItem::getCaloriesPer100g).reversed();
            };

            filtered = filtered.stream().sorted(comparator).toList();
        }

        return filtered;
    }
}

