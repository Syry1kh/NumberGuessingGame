package org.example;

import java.util.Random;
import java.util.Scanner;

public class Levels {
    Random rand = new Random();
    Scanner sc = new Scanner(System.in);


    public void levelEasyChoise() {
        boolean isGuessed = false;
        int counter = 0;
        int x = rand.nextInt(101);
        System.out.println("Great! You have selected the Easy difficulty level" + "\n" + "Let's start the game!");
        for (int i = 1; i <= 10; i++) {
            System.out.print("Enter your guess: ");
            int guess = sc.nextInt();
            counter++;
            if (guess != x) {
                if (guess < x) {
                    System.out.println("Incorrect! The number is greater than " + guess);
                } else if (guess > x) {
                    System.out.println("Incorrect! The number is less than " + guess);
                }
            }
            if (guess == x) {
                System.out.println("Congratulations! You guessed the correct number in " + counter + " attempts");
                isGuessed = true;
                break;
            }
        }
        if (isGuessed == false) {
            System.out.println("Sorry you are loser, the right answer is " + x);
        }
    }

    public void levelMediumChoise() {
        boolean isGuessed = false;
        int counter = 0;
        int x = rand.nextInt(101);
        System.out.println("Great! You have selected the Medium difficulty level" + "\n" + "Let's start the game!");
        for (int i = 1; i <= 5; i++) {
            System.out.print("Enter your guess: ");
            int guess = sc.nextInt();
            counter++;
            if (guess != x) {
                if (guess < x) {
                    System.out.println("Incorrect! The number is greater than " + guess);
                } else if (guess > x) {
                    System.out.println("Incorrect! The number is less than " + guess);
                }
            }
            if (guess == x) {
                System.out.println("Congratulations! You guessed the correct number in " + counter + " attempts");
                isGuessed = true;
                break;
            }
        }
        if (isGuessed == false) {
            System.out.println("Sorry you are loser, the right answer is " + x);
        }
    }
    public void levelHardChoise(){
        boolean isGuessed = false;
        int counter = 0;
        int x = rand.nextInt(101);
        System.out.println("Great! You have selected the Hard difficulty level" + "\n" + "Let's start the game!");
        for (int i = 1; i <= 3; i++) {
            System.out.print("Enter your guess: ");
            int guess = sc.nextInt();
            counter++;
            if (guess != x) {
                if (guess < x) {
                    System.out.println("Incorrect! The number is greater than " + guess);
                }
                if (guess > x) {
                    System.out.println("Incorrect! The number is less than " + guess);
                }
            }
            if (guess == x) {
                System.out.println("Congratulations! You guessed the correct number in " + counter + " attempts");
                isGuessed = true;
                break;
            }
        }
        if (isGuessed == false) {
            System.out.println("Sorry you are loser, the right answer is " + x);
        }
    }
}


