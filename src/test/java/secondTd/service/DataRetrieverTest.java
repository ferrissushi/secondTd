package secondTd.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.CsvSources;
import secondTd.db.DBConnection;
import secondTd.model.Ingredient;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DataRetrieverTest {

    private Connection connection;
    private DataRetriever dataRetriever;
    private DBConnection dbConnection;

    @BeforeEach
    void setUp() throws SQLException {
        dataRetriever = new DataRetriever();
        dbConnection = new DBConnection();
        connection = dbConnection.getConnection();
    }

    @AfterEach
    void cleanUp() throws SQLException {
        dbConnection.closeConnection(connection);
    }

    @Test
    void findDishById() {
    }

    @ParameterizedTest
    @CsvSource(value = {"1, 2", "2, 1"})
    void should_find_ingredient_by_id_dish_ok(Integer idDish, Integer expectedValueLength) {
        List<Ingredient> ingredients = dataRetriever.findIngredientById(idDish);
        assertEquals(expectedValueLength, ingredients.size());
    }
}