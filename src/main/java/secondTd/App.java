package secondTd;

import secondTd.model.*;
import secondTd.model.Ingredient.CategoryEnum;

import java.time.Instant;
import java.util.List;

import secondTd.model.Dish.DishTypeEnum;
import secondTd.service.DataRetriever;

public class App {
    public static void main(String[] args) {

        Instant t1 = Instant.now();
        Instant t2 = t1.plusSeconds(300);
        Instant t3 = t2.plusSeconds(300);
        Instant t4 = t3.plusMillis(300);


        DataRetriever retriever = new DataRetriever();
        Dish dish = retriever.findDishById(2);

        Table table = retriever.findTableById(1);

        TableOrder tableOrder = new TableOrder();
        tableOrder.setTable(table);
        tableOrder.setArrivalDatetime(t1);
        tableOrder.setDepartureDatetime(t2);

        DishOrder dishOrder1 = new DishOrder();
        dishOrder1.setId(13);
        dishOrder1.setQuantity(1);
        dishOrder1.setDish(dish);

        Order order = new Order();
        order.setId(8);
        order.setCreationDatetime(Instant.now());
        order.setReference("ORD00006");
        order.setDishOrders(List.of(dishOrder1));
        order.setTable(tableOrder);

        retriever.saveOrder(order);
    }

}
