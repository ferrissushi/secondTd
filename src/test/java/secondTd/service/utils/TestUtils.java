package secondTd.service.utils;

import java.sql.Connection;
import java.sql.Statement;

import secondTd.db.DBConnection;

public class TestUtils {


    public void insertDatabaseTestData() {
        String sql = """
                    delete from stock_movement;
                    delete from dish_ingredient;
                    delete from ingredient;
                    delete from dish;
                    insert into dish(id, name, dish_type, selling_price) values
                      (1, 'Salade fraiche', 'START', 3500.00),
                      (2, 'Poulet grille', 'MAIN', 12000.00),
                      (3, 'Riz aux legumes', 'MAIN', null),
                      (4, 'Gateau au chocolat', 'DESSERT', 8000.00),
                      (5, 'Salade de fruits', 'DESSERT', null);

                    insert into ingredient (id, name, price, category) values
                      (1, 'Laitue', 800.00, 'VEGETABLE'),
                      (2, 'Tomate', 600.00, 'VEGETABLE'),
                      (3, 'Poulet', 4500.00, 'ANIMAL'),
                      (4, 'Chocolat', 3000.00, 'OTHER'),
                      (5, 'Beurre', 2500.00, 'DAIRY');


                    insert into dish_ingredient (id, id_dish, id_ingredient, quantity_required, unit)
                    values (1, 1, 1, 0.20, 'KG'::unit_type),
                        (2, 1, 2, 0.15, 'KG'::unit_type),
                        (3, 2, 3, 1.00, 'KG'::unit_type),
                        (4, 4, 4, 0.30, 'KG'::unit_type),
                        (5, 4, 5, 0.20, 'KG'::unit_type);

                    insert into stock_movement values
                        (1, 1, 5.0, 'IN', 'KG', '2024-01-05 08:00'),
                        (2, 1, 0.2, 'OUT', 'KG', '2024-01-06 12:00'),
                        (3, 2, 4.0, 'IN', 'KG', '2024-01-05 08:00'),
                        (4, 2, 0.15, 'OUT', 'KG', '2024-01-06 12:00'),
                        (5, 3, 10.00, 'IN', 'KG', '2024-01-04 09:00'),
                        (6, 3, 1.0, 'OUT', 'KG', '2024-01-06 13:00'),
                        (7, 4, 3.0, 'IN', 'KG', '2024-01-05 10:00'),
                        (8, 4, 0.3, 'OUT', 'KG', '2024-01-06 14:00'),
                        (9, 5, 2.5, 'IN', 'KG', '2024-01-05 10:00'),
                        (10, 5, 0.2, 'OUT', 'KG', '2024-01-06 14:00');
                        
                    select setval(pg_get_serial_sequence('dish', 'id'), (select max(id) from dish));
                    select setval(pg_get_serial_sequence('ingredient', 'id'), (select max(id) from ingredient));
                    select setval(pg_get_serial_sequence('dish_ingredient', 'id'), (select max(id) from dish_ingredient));
                    select setval(pg_get_serial_sequence('stock_movement', 'id'), (select max(id) from stock_movement));
                """;
        try {
            DBConnection dbConnection = new DBConnection();
            Connection connection = dbConnection.getConnection();
            Statement statement = connection.createStatement();
            statement.execute(sql);
            dbConnection.closeJDBCRessources(statement, connection);
        } catch (Exception e) {
            throw new RuntimeException("Error while inserting test data: " + e);
        }
    }

}
