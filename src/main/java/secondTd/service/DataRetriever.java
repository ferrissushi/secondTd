package secondTd.service;

import secondTd.db.DBConnection;
import secondTd.model.Dish;
import secondTd.model.Ingredient;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {
    private DBConnection dbConnection;

    public DataRetriever() {
        dbConnection = new DBConnection();
    }

    public Dish findDishById(Integer id) {
        String sql = """
                select id, name, dish_type from dish where id = ?;
                """;
        return null;
    }

    public List<Ingredient> findIngredientById(Integer id) {
        String sql = """
                select id, name, price, category, id_dish from ingredient where id_dish = ?;
                """;
        List<Ingredient> ingredients = new ArrayList<>();
        try {
            Connection connection = dbConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                Ingredient ingredient = mapToIngredient(rs);
                ingredients.add(ingredient);
            }
            dbConnection.closeConnection(connection);
            return ingredients;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Ingredient mapToIngredient(ResultSet rs) throws SQLException {
        Ingredient ingredient = new Ingredient();
        ingredient.setId(rs.getInt("id"));
        ingredient.setName(rs.getString("name"));
        ingredient.setCategory(Ingredient.CategoryEnum.valueOf(rs.getString("category")));
        return ingredient;
    }

    public Dish mapToDish(ResultSet rs, List<Ingredient> ingredients) throws SQLException {
        Dish dish = new Dish();
        dish.setId(rs.getInt("id"));
        dish.setName(rs.getString("name"));
        dish.setDishType(Dish.DishTypeEnum.valueOf(rs.getString("dish_type")));
        dish.setIngredients(ingredients);
        return dish;
    }
}
