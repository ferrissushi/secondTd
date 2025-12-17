package secondTd;

import secondTd.model.Ingredient;
import secondTd.service.DataRetriever;

import java.util.List;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
      DataRetriever dr = new DataRetriever();
      List<Ingredient> ingredients = dr.findIngredients(1, 4);
      System.out.println(ingredients.toString());
    }

}
