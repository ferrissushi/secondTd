package secondTd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {

  private DBConnection dbConnection;

  public DataRetriever() {
    this.dbConnection = new DBConnection();
  }

  public Dish findDishById(Integer id) {
    Dish dish = null;
    List<Ingredient> ingredients = new ArrayList<>();
    String sql =
        """
          select d.id, d.name, d.dish_type, i.id ingredient_id, i.name ingredient_name,
          i.price ingredient_price, i.category ingredient_category from dish d
          left join ingredient i on d.id = i.id_dish where d.id = ? order by d.id asc;
        """;
    try (Connection connection = dbConnection.getConnection();
        PreparedStatement ps = connection.prepareStatement(sql); ) {
      ps.setInt(1, id);
      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        dish = mapToDish(rs);
        Ingredient ingredient = mapToIngredient(rs);
        ingredient.setDish(dish);
        ingredients.add(ingredient);
      }

      dish.setIngredients(ingredients);
      return dish;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public Ingredient mapToIngredient(ResultSet rs) throws SQLException {
    Ingredient ingredient = new Ingredient();
    ingredient.setId(rs.getInt("ingredient_id"));
    ingredient.setName(rs.getString("ingredient_name"));
    ingredient.setPrice(rs.getDouble("ingredient_price"));
    ingredient.setCategory(CategoryEnum.valueOf(rs.getString("ingredient_category")));
    ingredient.setDish(null);
    return ingredient;
  }

  public Dish mapToDish(ResultSet rs) throws SQLException {
    Dish dish = new Dish();
    dish.setId(rs.getInt("id"));
    dish.setName(rs.getString("name"));
    dish.setDishType(DishTypeEnum.valueOf(rs.getString("dish_type")));
    dish.setIngredients(null);
    return dish;
  }

  public List<Ingredient> findIngredients(int page, int size) {
    if (page < 1 || size < 1) {
      throw new IllegalArgumentException("Page and size must be greater than 0");
    }
    int offset = (page - 1) * size;
    List<Ingredient> ingredients = new ArrayList<>();
    String sql = """
      select i.id ingredient_id, i.name ingredient_name, i.price ingredient_price, i.category ingredient_category, d.id, d.name, d.dish_type
      from ingredient i left join dish d on i.id_dish = d.id
      order by i.id asc limit ? offset ?;
    """;

    try (Connection connection = dbConnection.getConnection();
    PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setInt(1, size);
      ps.setInt(2, offset);
      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        Dish dish = mapToDish(rs);
        Ingredient ingredient = mapToIngredient(rs);
        ingredient.setDish(dish);
        ingredients.add(ingredient);
      }
      return ingredients;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
