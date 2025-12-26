package secondTd;

import secondTd.service.DataRetriever;

public class App {
    public static void main(String[] args) {

        DataRetriever retriever = new DataRetriever();

        System.out.println("Find dish by id = 1");
        System.out.println("---------------------");
        System.out.println(retriever.findDishById(1).toString());
        System.out.println("\n");

        System.out.println("Find dish id = 999");
        System.out.println("---------------------");
        System.out.println(retriever.findDishById(999));

    }

}
