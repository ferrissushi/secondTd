package secondTd.model;

import java.util.List;

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

  public Dish() {}

  public Double getDishPrice() {
    if (ingredients == null) {
      throw new RuntimeException("dish cannot be null");
    }
    return ingredients.stream().mapToDouble((e) -> e.getPrice()).sum();
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

  public DishTypeEnum getDishType() {
    return dishType;
  }

  public void setDishType(DishTypeEnum dishType) {
    this.dishType = dishType;
  }

  public List<Ingredient> getIngredients() {
    return ingredients;
  }

  public void setIngredients(List<Ingredient> ingredients) {
    this.ingredients = ingredients;
  }

  @Override
  public String toString() {
    return "Dish{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", dishType=" + dishType +
            ", ingredients=" + ingredients.toString() +
            '}';
  }
}
