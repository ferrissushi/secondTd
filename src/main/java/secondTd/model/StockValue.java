package secondTd.model;

import java.util.Objects;

public class StockValue {
    private Double quantity;
    private DishIngredient.UnitType unit;

    public StockValue(Double quantity, DishIngredient.UnitType unit) {
        this.quantity = quantity;
        this.unit = unit;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        StockValue that = (StockValue) o;
        return Objects.equals(quantity, that.quantity) && unit == that.unit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(quantity, unit);
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public DishIngredient.UnitType getUnit() {
        return unit;
    }

    public void setUnit(DishIngredient.UnitType unit) {
        this.unit = unit;
    }
}
