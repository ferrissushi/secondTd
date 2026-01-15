package secondTd.service.utils;

import java.sql.Connection;
import java.sql.Statement;

import secondTd.db.DBConnection;

public class TestUtils {


    public void insertDatabaseTestData() {
        String sql = """
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

                    select setval(pg_get_serial_sequence('dish', 'id'), (select max(id) from dish));
                    select setval(pg_get_serial_sequence('ingredient', 'id'), (select max(id) from ingredient));
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
