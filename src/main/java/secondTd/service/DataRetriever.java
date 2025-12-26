package secondTd.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import secondTd.db.DBConnection;
import secondTd.model.Dish;
import secondTd.model.Ingredient;

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
        Dish dish;
        try {
            Connection connection = dbConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                dish = mapToDish(rs, ingredients);
            } else {
                throw new RuntimeException("Dish with id " + id + " not found");
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
            while (rs.next()) {
                Ingredient ingredient = mapToIngredient(rs);
                ingredients.add(ingredient);
            }
            dbConnection.closeConnection(connection);
            return ingredients;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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
            while (rs.next()) {
                Ingredient ingredient = mapToIngredient(rs);
                ingredients.add(ingredient);
            }
            dbConnection.closeConnection(connection);
            return ingredients;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public List<Ingredient> createIngredients(List<Ingredient> newIngredients) {
        String sql = """
                insert into ingredient (id, name, price, category, id_dish)
                values (?, ?, ?, ?::ingredient_category, ?) returning id, name, price, category, id_dish;
                """;
        Connection connection;
        try {
            connection = dbConnection.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        List<Ingredient> newIngredientsInserted = new ArrayList<>();
        try {
            connection.setAutoCommit(false);
            for (Ingredient newIngredient : newIngredients) {
                Ingredient fetchedIngredient = findIngredientById(newIngredient.getId());
                if (fetchedIngredient != null) {
                    throw new RuntimeException("Ingredient is already in the database");
                }
                PreparedStatement ps = connection.prepareStatement(sql);
                ps.setInt(1, newIngredient.getId());
                ps.setString(2, newIngredient.getName());
                ps.setDouble(3, newIngredient.getPrice());
                ps.setString(4, newIngredient.getCategory().toString());
                if (newIngredient.getDish() != null) {
                    ps.setInt(5, newIngredient.getDish().getId());
                }
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    Ingredient newIngredientInserted = mapToIngredient(rs);
                    newIngredientsInserted.add(newIngredientInserted);
                }
            }
            connection.commit();
            return newIngredientsInserted;
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException e1) {
                throw new RuntimeException(e1);
            }
            throw new RuntimeException(e);
        } finally {
            try {
                dbConnection.closeConnection(connection);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
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
                 values (?, ?, ?::dish_types) on conflict (id) do
                 update set name = excluded.name, dish_type = excluded.dish_type::dish_types
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
        for (Ingredient ingredient : ingredients) {
            Ingredient fetchedIngredient = findIngredientById(ingredient.getId());
            if (fetchedIngredient == null) {
                listOfNotFoundIngredient.add(ingredient);
            }
        }
        createIngredients(listOfNotFoundIngredient);
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

    private FindIngredientByNameResult findIngredientByName(String ingredientName) {
        String sql = """
                select id, name, price, category, id_dish from ingredient where name ilike ?;
                """;
        List<Ingredient> ingredients = new ArrayList<>();
        List<Integer> dishIds = new ArrayList<>();
        try {
            Connection connection = dbConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, "%" + ingredientName + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Ingredient ingredient = mapToIngredient(rs);
                ingredients.add(ingredient);
                Integer dishId = rs.getInt("id_dish");
                dishIds.add(dishId);
            }
            dbConnection.closeConnection(connection);
            return new FindIngredientByNameResult(ingredients, dishIds);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteDish(int i) {
        String sql = """
                delete from dish where id = ?;
                """;
        try {
            Connection connection = dbConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, i);
            ps.executeUpdate();
            dbConnection.closeConnection(connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private record FindIngredientByNameResult(List<Ingredient> ingredients, List<Integer> dishIds) {
    }

    public List<Dish> findDishByIngredientName(String ingredientName) {
        String sql = """
                select id, name, dish_type from dish where id = ?;
                """;
        List<Dish> dishes = new ArrayList<>();
        FindIngredientByNameResult ingredientsAndDishId = findIngredientByName(ingredientName);
        System.out.println(ingredientsAndDishId.ingredients());
        for (int i = 0; i < ingredientsAndDishId.ingredients().size(); i++) {
            try {
                Connection connection = dbConnection.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql);
                ps.setInt(1, ingredientsAndDishId.dishIds().get(i));
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    Dish dish = mapToDish(rs, List.of(ingredientsAndDishId.ingredients().get(i)));
                    dishes.add(dish);
                }
                dbConnection.closeConnection(connection);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return dishes;
    }

    public List<Ingredient> findIngredientByCriteria(
            String ingredientName,
            Ingredient.CategoryEnum category,
            String dishName,
            int page,
            int size) {
        String sql = buildSql(ingredientName, category, dishName, page, size);
        try {
            Connection connection = dbConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
            int paramIndex = 1;
            if (ingredientName != null) {
                ps.setString(paramIndex++, "%" + ingredientName + "%");
            }
            if (category != null) {
                ps.setString(paramIndex++, category.toString());
            }
            if (dishName != null) {
                ps.setString(paramIndex++, "%" + dishName + "%");
            }
            ResultSet rs = ps.executeQuery();
            List<Ingredient> ingredients = new ArrayList<>();
            while (rs.next()) {
                Ingredient ingredient = mapToIngredient(rs);
                ingredients.add(ingredient);
            }
            dbConnection.closeConnection(connection);
            return ingredients;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String buildSql(
            String ingredientName,
            Ingredient.CategoryEnum category,
            String dishName,
            int page,
            int size) {
        StringBuilder sql = new StringBuilder(
                """
                        select i.id id, i.price price, i.name name, i.category category, i.id_dish id_dish
                        from dish d right join ingredient i on d.id = i.id_dish
                        """);
        List<String> conditionClauses = new ArrayList<>();
        String orderByClauses = null;

        if (ingredientName != null) {
            conditionClauses.add("i.name ilike ?");
        }
        if (category != null) {
            conditionClauses.add("category = ?::ingredient_category");
        }
        if (dishName != null) {
            conditionClauses.add("d.name ilike ?");
        }

        if (page > 0 && size > 0) {
            int offset = size * (page - 1);
            orderByClauses = "order by i.id limit " + size + " offset " + offset;
        }

        if (!conditionClauses.isEmpty()) {
            sql.append(" where ");
            sql.append(String.join(" and ", conditionClauses));
        }

        if (orderByClauses != null) {
            sql.append(" ").append(orderByClauses);
        }

        return sql.toString();
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
}
