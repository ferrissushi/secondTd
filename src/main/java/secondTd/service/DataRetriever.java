package secondTd.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
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
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = dbConnection.getConnection();
            ps = connection.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                dish = mapToDish(rs, ingredients);
            } else {
                throw new RuntimeException("Dish with id " + id + " not found");
            }
            return dish;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            dbConnection.closeJDBCRessources(connection, ps, rs);
        }
    }

    public List<Ingredient> findIngredientsByDishId(Integer id) {
        String sql = """
                select id, name, price, category, id_dish from ingredient where id_dish = ?;
                """;
        List<Ingredient> ingredients = new ArrayList<>();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = dbConnection.getConnection();
            ps = connection.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            while (rs.next()) {
                Ingredient ingredient = mapToIngredient(rs);
                ingredients.add(ingredient);
            }
            return ingredients;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            dbConnection.closeJDBCRessources(connection, ps, rs);
        }
    }

    public List<Ingredient> findIngredients(int page, int size) {
        if (page < 1 || size < 1) {
            throw new IllegalArgumentException("Page and size shouldn't be negative or 0");
        }

        int offset = size * (page - 1);
        String sql = """
                select id, name, price, category, id_dish
                from ingredient
                order by id
                limit ? offset ?;
                """;

        List<Ingredient> ingredients = new ArrayList<>();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connection = dbConnection.getConnection();
            ps = connection.prepareStatement(sql);
            ps.setInt(1, size);
            ps.setInt(2, offset);
            rs = ps.executeQuery();

            while (rs.next()) {
                ingredients.add(mapToIngredient(rs));
            }
            return ingredients;

        } catch (SQLException e) {
            throw new RuntimeException(e);

        } finally {
            dbConnection.closeJDBCRessources(rs, ps, connection);
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
                List<Ingredient> fetchedIngredient = findIngredientByName(newIngredient.getName()).ingredients();
                if (!fetchedIngredient.isEmpty()) {
                    throw new RuntimeException("Ingredient is already in the database");
                }
                PreparedStatement ps = connection.prepareStatement(sql);
                ps.setInt(1, newIngredient.getId());
                ps.setString(2, newIngredient.getName());
                ps.setDouble(3, newIngredient.getPrice());
                ps.setString(4, newIngredient.getCategory().toString());
                if (newIngredient.getDish() != null) {
                    ps.setInt(5, newIngredient.getDish().getId());
                } else {
                    ps.setNull(5, Types.INTEGER);
                }
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    Ingredient newIngredientInserted = mapToIngredient(rs);
                    newIngredientsInserted.add(newIngredientInserted);
                }
                dbConnection.closeJDBCRessources(ps, rs);
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
            dbConnection.closeJDBCRessources(connection);
        }
    }

    public void deleteIngredient(Integer id) {
        String sql = "delete from ingredient where id = ?;";
        Connection connection = null;
        PreparedStatement ps = null;

        try {
            connection = dbConnection.getConnection();
            ps = connection.prepareStatement(sql);
            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);

        } finally {
            dbConnection.closeJDBCRessources(ps, connection);
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
            dbConnection.closeJDBCRessources(connection, rs, ps);
            dissociateIngredientsFromDish(dish.getId());
            checkAndInsertIngredients(dishToSave.getIngredients());
            return dish;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void dissociateIngredientsFromDish(int dishId) {
        String sql = """
                update ingredient
                set id_dish = null
                where id_dish = ?;
                """;
        try {
            Connection connection = dbConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, dishId);
            ps.executeUpdate();
            dbConnection.closeJDBCRessources(connection, ps);
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
                select id, name, price, category, id_dish
                from ingredient
                where id = ?;
                """;

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Ingredient ingredient = null;

        try {
            connection = dbConnection.getConnection();
            ps = connection.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();

            if (rs.next()) {
                ingredient = mapToIngredient(rs);
            }
            return ingredient;

        } catch (SQLException e) {
            throw new RuntimeException(e);

        } finally {
            dbConnection.closeJDBCRessources(rs, ps, connection);
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
            dbConnection.closeJDBCRessources(connection, rs, ps);
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
            dbConnection.closeJDBCRessources(connection, ps);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private record FindIngredientByNameResult(List<Ingredient> ingredients, List<Integer> dishIds) {
    }

    public List<Dish> findDishByIngredientName(String ingredientName) {
        String sql = """
                select id, name, dish_type
                from dish
                where id = ?;
                """;

        List<Dish> dishes = new ArrayList<>();
        FindIngredientByNameResult result = findIngredientByName(ingredientName);

        for (int i = 0; i < result.ingredients().size(); i++) {
            Connection connection = null;
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                connection = dbConnection.getConnection();
                ps = connection.prepareStatement(sql);
                ps.setInt(1, result.dishIds().get(i));
                rs = ps.executeQuery();

                if (rs.next()) {
                    dishes.add(mapToDish(rs, List.of(result.ingredients().get(i))));
                }

            } catch (SQLException e) {
                throw new RuntimeException(e);

            } finally {
                dbConnection.closeJDBCRessources(rs, ps, connection);
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
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = dbConnection.getConnection();
            ps = connection.prepareStatement(sql);
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
            rs = ps.executeQuery();
            List<Ingredient> ingredients = new ArrayList<>();
            while (rs.next()) {
                Ingredient ingredient = mapToIngredient(rs);
                ingredients.add(ingredient);
            }
            return ingredients;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            dbConnection.closeJDBCRessources(connection, ps, rs);
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
