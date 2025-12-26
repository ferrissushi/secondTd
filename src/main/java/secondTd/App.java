package secondTd;

import secondTd.model.Ingredient.CategoryEnum;
import java.util.List;
import secondTd.model.Ingredient;
import secondTd.service.DataRetriever;

public class App {
    public static void main(String[] args) {

        DataRetriever retriever = new DataRetriever();

        // Please uncomment the code because if all of them are uncommented,
        // the console will be flooded with too much information and some code will not
        // run because
        // of some exception...............

        // System.out.println("Find dish by id = 1");
        // System.out.println("---------------------");
        // System.out.println(retriever.findDishById(1).toString());
        // System.out.println("\n");

        // System.out.println("Find dish id = 999");
        // System.out.println("---------------------");
        // System.out.println(retriever.findDishById(999));
        // System.out.println("\n");

        // System.out.println("Find ingredients with pagination (page = 2, size = 2)");
        // System.out.println("---------------------");
        // System.out.println(retriever.findIngredients(2, 2).toString());
        // System.out.println("\n");

        // System.out.println("Find ingredients with pagination (page = 3, size = 5)");
        // System.out.println("---------------------");
        // System.out.println(retriever.findIngredients(3, 5));

        // System.out.println("Find dish by ingredients name (ingredient name =
        //
        //
        //
        //
        //
        //
        //
        // 'eur')");
        // System.out.println("---------------------");
        // System.out.println(retriever.findDishByIngredientName("eur").toString());

        // System.out.println("Find Ingredient by criteria");
        //
        // System.out.println("---------------------");

        // List<Ingredient> ingredients1 = retriever.findIngredientByCriteria(null,
        // CategoryEnum.VEGETABLE, null, 1, 10);
        // List<Ingredient> ingredients2 = retriever.findIngredientByCriteria("cho",
        // null, "Sal", 1, 10);
        // List<Ingredient> ingredients3 = retriever.findIngredientByCriteria("cho",
        // null, "gateau", 1, 10);

        // System.out.println(ingredients1.toString());
        // System.out.println("\n");
        // System.out.println(ingredients2.toString());
        // System.out.println("\n");
        // System.out.println(ingredients3.toString());

        List<Ingredient> newIngredients = retriever.createIngredients(List.of(
                new Ingredient(8, "Fromage", 1200.00, CategoryEnum.DAIRY, null),
                new Ingredient(9, "Oignon", 300.00, CategoryEnum.VEGETABLE, null)));

        System.out.println("New ingredients created: ");
        System.out.println(newIngredients.toString());

        retriever.deleteIngredient(8);
        retriever.deleteIngredient(9);
    }

}
