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
import secondTd.model.Ingredient.CategoryEnum;
import secondTd.service.utils.TestUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DataRetrieverTest {

    private DataRetriever dataRetriever;

    @BeforeEach
    void setUp() throws SQLException {
        dataRetriever = new DataRetriever();
        TestUtils testUtils = new TestUtils();
        testUtils.insertDatabaseTestData();
    }

    @Test
    void should_find_dish_by_id_ok() {
        Dish dish = dataRetriever.findDishById(1);
        assertEquals("Salade fraiche", dish.getName());
        assertEquals("Laitue", dish.getIngredients().get(0).getName());
        assertEquals("Tomate", dish.getIngredients().get(1).getName());
    }

    @Test
    void should_find_dish_by_id_ko() {
        assertThrows(RuntimeException.class, () -> {
            dataRetriever.findDishById(999);
        });
    }

    @ParameterizedTest
    @CsvSource(
    value = {
        "2,2,Poulet,Chocolat",
        "3,5,null,null"
    },
    nullValues = {"null"})
    void should_find_ingredients_with_pagination_ok(
        int page,
        int size,
        String expectedFirstIngredientName,
        String expectedSecondIngredientName) {
        List<Ingredient> ingredients = dataRetriever.findIngredients(page, size);
        assertEquals(expectedFirstIngredientName, ingredients.size() > 0 ? ingredients.get(0).getName() : null);
        assertEquals(expectedSecondIngredientName, ingredients.size() > 1 ? ingredients.get(1).getName() : null);
    }

    @Test
    void should_find_dish_by_ingredient_name_ok() {
        List<Dish> dishes = dataRetriever.findDishByIngredientName("eur");
        assertEquals("Gateau au chocolat", dishes.get(0).getName());
    }

    @ParameterizedTest
    @CsvSource(
    value = {
        "null, VEGETABLE,null,1, 10,Laitue, Tomate",
        "cho, null, Sal, 1, 10, null, null",
        "cho, null, gateau, 1, 10, Chocolat, null"
    },
    nullValues = {"null"})
    void should_find_ingredient_by_criteria_ok(
        String name,
        String category,
        String dishName,
        int page,
        int size,
        String expectedFirstIngredientName,
        String expectedSecondIngredientName) {
        List<Ingredient> ingredients = dataRetriever.findIngredientByCriteria(
            name,
            category != null ? Ingredient.CategoryEnum.valueOf(category) : null,
            dishName,
            page,
            size);
        assertEquals(expectedFirstIngredientName, ingredients.size() > 0 ? ingredients.get(0).getName() : null);
        assertEquals(expectedSecondIngredientName, ingredients.size() > 1 ? ingredients.get(1).getName() : null);
    }

    @Test
    void should_create_ingredient_ok() {
        Ingredient ingredient1 = new Ingredient();
        ingredient1.setId(6);
        ingredient1.setName("Fromage");
        ingredient1.setPrice(1200.00);
        ingredient1.setCategory(CategoryEnum.DAIRY);
        Ingredient ingredient2 = new Ingredient();
        ingredient2.setId(7);
        ingredient2.setName("Oignon");
        ingredient2.setPrice(500.00);
        ingredient2.setCategory(CategoryEnum.VEGETABLE);
        List<Ingredient> ingredientsInserted = dataRetriever.createIngredients(
            List.of(
                ingredient1, ingredient2
            )
        );
        assertEquals(2, ingredientsInserted.size());
    }

    @Test
    void should_create_ingredient_ko() {
        Ingredient ingredient1 = new Ingredient();
        ingredient1.setId(6);
        ingredient1.setName("Carotte");
        ingredient1.setPrice(2000.00);
        ingredient1.setCategory(CategoryEnum.VEGETABLE);
        Ingredient ingredient2 = new Ingredient();
        ingredient2.setId(7);
        ingredient2.setName("Laitue");
        ingredient2.setPrice(2000.00);
        ingredient2.setCategory(CategoryEnum.VEGETABLE);
        assertThrows(RuntimeException.class, () -> {
            dataRetriever.createIngredients(
                List.of(
                    ingredient1, ingredient2
                )
            );
        });
    }

    @ParameterizedTest
    @CsvSource(value = {
        "7, Oignon, 500.00, VEGETABLE, 10, Soupe de legumes, START, Soupe de legumes",
        "7, Oignon, 500.00, VEGETABLE, 1, Salade fraiche, START, Salade fraiche",
        "8, Fromage, 2000.00, DAIRY, 1, Salade de fromage, START, Salade de fromage"
    })
    void should_save_dish_ok(
        int ingredientId,
        String ingredientName,
        Double ingredientPrice,
        CategoryEnum ingredientCategory,
        int dishId,
        String dishName,
        Dish.DishTypeEnum dishType,
        String expectedDishName
    ) {
        Ingredient ingredient1 = new Ingredient();
        ingredient1.setId(ingredientId);
        ingredient1.setName(ingredientName);
        ingredient1.setPrice(ingredientPrice);
        ingredient1.setCategory(ingredientCategory);
        Dish dish = new Dish();
        dish.setId(dishId);
        dish.setName(dishName);
        dish.setDishType(dishType);
        dish.setIngredients(List.of(ingredient1));
        Dish dishInserted = dataRetriever.saveDish(dish);
        assertEquals(expectedDishName, dishInserted.getName());
    }
}
