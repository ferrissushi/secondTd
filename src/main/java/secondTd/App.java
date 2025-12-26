package secondTd;

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

        System.out.println("Find dish by ingredients name (ingredient name = 'eur')");
        System.out.println("---------------------");
        System.out.println(retriever.findDishByIngredientName("eur").toString());

    }
}

