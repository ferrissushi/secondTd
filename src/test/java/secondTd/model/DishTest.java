package secondTd.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DishTest {

    @Test
    void should_return_dish_price_ok() {
        Ingredient rice = new Ingredient(0, "Rice", 1000.00, Ingredient.CategoryEnum.OTHER, null);
        Ingredient chicken = new Ingredient(1, "Chicken", 20000.00, Ingredient.CategoryEnum.ANIMAL, null);
        List<Ingredient> dishIngredients = List.of(rice, chicken);
        Dish dish = new Dish(0, "Rice and chicken", Dish.DishTypeEnum.MAIN, dishIngredients);

        Double expectedPrice = 21000.00;
        assertEquals(expectedPrice, dish.getDishCost());
    }
}
