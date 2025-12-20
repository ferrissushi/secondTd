package secondTd.model;

import java.util.List;
import java.util.Objects;

public class Dish {
    private int id;
    private String name;
    private DishTypeEnum dishType;
    private List<Ingredient> ingredients;

    public Dish(int id, String name, DishTypeEnum dishType, List<Ingredient> ingredients) {
        this.id = id;
        this.name = name;
        this.dishType = dishType;
        this.ingredients = ingredients;
    }

    public Dish() {
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Dish dish = (Dish) o;
        return getId() == dish.getId() && Objects.equals(getName(),
                dish.getName()) && getDishType() == dish.getDishType() &&
                Objects.equals(getIngredients(), dish.getIngredients());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getDishType(), getIngredients());
    }

    @Override
    public String toString() {
        return "Dish{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", dishType=" + dishType +
                ", ingredients=" + ingredients +
                '}';
    }

    public Double getDishPrice(){
        return ingredients.stream().mapToDouble(Ingredient::getPrice).sum();
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

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        if (ingredients == null) {
            throw new IllegalArgumentException("Ingredient list cannot be null");
        }
        for (Ingredient ingredient: ingredients) {
            ingredient.setDish(this);
        }
        this.ingredients = ingredients;
    }

    public DishTypeEnum getDishType() {
        return dishType;
    }

    public void setDishType(DishTypeEnum dishType) {
        this.dishType = dishType;
    }

    public enum DishTypeEnum {
        START, MAIN, DESSERT
    }
}
