package adapters.API;

import domain.entity.DishCategory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record DishRequest(
        @NotBlank String name,
        DishCategory category,
        @Positive(message = "servingWeight muss größer als 0 sein") double servingWeight,
        @Min(0) int preparationTime,
        String imageUrl,
        @NotNull Long userId,
        @NotNull @Size(min = 1) List<@Valid DishIngredientRequest> ingredients
) {
    public record DishIngredientRequest(
            @NotNull Long foodItemId,
            @Positive(message = "Gewicht muss größer als 0 sein") double weight
    ) {}
}
