package secondTd.model;

public class DishIngredient {
    private Integer id;
    private Dish dish;
    private Ingredient ingredient;
    private Double requiredQuantity;
    private UnitType unit;

    @Override
    public String toString() {
        return "DishIngredient{" +
                "id=" + id +
                ", ingredient=" + ingredient +
                ", requiredQuantity=" + requiredQuantity +
                ", unit=" + unit +
                '}';
    }

    public enum UnitType {
        PCS, KG, L
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Dish getDish() {
        return dish;
    }

    public void setDish(Dish dish) {
        this.dish = dish;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public void setIngredient(Ingredient ingredient) {
        this.ingredient = ingredient;
    }

    public Double getQuantityRequired() {
        return requiredQuantity;
    }

    public void setQuantityRequired(Double requiredQuantity) {
        this.requiredQuantity = requiredQuantity;
    }

    public UnitType getUnit() {
        return unit;
    }

    public void setUnit(UnitType unit) {
        this.unit = unit;
    }

    public Double getTotalPrice() {
        return requiredQuantity* ingredient.getPrice();
    }
}
