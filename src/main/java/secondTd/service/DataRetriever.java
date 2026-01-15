package secondTd.service;

import java.math.BigDecimal;
import java.sql.*;
import secondTd.model.*;
import secondTd.db.DBConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DataRetriever {
    private final DBConnection dbConnection;

    public DataRetriever() {
        dbConnection = new DBConnection();
    }

    public Dish findDishById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("Dish id cannot be null");
        }
        String sql = """
                select id, name, dish_type, selling_price from dish where id = ?;
                """;
        Dish dish;
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = dbConnection.getConnection();
            List<Ingredient> ingredients = findIngredientsByDishId(id, connection);
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

    public List<Ingredient> findIngredients(int page, int size) {
        return findIngredientByCriteria(null, null, null, page, size);
    }

    public List<Ingredient> createIngredients(List<Ingredient> newIngredients) {
        try {
            return unhandledCreateIngredients(newIngredients);
        } catch (SQLException e) {
            throw new RuntimeException("Error while creating ingredients: " + e);
        }
    }


    public Dish saveDish(Dish dishToSave) {
        String sql = """
                 insert into dish (id, name, dish_type, price)
                 values (?, ?, ?::dish_types, ?) on conflict (id) do
                 update set name = excluded.name,
                 dish_type = excluded.dish_type::dish_types,
                 price = excluded.price
                 returning id, name, dish_type, price;
                """;
        Dish dish = new Dish();
        try {
            Connection connection = dbConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
            if (dishToSave.getId() != null) {
                ps.setInt(1, dishToSave.getId());
            } else {
                ps.setInt(1, getNextSerialValue(connection, "dish", "id"));
            }
            ps.setString(2, dishToSave.getName());
            ps.setString(3, dishToSave.getDishType().toString());
            if (dishToSave.getPrice() != null) {
                ps.setDouble(4, dishToSave.getPrice());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                dish = mapToDish(rs, dishToSave.getIngredients());
            }
            detachIngredients(connection, dishToSave.getId(), dishToSave.getIngredients());
            attachIngredients(connection, dishToSave.getId(), dish.getIngredients());
            dbConnection.closeJDBCRessources(connection, rs, ps);
            return dish;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private int getNextSerialValue(Connection conn, String tableName, String columnName)
            throws SQLException {

        String sequenceName = getSerialSequenceName(conn, tableName, columnName);
        if (sequenceName == null) {
            throw new IllegalArgumentException(
                    "Any sequence found for " + tableName + "." + columnName
            );
        }
        updateSequenceNextValue(conn, tableName, columnName, sequenceName);

        String nextValSql = "SELECT nextval(?)";

        try (PreparedStatement ps = conn.prepareStatement(nextValSql)) {
            ps.setString(1, sequenceName);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

private void updateSequenceNextValue(Connection conn, String tableName, String columnName, String sequenceName) throws SQLException {
        String setValSql = String.format(
                "SELECT setval('%s', (SELECT COALESCE(MAX(%s), 0) FROM %s))",
                sequenceName, columnName, tableName
        );

        try (PreparedStatement ps = conn.prepareStatement(setValSql)) {
            ps.executeQuery();
        }
    }

    private String getSerialSequenceName(Connection conn, String tableName, String columnName)
            throws SQLException {

        String sql = "SELECT pg_get_serial_sequence(?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tableName);
            ps.setString(2, columnName);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        }
        return null;
    }

    public List<Dish> findDishByIngredientName(String ingredientName) {
        String sql = """
                select id, name, dish_type, price
                from dish
                where id = ?;
                """;
        List<Dish> dishes = new ArrayList<>();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = dbConnection.getConnection();
            FindIngredientByNameResult result = findIngredientByName(ingredientName, connection);
            for (int i = 0; i < result.ingredients().size(); i++) {
                ps = connection.prepareStatement(sql);
                ps.setInt(1, result.dishIds().get(i));
                rs = ps.executeQuery();

                if (rs.next()) {
                    dishes.add(mapToDish(rs,
                        List.of(result.ingredients().get(i))));
                }
            }
            return dishes;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            dbConnection.closeJDBCRessources(connection, ps, rs);
        }
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

    public Dish findDishPriceById(Integer id) {
        String sql = """
            select id, price from dish where id = ?;
        """;
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connection = dbConnection.getConnection();
            List<Ingredient> ingredients = findIngredientsByDishId(id, connection);
            ps = connection.prepareStatement(sql);
            ps.setInt(1, id);

            rs = ps.executeQuery();

            return rs.next() ? mapToDish(rs, ingredients) : null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            dbConnection.closeJDBCRessources(connection, ps, rs);
        }
    };

    public List<Ingredient> unhandledCreateIngredients(
        List<Ingredient> newIngredients) throws SQLException {
        String sql = """
                insert into ingredient (id, name, price, category, id_dish)
                values (?, ?, ?, ?::ingredient_category, ?)
                returning id, name, price, category, id_dish;
                """;
        Connection connection = dbConnection.getConnection();
        List<Ingredient> newIngredientsInserted = new ArrayList<>();

        try {
            connection.setAutoCommit(false);

            for (Ingredient newIngredient : newIngredients) {
                prepareAndPushIngredientToList(newIngredient,
                    newIngredientsInserted,
                    connection,
                    sql);
            }

            connection.commit();

            return newIngredientsInserted;
        } catch (SQLException e) {
            connection.rollback();
            throw new RuntimeException(e);
        } finally {
            dbConnection.closeJDBCRessources(connection);
        }
    }

    private void prepareAndPushIngredientToList(Ingredient newIngredient,
        List<Ingredient> newIngredientsInserted,
        Connection connection,
        String sql) throws SQLException {

        List<Ingredient> fetchedIngredient =
                findIngredientByName(newIngredient.getName(), connection).
                ingredients();
        if (!fetchedIngredient.isEmpty()) {
            throw new RuntimeException("Ingredient is already in the database");
        }

        PreparedStatement ps = connection.prepareStatement(sql);
        setPreparedStatementForIngredient(ps, newIngredient);

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            Ingredient newIngredientInserted = mapToIngredient(rs);
            newIngredientsInserted.add(newIngredientInserted);
        }

        dbConnection.closeJDBCRessources(ps, rs);
    }

    private void setPreparedStatementForIngredient(PreparedStatement ps,
        Ingredient newIngredient) throws SQLException {
        ps.setInt(1, newIngredient.getId());
        ps.setString(2, newIngredient.getName());
        ps.setDouble(3, newIngredient.getPrice());
        ps.setString(4, newIngredient.getCategory().toString());

        if (newIngredient.getDish() != null) {
            ps.setInt(5, newIngredient.getDish().getId());
        } else {
            ps.setNull(5, Types.INTEGER);
        }
    }


    public List<Ingredient> findIngredientsByDishId(Integer id,
        Connection connection) {
        String sql = """
                select ingredient.id id, ingredient.name name, ingredient.price price, ingredient.category category,
                dish_ingredient.quantity_required quantity_required, dish_ingredient.unit unit
                from ingredient join dish_ingredient on ingredient.id = dish_ingredient.id_ingredient where dish_ingredient.id_dish = ?;
                """;
        List<Ingredient> ingredients = new ArrayList<>();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
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
            dbConnection.closeJDBCRessources(rs, ps);
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


    private void updateIngredientFromDish(List<Ingredient> ingredients,
        Dish dish,
        Connection connection) {
        String sql = """
                insert into ingredient (id, name, price, category, id_dish)
                values (?, ?, ?, ?::ingredient_category, ?) on conflict (id)
                do nothing;
                """;

        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            for (Ingredient ingredient: ingredients) {
                setPreparedStatementForIngredient(ps, ingredient);
                ps.addBatch();
            }
            ps.executeBatch();
            dbConnection.closeJDBCRessources(ps);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private FindIngredientByNameResult findIngredientByName(String ingredientName, Connection connection) {
        String sql = """
                select id, name, price, category, id_dish from ingredient where name ilike ?;
                """;
        List<Ingredient> ingredients = new ArrayList<>();
        List<Integer> dishIds = new ArrayList<>();

        try {
            PreparedStatement ps = connection.prepareStatement(sql);

            ps.setString(1, "%" + ingredientName + "%");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Ingredient ingredient = mapToIngredient(rs);
                ingredients.add(ingredient);
                Integer dishId = rs.getInt("id_dish");
                dishIds.add(dishId);
            }

            dbConnection.closeJDBCRessources(rs, ps);

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

    private record FindIngredientByNameResult(
        List<Ingredient> ingredients,
        List<Integer> dishIds) {}

    private String buildSql(
            String ingredientName,
            Ingredient.CategoryEnum category,
            String dishName,
            int page,
            int size) {
        StringBuilder sql = new StringBuilder(
                """
    SELECT i.id id, i.price price, i.name name, i.category category, i.id_dish id_dish
    FROM dish d RIGHT JOIN ingredient i ON d.id = i.id_dish
                        """);
        List<String> conditionClauses = new ArrayList<>();

        if (ingredientName != null) {
            conditionClauses.add("i.name ilike ?");
        }

        if (category != null) {
            conditionClauses.add("category = ?::ingredient_category");
        }

        if (dishName != null) {
            conditionClauses.add("d.name ilike ?");
        }

        if (!conditionClauses.isEmpty()) {
            sql.append(" where ");
            sql.append(String.join(" and ", conditionClauses));
        }

        String orderByClauses = null;

        if (page > 0 && size > 0) {
            int offset = size * (page - 1);
            orderByClauses = "order by i.id limit " + size + " offset " + offset;
        }

        if (orderByClauses != null) {
            sql.append(" ").append(orderByClauses);
        }

        return sql.toString();
    }

    public Ingredient mapToIngredient(ResultSet rs) throws SQLException {
        Ingredient ingredient = new Ingredient();

        ingredient.setId(rs.getInt("id"));
        ingredient.setName(rs.getString("name"));
        ingredient.setPrice(rs.getDouble("price"));
        ingredient.setCategory(Ingredient.CategoryEnum.valueOf(rs.getString("category")));
        ingredient.setQuantityRequired(rs.getDouble("quantity_required"));
        ingredient.setUnit(Ingredient.UnitType.valueOf(rs.getString("unit")));

        return ingredient;
    }

    public Dish mapToDish(ResultSet rs,
        List<Ingredient> ingredients) throws SQLException {

        Dish dish = new Dish();

        dish.setId(rs.getInt("id"));
        dish.setName(rs.getString("name"));
        dish.setDishType(Dish.DishTypeEnum.valueOf(rs.getString("dish_type")));
        dish.setIngredients(ingredients);

        Double price = rs.getObject("selling_price", BigDecimal.class).doubleValue();
        dish.setPrice(price);

        return dish;
    }

    private void detachIngredients(Connection conn, Integer dishId, List<Ingredient> ingredients)
            throws SQLException {
        if (ingredients == null || ingredients.isEmpty()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE ingredient SET id_dish = NULL WHERE id_dish = ?")) {
                ps.setInt(1, dishId);
                ps.executeUpdate();
            }
            return;
        }

        String baseSql = """
                    UPDATE ingredient
                    SET id_dish = NULL
                    WHERE id_dish = ? AND id NOT IN (%s)
                """;

        String inClause = ingredients.stream()
                .map(i -> "?")
                .collect(Collectors.joining(","));

        String sql = String.format(baseSql, inClause);

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dishId);
            int index = 2;
            for (Ingredient ingredient : ingredients) {
                ps.setInt(index++, ingredient.getId());
            }
            ps.executeUpdate();
        }
    }

    private void attachIngredients(Connection conn, Integer dishId, List<Ingredient> ingredients)
            throws SQLException {

        if (ingredients == null || ingredients.isEmpty()) {
            return;
        }

        String attachSql = """
                    UPDATE ingredient
                    SET id_dish = ?
                    WHERE id = ?
                """;

        try (PreparedStatement ps = conn.prepareStatement(attachSql)) {
            for (Ingredient ingredient : ingredients) {
                ps.setInt(1, dishId);
                ps.setInt(2, ingredient.getId());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}
