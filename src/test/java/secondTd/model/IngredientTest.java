package secondTd.model;


import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IngredientTest {

    @Test
    void should_return_stock_value_at() {
        List<StockMovement> stockMovements = new ArrayList<>();

        StockMovement stockMovement1 = new StockMovement();
        stockMovement1.setValue(new StockValue(60.00, DishIngredient.UnitType.KG));
        stockMovement1.setCreationDatetime(Instant.now());

        StockMovement stockMovement5 = new StockMovement();
        stockMovement5.setValue(new StockValue(50.00, DishIngredient.UnitType.KG));
        stockMovement5.setCreationDatetime(Instant.now());

        StockMovement stockMovement2 = new StockMovement();
        stockMovement2.setValue(new StockValue(40.00, DishIngredient.UnitType.KG));
        stockMovement2.setCreationDatetime(Instant.now());

        Instant t = Instant.now();

        StockMovement stockMovement3 = new StockMovement();
        stockMovement3.setValue(new StockValue(100.00, DishIngredient.UnitType.KG));
        stockMovement3.setCreationDatetime(Instant.now());

        StockMovement stockMovement4 = new StockMovement();
        stockMovement4.setValue(new StockValue(200.00, DishIngredient.UnitType.KG));
        stockMovement4.setCreationDatetime(Instant.now());

        stockMovements.add(stockMovement1);
        stockMovements.add(stockMovement2);
        stockMovements.add(stockMovement3);
        stockMovements.add(stockMovement4);
        stockMovements.add(stockMovement5);

        Ingredient ingredient = new Ingredient();
        ingredient.setStockMovementList(stockMovements);

        assertEquals(new StockValue(40.00, DishIngredient.UnitType.KG), ingredient.getStockValueAt(t));
    }
}
