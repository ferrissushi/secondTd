package secondTd.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class Ingredient {
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
        StockMovement stockRelatedToInstantT = null;
        for (StockMovement stockMovement: this.stockMovementList) {
            if (stockMovement.getCreationDatetime().isBefore(t)) {
                if (stockRelatedToInstantT == null) {
                    stockRelatedToInstantT = stockMovement;
                } else {
                    if (stockRelatedToInstantT.getCreationDatetime().isBefore(stockMovement.getCreationDatetime())) {
                        stockRelatedToInstantT = stockMovement;
                    }
                }
            }
        }
        if (stockRelatedToInstantT == null) {
            throw new IllegalArgumentException("Cannot get stock value of empty stock");
        }
        return stockRelatedToInstantT.getValue();
    }


}
