package secondTd.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.CsvSources;
import secondTd.db.DBConnection;
import secondTd.model.Dish;
import secondTd.model.Ingredient;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DataRetrieverTest {

    private DataRetriever dataRetriever;

    @BeforeEach
    void setUp() throws SQLException {
        dataRetriever = new DataRetriever();
    }

    @ParameterizedTest
    @CsvSource(value = {
            "1, 1",
            "2, 2",
            "3, 3",
            "4, 4",
            "5, 5"
    })
    void should_return_dish_by_id_ok(Integer id, Integer expectedId) {
        Dish dish = dataRetriever.findDishById(id);
        assertEquals(expectedId, dish.getId());
    }

    @ParameterizedTest
    @CsvSource(value = {  "1, 2", "2, 1 " })
    void should_find_ingredient_by_id_dish_ok(Integer idDish, Integer expectedValueLength) {
        List<Ingredient> ingredients = dataRetriever.findIngredientsByDishId(idDish);
        assertEquals(expectedValueLength, ingredients.size());
    }

    @Test
    void findIngredients() {
    }

    @Test
    void should_create_new_ingredient_ok() throws SQLException {
        Dish dish = new Dish();
        dish.setId(1);
        Ingredient newIngredient = new Ingredient(7, "Potatoe", 1000.00, Ingredient.CategoryEnum.VEGETABLE, dish);
        Ingredient newIngredient2 = new Ingredient(8, "Paprica", 100.00, Ingredient.CategoryEnum.OTHER, dish);
        List<Ingredient> newIngredientList = List.of(newIngredient, newIngredient2);
        List<Ingredient> newIngredientsInserted = dataRetriever.createIngredients(newIngredientList);

        assertEquals(newIngredientList, newIngredientsInserted);

        dataRetriever.deleteIngredient(7);
        dataRetriever.deleteIngredient(8);
    }

    @Test
    void should_delete_ingredient_ok() throws SQLException {
        Dish dish = new Dish();
        dish.setId(1);
        Ingredient newIngredient = new Ingredient(9, "Potatoe", 1000.00, Ingredient.CategoryEnum.VEGETABLE, dish);
        dataRetriever.createIngredients(List.of(newIngredient));
        List<Ingredient> ingredients = dataRetriever.findIngredients(1,  10);
        int sizeBefore = 6;
        dataRetriever.deleteIngredient(9);
        List<Ingredient> currentIngredients = dataRetriever.findIngredients(1,  10);
        int sizeAfter = 5;

        assertEquals(sizeBefore, ingredients.size());
        assertEquals(sizeAfter, currentIngredients.size());
    }

}
