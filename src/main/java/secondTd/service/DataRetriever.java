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
        List<Ingredient> ingredients = findIngredientsByDishId(id);
        Dish dish = null;
        try {
            Connection connection = dbConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                dish = mapToDish(rs, ingredients);
            }
            dbConnection.closeConnection(connection);
            return dish;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Ingredient> findIngredientsByDishId(Integer id) {
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
        Dish dish = new Dish();
        ingredient.setId(rs.getInt("id"));
        ingredient.setName(rs.getString("name"));
        ingredient.setPrice(rs.getDouble("price"));
        ingredient.setCategory(Ingredient.CategoryEnum.valueOf(rs.getString("category")));
        dish.setId(rs.getInt("id_dish"));
        ingredient.setDish(dish);
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

    public List<Ingredient> findIngredients(int page, int size) {
        if (page < 1 || size < 1) {
            throw new IllegalArgumentException("Page and size should'nt be negative or 0");
        }
        int offset = size * (page - 1);
        String sql = """
                select id, name, price, category, id_dish from ingredient order by id
                limit ? offset ?;
                """;
        List<Ingredient> ingredients = new ArrayList<>();
        try {
            Connection connection = dbConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, size);
            ps.setInt(2, offset);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                Ingredient ingredient = mapToIngredient(rs);
                ingredients.add(ingredient);
            }
            dbConnection.closeConnection(connection);
            return ingredients;
        } catch(SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Ingredient> createIngredients(List<Ingredient> newIngredients) throws SQLException {
        String sql = """
                insert into ingredient (id, name, price, category, id_dish)
                values (?, ?, ?, ?::ingredient_category, ?) returning id, name, price, category, id_dish;
                """;
        Connection connection = dbConnection.getConnection();
        List<Ingredient> newIngredientsInserted = new ArrayList<>();
        try {
            connection.setAutoCommit(false);
            for (Ingredient newIngredient: newIngredients) {
                Ingredient fethedIngredient = findIngredientById(newIngredient.getId());
                if (fethedIngredient != null) {
                    throw new RuntimeException("Ingredient is already in the database");
                }
                PreparedStatement ps = connection.prepareStatement(sql);
                ps.setInt(1, newIngredient.getId());
                ps.setString(2, newIngredient.getName());
                ps.setDouble(3, newIngredient.getPrice());
                ps.setString(4, newIngredient.getCategory().toString());
                ps.setInt(5, newIngredient.getDish().getId());
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    Ingredient newIngredientInserted = mapToIngredient(rs);
                    newIngredientsInserted.add(newIngredientInserted);
                }
            }
            connection.commit();
            return newIngredientsInserted;
        } catch (SQLException e) {
            connection.rollback();
            throw new RuntimeException(e);
        } finally {
            dbConnection.closeConnection(connection);
        }
    }

    public void deleteIngredient(Integer id) {
        String sql = """
                delete from ingredient where id = ?;
                """;
        try {
            Connection connection = dbConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, id);
            ps.executeUpdate();
            dbConnection.closeConnection(connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Dish saveDish(Dish dishToSave) {
        String sql = """
                insert into dish (id, name, dish_type)
                values (?, ?, ?) on conflict (id) do
                update set name = excluded.name, dish_type = excluded.dish_type
                returning id, name, dish_type;
               """;
        Dish dish = new Dish();
        checkAndInsertIngredients(dishToSave.getIngredients());
        try {
            Connection connection = dbConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, dishToSave.getId());
            ps.setString(2, dishToSave.getName());
            ps.setString(3, dishToSave.getDishType().toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                dish = mapToDish(rs, dishToSave.getIngredients());
            }
            dbConnection.closeConnection(connection);
            return dish;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void checkAndInsertIngredients(List<Ingredient> ingredients) {
        List<Ingredient> listOfNotFoundIngredient = new ArrayList<>();
        for (Ingredient ingredient: ingredients) {
            Ingredient fetchedIngredient = findIngredientById(ingredient.getId());
            if (fetchedIngredient == null) {
               listOfNotFoundIngredient.add(ingredient);
            }
        }
        try {
            createIngredients(listOfNotFoundIngredient);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Ingredient findIngredientById(int id) {
        String sql = """
                select id, name, price, category, id_dish from ingredient where id = ?;
                """;
        Ingredient ingredient = null;
        try {
            Connection connection = dbConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ingredient = mapToIngredient(rs);
            }
            dbConnection.closeConnection(connection);
            return ingredient;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
