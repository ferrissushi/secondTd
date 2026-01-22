package secondTd.service;

import java.math.BigDecimal;
import java.sql.*;

import secondTd.model.*;
import secondTd.db.DBConnection;

import java.time.Instant;
import java.time.temporal.TemporalAccessor;
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

    public Double findDishPriceById(Integer id) {
        String sql = """
                    select selling_price from dish where id = ?;
                """;
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connection = dbConnection.getConnection();
            ps = connection.prepareStatement(sql);
            ps.setInt(1, id);

            rs = ps.executeQuery();

            if (rs.next()) {
                Double sellingPrice = null;
                if (rs.getObject("selling_price") != null) {
                    sellingPrice = rs.getObject("selling_price", BigDecimal.class).doubleValue();
                }
                return sellingPrice;
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            dbConnection.closeJDBCRessources(connection, ps, rs);
        }
    }

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

    public Ingredient saveIngredient(Ingredient toSave) {
        String ingredientSql = """
                insert into ingredient (id, name, price, category) values (?, ?, ?, ?::ingredient_category)
                on conflict (id) do
                update set id = excluded.id, name = excluded.name,
                price = excluded.price, category = excluded.category::ingredient_category
                returning id, name, price, category;
                """;
        String stockSql = """
                insert into stock_movement (id, id_ingredient, quantity, type, unit, creation_datetime)
                values (?, ?, ?::movement_type, ?::unit_type, ?, ?)
                on conflict (id) do nothing;
                """;
        Connection connection = null;
        try {
            Ingredient savedIngredient = null;
            connection = dbConnection.getConnection();
            PreparedStatement ingredientPs = connection.prepareStatement(ingredientSql);

            ingredientPs.setInt(1, toSave.getId());
            ingredientPs.setString(2, toSave.getName());
            ingredientPs.setDouble(3, toSave.getPrice());
            ingredientPs.setString(4, toSave.getCategory().toString());

            ResultSet ingredientRs = ingredientPs.executeQuery();
            if (ingredientRs.next()) {
                savedIngredient = mapToIngredient(ingredientRs);
                savedIngredient.setStockMovementList(toSave.getStockMovementList());
            }
            PreparedStatement stockPs = connection.prepareStatement(stockSql);
            for (StockMovement stockMovement: toSave.getStockMovementList()) {
                stockPs.setInt(1, stockMovement.getId());
                stockPs.setInt(2, toSave.getId());
                stockPs.setDouble(3, stockMovement.getValue().getQuantity());
                stockPs.setString(4,stockMovement.getType().toString());
                stockPs.setString(5, stockMovement.getValue().getUnit().toString());
                stockPs.setTimestamp(6, Timestamp.from(stockMovement.getCreationDatetime()));
                stockPs.addBatch();
            }
            stockPs.executeBatch();
            dbConnection.closeJDBCRessources(stockPs, ingredientRs, ingredientPs);
            return savedIngredient;
        } catch (SQLException error) {
            throw new RuntimeException(error);
        } finally {
            dbConnection.closeJDBCRessources(connection);
        }
    }

    public Ingredient findIngredientById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null");
        }
        String sql = """
                select id, name, price, category from ingredient where id = ?;
                """;
        Connection connection = null;
        Ingredient ingredient = null;
        List<StockMovement> stockMovements = findStockMovementByIngredientId(id);
        try {
            connection = dbConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                ingredient = mapToIngredient(rs);
                ingredient.setStockMovementList(stockMovements);
            }
            dbConnection.closeJDBCRessources(rs, ps);
            return ingredient;
        } catch(SQLException error) {
            throw new RuntimeException(error);
        } finally {
            dbConnection.closeJDBCRessources(connection);
        }
    }

    public List<StockMovement> findStockMovementByIngredientId(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null");
        }
        String sql = """
                select id, id_ingredient, quantity, type, unit, creation_datetime
                from stock_movement where id_ingredient = ?;
                """;
        Connection connection = null;
        List<StockMovement> stockMovements = new ArrayList<>();
        try {
            connection = dbConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                StockMovement stockMovement = mapToStockMovement(rs);
                stockMovements.add(stockMovement);
            }
            dbConnection.closeJDBCRessources(rs, ps);
            return stockMovements;
        } catch(SQLException error) {
            throw new RuntimeException(error);
        } finally {
            dbConnection.closeJDBCRessources(connection);
        }
    }

    public StockMovement mapToStockMovement(ResultSet rs ) throws SQLException {
        StockMovement stockMovement = new StockMovement();
        stockMovement.setId(rs.getInt("id"));
        stockMovement.setValue(new StockValue(rs.getDouble("quantity"), DishIngredient.UnitType.valueOf(rs.getString("unit"))));
        stockMovement.setType(StockMovement.MovementTypeEnum.valueOf(rs.getString("type")));
        stockMovement.setCreationDatetime(rs.getTimestamp("creation_datetime").toInstant());
        return stockMovement;
    }
    
    public Order saveOrder(Order orderToSave) {
        String orderSql = """
                    insert into "order" (id, reference, creation_datetime)
                    values (?, ?, ?) returning id, reference, creation_datetime;
                """;
        String dishOrderSql = """
                    insert into dish_order (id, id_order, id_dish, quantity)
                    values (?, ?, ?, ?) returning id, id_order, id_dish, quantity;
                """;
        checkIngredientsAvailability(orderToSave);
        Connection connection = null;
        Order orderSaved = new Order();
        List<DishOrder> dishOrdersSaved = new ArrayList<>();
        try {
            connection = dbConnection.getConnection();
            PreparedStatement orderPs = connection.prepareStatement(orderSql);

            orderPs.setInt(1, orderToSave.getId());
            orderPs.setString(2, orderToSave.getReference());
            orderPs.setTimestamp(3, Timestamp.from(orderToSave.getCreationDatetime()));
            ResultSet orderRs = orderPs.executeQuery();
            for (DishOrder dishOrder: orderToSave.getDishOrders()) {
                PreparedStatement dishOrderPs = connection.prepareStatement(dishOrderSql);
                dishOrderPs.setInt(1, dishOrder.getId());
                dishOrderPs.setInt(2, orderToSave.getId());
                dishOrderPs.setInt(3, dishOrder.getDish().getId());
                dishOrderPs.setInt(4, dishOrder.getQuantity());

                ResultSet dishOrderRs = dishOrderPs.executeQuery();

                if (dishOrderRs.next()) {
                    DishOrder dishOrderSaved = new DishOrder();
                    dishOrderSaved.setId(dishOrderRs.getInt("id"));
                    dishOrderSaved.setDish(findDishById(dishOrderRs.getInt("id_dish")));
                    dishOrderSaved.setQuantity(dishOrderRs.getInt("quantity"));

                    dishOrdersSaved.add(dishOrderSaved);
                }
                dbConnection.closeJDBCRessources(dishOrderRs, dishOrderPs);
            }
            if (orderRs.next()) {
                orderSaved.setDishOrders(dishOrdersSaved);
                orderSaved.setReference(orderRs.getString("reference"));
                orderSaved.setId(orderRs.getInt("id"));
                orderSaved.setCreationDatetime(orderRs.getTimestamp("creation_datetime").toInstant());
            }
            dbConnection.closeJDBCRessources(orderRs, orderPs);
            return orderSaved;
        } catch (SQLException error) {
            throw new RuntimeException(error);
        } finally {
            dbConnection.closeJDBCRessources(connection);
        }
    }

    public void checkIngredientsAvailability(Order orderToSave) {
        List<DishOrder> dishOrders = orderToSave.getDishOrders();
        for (DishOrder order: dishOrders) {
            Dish dish = order.getDish();
            checkDishIngredientAvailability(dish, order, orderToSave.getCreationDatetime());
        }
    }

    public void checkDishIngredientAvailability(Dish dish,
                                                DishOrder dishOrderToSave,
                                                Instant orderToSaveCreationDatetime) {
        List<DishIngredient> dishIngredients = dish.getIngredients();
        for (DishIngredient dishIngredient: dishIngredients) {
            Double requiredIngredientQuantityForDish =
                    dishIngredient.getQuantityRequired() * dishOrderToSave.getQuantity();
            Integer ingredientId = dishIngredient.getIngredient().getId();
            Ingredient ingredient = findIngredientById(ingredientId);
            StockValue currentStockValue = ingredient.getStockValueAt(orderToSaveCreationDatetime);
            Double currentStockValueQuantity = currentStockValue.getQuantity();
            if (currentStockValueQuantity < requiredIngredientQuantityForDish) {
                throw new IllegalStateException(ingredient.getName() + "'s current stock is not enough for this command");
            }
        }
    }

}
