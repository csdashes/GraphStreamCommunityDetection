package th.utils;

import java.util.InputMismatchException;
import java.util.Scanner;

/**
 *
 * @author Ilias Trichopoulos <itrichop@csd.auth.gr>
 */
public class Menu {
    
    static public int printMenu() {
        Scanner scanner = new Scanner(System.in);
        int selection = -1;
        boolean flag = true;
        
        System.out.println("Welcome to the Super Awesome Community Detection App!");
        System.out.println("Choose the application that you want to run:");
        System.out.println("1. Propinquity Dynamics (Erdos subgraph with absolute"
                + "fractions and max to min");
        System.out.println("2. Propinquity Dynamics (Erdos subgraph with application"
                + "of communities to original graph");
        System.out.println("3. Louvain (example)");
        System.out.println("0. EXIT");
        
        while(flag) {
            try {
                System.out.println("Enter number of choise: ");
                selection = scanner.nextInt();
                flag = false;
            } catch (InputMismatchException e) {
                System.out.println("Wrong input. Try again.\n");
            }
        }
        
        return selection;
    }
}
