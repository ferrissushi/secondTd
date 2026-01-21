package secondTd.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import secondTd.model.Dish;
import secondTd.model.DishIngredient;
import secondTd.model.Ingredient;
import secondTd.model.Ingredient.CategoryEnum;
import secondTd.service.utils.TestUtils;

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
        assertEquals("Laitue", dish.getIngredients().get(0).getIngredient().getName());
        assertEquals("Tomate", dish.getIngredients().get(1).getIngredient().getName());
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
            "7, Oignon, 500.00, VEGETABLE, 10, Soupe de legumes, START, Soupe de legumes, 1000",
            "7, Oignon, 500.00, VEGETABLE, 1, Salade fraiche, START, Salade fraiche, 1000",
            "8, Fromage, 2000.00, DAIRY, 1, Salade de fromage, START, Salade de fromage, null"
    }, nullValues = {"null"})
    void should_save_dish_ok(
            int ingredientId,
            String ingredientName,
            Double ingredientPrice,
            CategoryEnum ingredientCategory,
            int dishId,
            String dishName,
            Dish.DishTypeEnum dishType,
            String expectedDishName,
            Double dishPrice
    ) {
        DishIngredient ingredient1 = new DishIngredient();
        Ingredient ingredient = new Ingredient();
        ingredient1.setId(ingredientId);
        ingredient.setName(ingredientName);
        ingredient.setPrice(ingredientPrice);
        ingredient.setCategory(ingredientCategory);
        ingredient1.setQuantityRequired(2.50);
        ingredient1.setUnit(DishIngredient.UnitType.KG);
        ingredient1.setIngredient(ingredient);
        Dish dish = new Dish();
        dish.setId(dishId);
        dish.setName(dishName);
        dish.setDishType(dishType);
        dish.setIngredients(List.of(ingredient1));
        dish.setPrice(dishPrice);
        Dish dishInserted = dataRetriever.saveDish(dish);
        assertEquals(expectedDishName, dishInserted.getName());
    }

    @ParameterizedTest
    @CsvSource(value = {"1, 250.00", "2, 4500.00", "3, 0.00", "4, 1400.00", "5, 0.00"})
    void should_return_dish_cost(Integer dishId, Double expectedDishCost) {
        Dish dish = dataRetriever.findDishById(dishId);
        Double dishCost = dish.getDishCost();
        assertEquals(expectedDishCost, dishCost);
    }

    @ParameterizedTest
    @CsvSource(value = {"1, 3250.00", "2, 7500.00", "4, 6600.00"})
    void should_return_gross_margin_ok(Integer dishId, Double expectedGrossMargin) {
        Dish dish = dataRetriever.findDishById(dishId);
        Double grossMargin = dish.getGrossMargin();
        assertEquals(expectedGrossMargin, grossMargin);
    }

    @ParameterizedTest
    @CsvSource(value = {"3", "5"})
    void should_return_gross_margin_ko(Integer dishId) {
        Dish dish = dataRetriever.findDishById(dishId);
        assertThrows(RuntimeException.class, dish::getGrossMargin);
    }

    @ParameterizedTest
    @CsvSource(value = {"1, 3500.00", "2, 12000.00", "3, null", "4, 8000.00", "5, null"}, nullValues = {"null"})
    void should_return_dish_selling_price_ok(Integer dishId, Double expectedSellingPrice) {
        Double dishSellingPrice = dataRetriever.findDishPriceById(dishId);
        assertEquals(expectedSellingPrice, dishSellingPrice);
    }
}
