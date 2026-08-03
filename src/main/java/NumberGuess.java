import org.example.Levels;

import java.util.Random;
import java.util.Scanner;
import java.util.Timer;

public class NumberGuess {
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        Levels level = new Levels();

        System.out.println("Welcome to the Number Guessing Game!" + "\n"
                + "I'm thinking of a number between 1 and 100"+"\n"
        + "You have 5 chances to guess the correct number.");
        System.out.println();
        System.out.println("Please select the difficulty level: "+ "\n" +
                "1. Easy (10 chances)" +"\n"+
                "2. Medium (5 chances)" +"\n"+
                "3. Hard (3 chances)");

        System.out.println();
try{
        System.out.print("Enter your choice: ");
        int number = sc.nextInt();
        long startTime = System.currentTimeMillis();

        switch (number) {
            case 1: level.levelEasyChoise();
                System.out.println("Time taken: " + (System.currentTimeMillis() - startTime)/1000 + " seconds");
            break;
            case 2: level.levelMediumChoise();
                System.out.println("Time taken: " + (System.currentTimeMillis() - startTime)/1000 + " seconds");
            break;
            case 3: level.levelHardChoise();
                System.out.println("Time taken: " + (System.currentTimeMillis() - startTime)/1000 + " seconds");
            break;
            default:
                System.out.println("Wrong choice!");
        }
    }catch(Exception e){
    System.out.println("Enter only numbers ");
}
    }
}