package secondTd.util;

import secondTd.model.DishIngredient;
import secondTd.model.DishIngredient.UnitType;

public class UnitCaster {
    public static Double convertTo(Double quantity, UnitType unit, String ingredientName) {
        if (unit == UnitType.KG) {
            return quantity;
        }
        return executeConversion(ingredientName, unit, quantity);
    }

    public static Double convertTomatoes(Double quantity, UnitType unit) {
        if (unit.equals(UnitType.PCS)) {
            return quantity / 10;
        }
        throw new IllegalArgumentException("Cannot convert to L");
    }

    public static Double convertLaitue(Double quantity, UnitType unit) {
        if (unit.equals(UnitType.PCS)) {
            System.out.println(quantity);
            return quantity / 2;
        }
        throw new IllegalArgumentException("Cannot convert to L");
    }

    public static Double convertChocolat(Double quantity, UnitType unit) {
        if (unit.equals(UnitType.PCS)) {
            return quantity / 10;
        }
        return quantity / 2.5;
    }

    public static Double convertChicken(Double quantity, UnitType unit) {
        if (unit.equals(UnitType.PCS)) {
            return quantity / 8;
        }
        throw new IllegalArgumentException("Cannot convert to L");
    }

    public static Double convertButter(Double quantity, UnitType unit) {
        if (unit.equals(UnitType.PCS)) {
            return quantity / 4;
        }
        return quantity / 5;
    }

    public static Double executeConversion(String ingredientName, UnitType unit, Double quantity) {
        if (ingredientName.equals("Laitue")) {
            return convertLaitue(quantity, unit);
        } else if (ingredientName.equals("Tomate")) {
            return convertTomatoes(quantity, unit);
        } else if (ingredientName.equals("Chocolat")) {
            return convertChocolat(quantity, unit);
        } else if (ingredientName.equals("Poulet")) {
            return convertChicken(quantity, unit);
        } else if (ingredientName.equals("Beurre")) {
            return convertButter(quantity, unit);
        }
        return null;
    }
}

