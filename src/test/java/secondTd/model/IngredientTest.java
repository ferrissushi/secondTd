package secondTd.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IngredientTest {

    @Test
    void should_return_dish_name_ok() {
        Dish dish = new Dish(0, "Rice and chicken", Dish.DishTypeEnum.MAIN, null);
        Ingredient rice = new Ingredient(0, "Rice", 1000.00, Ingredient.CategoryEnum.OTHER, dish);
        Ingredient chicken = new Ingredient(1, "Chicken", 20000.00, Ingredient.CategoryEnum.ANIMAL, dish);

        String expectedName = "Rice and chicken";
        assertEquals(expectedName, rice.getDishName());
        assertEquals(expectedName, chicken.getDishName());
    }
}