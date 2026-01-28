package secondTd.model;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import secondTd.util.UnitCaster;

public class Ingredient {

    public enum CategoryEnum {
        VEGETABLE, ANIMAL, MARINE, DAIRY, OTHER
    }

    private int id;
    private String name;
    private Double price;
    private CategoryEnum category;
    private List<StockMovement> stockMovementList;


    public Ingredient(Integer id,
                      String name,
                      Double price,
                      CategoryEnum category,
                      Dish dish) {

        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
    }

    public Ingredient(Integer id,
                      String name,
                      Double price,
                      CategoryEnum category,
                      Dish dish,
                      Double quantityRequired) {

        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
    }

    public Ingredient() {
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public CategoryEnum getCategory() {
        return category;
    }

    public void setCategory(CategoryEnum category) {
        this.category = category;
    }


    @Override
    public String toString() {
        return "Ingredient{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", category=" + category +
                '}';
    }

    public List<StockMovement> getStockMovementList() {
        return stockMovementList;
    }

    public void setStockMovementList(List<StockMovement> stockMovementList) {
        this.stockMovementList = stockMovementList;
    }

    public StockValue getStockValueAt(Instant t) {
        List<StockMovement> sortedStockMovement = this.stockMovementList.stream()
                .sorted(Comparator.comparing(StockMovement::getCreationDatetime))
                .toList();
        StockValue remainingStockValue = new StockValue();
        remainingStockValue.setUnit(sortedStockMovement.get(0).getValue().getUnit());
        Double remainingQuantity = 0.00;

        for (StockMovement sortedStockMovementElement: sortedStockMovement) {

            if (sortedStockMovementElement.getCreationDatetime().isBefore(t)
                    || sortedStockMovementElement.getCreationDatetime().equals(t)) {

                if (sortedStockMovementElement.getType() == StockMovement.MovementTypeEnum.IN) {
                    remainingQuantity += sortedStockMovementElement.getValue().getQuantity();

                } else if (sortedStockMovementElement.getType() == StockMovement.MovementTypeEnum.OUT) {
                    Double quantity = sortedStockMovementElement.getValue().getQuantity();
                    String ingredientName = this.name;
                    Double quantityConverted = UnitCaster.convertTo(quantity,
                            sortedStockMovementElement.getValue().getUnit(),
                            ingredientName);
                    remainingQuantity -= quantityConverted;
                }
            }
        }
        remainingStockValue.setQuantity(remainingQuantity);
        return remainingStockValue;
    }
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Ingredient that = (Ingredient) o;
        return id == that.id && Objects.equals(name, that.name)
                && Objects.equals(price, that.price)
                && category == that.category
                && Objects.equals(stockMovementList, that.stockMovementList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, price, category, stockMovementList);
    }
}
