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
            List<DishIngredient> ingredients = findDishIngredientByDishId(id, connection);
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
                 insert into dish (id, name, dish_type, selling_price)
                 values (?, ?, ?::dish_types, ?) on conflict (id) do
                 update set name = excluded.name,
                 dish_type = excluded.dish_type::dish_types,
                 selling_price = excluded.selling_price
                 returning id, name, dish_type, selling_price;
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
            attachIngredients(connection, dishToSave.getId(), dishToSave.getIngredients());
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
                select id, name, dish_type, selling_price
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
            List<DishIngredient> ingredients = findDishIngredientByDishId(id, connection);
            ps = connection.prepareStatement(sql);
            ps.setInt(1, id);

            rs = ps.executeQuery();

            return rs.next() ? mapToDish(rs, ingredients) : null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            dbConnection.closeJDBCRessources(connection, ps, rs);
        }
    }

    ;

    public List<Ingredient> unhandledCreateIngredients(
            List<Ingredient> newIngredients) throws SQLException {
        String sql = """
                insert into ingredient (id, name, price, category)
                values (?, ?, ?, ?::ingredient_category)
                returning id, name, price, category;
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

        List<DishIngredient> fetchedIngredient =
                findIngredientByName(newIngredient.getName(), connection).
                        ingredients();
        if (!fetchedIngredient.isEmpty()) {
            throw new RuntimeException("Ingredient is already in the database");
        }

        PreparedStatement ps = connection.prepareStatement(sql);
        setPreparedStatementForIngredient(ps, newIngredient);

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            Ingredient newIngredientInserted = mapToIngredientWithoutDish(rs);
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
    }


    public List<DishIngredient> findDishIngredientByDishId(Integer id,
                                                    Connection connection) {
        String sql = """
                select ingredient.id id, ingredient.name name, ingredient.price price, ingredient.category category,
                dish_ingredient.quantity_required quantity_required, dish_ingredient.unit unit
                from ingredient inner join dish_ingredient on ingredient.id = dish_ingredient.id_ingredient where dish_ingredient.id_dish = ?;
                """;
        List<DishIngredient> ingredients = new ArrayList<>();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = connection.prepareStatement(sql);

            ps.setInt(1, id);

            rs = ps.executeQuery();

            while (rs.next()) {
                DishIngredient dishIngredient = mapToDishIngredient(rs);
                ingredients.add(dishIngredient);
            }

            return ingredients;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            dbConnection.closeJDBCRessources(rs, ps);
        }
    }

    public DishIngredient mapToDishIngredient(ResultSet rs) throws SQLException {
        DishIngredient dishIngredient = new DishIngredient();
        dishIngredient.setId(rs.getInt("id"));
        dishIngredient.setIngredient(mapToIngredient(rs));
        dishIngredient.setQuantityRequired(rs.getDouble("quantity_required"));
        dishIngredient.setUnit(DishIngredient.UnitType.valueOf(rs.getString("unit")));
        return dishIngredient;
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
            for (Ingredient ingredient : ingredients) {
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
                select ingredient.id, name, price, category, quantity_required, unit, dish_ingredient.id_dish
                from ingredient inner join dish_ingredient
                on ingredient.id = dish_ingredient.id_ingredient
                where ingredient.name ilike ?;
                """;
        List<DishIngredient> ingredients = new ArrayList<>();
        List<Integer> dishIds = new ArrayList<>();

        try {
            PreparedStatement ps = connection.prepareStatement(sql);

            ps.setString(1, "%" + ingredientName + "%");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                DishIngredient ingredient = mapToDishIngredient(rs);
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
            List<DishIngredient> ingredients,
            List<Integer> dishIds) {
    }

    private String buildSql(
            String ingredientName,
            Ingredient.CategoryEnum category,
            String dishName,
            int page,
            int size) {
        StringBuilder sql = new StringBuilder(
                """
                        select i.id id,
                        i.name name,
                        i.price price,
                        i.category category,
                        di.quantity_required quantity_required,
                        d.name dish_name,
                        di.unit unit
                        from ingredient i
                        inner join dish_ingredient di on i.id = di.id_ingredient
                        inner join dish d on di.id_dish = d.id
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

    public Ingredient mapToIngredientWithoutDish(ResultSet rs) throws SQLException {
        Ingredient ingredient = new Ingredient();

        ingredient.setId(rs.getInt("id"));
        ingredient.setName(rs.getString("name"));
        ingredient.setPrice(rs.getDouble("price"));
        ingredient.setCategory(Ingredient.CategoryEnum.valueOf(rs.getString("category")));

        return ingredient;
    }

    public Ingredient mapToIngredient(ResultSet rs) throws SQLException {
        Ingredient ingredient = new Ingredient();

        ingredient.setId(rs.getInt("id"));
        ingredient.setName(rs.getString("name"));
        ingredient.setPrice(rs.getDouble("price"));
        ingredient.setCategory(Ingredient.CategoryEnum.valueOf(rs.getString("category")));

        return ingredient;
    }

    public Dish mapToDish(ResultSet rs,
                          List<DishIngredient> ingredients) throws SQLException {

        Dish dish = new Dish();

        dish.setId(rs.getInt("id"));
        dish.setName(rs.getString("name"));
        dish.setDishType(Dish.DishTypeEnum.valueOf(rs.getString("dish_type")));
        dish.setIngredients(ingredients);
        Double price = null;
        if (rs.getObject("selling_price") != null) {
            price = rs.getObject("selling_price", BigDecimal.class).doubleValue();
        }
        dish.setPrice(price);

        return dish;
    }

    private void detachIngredients(Connection conn, Integer dishId, List<DishIngredient> ingredients)
            throws SQLException {
        if (ingredients == null || ingredients.isEmpty()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM dish_ingredient WHERE id_dish = ?")) {
                ps.setInt(1, dishId);
                ps.executeUpdate();
            }
            return;
        }
        String baseSql = """
                    DELETE FROM dish_ingredient
                    WHERE id_dish = ? AND id_ingredient NOT IN (%s)
                """;

        String inClause = ingredients.stream()
                .map(i -> "?")
                .collect(Collectors.joining(","));

        String sql = String.format(baseSql, inClause);

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dishId);
            int index = 2;
            for (DishIngredient ingredient : ingredients) {
                ps.setInt(index++, ingredient.getId());
            }
            ps.executeUpdate();
        }
    }

    private void attachIngredients(Connection conn, Integer dishId, List<DishIngredient> ingredients)
            throws SQLException {

        if (ingredients == null || ingredients.isEmpty()) {
            return;
        }

        String ingredientsInsertClause = """
                    INSERT INTO ingredient (id, name, price, category)
                    VALUES (?, ?, ?, ?::ingredient_category)
                """;

        String attachSql = """
                    INSERT INTO dish_ingredient (id_dish, id_ingredient, quantity_required, unit)
                    VALUES (?, ?, ?, ?::unit_type)
                """;

        try (PreparedStatement ps = conn.prepareStatement(attachSql);
             PreparedStatement psIngredients = conn.prepareStatement(ingredientsInsertClause)
        ) {
            for (DishIngredient ingredient : ingredients) {
                psIngredients.setInt(1, ingredient.getIngredient().getId());
                psIngredients.setString(2, ingredient.getIngredient().getName());
                psIngredients.setDouble(3, ingredient.getIngredient().getPrice());
                psIngredients.setString(4, ingredient.getIngredient().getCategory().toString());
                psIngredients.addBatch();
                ps.setInt(1, dishId);
                ps.setInt(2, ingredient.getIngredient().getId());
                ps.setDouble(3, ingredient.getQuantityRequired());
                ps.setString(4, ingredient.getUnit().toString());
                ps.addBatch();
            }
            psIngredients.executeBatch();
            ps.executeBatch();
        }
    }
}
