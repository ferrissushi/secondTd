package secondTd.service;

import secondTd.model.Dish;
import secondTd.model.Ingredient;

public class DataRetriever {
    public Dish findDishById(Integer id) {
        String sql = """
                select id, name, dish_type from dish where id = ?;
                """;
        return null;
    }

    public Ingredient findIngredientById(Integer id) {
        String sql = """
                select id, name, price, category, id_dish from ingredient where id_dish = ?;
                """;
        return null;
    }
}
