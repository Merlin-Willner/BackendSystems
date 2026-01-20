package domain.service;

import application.port.in.FoodItemAPI;
import application.port.out.FoodItemRepository;
import domain.entity.FoodItem;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;

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

    @Override
    @Transactional
    public FoodItem update(Long id, FoodItem foodItem) {
        FoodItem existing = foodItemRepository.findById(id).orElse(null);
        if (existing == null) {
            return null;
        }

        if (foodItem.getName() != null && !foodItem.getName().equals(existing.getName())) {
            if (foodItemRepository.findByName(foodItem.getName()).isPresent()) {
                throw new WebApplicationException("Ein FoodItem mit diesem Namen existiert bereits.", 409);
            }
        }

        existing.setName(foodItem.getName());
        existing.setBrand(foodItem.getBrand());
        existing.setPackSize(foodItem.getPackSize());
        existing.setPackPrice(foodItem.getPackPrice());
        existing.setProteinPer100g(foodItem.getProteinPer100g());
        existing.setCarbsPer100g(foodItem.getCarbsPer100g());
        existing.setFatPer100g(foodItem.getFatPer100g());
        existing.setCaloriesPer100g(foodItem.getCaloriesPer100g());

        return foodItemRepository.save(existing);
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        FoodItem existing = foodItemRepository.findById(id).orElse(null);
        if (existing == null) {
            return false;
        }
        foodItemRepository.delete(existing);
        return true;
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
                case PRICE_PER_PROTEIN -> Comparator.comparing(FoodItem::getPricePer100gProtein);
                case PRICE_PER_1000_CALORIES -> Comparator.comparing(FoodItem::getPricePer1000Calories);
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
