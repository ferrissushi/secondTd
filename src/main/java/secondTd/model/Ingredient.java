package secondTd.model;

import java.util.Objects;

public class Ingredient {
    public enum CategoryEnum {
        VEGETABLE, ANIMAL, MARINE, DAIRY, OTHER
    }
    public enum UnitType {
        PCS, KG, L
    }
    private int id;
    private String name;
    private Double price;
    private CategoryEnum category;
    private Dish dish;
    private UnitType unit;

    private Double quantityRequired;

    public Ingredient(Integer id,
        String name,
        Double price,
        CategoryEnum category,
        Dish dish) {

        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.dish = dish;
    }

    public Ingredient(Integer id,
        String name,
        Double price,
        CategoryEnum category,
        Dish dish,
        Double quantityRequired,
        UnitType unit) {

        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.dish = dish;
        this.quantityRequired = quantityRequired;
        this.unit = unit;
    }

    public Ingredient() {
    }

    public UnitType getUnit() {
        return unit;
    }

    public void setUnit(UnitType unit) {
        this.unit = unit;
    }

    public Double getQuantityRequired() {
        return quantityRequired;
    }

    public void setQuantityRequired(Double quantityRequired) {
        this.quantityRequired = quantityRequired;
    }

    public String getDishName() {
        if (this.dish == null) {
            throw new NullPointerException("Dish name cannot be null");
        }
        return this.dish.getName();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public Double getTotalPrice() {
        return quantityRequired * price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public CategoryEnum getCategory() {
        return category;
    }

    public void setCategory(CategoryEnum category) {
        this.category = category;
    }

    public Dish getDish() {
        return dish;
    }

    public void setDish(Dish dish) {
        this.dish = dish;
    }

    @Override
    public String toString() {
        return "Ingredient{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", category=" + category +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Ingredient that = (Ingredient) o;
        return getId() == that.getId() && Objects.equals(getName(), that.getName())
                && Objects.equals(getPrice(), that.getPrice()) && getCategory() == that.getCategory()
                && Objects.equals(getDish(), that.getDish());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getPrice(), getCategory(), getDish());
    }
}
