package secondTd;

public class Ingredient {
  private int id;
  private String name;
  private double price;
  private CategoryEnum category;
  private Dish dish;

  public Ingredient(int id, String name, double price, CategoryEnum category, Dish dish) {
    this.id = id;
    this.name = name;
    this.price = price;
    this.category = category;
    this.dish = dish;
  }

  public Ingredient() {}

  public String getDishName() {
    return dish.getName();
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

  public double getPrice() {
    return price;
  }

  public void setPrice(double price) {
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
}
