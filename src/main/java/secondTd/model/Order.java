package secondTd.model;

import java.time.Instant;
import java.util.List;

public class Order {
    private Integer id;
    private String reference;
    private Instant creationDatetime;
    private List<DishOrder> dishOrders;
    private TableOrder table;

    public TableOrder getTable() {
        return table;
    }

    public void setTable(TableOrder table) {
        this.table = table;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public Instant getCreationDatetime() {
        return creationDatetime;
    }

    public void setCreationDatetime(Instant creationDatetime) {
        this.creationDatetime = creationDatetime;
    }

    public List<DishOrder> getDishOrders() {
        return dishOrders;
    }

    public void setDishOrders(List<DishOrder> dishOrders) {
        this.dishOrders = dishOrders;
    }

    public Double getTotalAmountWithoutVAT() {
        return dishOrders.stream().mapToDouble((DishOrder dishOrder) -> {
            return dishOrder.getDish().getPrice();
        }).sum();
    }

    public Double getTotalAmountWithVAT() {
        return getTotalAmountWithoutVAT() * (1.20); //.......VAT 20%
    }

    @Override
    public String toString() {
        return "Order{" +
                "dishOrders=" + dishOrders +
                ", creationDatetime=" + creationDatetime +
                ", reference='" + reference + '\'' +
                ", id=" + id +
                '}';
    }
}
