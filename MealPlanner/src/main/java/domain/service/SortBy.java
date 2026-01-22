package domain.service;

public enum SortBy {
    TOTAL_PRICE("totalPrice"),
    PRICE_PER_PROTEIN("pricePerProtein"),
    PRICE_PER_1000_CALORIES("pricePer1000Calories"),
    PROTEIN("protein"),
    CARBS("carbs"),
    FAT("fat"),
    CALORIES("calories");

    private final String value;

    SortBy(String value) {
        this.value = value;
    }

    public static SortBy fromString(String text) {
        for (SortBy sb : values()) {
            if (sb.value.equalsIgnoreCase(text)) {
                return sb;
            }
        }
        throw new IllegalArgumentException("Ungültiger sortBy-Wert: " + text);
    }
}
